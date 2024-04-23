package dev.vanutp.tgbridge.forge

import dev.vanutp.tgbridge.common.AbstractLogger
import org.apache.logging.log4j.LogManager

class ForgeLogger : AbstractLogger() {
    private val forgeLogger = LogManager.getLogger(ForgeTelegramBridge.MOD_ID)
    override fun info(message: Any) {
        forgeLogger.info(message)
    }

    override fun warn(message: Any) {
        forgeLogger.warn(message)
    }

    override fun error(message: Any) {
        forgeLogger.error(message)
    }
}
