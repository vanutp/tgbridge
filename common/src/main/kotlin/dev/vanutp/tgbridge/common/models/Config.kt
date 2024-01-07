package dev.vanutp.tgbridge.common.models

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.Serializable


@Serializable
data class Config(
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
    val chatId: Long = 0,
    @YamlComment(
        "If the specified chat has topics enabled, specify the topic id where bot should send all messages.",
        "Commands will have no effect in other topics.",
        "If you don't set this, the bot will send all messages to the \"General\" topic",
        "and the /list command will have weird behaviour when sent in other topics.",
        "Default value: null (disabled)",
    )
    val topicId: Int? = null,
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
        "Default value: 0 (disabled)",
    )
    val messageMergeWindow: Int? = 0,
    @YamlComment(
        "If a player leaves and then joins within the specified time interval (in seconds),",
        "the leave and join messages will be deleted.",
        "This is useful when players frequently re-join, for example because of connection problems.",
        "Default value: 0 (disabled)",
    )
    val leaveJoinMergeWindow: Int? = 0,
)
