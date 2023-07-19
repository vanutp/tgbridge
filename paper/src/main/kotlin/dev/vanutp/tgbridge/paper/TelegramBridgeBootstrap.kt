package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class TelegramBridgeBootstrap : JavaPlugin() {
    private val mod = TelegramBridge(this)
    override fun onEnable() {
        mod.init()
    }

    override fun onDisable() {
    }
}
