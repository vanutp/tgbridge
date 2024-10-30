package dev.vanutp.tgbridge.common.models

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.util.RGBLike
import java.util.*

@Serializable
data class LangAdvancements(
    val regular: String = "😼 **{username} has made the advancement {title}**\n\n*{description}*",
    val goal: String = "🎯 **{username} has reached the goal {title}**\n\n*{description}*",
    val challenge: String = "🏅 **{username} has completed the challenge {title}**\n\n*{description}*",
)

@Serializable
data class LangTelegram(
    val serverStarted: String = "✅ **Server started!**",
    val serverStopped: String = "❌ **Server stopped!**",

    val playerJoined: String = "🥳 **{username} joined the game**",
    val playerLeft: String = "😕 **{username} left the game**",
    val playerDied: String = "☠️ **{deathMessage}**",

    val chatMessage: String = "**[{username}]** {text}",

    val advancements: LangAdvancements = LangAdvancements(),

    val playerList: String = "📝 **{count} players online:** {usernames}",
    val playerListZeroOnline: String = "📝 **0 players online**",
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
    val replyFormatting: List<TextDecoration>? = Collections.emptyList(),
    val forwardColor: String = "#AAAAAA",
    val forwardFormatting: List<TextDecoration>? = Collections.emptyList(),
    val mediaColor: String = "#FFFF55",
    val mediaFormatting: List<TextDecoration>? = Collections.emptyList(),
    val pinnedMessageColor: String = "#FFFF55",
    val pinnedMessageFormatting: List<TextDecoration>? = Collections.emptyList(),
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
