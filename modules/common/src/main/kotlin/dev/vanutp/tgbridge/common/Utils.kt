package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.models.Config
import dev.vanutp.tgbridge.common.models.ProxyType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import okhttp3.Credentials
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.coroutines.executeAsync
import java.net.InetSocketAddress
import java.net.Proxy

@Deprecated(
    "Deprecated, use Consumer<A> instead",
    level = DeprecationLevel.WARNING,
    replaceWith = ReplaceWith("java.util.function.Consumer<A>"),
)
fun interface Function1<A> {
    fun apply(arg: A)
}

fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")

fun Component.asString() = MinecraftToTelegramConverter.convert(this).text

data class Placeholders(
    val plain: Map<String, String> = emptyMap(),
    val component: Map<String, Component> = emptyMap(),
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

    fun addPlain(key: String, value: String) = this + Pair(key, value)

    @JvmName("addComponent")
    operator fun plus(other: Pair<String, Component>) = Placeholders(
        plain,
        component + mapOf(other)
    )

    fun addComponent(key: String, value: Component) = this + Pair(key, value)

    fun withDefaults(other: Placeholders) = other + this
}


fun String.formatLang(placeholders: Placeholders = Placeholders()): String {
    // TODO: use escapeHTML here?
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

fun String.asBluemapLinkOrNone(): Component? {
    if (config.integrations.bluemapUrl == null) {
        return null
    }
    val match = XAERO_WAYPOINT_RGX.matchEntire(this) ?: return null
    try {
        val waypointNameRaw = match.groupValues[1]
        val waypointName =
            if (waypointNameRaw == "gui.xaero-deathpoint-old" || waypointNameRaw == "gui.xaero-deathpoint") {
                Component.translatable(waypointNameRaw)
            } else {
                Component.text(waypointNameRaw)
            }
        val x = Integer.parseInt(match.groupValues[2])
        val yRaw = match.groupValues[3]
        val y = Integer.parseInt(if (yRaw == "~") "100" else yRaw)
        val z = Integer.parseInt(match.groupValues[4])
        val worldName = match.groupValues[5]

        val url = "${config.integrations.bluemapUrl}#$worldName:$x:$y:$z:50:0:0:0:0:perspective"
        return waypointName.style { it.clickEvent(ClickEvent.openUrl(url)) }
    } catch (_: NumberFormatException) {
        return null
    }
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


fun OkHttpClient.Builder.withProxyConfig(): OkHttpClient.Builder {
    val proxy = config.advanced.proxy
    val addr = InetSocketAddress(proxy.host, proxy.port)
    return when (proxy.type) {
        ProxyType.NONE -> this
        ProxyType.SOCKS5 -> this.proxy(Proxy(Proxy.Type.SOCKS, addr))
        ProxyType.HTTP -> this
            .proxy(Proxy(Proxy.Type.HTTP, addr))
            .let { builder ->
                if (proxy.username != null && proxy.password != null) {
                    builder.proxyAuthenticator { _, response ->
                        val credential = Credentials.basic(proxy.username, proxy.password)
                        response.request.newBuilder().header("Proxy-Authorization", credential).build()
                    }
                } else {
                    builder
                }
            }
    }
}

suspend fun OkHttpClient.get(url: String) =
    newCall(Request.Builder().url(url).build()).executeAsync().use {
        it.body.string()
    }
