package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.AbstractLogger
import org.bukkit.plugin.java.JavaPlugin

class PaperLogger(private val plugin: JavaPlugin) : AbstractLogger() {
    override fun info(message: Any) {
        plugin.logger.info(message.toString())
    }

    override fun warn(message: Any) {
        plugin.logger.warning(message.toString())
    }

    override fun error(message: Any) {
        plugin.logger.severe(message.toString())
    }
}
