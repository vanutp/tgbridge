package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.LoggerBase
import org.apache.logging.log4j.LogManager
import org.slf4j.LoggerFactory

class Logger : LoggerBase() {
    private val fabricLogger = LoggerFactory.getLogger(TelegramBridge.MOD_ID)
    override fun info(message: String) {
        fabricLogger.info(message)
    }

    override fun warn(message: String) {
        fabricLogger.warn(message)
    }

    override fun error(message: String) {
        fabricLogger.error(message)
    }
}
