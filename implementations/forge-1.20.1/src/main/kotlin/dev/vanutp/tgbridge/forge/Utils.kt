package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.text.Text


fun Component.toMinecraft(): Text {
    return Text.Serializer.fromJson(GsonComponentSerializer.gson().serializeToTree(this))!!
}

fun Text.toAdventure(): Component {
    return GsonComponentSerializer.gson().deserializeFromTree(Text.Serializer.toJsonTree(this))
}

fun PlayerEntity.toTgbridge() = TgbridgePlayer(
    uuid,
    name.string,
    displayName?.string,
)
