package dev.vanutp.tgbridge.common.models

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.util.RGBLike
import java.util.*

@Serializable
data class LangAdvancements(
    val regular: String = "😼 <b>{username} has made the advancement {title}</b>\n\n<i>{description}</i>",
    val goal: String = "🎯 <b>{username} has reached the goal {title}</b>\n\n<i>{description}</i>",
    val challenge: String = "🏅 <b>{username} has completed the challenge {title}</b>\n\n<i>{description}</i>",
)

@Serializable
data class LangTelegram(
    val serverStarted: String = "✅ <b>Server started!</b>",
    val serverStopped: String = "❌ <b>Server stopped!</b>",

    val playerJoined: String = "🥳 <b>{username} joined the game</b>",
    val playerLeft: String = "😕 <b>{username} left the game</b>",
    val playerDied: String = "☠️ <b>{deathMessage}</b>",

    val chatMessage: String = "<b>[{username}]</b> {text}",

    val advancements: LangAdvancements = LangAdvancements(),

    val playerList: String = "📝 <b>{count} players online:</b> {usernames}",
    val playerListZeroOnline: String = "📝 <b>0 players online</b>",
)

@Serializable
data class MessageMeta(
    val messageFormat: String = "§b<{sender}> §r{text}",
    val hoverOpenInTelegram: String = "Open in Telegram",
    val hoverOpenInBrowser: String = "Open in Web Browser",
    val hoverCopyToClipboard: String = "Copy to clipboard",
    val hoverTagToReply: String = "Tag him/her",
    val reply: String = "[R {sender}: {text}]",
    val replyToMinecraft: String = "[R {text}]",
    val forward: String = "[F {from}]",
    val gif: String = "[GIF]",
    val document: String = "[Document]",
    val photo: String = "[Photo]",
    val audio: String = "[Audio]",
    val sticker: String = "[Sticker]",
    val video: String = "[Video]",
    val videoMessage: String = "[Video message]",
    val voiceMessage: String = "[Voice message]",
    val poll: String = "[Poll: {title}]",
    val pin: String = "[pinned a message]",
)

@Serializable
data class MessageFormatting(
    val linkColor: String = "#FFFF55",
    val linkFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.UNDERLINED),
    val mentionColor: String = "#FFFF55",
    val mentionFormatting: List<TextDecoration>? = Collections.emptyList(),
    val hashtagColor: String = "#FFFF55",
    val hashtagFormatting: List<TextDecoration>? = Collections.emptyList(),
    val codeColor: String = "#AAAAAA",
    val codeFormatting: List<TextDecoration>? = Collections.emptyList(),
    val spoilerColor: String = "#AAAAAA",
    val spoilerFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.OBFUSCATED),
    val spoilerReplaceWithChar: String? = "▌",
    val replyColor: String = "#AAAAAA",
    val replyFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.BOLD),
    val forwardColor: String = "#AAAAAA",
    val forwardFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.BOLD),
    val mediaColor: String = "#FFFF55",
    val mediaFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.BOLD),
    val pinnedMessageColor: String = "#AAAAAA",
    val pinnedMessageFormatting: List<TextDecoration>? = Collections.singletonList(TextDecoration.BOLD),
)

@Serializable
data class LangMinecraft(
    val messageMeta: MessageMeta = MessageMeta(),
    val messageFormatting: MessageFormatting = MessageFormatting(),
)

@Serializable
data class Lang(
    @YamlComment("Translations to other languages can be downloaded from https://github.com/vanutp/tgbridge")
    val telegram: LangTelegram = LangTelegram(),
    val minecraft: LangMinecraft = LangMinecraft(),
)
