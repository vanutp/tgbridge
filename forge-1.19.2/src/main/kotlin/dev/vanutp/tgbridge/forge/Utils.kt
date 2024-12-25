package dev.vanutp.tgbridge.forge

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.text.Text


fun Component.toMinecraft(): Text {
    return Text.Serializer.fromJson(GsonComponentSerializer.gson().serializeToTree(this))!!
}

fun Text.toAdventure(): Component {
    return GsonComponentSerializer.gson().deserializeFromTree(Text.Serializer.toJsonTree(this))
}
