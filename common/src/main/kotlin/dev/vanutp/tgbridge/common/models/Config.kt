package dev.vanutp.tgbridge.common.models

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable

@Serializable
data class GeneralConfig(
    @YamlComment(
        "Open https://t.me/BotFather to create a bot.",
        "Make sure to *disable* group privacy in the \"Bot Settings\" menu.",
    )
    val botToken: String = "your bot token",
    @YamlComment(
        "To get the chat id, right click/tap on any message and choose \"Copy Message Link\".",
        "If there is no such option, enable \"Chat history for new members\" in the group settings.",
        "It can then be disabled.",
        "The copied link will be in the format \"https://t.me/c/<chat_id>/<topic_id>/<message_id>\".",
        "Topic id will only be present if the chat has topics enabled.",
    )
    var chatId: Long = 0,
    @YamlComment(
        "If the specified chat has topics enabled, specify the id of the topic that will be",
        "synchronized with the game. Commands will have no effect in other topics.",
        "If you don't set this, the bot will send all messages to the \"General\" topic",
        "and the /list command will have weird behaviour when sent in other topics.",
        "Default value: null (disabled)",
    )
    val topicId: Int? = null,
)

@Serializable
data class GameMessagesConfig(
    @YamlComment(
        "If this value is set, waypoints shared from Xaero's Minimap/World Map will be rendered",
        "as links to a specified Bluemap instance.",
        "Note that shared waypoints will be sent regardless of the requirePrefixInMinecraft setting.",
        "Example: https://map.example.com",
        "Default value: null (disabled)",
    )
    val bluemapUrl: String? = null,
    @YamlComment(
        "If this value is set, messages without specified prefix won't be forwarded to Telegram.",
        "Example: \"!\" (quotes are required)",
        "Default value: \"\" (disabled)",
    )
    val requirePrefixInMinecraft: String? = "",
    @YamlComment(
        "Chat messages sent within the specified interval (in seconds) will be merged in one.",
        "The value is specified in seconds",
        "Default value: 0 (disabled)",
    )
    val mergeWindow: Int? = 0,
)

@Serializable
data class AdvancementsConfig(
    val enable: Boolean = true,
    @YamlComment("Include advancement descriptions in Telegram messages")
    val showDescription: Boolean = true,
)

@Serializable
data class GameEventsConfig(
    val advancementMessages: AdvancementsConfig = AdvancementsConfig(),
    val enableDeathMessages: Boolean = true,
    val enableJoinMessages: Boolean = true,
    val enableLeaveMessages: Boolean = true,
    @YamlComment(
        "If a player leaves and then joins within the specified time interval (in seconds),",
        "the leave and join messages will be deleted.",
        "This is useful when players frequently re-join, for example because of connection problems.",
        "Only has effect when both enableJoinMessages and enableLeaveMessages are set to true",
        "The value is specified in seconds",
        "Default value: 0 (disabled)",
    )
    val leaveJoinMergeWindow: Int? = 0,
)

@Serializable
data class Config(
    @YamlComment(
        "It's enough to set botToken and chatId for the plugin to work.",
        "When your group has topics enabled, you should also set topicId."
    )
    val general: GeneralConfig = GeneralConfig(),
    val messages: GameMessagesConfig = GameMessagesConfig(),
    val events: GameEventsConfig = GameEventsConfig(),
)
