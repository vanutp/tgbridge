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
    val playerJoinedFirstTime: String = "🥳 <b>{username} joined the game for the first time</b>",
    val playerLeft: String = "😕 <b>{username} left the game</b>",
    @YamlComment("This option uses MiniMessage formatting (see below for details)")
    val playerDied: String = "☠️ <b><death_message></b>",

    @YamlComment("This option uses MiniMessage formatting (see below for details)")
    val chatMessage: String = "<b>[<username>]</b>",

    val advancements: LangAdvancements = LangAdvancements(),

    val playerList: String = "📝 <b>{count} players online:</b> {usernames}",
    val playerListZeroOnline: String = "📝 <b>0 players online</b>",

    @YamlComment(
        "Available placeholders: tps5s, tps10s, tps1m, tps5m, tps15m, mspt10sAvg, mspt1mAvg, mspt5mAvg",
        "Available durations: 5s, 10s, 1m, 5m, 15m",
    )
    val tps: String = "📊 <b>TPS:</b> <code>{tps10s}</code>\n⏱️ <b>MSPT:</b> <code>{mspt10sAvg}</code>",
)

@Serializable
data class MessageMeta(
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
data class MessageFormatting(
    val linkFormatting: String = "<#7878ff><underlined>" +
            "<hover:show_text:'<green><url>'>" +
            "<click:open_url:'{url}'>" +
            "<text>",
    val mentionFormatting: String = "<#7878ff>" +
            "<hover:show_text:'<green>Shift-click to mention'>" +
            "<insert:{username}>" +
            "<text>",
    val hashtagFormatting: String = "<#7878ff><text>",
    val codeFormatting: String = "<gray>" +
            "<hover:show_text:'<green>Click to copy'>" +
            "<click:copy_to_clipboard:'{text_plain}'>" +
            "<insert:'{text_plain}'>" +
            "<text>",
    val quoteFormatting: String = "<italic><text>",
    val spoilerFormatting: String = "<obfuscated><hover:show_text:'<text>'><text_plain>",
)

@Serializable
data class LangMinecraft(
    val format: String = "<aqua>\\<<sender>></aqua> <text>",
    val messageMeta: MessageMeta = MessageMeta(),
    val messageFormatting: MessageFormatting = MessageFormatting(),
)

@Serializable
data class Lang(
    @YamlComment("Translations to other languages can be downloaded from https://github.com/vanutp/tgbridge")
    val telegram: LangTelegram = LangTelegram(),
    @YamlComment(
        "This section uses MiniMessage formatting (in non-strict mode).",
        "See https://docs.advntr.dev/minimessage/format.html for more information.",
        "Additionally, {variable} syntax can be used instead of <variable> for some plain-text placeholders."
    )
    val minecraft: LangMinecraft = LangMinecraft(),
    @YamlComment("Don't change the version manually")
    val version: Int = 1,
)
