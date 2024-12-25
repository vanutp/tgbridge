package dev.vanutp.tgbridge.common.converters

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.TgMessage
import dev.vanutp.tgbridge.common.TgMessageMedia
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

object TelegramToMinecraftConverter {
    private val mm = MiniMessage.miniMessage()

    private fun trimReplyMessageText(text: String): String {
        val lines = text.split("\n", limit = 2)
        return if (lines.size > 1 || lines[0].length > 50) {
            lines[0].take(50) + "..."
        } else {
            lines[0]
        }
    }

    private fun mediaToText(msg: TgMessageMedia): Component? {
        var inner = msg.poll?.let {
            mm.deserialize(lang.minecraft.messageMeta.poll, Placeholder.unparsed("title", it.question))
        }

        listOf(
            msg.animation to lang.minecraft.messageMeta.gif,
            msg.document to lang.minecraft.messageMeta.document,
            msg.photo to lang.minecraft.messageMeta.photo,
            msg.audio to lang.minecraft.messageMeta.audio,
            msg.sticker to lang.minecraft.messageMeta.sticker,
            msg.video to lang.minecraft.messageMeta.video,
            msg.videoNote to lang.minecraft.messageMeta.videoMessage,
            msg.voice to lang.minecraft.messageMeta.voiceMessage,
        ).forEach {
            if (it.first != null) {
                inner = mm.deserialize(it.second)
            }
        }

        return inner?.let {
            mm.deserialize(lang.minecraft.messageMeta.mediaFormatting, Placeholder.component("media", it))
        }
    }

    private data class ReplyInfo(
        var isReplyToMinecraft: Boolean,
        var senderName: String,
        var media: Component?,
        var text: String?,
    )

    private fun replyToText(msg: TgMessage, botId: Long): Component? {
        var info: ReplyInfo? = null
        msg.replyToMessage?.let { reply ->
            if (
            // Telegram sends reply message when message is pinned
                msg.pinnedMessage != null
                // All messages to a topic are sent as replies to a service message
                || reply.messageId == config.general.topicId
            ) {
                return@let
            }
            info = ReplyInfo(
                isReplyToMinecraft = reply.from?.id == botId,
                senderName = reply.senderName,
                media = mediaToText(reply),
                text = reply.effectiveText
            )
        }
        msg.externalReply?.let { reply ->
            info = ReplyInfo(
                isReplyToMinecraft = false,
                senderName = reply.senderName,
                media = mediaToText(reply),
                text = null,
            )
        }
        msg.quote?.let {
            info?.text = it.text
        }

        return info?.let {
            val fullText = "${it.media ?: ""} ${trimReplyMessageText(it.text ?: "")}".trim()
            if (it.isReplyToMinecraft) {
                mm.deserialize(
                    lang.minecraft.messageMeta.replyToMinecraft,
                    Placeholder.unparsed("text", fullText),
                )
            } else {
                mm.deserialize(
                    lang.minecraft.messageMeta.reply,
                    Placeholder.unparsed("sender", it.senderName),
                    Placeholder.unparsed("text", fullText),
                )
            }
        }
    }

    private fun forwardFromToText(msg: TgMessage): Component? {
        val forwardFromName = msg.forwardFrom?.let { _ ->
            (msg.forwardFrom.firstName + " " + (msg.forwardFrom.lastName ?: "")).trim()
        } ?: msg.forwardFromChat?.let {
            msg.forwardFromChat.title
        }
        return forwardFromName?.let {
            mm.deserialize(lang.minecraft.messageMeta.forward, Placeholder.unparsed("from", it))
        }
    }

    fun convert(msg: TgMessage, botId: Long): Component {
        val components = mutableListOf<Component>()

        msg.pinnedMessage?.let { pinnedMsg ->
            val pinnedMessageComponents = mutableListOf<Component>()
            forwardFromToText(pinnedMsg)?.let { pinnedMessageComponents.add(it) }
            mediaToText(pinnedMsg)?.let { pinnedMessageComponents.add(it) }
            pinnedMsg.effectiveText?.let { pinnedMessageComponents.add(Component.text(it)) }

            val pinnedMessageDataComponent = pinnedMessageComponents
                .flatMap { listOf(it, Component.text(" ")) }
                .fold(Component.text()) { acc, component -> acc.append(component) }
                .build()
            components.add(
                mm.deserialize(
                    lang.minecraft.messageMeta.pin,
                    Placeholder.component("message", pinnedMessageDataComponent)
                )
            )
        }

        forwardFromToText(msg)?.let { components.add(it) }
        replyToText(msg, botId)?.let { components.add(it) }
        mediaToText(msg)?.let { components.add(it) }
        msg.effectiveText?.let { components.add(Component.text(it)) }

        val textComponent = components
            .flatMap { listOf(it, Component.text(" ")) }
            .fold(Component.text()) { acc, component -> acc.append(component) }
            .build()

        return mm.deserialize(
            lang.minecraft.messageMeta.format,
            Placeholder.unparsed("sender", msg.senderName),
            Placeholder.component("text", textComponent),
        )
    }
}
