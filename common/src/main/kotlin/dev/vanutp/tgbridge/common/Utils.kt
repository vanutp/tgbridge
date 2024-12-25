package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.getMinecraftLangKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent

fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")


fun Component.asString(): String {
    return when (this) {
        is TranslatableComponent -> {
            var res = getMinecraftLangKey(this.key()) ?: this.key()
            // We're using older versions of kyori on some platforms, so using deprecated args() is ok
            this.args().forEachIndexed { i, x ->
                val child = x.asString()
                if (i == 0) {
                    res = res.replace("%s", child)
                }
                res = res.replace("%${i + 1}\$s", child)
            }
            res
        }

        is TextComponent -> {
            val children = this.children().joinToString("") {
                it.asString()
            }
            this.content() + children
        }

        else -> this.toString()
    }
}

fun String.formatLang(vararg args: Pair<String, String>): String {
    var res = this
    args.forEach {
        res = res.replace("{${it.first}}", it.second)
    }
    return res
}


val XAERO_WAYPOINT_RGX =
    Regex("""xaero-waypoint:([^:]+):[^:]:([-\d]+):([-\d]+|~):([-\d]+):\d+:(?:false|true):\d+:Internal-(?:the-)?(overworld|nether|end)-waypoints""")

fun String.asBluemapLinkOrNone(): String? {
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

            return """<a href="${config.messages.bluemapUrl}#$worldName:$x:$y:$z:50:0:0:0:0:perspective">$waypointName</a>"""
        } catch (_: NumberFormatException) {
        }
    }
    return null
}
