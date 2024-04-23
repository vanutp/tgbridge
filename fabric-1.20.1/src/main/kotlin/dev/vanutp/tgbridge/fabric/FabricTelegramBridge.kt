package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.TelegramBridge
import net.minecraft.server.MinecraftServer

class FabricTelegramBridge(server: MinecraftServer) : TelegramBridge() {
    companion object {
        const val MOD_ID = "tgbridge"
    }
    override val logger = FabricLogger()
    override val platform = FabricPlatform(server)
}
