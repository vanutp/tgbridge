package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.LoggerBase
import dev.vanutp.tgbridge.TelegramBridgeBase
import org.apache.logging.log4j.LogManager

class TelegramBridge : TelegramBridgeBase() {
    companion object {
        const val MOD_ID = "tgbridge"
    }
    override val logger = Logger()
    override val platform: String = "forge"
}
