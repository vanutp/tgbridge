package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : JavaPlugin() {
    val tgbridge = PaperTelegramBridge(this)

    override fun onEnable() {
        server.scheduler.scheduleSyncDelayedTask(this, tgbridge::asyncInit)
        tgbridge.init()
    }

    override fun onDisable() {
        tgbridge.shutdown()
    }
}
