package dev.vanutp.tgbridge.common

import net.kyori.adventure.builder.AbstractBuilder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.ComponentSerializer
import net.kyori.adventure.util.Buildable

class HTMLComponentSerializer : ComponentSerializer<Component, TextComponent, String> {
    override fun deserialize(input: String): TextComponent {
        throw NotImplementedError()
    }

    override fun serialize(component: Component): String {
        TODO()
    }

}
