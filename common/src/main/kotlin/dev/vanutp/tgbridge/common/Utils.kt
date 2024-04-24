package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.getMinecraftLangKey
import dev.vanutp.tgbridge.common.ConfigManager.lang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor

fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")

fun TranslatableComponent.translate(): String {
    var res = getMinecraftLangKey(this.key()) ?: this.key()
    // We're using older versions of kyori on some platforms, so using deprecated args() is ok
    this.args().forEachIndexed { i, x ->
        val child = when (x) {
            is TranslatableComponent -> x.translate()
            is TextComponent -> x.content() + x.children()
                .joinToString("") { if (it is TextComponent) it.content() else it.toString() }

            else -> x.toString()
        }
        if (i == 0) {
            res = res.replace("%s", child)
        }
        res = res.replace("%${i + 1}\$s", child)
    }
    return res
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

fun TgMessage.toMinecraft(botId: Long): Component {
    val components = mutableListOf<Component>()

    components.add(Component.text("<${this.senderName}>", NamedTextColor.AQUA))

    this.pinnedMessage?.let { pinnedMsg ->
        val pinnedMessageText = mutableListOf<String>()
        pinnedMsg.forwardFromToText()?.let { pinnedMessageText.add(it) }
        pinnedMsg.mediaToText()?.let { pinnedMessageText.add(it) }
        pinnedMsg.effectiveText?.let { pinnedMessageText.add(it) }
        components.add(
            Component.text(
                lang.minecraft.messageMeta.pin + " " + pinnedMessageText.joinToString(" "),
                NamedTextColor.DARK_AQUA
            )
        )
    }

    forwardFromToText()?.let { components.add(Component.text(it, NamedTextColor.GRAY)) }
    replyToText(botId)?.let { components.add(Component.text(it, NamedTextColor.BLUE)) }
    mediaToText()?.let { components.add(Component.text(it, NamedTextColor.GREEN)) }
    effectiveText?.let { components.add(Component.text(it)) }

    return components
        .flatMap { listOf(it, Component.text(" ")) }
        .fold(Component.text()) { acc, component -> acc.append(component) }
        .build()
}


val XAERO_WAYPOINT_RGX =
    Regex("""xaero-waypoint:([^:]+):[^:]:([-\d]+):([-\d]+):([-\d]+):\d+:(?:false|true):\d+:Internal-(?:the-)?(overworld|nether|end)-waypoints""")

fun String.asBluemapLinkOrNone(): String? {
    XAERO_WAYPOINT_RGX.matchEntire(this)?.let {
        try {
            var waypointName = it.groupValues[1]
            if (waypointName == "gui.xaero-deathpoint-old" || waypointName == "gui.xaero-deathpoint") {
                waypointName = Component.translatable(waypointName).translate()
            }
            val x = Integer.parseInt(it.groupValues[2])
            val y = Integer.parseInt(it.groupValues[3])
            val z = Integer.parseInt(it.groupValues[4])
            val worldName = it.groupValues[5]

            return """<a href="${config.messages.bluemapUrl}#$worldName:$x:$y:$z:50:0:0:0:0:perspective">$waypointName</a>"""
        } catch (_: NumberFormatException) {
        }
    }
    return null
}
