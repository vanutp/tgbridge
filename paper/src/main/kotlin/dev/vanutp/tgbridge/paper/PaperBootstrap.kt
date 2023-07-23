package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : JavaPlugin() {
    private val bridge = PaperTelegramBridge(this)
    override fun onEnable() {
        bridge.init()
    }

    override fun onDisable() {
        bridge.shutdown()
    }
}
