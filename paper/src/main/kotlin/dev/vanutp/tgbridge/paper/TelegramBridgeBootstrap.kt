package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class TelegramBridgeBootstrap : JavaPlugin() {
    private val mod = TelegramBridge()
    override fun onEnable() {
        println("meow meow from paper")
        mod.init()
    }

    override fun onDisable() {
    }
}
