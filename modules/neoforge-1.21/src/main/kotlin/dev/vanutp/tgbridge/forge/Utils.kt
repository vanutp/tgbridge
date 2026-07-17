package dev.vanutp.tgbridge.forge

import com.mojang.brigadier.context.CommandContext
import dev.vanutp.tgbridge.common.models.TBCommandContext
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component as Text


fun Component.toMinecraft(): Text {
    return Text.Serializer.fromJson(
        GsonComponentSerializer.gson().serializeToTree(this),
        NeoForgeTelegramBridge.registryManager,
    )!!
}

fun Text.toAdventure(): Component {
    return GsonComponentSerializer.gson().deserialize(
        Text.Serializer.toJson(
            this,
            NeoForgeTelegramBridge.registryManager,
        )
    )
}

fun CommandContext<CommandSourceStack>.toTgbridge() = TBCommandContext(
    source = source.player?.toTgbridge(),
    reply = this::reply
)

fun CommandContext<CommandSourceStack>.reply(
    text: String
) {
    source.sendSuccess({ Text.literal(text) }, false)
}
