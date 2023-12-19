package dev.vanutp.tgbridge.common

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent

fun String.escapeHTML(): String = this
    .replace("&", "&amp;")
    .replace(">", "&gt;")
    .replace("<", "&lt;")

fun TranslatableComponent.translate(lang: Map<String, String>): String {
    var res = lang[this.key()] ?: this.key()
    this.args().forEachIndexed { i, x ->
        val child = when (x) {
            is TranslatableComponent -> x.translate(lang)
            is TextComponent -> x.content() + x.children()
                .joinToString("") { if (it is TextComponent) it.content() else it.toString() }
            else -> x.toString()
        }
        if (i == 0) {
            res = res.replace("%s", child)
        }
        res = res.replace("%${i + 1}\$s", child)
    }
    return res
}
