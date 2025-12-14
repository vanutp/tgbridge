package dev.vanutp.tgbridge.common.models

import com.charleskorn.kaml.YamlComment
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Config(
    val botToken: String = "your bot token",
    val chats: List<ChatConfig> = listOf(
        ChatConfig(
            name = "global",
            isDefault = true,
            chatId = 0,
            minecraftFormat = "<aqua>\\<<sender>></aqua> <text>",
            telegramFormat = "<b>[<username>]</b> <text>"
        )
    ),
    val messages: MessagesConfig = MessagesConfig(),
    val integrations: IntegrationsConfig = IntegrationsConfig(),
    val events: EventsConfig = EventsConfig(),
    val advanced: AdvancedConfig = AdvancedConfig(),
    @YamlComment(
        "Config file version. Don't change manually",
    )
    val version: Int = 1,
) {
    fun getDefaultChat() = chats.find { it.isDefault }!!

    fun getChat(name: String?) =
        if (name == null) {
            getDefaultChat()
        } else {
            chats.find { it.name.equals(name, true) }
        }

    fun getChat(chatId: Long, topicId: Int?) =
        chats.find { it.chatId == chatId && (it.topicId == topicId || it.topicId == null && topicId == 1) }
}

@Serializable
data class ChatConfig(
    val name: String,
    val isDefault: Boolean = false,
    val chatId: Long,
    val topicId: Int? = null,
    @YamlComment(
        "Format for Telegram -> Minecraft messages. Uses MiniMessage formatting.",
    )
    val minecraftFormat: String = "<aqua>\\<<sender>></aqua> <text>",
    @YamlComment(
        "Format for Minecraft -> Telegram messages. Uses MiniMessage formatting.",
    )
    val telegramFormat: String = "<b>[<username>]</b> <text>",
)

@Serializable
data class MessagesConfig(
    @YamlComment(
        "Warning!",
        "Don't enable this if you have a chat plugin installed.",
        "See https://tgbridge.vanutp.dev/en/compatibility#chat for more info",
        "",
        "If this value is set, messages without specified prefix won't be forwarded to Telegram.",
        "Example: \"!\" (quotes are required)",
        "Default value: null (disabled)",
    )
    val requirePrefixInMinecraft: String? = null,
    @YamlComment(
        "Set to `true` to keep the prefix specified in the above setting in the message",
    )
    val keepPrefix: Boolean = false,
    @YamlComment(
        "Chat messages sent within the specified interval will be merged in one.",
        "The value is specified in seconds",
        "Default value: 0 (disabled)",
    )
    val mergeWindow: Int = 0,
    @YamlComment(
        "Set to `true` to use real player username instead of display name in all Telegram messages",
    )
    val useRealUsername: Boolean = false,
)

@Serializable
data class IntegrationsConfig(
    @YamlComment(
        "If this value is set, waypoints shared from Xaero's Minimap/World Map will be rendered",
        "as links to a specified BlueMap instance.",
        "Example: https://map.example.com",
        "Default value: null (disabled)",
    )
    val bluemapUrl: String? = null,
    @YamlComment(
        "See also: https://tgbridge.vanutp.dev/en/compatibility#chat",
        "Use this if you have an incompatible plugin, such as CMI or AdvancedChat installed.",
        "Will register a legacy chat listener with LOWEST priority (HIGHEST on Forge/NeoForge)",
        "and only forward messages that start with the specified string.",
        "Currently this only has an effect on Paper and Forge/NeoForge.",
        "Example: \"!\" (quotes are required)",
        "Default value: null (disabled)",
    )
    val incompatiblePluginChatPrefix: String? = null,
    @YamlComment(
        "DiscordSRV integration settings",
    )
    val discord: IntegrationsDiscordConfig = IntegrationsDiscordConfig(),
)

@Serializable
data class IntegrationsDiscordConfig(
    @YamlComment(
        "Format of Telegram -> Discord messages. Uses Discord Markdown formatting.",
    )
    val toDiscordFmt: String = "**[{sender}]** {text}",
    @YamlComment(
        "Format of Discord -> Telegram messages. Uses MiniMessage formatting.",
    )
    val toTelegramFmt: String = "<b>[<sender>]</b> <text>",
)

@Serializable
enum class JoinMessagesMode {
    @SerialName("true")
    ENABLED,

    @SerialName("first_join_only")
    FIRST_JOIN_ONLY,

    @SerialName("false")
    DISABLED,
}

@Serializable
data class EventsConfig(
    val advancementMessages: EventsAdvancementMessagesConfig = EventsAdvancementMessagesConfig(),
    val enableDeathMessages: Boolean = true,
    @YamlComment("Available values: true, first_join_only, false")
    val joinMessages: JoinMessagesMode = JoinMessagesMode.ENABLED,
    val enableLeaveMessages: Boolean = true,
    @YamlComment(
        "If a player leaves and then joins within the specified time interval,",
        "the leave and join messages will be deleted.",
        "This is useful when players frequently re-join, for example because of connection problems.",
        "Only has effect when both joinMessages = true and enableLeaveMessages = true.",
        "The value is specified in seconds",
        "Default value: 0 (disabled)",
    )
    val leaveJoinMergeWindow: Int = 0,
    @YamlComment(
        "Whether to send a Telegram message when the server starts",
    )
    val enableStartMessages: Boolean = true,
    @YamlComment(
        "Whether to send a Telegram message when the server stops",
    )
    val enableStopMessages: Boolean = true,
)

@Serializable
data class EventsAdvancementMessagesConfig(
    val enable: Boolean = true,
    @YamlComment(
        "Configure forwarding of each advancement type",
    )
    val enableTask: Boolean = true,
    @YamlComment(
        "Configure forwarding of each advancement type",
    )
    val enableGoal: Boolean = true,
    @YamlComment(
        "Configure forwarding of each advancement type",
    )
    val enableChallenge: Boolean = true,
    @YamlComment(
        "Include advancement descriptions in Telegram messages",
    )
    val showDescription: Boolean = true,
)

@Serializable
data class AdvancedConfig(
    val botApiUrl: String = "https://api.telegram.org",
    val proxy: AdvancedProxyConfig = AdvancedProxyConfig(),
    val connectionRetry: AdvancedConnectionRetryConfig = AdvancedConnectionRetryConfig(),
)

@Serializable
enum class ProxyType {
    @SerialName("none")
    NONE,

    @SerialName("socks5")
    SOCKS5,

    @SerialName("http")
    HTTP,
}

@Serializable
data class AdvancedProxyConfig(
    @YamlComment("Supported types: none, socks5, http")
    val type: ProxyType = ProxyType.NONE,
    val host: String = "",
    val port: Int = 0,
    val username: String? = null,
    val password: String? = null,
)

@Serializable
data class AdvancedConnectionRetryConfig(
    @YamlComment(
        "Max amount of connection retries. If the value is less than 1 the number of attempts is infinite",
    )
    val maxAttempts: Int = 10,
    @YamlComment(
        "Delay before first reconnect attempt in milliseconds",
    )
    val initialDelay: Long = 1000,
    @YamlComment(
        "Maximum delay between reconnection attempts in milliseconds",
    )
    val maxDelay: Long = 300000,
)
