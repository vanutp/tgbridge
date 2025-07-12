package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.compat.AbstractCompat
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import org.bukkit.event.Listener

abstract class AbstractPaperCompat(override val bridge: PaperTelegramBridge) : AbstractCompat(bridge), Listener {
    protected fun getPlugin() = paperId?.let { bridge.plugin.server.pluginManager.getPlugin(it) }

    override fun enable() {
        bridge.plugin.server.pluginManager.registerEvents(this, bridge.plugin)
    }
}
