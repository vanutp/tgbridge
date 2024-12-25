package dev.vanutp.tgbridge.fabric

import com.google.gson.JsonElement
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minecraft.registry.DynamicRegistryManager
import net.minecraft.registry.Registries
import net.minecraft.text.Text


fun Component.toMinecraft(): Text {
    val serializedTree = GsonComponentSerializer.gson().serializeToTree(this)

    return if (FabricTelegramBridge.versionInfo.IS_19_204) {
        // net.minecraft.text.Text$Serializer
        val textCls = Class.forName("net.minecraft.class_2561\$class_2562")
        // called fromJson on older versions
        val fromJsonTree = textCls.getMethod("method_10872", JsonElement::class.java)
        fromJsonTree.invoke(null, serializedTree) as Text
    } else {
        // 1.20.5+
        Text.Serialization.fromJsonTree(
            serializedTree,
            DynamicRegistryManager.of(Registries.REGISTRIES)
        )!!
    }
}

fun Text.toAdventure(): Component {
    val jsonString = if (FabricTelegramBridge.versionInfo.IS_19_204) {
        // net.minecraft.text.Text$Serializer
        val textCls = Class.forName("net.minecraft.class_2561\$class_2562")
        // called toJson on older versions
        val toJsonString = textCls.getMethod("method_10867", Text::class.java)
        toJsonString.invoke(null, this) as String
    } else {
        // 1.20.5+
        Text.Serialization.toJsonString(
            this,
            DynamicRegistryManager.of(Registries.REGISTRIES)
        )
    }

    return GsonComponentSerializer.gson().deserialize(jsonString)
}
