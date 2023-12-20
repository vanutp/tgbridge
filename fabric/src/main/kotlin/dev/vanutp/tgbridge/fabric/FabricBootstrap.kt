package dev.vanutp.tgbridge.fabric

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents

object FabricBootstrap : ModInitializer {
    private lateinit var bridge: FabricTelegramBridge

    override fun onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            bridge = FabricTelegramBridge(server)
            bridge.init()
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            bridge.shutdown()
        }
    }
}
