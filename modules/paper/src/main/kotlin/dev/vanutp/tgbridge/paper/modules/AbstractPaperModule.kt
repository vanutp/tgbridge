package dev.vanutp.tgbridge.paper.modules

import dev.vanutp.tgbridge.common.modules.AbstractModule
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener

abstract class AbstractPaperModule(override val bridge: PaperTelegramBridge) : AbstractModule(bridge), Listener {
    protected fun getPlugin() = paperId?.let { bridge.plugin.server.pluginManager.getPlugin(it) }

    override fun enable() {
        bridge.plugin.server.pluginManager.registerEvents(this, bridge.plugin)
    }

    override fun disable() {
        HandlerList.unregisterAll(this)
    }

    open fun onPluginEnable() {}
}
