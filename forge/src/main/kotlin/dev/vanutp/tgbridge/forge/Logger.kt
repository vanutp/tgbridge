package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.LoggerBase
import org.apache.logging.log4j.LogManager

class Logger : LoggerBase() {
    private val forgeLogger = LogManager.getLogger(TelegramBridge.MOD_ID)
    override fun info(message: String) {
        forgeLogger.info(message)
    }

    override fun warn(message: String) {
        forgeLogger.warn(message)
    }

    override fun error(message: String) {
        forgeLogger.error(message)
    }
}
