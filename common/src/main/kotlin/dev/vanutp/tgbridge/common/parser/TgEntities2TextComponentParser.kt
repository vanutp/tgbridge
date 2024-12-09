package dev.vanutp.tgbridge.common.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.TgEntity
import dev.vanutp.tgbridge.common.TgMessage

object TgEntities2TextComponentParser {

    fun parse(message: TgMessage, text: String, entities: List<TgEntity>?): TextComponent {
        if (entities == null) return Component.text(text)
        val components = mutableListOf<TextComponent>()
        val currentEntities = ArrayList<TgEntity>()
        val nextEntities = ArrayList<TgEntity>()
        var isLegacy = false
        var isSpoiler = false
        var previousIsSpoiler = false
        var tempText = ""
        entities.forEach { if (it.offset == 0) {
            currentEntities.add(it)
            nextEntities.remove(it)
        }}
        for (i in text.indices) {
            tempText += text[i]
            entities.forEach {
                if (it.offset!! + it.length!! == i+1) {
                    isLegacy = true
                    nextEntities.remove(it)
                }
                if (it.offset == i+1) {
                    isLegacy = true
                    nextEntities.add(it)
                }
            }
            if (isLegacy || i == text.length-1) {
                isLegacy = false
                var tempComponent  = Component.text(tempText).toBuilder()
                currentEntities.forEach {
                    when (it.type) {
                        "bold" -> tempComponent.decoration(TextDecoration.BOLD, true)
                        "italic" -> tempComponent.decoration(TextDecoration.ITALIC, true)
                        "underline" -> tempComponent.decoration(TextDecoration.UNDERLINED, true)
                        "strikethrough" -> tempComponent.decoration(TextDecoration.STRIKETHROUGH, true)
                        "text_link" -> tempComponent = FormattingManager.getAsLinkComponent(
                            tempComponent.build(),
                            it.url!!
                        ).toBuilder()
                        "url" -> tempComponent = FormattingManager.getAsLinkComponent(tempComponent.build()).toBuilder()
                        "mention" -> StyleManager.decorateAll(
                            tempComponent,
                            lang.minecraft.messageFormatting.mentionFormatting
                        ).color(
                            TextColor.fromHexString(lang.minecraft.messageFormatting.mentionColor))
                            .clickEvent(ClickEvent.suggestCommand(tempText))
                            .insertion(tempText)
                            .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverTagToReply).asHoverEvent())
                        "hashtag", "cashtag" -> StyleManager.decorateAll(
                            tempComponent,
                            lang.minecraft.messageFormatting.hashtagFormatting
                        ).color(
                            TextColor.fromHexString(lang.minecraft.messageFormatting.hashtagColor))
                            .clickEvent(ClickEvent.openUrl("https://t.me/c/${-message.chat.id-1000000000000}/" + (if (message.messageThreadId!=null) "${message.messageThreadId}/" else "") + "${message.messageId}"))
                            .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram).asHoverEvent())
                        "spoiler" -> isSpoiler = true
                        "code", "pre" -> tempComponent = FormattingManager.getAsCodeComponent(tempComponent.build()).toBuilder()
                    }
                }
                if (isSpoiler) {
                    if (previousIsSpoiler) {
                        tempComponent = FormattingManager.appendToSpoilerComponent(
                            components.last(),
                            tempComponent.build()
                        ).toBuilder()
                        components.removeLast()
                    }
                    else {
                        tempComponent = FormattingManager.getAsSpoilerComponent(tempComponent.build()).toBuilder()
                        previousIsSpoiler = true
                    }
                    components.add(tempComponent.build())
                }
                else {
                    if (previousIsSpoiler) previousIsSpoiler = false
                    components.add(tempComponent.build())
                }
                isSpoiler = false
                tempText = ""
                currentEntities.clear()
                currentEntities.addAll(nextEntities)
            }
        }
        return components.fold(Component.text()) { acc, component -> acc.append(component) } .build()
    }
}