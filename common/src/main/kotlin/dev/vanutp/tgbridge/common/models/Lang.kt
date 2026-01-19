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

    val advancements: LangAdvancements = LangAdvancements(),

    val playerList: String = "📝 <b>{count} players online:</b> {usernames}",
    val playerListZeroOnline: String = "📝 <b>0 players online</b>",

    @YamlComment(
        "Available placeholders: tps5s, tps10s, tps1m, tps5m, tps15m,",
        "mspt10sAvg, mspt1mAvg, mspt5mAvg",
    )
    val tps: String = "📊 <b>TPS:</b> <code>{tps10s}</code>\n⏱️ <b>MSPT:</b> <code>{mspt10sAvg}</code>",
)

@Serializable
data class MembersService(
    val joined: String = "<dark_purple>[joined the group]",
    val left: String = "<dark_purple>[left the group]",
    val added: String = "<dark_purple>[added <users> to the group]",
    val removed: String = "<dark_purple>[removed <user> from the group]",
)

@Serializable
data class VideoChatService(
    val scheduled: String = "<light_purple>[scheduled a video chat]",
    val started: String = "<light_purple>[started a video chat]",
    val ended: String = "<light_purple>[ended the video chat]",
    val invited: String = "<light_purple>[invited <users> to the video chat]",
)

@Serializable
data class MuteService(
    val muted: String = "You won't receive messages from Telegram anymore",
    val unmuted: String = "You will receive new messages from Telegram",
)

@Serializable
data class MessageMeta(
    val reply: String = "<blue>[R <sender>: <text>]",
    val replyToMinecraft: String = "<blue>[R <text>]",
    val forward: String = "<gray>[F <from>]",
    val viaBot: String = "<gray>[via @<bot_username>]",
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
data class ServiceMessages(
    val members: MembersService = MembersService(),
    val videoChat: VideoChatService = VideoChatService(),
    val mute: MuteService = MuteService(),
)

@Serializable
data class LangMinecraft(
    val messageMeta: MessageMeta = MessageMeta(),
    val messageFormatting: MessageFormatting = MessageFormatting(),
    val serviceMessages: ServiceMessages = ServiceMessages(),
)

@Serializable
data class LangAuth(
    @YamlComment("This message is sent to the user when they are kicked from the server if they are not authenticated.")
    val kickMessage: String = "<green>You need to authenticate via Telegram to join this server. Your code is {code}. Send it to @smetana_mc.",
    @YamlComment("This message is sent to the user when they are kicked from the server if they are not in the Telegram group.")
    val notInGroupMessage: String = "<red>You need to join the Telegram group to play on this server. Join @smetana_mc and try again.",
    @YamlComment("This message is sent to the user when they send the /start command to the bot.")
    val startMessage: String = "To authenticate, send me the code you received in Minecraft. You also need to join our group @smetana_mc.",
    @YamlComment("This message is sent to the user when they have successfully authenticated.")
    val successMessage: String = "You have successfully authenticated your account. You can now join the server.",
    @YamlComment("This message is sent to the user when they send an invalid authentication code.")
    val invalidCodeMessage: String = "Invalid authentication code.",
    @YamlComment("This message is sent when a user tries to link an already linked Telegram account.")
    val alreadyLinkedMessage: String = "Your Telegram account is already linked to the Minecraft account: {player}. Use /unlink in-game to unlink it.",
    val unlinkSuccess: String = "Your Telegram account has been unlinked.",
    val notLinked: String = "Your Minecraft account is not linked to a Telegram account.",
)

@Serializable
data class Lang(
    @YamlComment("Translations to other languages can be downloaded from https://github.com/vanutp/tgbridge")
    val telegram: LangTelegram = LangTelegram(),
    @YamlComment(
        "This section uses MiniMessage formatting (in non-strict mode).",
        "See https://docs.advntr.dev/minimessage/format.html for more information.",
        "Additionally, {variable} syntax can be used instead of <variable> for plain-text placeholders."
    )
    val minecraft: LangMinecraft = LangMinecraft(),
    val auth: LangAuth = LangAuth(),
    @YamlComment("Don't change the version manually")
    val version: Int = 1,
)
