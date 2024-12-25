package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : JavaPlugin() {
    val tgbridge = PaperTelegramBridge(this)

    override fun onEnable() {
        tgbridge.init()
    }

    override fun onDisable() {
        tgbridge.shutdown()
    }
}
