package dev.vanutp.tgbridge.fabric

import com.google.gson.JsonParseException
import com.mojang.brigadier.context.CommandContext
import com.mojang.serialization.JsonOps
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.fabric.FabricTelegramBridge.server
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.ComponentSerialization
import net.minecraft.network.chat.Component as Text


fun Component.toMinecraft(): Text {
    val serializedTree = GsonComponentSerializer.gson().serializeToTree(this)
    return ComponentSerialization.CODEC
        .decode(server.registryAccess().createSerializationContext(JsonOps.INSTANCE), serializedTree)
        .getOrThrow(::JsonParseException)
        .first
}

fun Text.toAdventure(): Component {
    val jsonTree = ComponentSerialization.CODEC
        .encodeStart(server.registryAccess().createSerializationContext(JsonOps.INSTANCE), this)
        .getOrThrow(::JsonParseException)
    return GsonComponentSerializer.gson().deserializeFromTree(jsonTree)
}

fun CommandContext<CommandSourceStack>.toTgbridge() = TBCommandContext(
    source = source.player?.toTgbridge(),
    reply = this::reply
)

fun CommandContext<CommandSourceStack>.reply(text: String) {
    val textComponent = Text.literal(text)
    source.sendSuccess({ textComponent }, false)
}
