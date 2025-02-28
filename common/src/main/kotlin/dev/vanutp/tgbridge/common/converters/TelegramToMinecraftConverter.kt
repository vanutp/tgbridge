package dev.vanutp.tgbridge.common.converters

import dev.vanutp.tgbridge.common.*
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration

object TelegramToMinecraftConverter {
    private val urlRegex = Regex("^[a-z]+://")

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
            lang.minecraft.messageMeta.poll.formatMiniMessage(listOf("title" to it.question))
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
                inner = it.second.formatMiniMessage()
            }
        }

        return inner?.let {
            lang.minecraft.messageMeta.mediaFormatting.formatMiniMessage(
                componentPlaceholders = listOf("media" to it)
            )
        }
    }

    private data class ReplyInfo(
        var isReplyToMinecraft: Boolean,
        var senderName: String,
        var media: Component?,
        var text: String,
        var entities: List<TgEntity>,
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
                text = reply.effectiveText ?: "",
                entities = reply.entities,
            )
        }
        msg.externalReply?.let { reply ->
            info = ReplyInfo(
                isReplyToMinecraft = false,
                senderName = reply.senderName,
                media = mediaToText(reply),
                text = "",
                entities = emptyList(),
            )
        }
        msg.quote?.let {
            info?.text = it.text
            info?.entities = it.entities ?: emptyList()
        }

        return info?.let {
            val formattedText = formattedTextToComponent(trimReplyMessageText(it.text), it.entities)
            val fullText = if (it.media != null && it.text.isNotEmpty()) {
                it.media!!.append(Component.text(" ")).append(formattedText)
            } else {
                it.media ?: formattedText
            }
            if (it.isReplyToMinecraft) {
                lang.minecraft.messageMeta.replyToMinecraft.formatMiniMessage(
                    componentPlaceholders = listOf("text" to fullText)
                )
            } else {
                lang.minecraft.messageMeta.reply.formatMiniMessage(
                    listOf("sender" to it.senderName),
                    listOf("text" to fullText),
                )
            }
        }
    }

    private fun forwardFromToText(msg: TgMessage): Component? {
        val forwardFromName = msg.forwardFrom?.fullName
            ?: msg.forwardFromChat?.title
        return forwardFromName?.let {
            lang.minecraft.messageMeta.forward.formatMiniMessage(listOf("from" to it))
        }
    }

    private fun ensureValidUrl(url: String): String {
        return if (urlRegex.matchesAt(url, 0)) {
            url
        } else {
            "https://$url"
        }
    }

    private fun formattedTextToComponent(text: String, entities: List<TgEntity>): Component {
        val components = mutableListOf<Component>()
        val currEntities = entities.filter { it.offset == 0 }.toMutableList()
        val nextEntities = currEntities.toMutableList()
        var prevSpoilerContent: Component? = null
        var currText = ""
        for (i in text.indices) {
            currText += text[i]
            var entitiesChanged = false
            entities.forEach {
                if (it.offset + it.length == i + 1) {
                    nextEntities.remove(it)
                    entitiesChanged = true
                }
                if (it.offset == i + 1) {
                    nextEntities.add(it)
                    entitiesChanged = true
                }
            }
            var isSpoiler = false
            if (entitiesChanged || i == text.length - 1) {
                var component: Component = Component.text(currText)
                currEntities.forEach {
                    component = when (it.type) {
                        TgEntityType.BOLD -> component.decoration(TextDecoration.BOLD, true)
                        TgEntityType.ITALIC -> component.decoration(TextDecoration.ITALIC, true)
                        TgEntityType.UNDERLINE -> component.decoration(TextDecoration.UNDERLINED, true)
                        TgEntityType.STRIKETHROUGH -> component.decoration(TextDecoration.STRIKETHROUGH, true)

                        TgEntityType.URL ->
                            lang.minecraft.messageFormatting.linkFormatting.formatMiniMessage(
                                listOf("url" to ensureValidUrl(currText), "text_plain" to currText),
                                listOf("text" to component),
                            )

                        TgEntityType.TEXT_LINK ->
                            lang.minecraft.messageFormatting.linkFormatting.formatMiniMessage(
                                listOf("url" to ensureValidUrl(it.url!!), "text_plain" to currText),
                                listOf("text" to component),
                            )

                        TgEntityType.MENTION ->
                            lang.minecraft.messageFormatting.mentionFormatting.formatMiniMessage(
                                listOf("username" to currText),
                                listOf("text" to component),
                            )

                        TgEntityType.HASHTAG, TgEntityType.CASHTAG ->
                            lang.minecraft.messageFormatting.hashtagFormatting.formatMiniMessage(
                                emptyList(),
                                listOf("text" to component),
                            )

                        TgEntityType.SPOILER -> {
                            isSpoiler = true
                            component
                        }

                        TgEntityType.CODE, TgEntityType.PRE ->
                            lang.minecraft.messageFormatting.codeFormatting.formatMiniMessage(
                                listOf("text_plain" to currText),
                                listOf("text" to component),
                            )

                        TgEntityType.BLOCKQUOTE, TgEntityType.EXPANDABLE_BLOCKQUOTE ->
                            lang.minecraft.messageFormatting.quoteFormatting.formatMiniMessage(
                                listOf("text_plain" to currText),
                                listOf("text" to component),
                            )

                        else -> component
                    }
                }
                if (isSpoiler) {
                    if (prevSpoilerContent != null) {
                        components.removeLast()
                        component = prevSpoilerContent.append(component)
                    }
                    prevSpoilerContent = component
                    component = lang.minecraft.messageFormatting.spoilerFormatting.formatMiniMessage(
                        listOf("text_plain" to component.asString()),
                        listOf("text" to component),
                    )
                } else {
                    prevSpoilerContent = null
                }
                components.add(component)
                currText = ""
                currEntities.clear()
                currEntities.addAll(nextEntities)
            }
        }
        return components.fold(Component.text()) { acc, component -> acc.append(component) }.build()
    }

    private fun serviceMessageToText(msg: TgMessage): Component? {
        val vcLang = lang.minecraft.serviceMessages.videoChat
        listOf(
            msg.videoChatScheduled to vcLang.scheduled,
            msg.videoChatStarted to vcLang.started,
            msg.videoChatEnded to vcLang.ended,
        ).forEach {
            if (it.first != null) {
                return it.second.formatMiniMessage()
            }
        }
        msg.videoChatParticipantsInvited?.let { inv ->
            return vcLang.invited.formatMiniMessage(
                listOf("users" to inv.users.joinToString(", ") { it.fullName })
            )
        }

        val membersLang = lang.minecraft.serviceMessages.members
        msg.newChatMembers?.let { users ->
            return if (msg.from == users[0]) {
                membersLang.joined.formatMiniMessage()
            } else {
                membersLang.added.formatMiniMessage(
                    listOf("users" to users.joinToString(", ") { it.fullName }),
                )
            }
        }
        msg.leftChatMember?.let { user ->
            return if (msg.from == user) {
                membersLang.left.formatMiniMessage()
            } else {
                membersLang.removed.formatMiniMessage(
                    listOf("user" to user.fullName),
                )
            }
        }

        return null
    }

    fun convert(msg: TgMessage, botId: Long): Component {
        val components = mutableListOf<Component>()

        msg.pinnedMessage?.let { pinnedMsg ->
            val pinnedMessageComponents = mutableListOf<Component>()
            forwardFromToText(pinnedMsg)?.let { pinnedMessageComponents.add(it) }
            mediaToText(pinnedMsg)?.let { pinnedMessageComponents.add(it) }
            pinnedMsg.effectiveText?.let {
                pinnedMessageComponents.add(
                    formattedTextToComponent(
                        it,
                        pinnedMsg.entities
                    )
                )
            }

            val pinnedMessageDataComponent = pinnedMessageComponents
                .flatMap { listOf(it, Component.text(" ")) }
                .fold(Component.text()) { acc, component -> acc.append(component) }
                .build()
            components.add(
                lang.minecraft.messageMeta.pin.formatMiniMessage(
                    componentPlaceholders = listOf("message" to pinnedMessageDataComponent)
                )
            )
        }

        serviceMessageToText(msg)?.let { components.add(it) }
        forwardFromToText(msg)?.let { components.add(it) }
        replyToText(msg, botId)?.let { components.add(it) }
        mediaToText(msg)?.let { components.add(it) }
        msg.effectiveText?.let { components.add(formattedTextToComponent(it, msg.entities)) }

        val textComponent = components
            .flatMap { listOf(it, Component.text(" ")) }
            .fold(Component.text()) { acc, component -> acc.append(component) }
            .build()

        return lang.minecraft.format.formatMiniMessage(
            listOf("sender" to msg.senderName),
            listOf("text" to textComponent),
        )
    }
}
