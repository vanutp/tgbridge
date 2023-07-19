package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.LoggerBase

class Logger(private val plugin: TelegramBridgeBootstrap) : LoggerBase() {
    override fun info(message: String) {
        plugin.logger.info(message)
    }

    override fun warn(message: String) {
        plugin.logger.warning(message)
    }

    override fun error(message: String) {
        plugin.logger.severe(message)
    }
}
