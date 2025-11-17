package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.converters.TelegramFormattedText
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.Config
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder

fun interface Function1<A> {
    fun apply(arg: A)
}

fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")

fun Component.asString() = MinecraftToTelegramConverter.convert(this).text

data class Placeholders(
    var plain: Map<String, String> = emptyMap(),
    var component: Map<String, Component> = emptyMap(),
) {
    operator fun plus(other: Placeholders) = Placeholders(
        plain + other.plain,
        component + other.component
    )

    @JvmName("addPlain")
    operator fun plus(other: Pair<String, String>) = Placeholders(
        plain + mapOf(other),
        component
    )

    @JvmName("addComponent")
    operator fun plus(other: Pair<String, Component>) = Placeholders(
        plain,
        component + mapOf(other)
    )

    fun withDefaults(other: Placeholders) = other + this
}


fun String.formatLang(placeholders: Placeholders = Placeholders()): String {
    val placeholdersMerged = placeholders.plain + placeholders.component.mapValues { it.value.asString() }
    var res = this
    placeholdersMerged.forEach {
        res = res.replace("{${it.key}}", it.value)
    }
    return res
}

private val mm = MiniMessage.miniMessage()

fun String.formatMiniMessage(placeholders: Placeholders = Placeholders()): Component {
    var res = this
    placeholders.plain.forEach {
        res = res.replace("{${it.key}}", mm.escapeTags(it.value))
    }
    return mm.deserialize(
        res,
        *placeholders.plain.map { Placeholder.unparsed(it.key, it.value) }.toTypedArray(),
        *placeholders.component.map { Placeholder.component(it.key, it.value) }.toTypedArray()
    )
}


val XAERO_WAYPOINT_RGX =
    Regex("""xaero-waypoint:([^:]+):[^:]:([-\d]+):([-\d]+|~):([-\d]+):\d+:(?:false|true):\d+:Internal-(?:the-)?(overworld|nether|end)-waypoints""")

fun String.asBluemapLinkOrNone(): TelegramFormattedText? {
    if (config.integrations.bluemapUrl == null) {
        return null
    }
    XAERO_WAYPOINT_RGX.matchEntire(this)?.let {
        try {
            var waypointName = it.groupValues[1]
            if (waypointName == "gui.xaero-deathpoint-old" || waypointName == "gui.xaero-deathpoint") {
                waypointName = Component.translatable(waypointName).asString()
            }
            val x = Integer.parseInt(it.groupValues[2])
            val yRaw = it.groupValues[3]
            val y = Integer.parseInt(if (yRaw == "~") "100" else yRaw)
            val z = Integer.parseInt(it.groupValues[4])
            val worldName = it.groupValues[5]

            val url = "${config.integrations.bluemapUrl}#$worldName:$x:$y:$z:50:0:0:0:0:perspective"
            return TelegramFormattedText(
                text = waypointName,
                entities = listOf(
                    TgEntity(
                        offset = 0,
                        length = waypointName.length,
                        type = TgEntityType.TEXT_LINK,
                        url = url
                    )
                )
            )
        } catch (_: NumberFormatException) {
        }
    }
    return null
}

fun Config.getError(): String? {
    return if (botToken == Config().botToken || chats.any { it.chatId == Config().chats[0].chatId }) {
        "Can't run with default config values: please fill in botToken and chatId, then run /tgbridge reload"
    } else if (chats.filter { it.isDefault }.size != 1) {
        "There must be exactly one default chat in the config"
    } else if (chats.map { it.name }.toSet().size != chats.size) {
        "Chat names must be unique"
    } else {
        null
    }
}

fun Config.getDefaultChat(): ChatConfig {
    return chats.find { it.isDefault }!!
}

fun Config.getChat(name: String?): ChatConfig? {
    return if (name == null) {
        getDefaultChat()
    } else {
        chats.find { it.name.equals(name, true) }
    }
}

fun Config.getChat(chatId: Long, topicId: Int?): ChatConfig? {
    return chats.find { it.chatId == chatId && (it.topicId == topicId || it.topicId == null && topicId == 1) }
}
