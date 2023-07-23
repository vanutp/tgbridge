package dev.vanutp.tgbridge.forge

import net.minecraftforge.event.server.ServerStoppingEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import thedarkcolour.kotlinforforge.forge.FORGE_BUS
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(ForgeTelegramBridge.MOD_ID)
object ForgeBootstrap {
    private val bridge = ForgeTelegramBridge()

    init {
        runForDist(
            clientTarget = {},
            serverTarget = {
                MOD_BUS.addListener(::onServerSetup)
                FORGE_BUS.addListener(::onServerShutdown)
            }
        )
    }

    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
        bridge.init()
    }

    private fun onServerShutdown(event: ServerStoppingEvent) {
        bridge.shutdown()
    }
}
