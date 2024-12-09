package dev.vanutp.tgbridge.common.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextDecoration
import dev.vanutp.tgbridge.common.parser.StyleManager.applyStyle
import dev.vanutp.tgbridge.common.parser.StyleManager.isDecorationsContains
import dev.vanutp.tgbridge.common.parser.StyleManager.isDecorationsEquals
import dev.vanutp.tgbridge.common.TgMessage
import dev.vanutp.tgbridge.common.ConfigManager.lang

object FormattingManager {

    fun getEntityNamesFromComponent(component: TextComponent): List<String> {
        val output = ArrayList<String>()
        if (isItTextLinkComponent(component)) output.add("text_link")
        if (isItUrlLinkComponent(component)) output.add("url")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.mentionFormatting)
            && component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.mentionColor.lowercase()) == true
            && component.content().startsWith("@")) output.add("mention")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.hashtagFormatting)
            && component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.hashtagColor.lowercase()) == true
            && component.content().startsWith("#")) output.add("hashtag")
        if (isDecorationsEquals(component, lang.minecraft.messageFormatting.hashtagFormatting)
            && component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.hashtagColor.lowercase()) == true
            && component.content().startsWith("$")) output.add("cashtag")
        if (isItSpoiler(component)) output.add("spoiler")
        if (isItCodeComponent(component)) output.add("code")
        if (isIt(component, TextDecoration.BOLD, output, false)) output.add("bold")
        if (isIt(component, TextDecoration.ITALIC, output, false)) output.add("italic")
        if (isIt(component, TextDecoration.UNDERLINED, output, false)) output.add("underline")
        if (isIt(component, TextDecoration.STRIKETHROUGH, output, false)) output.add("strikethrough")

        return output
    }

    fun addChatLink(message: TgMessage, component: TextComponent): TextComponent {
        return component.clickEvent(ClickEvent.openUrl("https://t.me/c/${-message.chat.id-1000000000000}/" + (if (message.messageThreadId!=null) "${message.messageThreadId}/" else "") + "${message.messageId}"))
            .hoverEvent(Component.text(lang.minecraft.messageMeta.hoverOpenInTelegram).asHoverEvent())
    }

    fun isItSpoiler(component: TextComponent): Boolean = component.hoverEvent()?.value() is TextComponent &&
            lang.minecraft.messageFormatting.spoilerReplaceWithChar
                ?.repeat(getFromSpoilerComponent(component).content().length) == component.content() &&
            lang.minecraft.messageFormatting.spoilerFormatting?.any { !component.hasDecoration(it) } == false &&
            component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.spoilerColor.lowercase()) == true

    fun getFromSpoilerComponent(component: TextComponent): TextComponent = component.hoverEvent()?.value() as TextComponent

    fun appendToSpoilerComponent(component: TextComponent, spoilerText: String): TextComponent = appendToSpoilerComponent(component, Component.text(spoilerText))
    fun appendToSpoilerComponent(component: TextComponent, spoilerComponent: TextComponent): TextComponent =
        applyStyle(
            component = Component.text(
                lang.minecraft.messageFormatting.spoilerReplaceWithChar
                    ?.repeat(getFromSpoilerComponent(component).content().length + spoilerComponent.content().length).toString()
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
                    ?.repeat(spoilerComponent.content().length).toString()
            ),
            decorations = lang.minecraft.messageFormatting.spoilerFormatting,
            hover = spoilerComponent,
            color = lang.minecraft.messageFormatting.spoilerColor
        )

    fun isItCodeComponent(component: TextComponent): Boolean =
        isDecorationsEquals(component, lang.minecraft.messageFormatting.codeFormatting) &&
                component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.codeColor.lowercase()) == true
    fun getAsCodeComponent(code: String): TextComponent = getAsCodeComponent(Component.text(code))
    fun getAsCodeComponent(component: TextComponent): TextComponent =
        applyStyle(
            component = component,
            color = lang.minecraft.messageFormatting.codeColor,
            clickEvent = ClickEvent.copyToClipboard(component.content()),
            hover = Component.text(lang.minecraft.messageMeta.hoverCopyToClipboard),
            insert = component.content()
        )

    fun isItLinkComponent(component: TextComponent): Boolean =
        isDecorationsEquals(component, lang.minecraft.messageFormatting.linkFormatting) &&
                component.clickEvent() != null &&
                component.color()?.asHexString()?.lowercase()?.equals(lang.minecraft.messageFormatting.linkColor.lowercase()) == true
    fun isItUrlLinkComponent(component: TextComponent): Boolean =
        isItLinkComponent(component) &&
                component.clickEvent()?.value() == component.content()
    fun isItTextLinkComponent(component: TextComponent): Boolean =
        isItLinkComponent(component) &&
                component.clickEvent()?.value() != component.content()
    fun getAsLinkComponent(link: String): TextComponent = getAsLinkComponent(Component.text(link))
    fun getAsLinkComponent(linkComponent: TextComponent): TextComponent = getAsLinkComponent(linkComponent, linkComponent.content())
    fun getAsLinkComponent(text: String, url: String): TextComponent = getAsLinkComponent(Component.text(text), url)
    fun getAsLinkComponent(component: TextComponent, url: String): TextComponent =
        applyStyle(
            component = component,
            color = lang.minecraft.messageFormatting.linkColor,
            clickEvent = ClickEvent.openUrl(url),
            hover = Component.text(lang.minecraft.messageMeta.hoverOpenInBrowser)
        )
    fun getUrlFromLinkComponent(component: TextComponent): String = (component.clickEvent()?.value()) ?: component.content()

    fun isIt(component: TextComponent, decorations: List<TextDecoration>, othersOfThis: List<String> = emptyList(), strictly:Boolean = true): Boolean = (if (strictly) isDecorationsEquals(component, decorations) else isDecorationsContains(component, decorations)) && !othersOfThis.any {
        (it == "spoiler" && lang.minecraft.messageFormatting.spoilerFormatting?.containsAll(decorations) == true) ||
                (it == "code" && lang.minecraft.messageFormatting.codeFormatting?.containsAll(decorations) == true) ||
                ((it == "hashtag" || it == "cashtag") && lang.minecraft.messageFormatting.hashtagFormatting?.containsAll(decorations) == true) ||
                (it == "mention" && lang.minecraft.messageFormatting.mentionFormatting?.containsAll(decorations) == true) ||
                ((it == "url" || it == "text_link") && lang.minecraft.messageFormatting.linkFormatting?.containsAll(decorations) == true)
    }
    fun isIt(component: TextComponent, decoration: TextDecoration, othersOfThis: List<String> = emptyList(), strictly:Boolean = true): Boolean = isIt(component, listOf(decoration), othersOfThis, strictly)

}