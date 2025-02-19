package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.fabric.compat.ICompat
import dev.vanutp.tgbridge.fabric.compat.VanishCompat
import net.fabricmc.api.DedicatedServerModInitializer
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.MinecraftServer

object FabricTelegramBridge : DedicatedServerModInitializer, TelegramBridge() {
    const val MOD_ID = "tgbridge"
    override val logger = FabricLogger()
    override val platform = FabricPlatform()
    lateinit var versionInfo: ServerVersionInfo private set
    lateinit var server: MinecraftServer private set

    val integrations: List<ICompat> = listOf(
        VanishCompat()
    )
        .filter { FabricLoader.getInstance().isModLoaded(it.modId) }


    override fun onInitializeServer() {
        EventManager.register()
        for (integration in integrations) {
            integration.enable()
        }
        ServerLifecycleEvents.SERVER_STARTING.register { server ->
            versionInfo = ServerVersionInfo(server)
            this.server = server
            init()
        }
        ServerLifecycleEvents.SERVER_STARTED.register {
            onServerStarted()
        }
        ServerLifecycleEvents.SERVER_STOPPING.register {
            shutdown()
        }
    }
}
