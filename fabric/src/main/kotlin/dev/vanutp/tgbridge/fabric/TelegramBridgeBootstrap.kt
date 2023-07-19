package dev.vanutp.tgbridge.fabric

import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

object TelegramBridgeBootstrap : ModInitializer {
    private val logger = LoggerFactory.getLogger("tgbridge")
    private val MOD = TelegramBridge()

    override fun onInitialize() {
        logger.info("meow meow from fabric")
        MOD.onStart()
    }
}
