package dev.vanutp.tgbridge.common.models

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

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
    val format: String = "<aqua>\\<<sender>></aqua> <text>",
    val reply: String = "<blue>[R <sender>: <text>]",
    val replyToMinecraft: String = "<blue>[R <text>]",
    val forward: String = "<gray>[F <from>]",
    val pin: String = "<dark_aqua>[pinned a message]",
    @YamlComment("Wrapper for all media types")
    val mediaFormatting: String = "<green><media>",
    val gif: String = "[GIF]",
    val document: String = "[Document]",
    val photo: String = "[Photo]",
    val audio: String = "[Audio]",
    val sticker: String = "[Sticker]",
    val video: String = "[Video]",
    val videoMessage: String = "[Video message]",
    val voiceMessage: String = "[Voice message]",
    val poll: String = "[Poll: <title>]",
)

@Serializable
data class LangMinecraft(
    val messageMeta: MessageMeta = MessageMeta(),
)

@Serializable
data class Lang(
    @YamlComment("Translations to other languages can be downloaded from https://github.com/vanutp/tgbridge")
    val version: Int = 1,
    val telegram: LangTelegram = LangTelegram(),
    @YamlComment(
        "This section uses MiniMessage formatting (in non-strict mode).",
        "See https://docs.advntr.dev/minimessage/format.html for more information.",
    )
    val minecraft: LangMinecraft = LangMinecraft(),
)
