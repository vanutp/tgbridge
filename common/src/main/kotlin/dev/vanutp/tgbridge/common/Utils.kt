package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.ConfigManager.minecraftLang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.format.NamedTextColor

fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")

fun TranslatableComponent.translate(): String {
    var res = minecraftLang[this.key()] ?: this.key()
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

fun trimTextForMessagePart(text: String): String {
    val lines = text.split("\n", limit = 1)
    return if (lines.size > 1 || lines[0].length > 50) {
        lines[0].take(50) + "..."
    } else {
        lines[0]
    }
}

fun TgMessage.toMinecraft(botId: Long): Component {
    val components = mutableListOf<Component>()

    components.add(Component.text("<${this.senderName}>", NamedTextColor.AQUA))

    this.pinnedMessage?.let {
        val langString = if (it.effectiveText == "") {
            lang.minecraft.messageMeta.pinNoText
        } else {
            lang.minecraft.messageMeta.pin
        }
        components.add(Component.text(langString.formatLang("text" to it.effectiveText), NamedTextColor.DARK_AQUA))
    }

    this.replyToMessage?.let { reply ->
        if (
            // Telegram sends reply message when message is pinned
            this.pinnedMessage != null
            // All messages to a topic are sent as replies to a service message
            || reply.messageId == config.topicId
        ) {
            return@let
        }
        val formattedReply = if (reply.from?.id == botId) {
            lang.minecraft.messageMeta.replyToMinecraft.formatLang(
                "text" to trimTextForMessagePart(reply.effectiveText),
            )
        } else {
            lang.minecraft.messageMeta.reply.formatLang(
                "sender" to reply.senderName,
                "text" to trimTextForMessagePart(reply.effectiveText),
            )
        }
        components.add(Component.text(formattedReply, NamedTextColor.BLUE))
    }

    val forwardFromName = this.forwardFrom?.let { _ ->
        (this.forwardFrom.firstName + " " + (this.forwardFrom.lastName ?: "")).trim()
    } ?: this.forwardFromChat?.let {
        this.forwardFromChat.title
    }
    forwardFromName?.let {
        components.add(
            Component.text(
                lang.minecraft.messageMeta.forward.formatLang("from" to it),
                NamedTextColor.GRAY
            )
        )
    }

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
        if (it.first == this.document && this.animation != null) {
            return@forEach
        }
        if (it.first != null) {
            components.add(Component.text(it.second, NamedTextColor.GREEN))
        }
    }

    this.poll?.let {
        components.add(
            Component.text(
                lang.minecraft.messageMeta.poll.formatLang("title" to it.question),
                NamedTextColor.GREEN
            )
        )
    }

    components.add(Component.text(this.effectiveText))

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
            val waypointName = it.groupValues[1]
            val x = Integer.parseInt(it.groupValues[2])
            val y = Integer.parseInt(it.groupValues[3])
            val z = Integer.parseInt(it.groupValues[4])
            val worldName = it.groupValues[5]

            return """<a href="${config.bluemapUrl}#$worldName:$x:$y:$z:50:0:0:0:0:perspective">$waypointName</a>"""
        } catch (_: NumberFormatException) {
        }
    }
    return null
}
