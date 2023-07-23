package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.AbstractLogger
import org.slf4j.LoggerFactory

class FabricLogger : AbstractLogger() {
    private val fabricLogger = LoggerFactory.getLogger(FabricTelegramBridge.MOD_ID)
    override fun info(message: Any) {
        fabricLogger.info(message.toString())
    }

    override fun warn(message: Any) {
        fabricLogger.warn(message.toString())
    }

    override fun error(message: Any) {
        fabricLogger.error(message.toString())
    }
}
