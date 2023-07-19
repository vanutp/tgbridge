package dev.vanutp.tgbridge.fabric

import net.fabricmc.api.ModInitializer

object TelegramBridgeBootstrap : ModInitializer {
    private val MOD = TelegramBridge()

    override fun onInitialize() {
        MOD.init()
    }
}
