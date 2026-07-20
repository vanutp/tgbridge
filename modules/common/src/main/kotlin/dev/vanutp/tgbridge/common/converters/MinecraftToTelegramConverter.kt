package dev.vanutp.tgbridge.common.converters

import dev.vanutp.tgbridge.common.LanguageService
import dev.vanutp.tgbridge.common.TgEntity
import dev.vanutp.tgbridge.common.TgEntityType
import dev.vanutp.tgbridge.common.adventure.args
import dev.vanutp.tgbridge.common.adventure.isOpenUrl
import dev.vanutp.tgbridge.common.adventure.value
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

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
    fun convert(comp: Component) = convert(comp, decodeLegacy = true)

    private fun convert(comp: Component, decodeLegacy: Boolean): TelegramFormattedText {
        var res = when (comp) {
            is TranslatableComponent -> {
                var curr = LanguageService.getString(comp.key())
                    ?: comp.fallback()?.let(Component::text)
                    ?: Component.text(comp.key())
                if (curr is TranslatableComponent) {
                    throw IllegalStateException("LanguageService.getString returned TranslatableComponent")
                }

                comp.args().forEachIndexed { i, x ->
                    if (i == 0) {
                        curr = curr.replaceText { it: TextReplacementConfig.Builder ->
                            it.matchLiteral("%s").replacement(x)
                        }
                    }
                    curr = curr.replaceText { it: TextReplacementConfig.Builder ->
                        it.matchLiteral("%${i + 1}\$s").replacement(x)
                    }
                }

                convert(curr)
            }

            // TODO: migrate to when guards
            is TextComponent -> if (decodeLegacy) {
                convert(
                    LegacyComponentSerializer.legacySection().deserialize(comp.content()),
                    decodeLegacy = false,
                )
            } else {
                TelegramFormattedText(comp.content())
            }

            else -> TelegramFormattedText(comp.toString())
        }
        comp.children().forEach {
            res += convert(it)
        }

        val clickEvent = comp.style().clickEvent()
        val isLink = clickEvent?.isOpenUrl() == true && clickEvent.value() != res.text
        if (isLink) {
            res += TgEntity(TgEntityType.TEXT_LINK, 0, res.text.length, clickEvent.value())
        }
        val hoverEvent = comp.style().hoverEvent()
        if (hoverEvent != null && hoverEvent.action() == HoverEvent.Action.SHOW_TEXT) {
            val hoverText = convert(hoverEvent.value() as Component)
            // TODO: get config from Styled Chat
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
                TextDecoration.UNDERLINED -> TgEntityType.UNDERLINE.takeIf { !isLink }
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
