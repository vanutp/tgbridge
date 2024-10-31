package dev.vanutp.tgbridge.fabric

import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.SharedConstants

object FabricBootstrap : DedicatedServerModInitializer {
    private lateinit var bridge: FabricTelegramBridge

    override fun onInitializeServer() {
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            bridge = FabricTelegramBridge(server)
            bridge.init()
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            bridge.shutdown()
        }
    }
}
