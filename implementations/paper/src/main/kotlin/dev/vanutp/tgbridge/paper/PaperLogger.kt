package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.ILogger
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class PaperLogger(private val plugin: JavaPlugin) : ILogger {
    override fun info(message: Any) {
        plugin.logger.info(message.toString())
    }

    override fun warn(message: Any) {
        plugin.logger.warning(message.toString())
    }

    override fun error(message: Any) {
        plugin.logger.severe(message.toString())
    }

    override fun error(message: Any, exc: Exception) {
        plugin.logger.log(Level.SEVERE, message.toString(), exc)
    }
}
