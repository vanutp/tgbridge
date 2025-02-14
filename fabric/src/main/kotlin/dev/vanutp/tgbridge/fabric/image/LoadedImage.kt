package dev.vanutp.tgbridge.fabric.image

import dev.vanutp.tgbridge.fabric.FabricTelegramBridge
import net.minecraft.client.MinecraftClient
import net.minecraft.client.texture.NativeImage
import net.minecraft.client.texture.NativeImageBackedTexture
import net.minecraft.util.Identifier
import java.nio.file.Files
import java.nio.file.Path


class LoadedImage(val width: Int, val height: Int, val textureId: Identifier) {
    companion object {
        private val registry: MutableMap<String, LoadedImage> = mutableMapOf()
        fun load(id: String, path: String) = registry.getOrPut(id) {
            Files.newInputStream(Path.of(path)).use {
                val nativeImage = NativeImage.read(it)
                val textureId = Identifier.of(FabricTelegramBridge.MOD_ID, "dynamic_textures/${id}");
                MinecraftClient.getInstance().textureManager.registerTexture(
                    textureId,
                    NativeImageBackedTexture(nativeImage),
                )
                LoadedImage(nativeImage.width / 4, nativeImage.height / 4, textureId)
            }
        }
    }
}
