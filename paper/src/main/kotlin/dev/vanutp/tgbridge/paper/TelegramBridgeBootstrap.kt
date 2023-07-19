package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class TelegramBridgeBootstrap : JavaPlugin() {
    private val MOD = TelegramBridge()
    override fun onEnable() {
        println("meow meow from paper")
        MOD.onStart()
    }

    override fun onDisable() {
    }
}
