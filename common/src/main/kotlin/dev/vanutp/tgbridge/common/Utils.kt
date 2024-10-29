package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.dataclass.*
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.getMinecraftLangKey
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.models.TgMessageMedia
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import dev.vanutp.tgbridge.common.FormattingParser

fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")

//fun String.parseBaseMarkdown() : String = this
//    .replaceMarkdownToHTML("**", "b")
//    .replaceMarkdownToHTML("*", "i")
//    .replaceMarkdownToHTML("_", "i")
//    .replaceMarkdownToHTML("_", "i")
//
//private fun String.replaceMarkdownToHTML(markdownCode: String, htmlCode: String) : String {
//    while (contains(markdownCode) && indexOf(markdownCode) != lastIndexOf(markdownCode)) {
//        substring(0, indexOf(markdownCode)-1) + "<${htmlCode}>" + this.substring(indexOf(markdownCode)+2)
//        substring(0, indexOf(markdownCode)-1) + "</${htmlCode}>" + this.substring(indexOf(markdownCode)+2)
//    }
//    return this;
//}

fun Component.translate(): String {
    return when (this) {
        is TranslatableComponent -> {
            var res = getMinecraftLangKey(this.key()) ?: this.key()
            // We're using older versions of kyori on some platforms, so using deprecated args() is ok
            this.args().forEachIndexed { i, x ->
                val child = x.translate()
                if (i == 0) {
                    res = res.replace("%s", child)
                }
                res = res.replace("%${i + 1}\$s", child)
            }
            res
        }

        is TextComponent -> {
            val children = this.children().joinToString("") {
                it.translate()
            }
            this.content() + children
        }

        else -> this.toString()
    }
}

fun String.formatLang(vararg args: Pair<String, String>): String {
    var res = this
    args.forEach {
        res = res.replace("{${it.first}}", it.second)
    }
    return res
}

fun trimReplyMessageText(text: String): String {
    val lines = text.split("\n", limit = 2)
    return if (lines.size > 1 || lines[0].length > 50) {
        lines[0].take(50) + "..."
    } else {
        lines[0]
    }
}

private fun TgMessageMedia.mediaToText(): String? {
    listOf(
        this.animation to lang.minecraft.messageMeta.gif,
        this.document to lang.minecraft.messageMeta.document,
        this.photo to lang.minecraft.messageMeta.photo,
        this.audio to lang.minecraft.messageMeta.audio,
        this.sticker to lang.minecraft.messageMeta.sticker,
        this.video to lang.minecraft.messageMeta.video,
        this.videoNote to lang.minecraft.messageMeta.videoMessage,
        this.voice to lang.minecraft.messageMeta.voiceMessage,
    ).forEach {
        if (it.first != null) {
            return it.second
        }
    }

    this.poll?.let {
        return lang.minecraft.messageMeta.poll.formatLang("title" to it.question)
    }

    return null
}

private data class ReplyInfo(
    var isReplyToMinecraft: Boolean,
    var senderName: String,
    var media: String?,
    var text: String?,
)

private fun TgMessage.replyToText(botId: Long): String? {
    var info: ReplyInfo? = null
    this.replyToMessage?.let { reply ->
        if (
        // Telegram sends reply message when message is pinned
            this.pinnedMessage != null
            // All messages to a topic are sent as replies to a service message
            || reply.messageId == config.general.topicId
        ) {
            return@let
        }
        info = ReplyInfo(
            isReplyToMinecraft = reply.from?.id == botId,
            senderName = reply.senderName,
            media = reply.mediaToText(),
            text = reply.effectiveText
        )
    }
    this.externalReply?.let { reply ->
        info = ReplyInfo(
            isReplyToMinecraft = false,
            senderName = reply.senderName,
            media = reply.mediaToText(),
            text = null,
        )
    }
    this.quote?.let {
        info?.text = it.text
    }

    return info?.let {
        val fullText = "${it.media ?: ""} ${trimReplyMessageText(it.text ?: "")}".trim()
        if (it.isReplyToMinecraft) {
            lang.minecraft.messageMeta.replyToMinecraft.formatLang("text" to fullText)
        } else {
            lang.minecraft.messageMeta.reply.formatLang(
                "sender" to it.senderName,
                "text" to fullText,
            )
        }
    }
}

private fun TgMessage.forwardFromToText(): String? {
    val forwardFromName = this.forwardFrom?.let { _ ->
        (this.forwardFrom.firstName + " " + (this.forwardFrom.lastName ?: "")).trim()
    } ?: this.forwardFromChat?.let {
        this.forwardFromChat.title
    }
    return forwardFromName?.let {
        lang.minecraft.messageMeta.forward.formatLang("from" to it)
    }
}

fun TgMessage.toMinecraft(botId: Long, platform: Platform): Component {
    val components = mutableListOf<Component>()

//    components.add(Component.text("<${this.senderName}>", NamedTextColor.AQUA))

    this.pinnedMessage?.let { pinnedMsg ->
        val pinnedMessageText = mutableListOf<String>()
        pinnedMsg.forwardFromToText()?.let { pinnedMessageText.add(it) }
        pinnedMsg.mediaToText()?.let { pinnedMessageText.add(it) }
//        pinnedMsg.effectiveText?.let { pinnedMessageText.add(it) }
        effectiveText?.let { pinnedMessageText.add(it) }
        components.add(
            FormattingParser.addChatLink(this,
                Component.text(
                    lang.minecraft.messageMeta.pin + " " + pinnedMessageText.joinToString(" "),
                    NamedTextColor.DARK_AQUA
                )
            )
        )
    }

    forwardFromToText()?.let { components.add(FormattingParser.addChatLink(this, Component.text(it, NamedTextColor.GRAY))) }
    replyToText(botId)?.let {
        val replyText = Component.text(it).color(NamedTextColor.GRAY)
        if (!config.messages.replyInDifferentLine) components.add(replyText)
        else platform.broadcastMessage(replyText)
    }
    mediaToText()?.let { components.add(FormattingParser.addChatLink(this, Component.text(it, NamedTextColor.GREEN))) }
    effectiveText?.let { components.add((if (config.messages.styledTelegramMessagesInMinecraft) FormattingParser.formatTgEntity2MinecraftComponent(this, it, this.entities) else Component.text(it))) }

    return Component.text(lang.minecraft.messageMeta.messageFormat)
        .replaceText {it.matchLiteral("{sender}")
            .replacement(
                Component.text(this.senderName)
                    .clickEvent(ClickEvent.suggestCommand("@${this.senderUserName}"))
                    .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverTagToReply).asHoverEvent())
            )}
        .replaceText { it.matchLiteral("{text}").replacement(components
            .flatMap { component -> listOf(component, Component.text(" ")) }
            .fold(Component.text()) { acc, component -> acc.append(component) }
            .build()) }
}

fun TgMessage.resolveMessageLink(): String = "https://t.me/c/${-this.chat.id-1000000000000}/" + (if (this.messageThreadId!=null) "${this.messageThreadId}/" else "") + "${this.messageId}"
fun TextComponent.Builder.decorateAll(decorations: List<TextDecoration>?): TextComponent.Builder = this.apply { decorations?.forEach { this.decoration(it, true) } }
fun TextComponent.decorateAll(decorations: List<TextDecoration>?): TextComponent = this.toBuilder().decorateAll(decorations).build()

val XAERO_WAYPOINT_RGX =
    Regex("""xaero-waypoint:([^:]+):[^:]:([-\d]+):([-\d]+|~):([-\d]+):\d+:(?:false|true):\d+:Internal-(?:the-)?(overworld|nether|end)-waypoints""")

fun String.asBluemapLinkOrNone(): String? {
    XAERO_WAYPOINT_RGX.matchEntire(this)?.let {
        try {
            var waypointName = it.groupValues[1]
            if (waypointName == "gui.xaero-deathpoint-old" || waypointName == "gui.xaero-deathpoint") {
                waypointName = Component.translatable(waypointName).translate()
            }
            val x = Integer.parseInt(it.groupValues[2])
            val yRaw = it.groupValues[3]
            val y = Integer.parseInt(if (yRaw == "~") "100" else yRaw)
            val z = Integer.parseInt(it.groupValues[4])
            val worldName = it.groupValues[5]

            return """<a href="${config.messages.bluemapUrl}#$worldName:$x:$y:$z:50:0:0:0:0:perspective">$waypointName</a>"""
        } catch (_: NumberFormatException) {
        }
    }
    return null
}
