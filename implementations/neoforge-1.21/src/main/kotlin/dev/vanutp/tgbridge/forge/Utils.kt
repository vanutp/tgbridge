package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
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
            NeoForgeTelegramBridge.registryManager,
        )
    )
}

fun PlayerEntity.toTgbridge() = TgbridgePlayer(
    uuid,
    name.string,
    displayName?.string,
)

fun CommandContext<ServerCommandSource>.toTgbridge() = TBCommandContext(
    source = source.player?.toTgbridge(),
    reply = this::reply
)

fun CommandContext<ServerCommandSource>.reply(
    text: String
) {
    source.sendFeedback({ Text.literal(text) }, false)
}
