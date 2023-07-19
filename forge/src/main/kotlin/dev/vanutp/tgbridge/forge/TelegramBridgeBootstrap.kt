package dev.vanutp.tgbridge.forge

import net.minecraft.client.MinecraftClient
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import thedarkcolour.kotlinforforge.forge.runForDist

@Mod(TelegramBridge.MOD_ID)
object TelegramBridgeBootstrap {
    val mod = TelegramBridge()

    init {
        val obj = runForDist(
            clientTarget = {},
            serverTarget = {
                MOD_BUS.addListener(::onServerSetup)
            }
        )
    }

    private fun onServerSetup(event: FMLDedicatedServerSetupEvent) {
        mod.init()
    }
}
