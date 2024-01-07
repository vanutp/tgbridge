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

fun TgMessage.toMinecraft(): Component {
    val components = mutableListOf<Component>()

    components.add(Component.text("<${this.senderName}>", NamedTextColor.AQUA))

    this.replyToMessage?.let { reply ->
        if (reply.messageId == config.topicId) {
            return@let
        }
        components.add(
            Component.text(
                lang.minecraft.messageMeta.reply.formatLang(
                    "sender" to reply.senderName,
                    "text" to reply.effectiveText.take(40)
                ),
                NamedTextColor.BLUE
            )
        )
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
