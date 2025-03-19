package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.paper.PaperBootstrap
import org.bukkit.event.Listener

abstract class AbstractCompat(protected val bootstrap: PaperBootstrap) : Listener {
    abstract val pluginId: String?

    protected fun getPlugin() = pluginId?.let { bootstrap.server.pluginManager.getPlugin(it) }

    open fun shouldEnable() = getPlugin()?.isEnabled == true

    open fun enable() {
        bootstrap.server.pluginManager.registerEvents(this, bootstrap)
    }
}
