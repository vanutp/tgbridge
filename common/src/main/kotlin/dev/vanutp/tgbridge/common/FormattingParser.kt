package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.dataclass.TgEntity
import dev.vanutp.tgbridge.common.dataclass.TgMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.*
import kotlin.collections.ArrayList

object FormattingParser {

    fun addChatLink(message: TgMessage, component: Component): Component {
        return component.clickEvent(ClickEvent.openUrl("https://t.me/c/${-message.chat.id-1000000000000}/" + (if (message.messageThreadId!=null) "${message.messageThreadId}/" else "") + "${message.messageId}"))
            .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram).asHoverEvent())
    }
    fun parsePlaceholdersOrGetComponent(text: String, platform: Platform): Component = platform.placeholderAPIInstance?.parse(text, platform) ?: Component.text(text)
    fun parsePlaceholdersOrGetString(text: String, platform: Platform): String {
        val parsed = platform.placeholderAPIInstance?.parse(text, platform).toString()
        return parsed.ifEmpty { text }
    }
    fun formatTgEntity2MinecraftComponent(message: TgMessage, text: String, entities: List<TgEntity>?): Component {
        if (entities == null) return Component.text(text)
        val components = mutableListOf<Component>()
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
                        "text_link" -> tempComponent.decorateAll(lang.minecraft.messageFormatting.linkFormatting).color(
                            TextColor.color(lang.minecraft.messageFormatting.linkColor)).clickEvent(
                            ClickEvent.openUrl(it.url!!))
                        "url" -> tempComponent.decorateAll(lang.minecraft.messageFormatting.linkFormatting).color(
                            TextColor.color(lang.minecraft.messageFormatting.linkColor)).clickEvent(
                            ClickEvent.openUrl(tempText))
                        "mention" -> tempComponent.decorateAll(lang.minecraft.messageFormatting.mentionFormatting).color(
                            TextColor.color(lang.minecraft.messageFormatting.mentionColor))
                            .clickEvent(ClickEvent.suggestCommand(tempText))
                            .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverTagToReply).asHoverEvent())
                        "hashtag", "cashtag" -> tempComponent.decorateAll(lang.minecraft.messageFormatting.hashtagFormatting).color(
                            TextColor.color(lang.minecraft.messageFormatting.hashtagColor))
                            .clickEvent(ClickEvent.openUrl("https://t.me/c/${-message.chat.id-1000000000000}/" + (if (message.messageThreadId!=null) "${message.messageThreadId}/" else "") + "${message.messageId}"))
                            .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram).asHoverEvent())
                        "spoiler" -> isSpoiler = true
                        "code" -> tempComponent.decorateAll(lang.minecraft.messageFormatting.codeFormatting).color(
                            TextColor.color(lang.minecraft.messageFormatting.codeColor)).clickEvent(ClickEvent.copyToClipboard(tempText))
                    }
                }
                if (isSpoiler) {
                    if (previousIsSpoiler) {
                        tempComponent = appendToSpoilerComponent(components.last(), tempText).toBuilder()
                        components.removeLast()
                    }
                    else {
                        tempComponent = getAsSpoilerComponent(tempText).toBuilder()
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

    fun getTextFromComponent(component: Component): String = component.translate()

    fun formatMinecraftComponent2TgEntity(message: Component, startThisComponentOffset: Int = 0): List<TgEntity> {
        val components = getAllOfChildren(message)
        val entities = ArrayList<TgEntity>()
        val currentEntities = ArrayList<TgEntity>()
        val forRemoveFromCurrentEntities = ArrayList<TgEntity>()
        val tempEntitiesKeys = ArrayList<String>()
        var currentOffset = startThisComponentOffset
        var lengthModifier: Int

        components.forEach {
            tempEntitiesKeys.addAll(getEntityNamesFromComponent(it))
            lengthModifier = it.translate().length
            currentEntities.forEach { it1 -> if (!tempEntitiesKeys.contains(it1.type)) {
                entities.add(it1)
                forRemoveFromCurrentEntities.add(it1)
            } else it1.length = it1.length?.plus(lengthModifier) }
            currentEntities.removeAll(forRemoveFromCurrentEntities.toSet())
            forRemoveFromCurrentEntities.clear()
            tempEntitiesKeys.forEach { it1 -> if (!currentEntities.stream().map { it2 -> it2.type }.toList().contains(it1)) {
                currentEntities.add(TgEntity(type=it1, length = lengthModifier, offset=currentOffset, url=(if (it1 == "text_link") it.clickEvent()?.value() else null)))
            } }
            tempEntitiesKeys.clear()
            currentOffset += lengthModifier
        }

        currentEntities.forEach { if (!tempEntitiesKeys.contains(it.type)) entities.add(it) }

        return entities
    }

    fun getEntityNamesFromComponent(component: Component): List<String> {
        val output = ArrayList<String>()
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.linkFormatting)
                && component.color()?.value()?.equals(lang.minecraft.messageFormatting.linkColor) == true
                && component.clickEvent() != null && component.clickEvent()?.value() != component.translate()) output.add("text_link")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.linkFormatting)
                && component.color()?.value()?.equals(lang.minecraft.messageFormatting.linkColor) == true
                && component.clickEvent() != null && component.clickEvent()?.value() == component.translate()) output.add("url")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.mentionFormatting)
                && component.color()?.value()?.equals(lang.minecraft.messageFormatting.mentionColor) == true
                && component.translate().startsWith("@")) output.add("mention")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.hashtagFormatting)
                && component.color()?.value()?.equals(lang.minecraft.messageFormatting.hashtagColor) == true
                && component.translate().startsWith("#")) output.add("hashtag")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.hashtagFormatting)
                && component.color()?.value()?.equals(lang.minecraft.messageFormatting.hashtagColor) == true
                && component.translate().startsWith("$")) output.add("cashtag")
        if (isItSpoiler(component)) output.add("spoiler")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.codeFormatting)
                && component.color()?.value()?.equals(lang.minecraft.messageFormatting.codeColor) == true) output.add("code")
        if (isIt(component, TextDecoration.BOLD, output, false)) output.add("bold")
        if (isIt(component, TextDecoration.ITALIC, output, false)) output.add("italic")
        if (isIt(component, TextDecoration.UNDERLINED, output, false)) output.add("underline")
        if (isIt(component, TextDecoration.STRIKETHROUGH, output, false)) output.add("strikethrough")

        return output
    }

    fun isDecorationsEquals(component: Component, decorations: List<TextDecoration>?): Boolean {
        val componentFormatting = getIsSetDecorations(component)
        return componentFormatting.containsAll(decorations ?: emptyList()) && (decorations
            ?: emptyList()).containsAll(componentFormatting)
    }
    fun isDecorationsContains(component: Component, decorations: List<TextDecoration>?): Boolean {
        val componentFormatting = getIsSetDecorations(component)
        return componentFormatting.containsAll(decorations ?: emptyList())
    }

    fun getIsSetDecorations(component: Component): List<TextDecoration> = component.decorations().filter { it.value.equals(TextDecoration.State.TRUE) } .keys.toList()

    fun isItSpoiler(component: Component): Boolean = component.hoverEvent()?.value() is TextComponent &&
            lang.minecraft.messageFormatting.spoilerReplaceWith
                ?.replace("{text}", getFromSpoilerComponent(component).translate()) == component.translate() &&
            lang.minecraft.messageFormatting.spoilerReplaceWithStyleFormatting?.any { !component.hasDecoration(it) } == false

    fun getFromSpoilerComponent(component: Component): Component = component.hoverEvent()?.value() as TextComponent

    fun appendToSpoilerComponent(component: Component, spoilerText: String): TextComponent =
        Component.text(lang.minecraft.messageFormatting.spoilerReplaceWith
            ?.replace("{text}",
                getFromSpoilerComponent(component).translate()+spoilerText.replace(" ", "_")).toString())
            .hoverEvent(Component.text().append(getFromSpoilerComponent(component)).append(Component.text(spoilerText)).build()).decorateAll(lang.minecraft.messageFormatting.spoilerReplaceWithStyleFormatting)

    fun getAsSpoilerComponent(spoilerText: String): TextComponent = Component.text(lang.minecraft.messageFormatting.spoilerReplaceWith
        ?.replace("{text}",spoilerText.replace(" ", "_")).toString())
        .hoverEvent(Component.text(spoilerText).decorateAll(lang.minecraft.messageFormatting.spoilerReplaceWithStyleFormatting))

    fun isIt(component: Component, decorations: List<TextDecoration>, othersOfThis: List<String> = emptyList(), strictly:Boolean = true): Boolean = (if (strictly) isDecorationsEquals(component, decorations) else isDecorationsContains(component, decorations)) && !othersOfThis.any {
        (it == "spoiler" && lang.minecraft.messageFormatting.spoilerReplaceWithStyleFormatting?.containsAll(decorations) == true) ||
        (it == "code" && lang.minecraft.messageFormatting.codeFormatting?.containsAll(decorations) == true) ||
        ((it == "hashtag" || it == "cashtag") && lang.minecraft.messageFormatting.hashtagFormatting?.containsAll(decorations) == true) ||
        (it == "mention" && lang.minecraft.messageFormatting.mentionFormatting?.containsAll(decorations) == true) ||
        ((it == "url" || it == "text_link") && lang.minecraft.messageFormatting.linkFormatting?.containsAll(decorations) == true)
    }
    fun isIt(component: Component, decoration: TextDecoration, othersOfThis: List<String> = emptyList(), strictly:Boolean = true): Boolean = isIt(component, listOf(decoration), othersOfThis, strictly)

    fun getAllOfChildren(component: Component): List<Component> {
        val output = ArrayList<Component>()
        if (component.children().isNotEmpty()) component.children().forEach { output.addAll(
            splitComponent(it)
        ) }
        return output
    }

    fun splitComponent(component: Component): List<Component> {
        val output = ArrayList<Component>()
        val components = component.children()
        var tempLine: String
        var tempOffset: Int
        var leftString: String = component.translate()
        var i = 0
        while (leftString.isNotEmpty()) {
            tempOffset = if (i<components.size) leftString.indexOf(components[i].translate()) else leftString.length
            if (tempOffset!=0) {
                tempLine = leftString.substring(0, tempOffset)
                output.add(crateComponentAndCopyFormatting(tempLine, component))
                leftString = leftString.substring(tempOffset, leftString.length)
            }
            else if (i<components.size) {
                output.add(components[i].mergeStyle(component))
                leftString = leftString.substring(components[i].translate().length, leftString.length)
                i++
            }
        }
        return output
    }

    fun crateComponentAndCopyFormatting(text: String, component: Component): Component = Component.text(text)
            .style(component.style())
}