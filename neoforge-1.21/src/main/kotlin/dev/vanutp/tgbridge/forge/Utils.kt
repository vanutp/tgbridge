package dev.vanutp.tgbridge.forge

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.text.Text


fun Component.toMinecraft(): Text {
    return Text.Serialization.fromJsonTree(
        GsonComponentSerializer.gson().serializeToTree(this),
        DynamicRegistryManager.of(Registries.REGISTRIES)
    )!!
}

fun Text.toAdventure(): Component {
    return GsonComponentSerializer.gson().deserialize(
        Text.Serialization.toJsonString(
            this,
            DynamicRegistryManager.of(Registries.REGISTRIES)
        )
    )
}
