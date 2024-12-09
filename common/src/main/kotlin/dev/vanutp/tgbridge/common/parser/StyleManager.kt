package dev.vanutp.tgbridge.common.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.*
import kotlin.collections.ArrayList

object StyleManager {

    fun isDecorationsEquals(component: TextComponent, decorations: List<TextDecoration>?): Boolean {
        val componentFormatting = getIsSetDecorations(component)
        return componentFormatting.containsAll(decorations ?: emptyList()) && (decorations
            ?: emptyList()).containsAll(componentFormatting)
    }
    fun isDecorationsContains(component: TextComponent, decorations: List<TextDecoration>?): Boolean {
        val componentFormatting = getIsSetDecorations(component)
        return componentFormatting.containsAll(decorations ?: emptyList())
    }

    fun getIsSetDecorations(component: TextComponent): List<TextDecoration> = component.decorations().filter { it.value.equals(TextDecoration.State.TRUE) } .keys.toList()

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
        if (decorations?.isNotEmpty() == true) decorateAll(builder, decorations)
        if (hover != null) builder.hoverEvent(hover.asHoverEvent())
        if (clickEvent != null) builder.clickEvent(clickEvent)
        if (insert != null) builder.insertion(insert)
        return builder.build()
    }

    fun decorateAll(component: TextComponent, decorations: List<TextDecoration>?): TextComponent =
        decorateAll(component.toBuilder(), decorations).build()
    fun decorateAll(builder: TextComponent.Builder, decorations: List<TextDecoration>?): TextComponent.Builder =
        builder.apply {  -> decorations?.forEach { this.decoration(it, true) } }
}