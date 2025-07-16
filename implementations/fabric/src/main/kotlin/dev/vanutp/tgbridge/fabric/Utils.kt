package dev.vanutp.tgbridge.fabric

import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.mojang.serialization.JsonOps
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.registry.RegistryWrapper
import net.minecraft.text.Text
import net.minecraft.text.TextCodecs


fun Component.toMinecraft(): Text {
    val serializedTree = GsonComponentSerializer.gson().serializeToTree(this)

    return if (FabricTelegramBridge.versionInfo.IS_19_204) {
        // net.minecraft.text.Text$Serializer
        val textCls = Class.forName("net.minecraft.class_2561\$class_2562")
        // called fromJson on older versions
        val fromJsonTree = textCls.getMethod("method_10872", JsonElement::class.java)
        fromJsonTree(null, serializedTree) as Text
    } else if (FabricTelegramBridge.versionInfo.IS_205_215) {
        // 1.20.5+
        // net.minecraft.text.Text$Serialization
        val textCls = Class.forName("net.minecraft.class_2561\$class_2562")
        val fromJsonTree =
            textCls.getMethod("method_10872", JsonElement::class.java, RegistryWrapper.WrapperLookup::class.java)
        fromJsonTree(
            null,
            serializedTree,
            DynamicRegistryManager.of(Registries.REGISTRIES)
        ) as Text
    } else {
        // 1.21.6+
        TextCodecs.CODEC
            .decode(DynamicRegistryManager.of(Registries.REGISTRIES).getOps(JsonOps.INSTANCE), serializedTree)
            .getOrThrow(::JsonParseException)
            .first
    }
}

fun Text.toAdventure(): Component {
    return if (FabricTelegramBridge.versionInfo.IS_19_204) {
        // net.minecraft.text.Text$Serializer
        val textCls = Class.forName("net.minecraft.class_2561\$class_2562")
        // called toJson on older versions
        val toJsonString = textCls.getMethod("method_10867", Text::class.java)
        val jsonString = toJsonString(null, this) as String
        GsonComponentSerializer.gson().deserialize(jsonString)
    } else if (FabricTelegramBridge.versionInfo.IS_205_215) {
        // 1.20.5+
        // net.minecraft.text.Text$Serialization
        val textCls = Class.forName("net.minecraft.class_2561\$class_2562")
        // TODO: can toJson be used here?
        val toJsonString =
            textCls.getMethod("method_10867", Text::class.java, RegistryWrapper.WrapperLookup::class.java)
        val jsonString = toJsonString(
            null,
            this,
            DynamicRegistryManager.of(Registries.REGISTRIES)
        ) as String
        GsonComponentSerializer.gson().deserialize(jsonString)
    } else {
        // 1.21.6+
        val jsonTree = TextCodecs.CODEC
            .encodeStart(DynamicRegistryManager.of(Registries.REGISTRIES).getOps(JsonOps.INSTANCE), this)
            .getOrThrow(::JsonParseException)
        GsonComponentSerializer.gson().deserializeFromTree(jsonTree)
    }
}

fun PlayerEntity.toTgbridge() = TgbridgePlayer(
    uuid,
    name.string,
    displayName?.string,
)
