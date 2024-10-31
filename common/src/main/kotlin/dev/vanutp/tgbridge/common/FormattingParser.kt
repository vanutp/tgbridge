package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.dataclass.TgEntity
import dev.vanutp.tgbridge.common.dataclass.TgMessage
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
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
                        "text_link" -> tempComponent = getAsLinkComponent(tempComponent.build(), it.url!!).toBuilder()
                        "url" -> tempComponent = getAsLinkComponent(tempComponent.build()).toBuilder()
                        "mention" -> tempComponent.decorateAll(lang.minecraft.messageFormatting.mentionFormatting).color(
                            TextColor.fromHexString(lang.minecraft.messageFormatting.mentionColor))
                            .clickEvent(ClickEvent.suggestCommand(tempText))
                            .insertion(tempText)
                            .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverTagToReply).asHoverEvent())
                        "hashtag", "cashtag" -> tempComponent.decorateAll(lang.minecraft.messageFormatting.hashtagFormatting).color(
                            TextColor.fromHexString(lang.minecraft.messageFormatting.hashtagColor))
                            .clickEvent(ClickEvent.openUrl("https://t.me/c/${-message.chat.id-1000000000000}/" + (if (message.messageThreadId!=null) "${message.messageThreadId}/" else "") + "${message.messageId}"))
                            .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram).asHoverEvent())
                        "spoiler" -> isSpoiler = true
                        "code", "pre" -> tempComponent = getAsCodeComponent(tempComponent.build()).toBuilder()
                    }
                }
                if (isSpoiler) {
                    if (previousIsSpoiler) {
                        tempComponent = appendToSpoilerComponent(components.last(), tempComponent.build()).toBuilder()
                        components.removeLast()
                    }
                    else {
                        tempComponent = getAsSpoilerComponent(tempComponent.build()).toBuilder()
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

    fun formatMinecraftComponent2TgEntity(message: Component, startThisComponentOffset: Int = 0, ignoredEntityTags: List<String> = emptyList()): Pair<String, List<TgEntity>> {
        message as TextComponent
        val components = getAllOfChildren(message)
        val entities = ArrayList<TgEntity>()
        val currentEntities = ArrayList<TgEntity>()
        val forRemoveFromCurrentEntities = ArrayList<TgEntity>()
        val tempEntitiesKeys = ArrayList<String>()
//        val previousEntitiesKeys = ArrayList<String>()
        var isItSpoiler: Boolean
        var currentOffset = startThisComponentOffset
        var lengthModifier: Int
        var text = ""

        components.forEach {
            tempEntitiesKeys.addAll(getEntityNamesFromComponent(it).filter { it0 -> !ignoredEntityTags.contains(it0) })
            lengthModifier = it.content().length
            isItSpoiler = tempEntitiesKeys.contains("spoiler")
            currentEntities.forEach { it1 -> if (!tempEntitiesKeys.contains(it1.type) && !(isItSpoiler && lang.minecraft.messageFormatting.spoilerFormatting?.map { it2 -> it2.name } ?.contains(it1.type) == true)) {
                entities.add(it1)
                forRemoveFromCurrentEntities.add(it1)
            } else it1.length = it1.length?.plus(lengthModifier) }
            if (isItSpoiler) {
                val inSpoiler = formatMinecraftComponent2TgEntity(getFromSpoilerComponent(it), currentOffset)
                text += inSpoiler.first
                entities.addAll(inSpoiler.second)
            }
            else text += it.content()
            currentEntities.removeAll(forRemoveFromCurrentEntities.toSet())
            forRemoveFromCurrentEntities.clear()
            tempEntitiesKeys.forEach { it1 -> if (!currentEntities.stream().map { it2 -> it2.type }.toList().contains(it1)) {
                currentEntities.add(TgEntity(type=it1, length = lengthModifier, offset=currentOffset, url=(if (it1 == "text_link") it.clickEvent()?.value() else null)))
            } }
//            previousEntitiesKeys.clear()
//            previousEntitiesKeys.addAll(tempEntitiesKeys)
            tempEntitiesKeys.clear()
            currentOffset += lengthModifier
        }

        currentEntities.forEach { if (!tempEntitiesKeys.contains(it.type)) entities.add(it) }

        return Pair(text, entities)
    }

    fun getEntityNamesFromComponent(component: Component): List<String> {
        val output = ArrayList<String>()
        if (isItTextLinkComponent(component)) output.add("text_link")
        if (isItUrlLinkComponent(component)) output.add("url")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.mentionFormatting)
                && component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.mentionColor.lowercase()) == true
                && component.translate().startsWith("@")) output.add("mention")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.hashtagFormatting)
                && component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.hashtagColor.lowercase()) == true
                && component.translate().startsWith("#")) output.add("hashtag")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.hashtagFormatting)
                && component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.hashtagColor.lowercase()) == true
                && component.translate().startsWith("$")) output.add("cashtag")
        if (isItSpoiler(component)) output.add("spoiler")
        if (isItCodeComponent(component)) output.add("code")
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
            lang.minecraft.messageFormatting.spoilerReplaceWithChar
                ?.repeat(getFromSpoilerComponent(component).translate().length) == component.translate() &&
            lang.minecraft.messageFormatting.spoilerFormatting?.any { !component.hasDecoration(it) } == false &&
            component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.spoilerColor.lowercase()) == true

    fun getFromSpoilerComponent(component: Component): TextComponent = component.hoverEvent()?.value() as TextComponent

    fun appendToSpoilerComponent(component: Component, spoilerText: String): TextComponent = appendToSpoilerComponent(component, Component.text(spoilerText))
    fun appendToSpoilerComponent(component: Component, spoilerComponent: TextComponent): TextComponent =
        applyStyle(
            component = Component.text(
                lang.minecraft.messageFormatting.spoilerReplaceWithChar
                    ?.repeat(getFromSpoilerComponent(component).translate().length + spoilerComponent.translate().length).toString()
            ),
            decorations = lang.minecraft.messageFormatting.spoilerFormatting,
            hover = Component.text().append(getFromSpoilerComponent(component)).append(spoilerComponent).build(),
            color = lang.minecraft.messageFormatting.spoilerColor
        )

    fun getAsSpoilerComponent(spoilerText: String): TextComponent = getAsSpoilerComponent(Component.text(spoilerText))
    fun getAsSpoilerComponent(spoilerComponent: TextComponent): TextComponent =
        applyStyle(
            component = Component.text(
                lang.minecraft.messageFormatting.spoilerReplaceWithChar
                ?.repeat(spoilerComponent.translate().length).toString()
            ),
            decorations = lang.minecraft.messageFormatting.spoilerFormatting,
            hover = spoilerComponent,
            color = lang.minecraft.messageFormatting.spoilerColor
        )

    fun isItCodeComponent(component: Component): Boolean =
        isDecorationsEquals(component, lang.minecraft.messageFormatting.codeFormatting) &&
                component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.codeColor.lowercase()) == true
    fun getAsCodeComponent(code: String): TextComponent = getAsCodeComponent(Component.text(code))
    fun getAsCodeComponent(component: TextComponent): TextComponent =
        applyStyle(
            component = component,
            color = lang.minecraft.messageFormatting.codeColor,
            clickEvent = ClickEvent.copyToClipboard(component.translate()),
            hover = Component.text(lang.minecraft.messageMeta.hoverCopyToClipboard),
            insert = component.translate()
        )

    fun isItLinkComponent(component: Component): Boolean =
        isDecorationsEquals(component, lang.minecraft.messageFormatting.linkFormatting) &&
        component.clickEvent() != null &&
        component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.linkColor.lowercase()) == true
    fun isItUrlLinkComponent(component: Component): Boolean =
        isItLinkComponent(component) &&
        component.clickEvent()?.value() == component.translate()
    fun isItTextLinkComponent(component: Component): Boolean =
        isItLinkComponent(component) &&
        component.clickEvent()?.value() != component.translate()
    fun getAsLinkComponent(link: String): TextComponent = getAsLinkComponent(Component.text(link))
    fun getAsLinkComponent(linkComponent: TextComponent): TextComponent = getAsLinkComponent(linkComponent, linkComponent.translate())
    fun getAsLinkComponent(text: String, url: String): TextComponent = getAsLinkComponent(Component.text(text), url)
    fun getAsLinkComponent(component: TextComponent, url: String): TextComponent =
        applyStyle(
            component = component,
            color = lang.minecraft.messageFormatting.linkColor,
            clickEvent = ClickEvent.openUrl(url),
            hover = Component.text(lang.minecraft.messageMeta.hoverOpenInBrowser)
        )
    fun getUrlFromLinkComponent(component: TextComponent): String = (component.clickEvent()?.value()) ?: component.translate()

    fun isIt(component: Component, decorations: List<TextDecoration>, othersOfThis: List<String> = emptyList(), strictly:Boolean = true): Boolean = (if (strictly) isDecorationsEquals(component, decorations) else isDecorationsContains(component, decorations)) && !othersOfThis.any {
        (it == "spoiler" && lang.minecraft.messageFormatting.spoilerFormatting?.containsAll(decorations) == true) ||
        (it == "code" && lang.minecraft.messageFormatting.codeFormatting?.containsAll(decorations) == true) ||
        ((it == "hashtag" || it == "cashtag") && lang.minecraft.messageFormatting.hashtagFormatting?.containsAll(decorations) == true) ||
        (it == "mention" && lang.minecraft.messageFormatting.mentionFormatting?.containsAll(decorations) == true) ||
        ((it == "url" || it == "text_link") && lang.minecraft.messageFormatting.linkFormatting?.containsAll(decorations) == true)
    }
    fun isIt(component: Component, decoration: TextDecoration, othersOfThis: List<String> = emptyList(), strictly:Boolean = true): Boolean = isIt(component, listOf(decoration), othersOfThis, strictly)

    fun getAllOfChildren(component: TextComponent): MutableList<TextComponent> {
        val output = ArrayList<TextComponent>()
        val stack = Stack<TextComponent>()
        stack.push(component)
        var tempComponent: TextComponent
        while (stack.isNotEmpty()) {
            tempComponent = stack.pop()
            val split = splitComponent(tempComponent)
            if (split.size > 1) {
                split.reversed().forEach { stack.push(it) }
            } else output.add(tempComponent)
        }
        return output
    }

    fun splitComponent(component: TextComponent): List<TextComponent> {
        val output = ArrayList<TextComponent>()
        val components = component.children().map { it as TextComponent }
        output.add(crateComponentAndCopyFormatting(component.content(), component))
        if (components.isNotEmpty()) {
            val hasStyle = component.hasStyling()
            output.addAll(components.map {
                if (hasStyle) it.toBuilder().mergeStyle(component).build()
                else it
            })
        }
        return output
    }

    fun crateComponentAndCopyFormatting(text: String, component: TextComponent): TextComponent = Component.text(text)
            .style(component.style())

    fun applyStyle(
        component: TextComponent,
        color: String? = "#FFFFFF",
        decorations: List<TextDecoration>? = emptyList(),
        hover: TextComponent? = null,
        clickEvent: ClickEvent? = null,
        insert: String? = null
    ): TextComponent = applyStyle(component, TextColor.fromHexString(color ?: "#FFFFFF"), decorations, hover, clickEvent, insert)
    fun applyStyle(
        component: TextComponent,
        color: TextColor? = NamedTextColor.WHITE,
        decorations: List<TextDecoration>? = emptyList(),
        hover: TextComponent? = null,
        clickEvent: ClickEvent? = null,
        insert: String? = null
    ): TextComponent {
        val builder = component.toBuilder()
        if (color != null) builder.color(color)
        if (decorations?.isNotEmpty() == true) builder.decorateAll(decorations)
        if (hover != null) builder.hoverEvent(hover.asHoverEvent())
        if (clickEvent != null) builder.clickEvent(clickEvent)
        if (insert != null) builder.insertion(insert)
        return builder.build()
    }
}