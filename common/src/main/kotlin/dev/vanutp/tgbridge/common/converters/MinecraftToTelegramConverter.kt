package dev.vanutp.tgbridge.common.converters

import dev.vanutp.tgbridge.common.LanguageService
import dev.vanutp.tgbridge.common.TgEntity
import dev.vanutp.tgbridge.common.TgEntityType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration

data class TelegramFormattedText(
    val text: String = "",
    val entities: List<TgEntity> = emptyList(),
) {
    operator fun plus(other: TelegramFormattedText) =
        TelegramFormattedText(
            text + other.text,
            entities + other.entities.map { it.copy(offset = it.offset + text.length) }
        )

    operator fun plus(other: String) =
        TelegramFormattedText(text + other, entities)

    operator fun plus(other: TgEntity) =
        TelegramFormattedText(text, entities + other)
}

object MinecraftToTelegramConverter {
    private fun replace(
        text: TelegramFormattedText,
        find: String,
        replace: Component,
    ) =
        when (val pos = text.text.indexOf(find)) {
            -1 -> text
            else -> {
                val converted = convert(replace)
                TelegramFormattedText(
                    text.text.replaceFirst(find, converted.text),
                    // this assumes that all existing entities are before the replaced text
                    text.entities + converted.entities.map { it.copy(offset = it.offset + pos) }
                )
            }
        }

    fun convert(comp: Component): TelegramFormattedText {
        var res = when (comp) {
            is TranslatableComponent -> {
                var curr = TelegramFormattedText(
                    LanguageService.getString(comp.key()) ?: comp.key()
                )
                // We're using older versions of kyori on some platforms, so using deprecated args() is ok
                comp.args().forEachIndexed { i, x ->
                    if (i == 0) {
                        curr = replace(curr, "%s", x)
                    }
                    curr = replace(curr, "%${i + 1}\$s", x)
                }
                curr
            }

            is TextComponent -> {
                TelegramFormattedText(comp.content())
            }

            else -> TelegramFormattedText(comp.toString())
        }
        comp.children().forEach {
            res += convert(it)
        }

        val clickEvent = comp.style().clickEvent()
        if (clickEvent?.action() == ClickEvent.Action.OPEN_URL && clickEvent.value() != res.text) {
            res += TgEntity(TgEntityType.TEXT_LINK, 0, res.text.length, clickEvent.value())
        }
        val hoverEvent = comp.style().hoverEvent()
        if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            val hoverText = convert(hoverEvent.value() as Component)
            if (res.text.all { it == '▌' } && hoverText.text.length == res.text.length) {
                res = TelegramFormattedText(
                    hoverText.text,
                    res.entities + hoverText.entities + TgEntity(TgEntityType.SPOILER, 0, res.text.length),
                )
            }
        }
        comp.style().decorations().forEach {
            if (it.value != TextDecoration.State.TRUE) {
                return@forEach
            }
            when (it.key) {
                TextDecoration.BOLD -> TgEntityType.BOLD
                TextDecoration.ITALIC -> TgEntityType.ITALIC
                TextDecoration.UNDERLINED -> TgEntityType.UNDERLINE
                TextDecoration.STRIKETHROUGH -> TgEntityType.STRIKETHROUGH
                TextDecoration.OBFUSCATED -> TgEntityType.SPOILER
                else -> null
            }?.let { ent ->
                res += TgEntity(ent, 0, res.text.length)
            }
        }
        return res
    }
}
