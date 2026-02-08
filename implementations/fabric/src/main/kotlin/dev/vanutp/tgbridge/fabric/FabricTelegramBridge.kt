package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.TgbridgeJvm21
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

object FabricTelegramBridge : DedicatedServerModInitializer, TelegramBridge() {
    const val MOD_ID = "tgbridge"
    override val logger = FabricLogger()
    override val platform = FabricPlatform()
    lateinit var server: MinecraftServer private set

    init {
        init()
    }

    override fun onInitializeServer() {
        if (Runtime.version().feature() >= 21) {
            TgbridgeJvm21.register(this)
        }
        EventManager.register()
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            this.server = server
        }
        ServerLifecycleEvents.SERVER_STARTED.register {
            onServerStarted()
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            shutdown()
        }
    }
}
