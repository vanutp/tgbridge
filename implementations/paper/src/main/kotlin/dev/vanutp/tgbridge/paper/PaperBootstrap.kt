package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : JavaPlugin() {
    val tgbridge = PaperTelegramBridge(this)

    override fun onEnable() {
        EventManager(this).register()
        server.scheduler.scheduleSyncDelayedTask(this, tgbridge::onServerStarted)
        tgbridge.init()
    }

    override fun onDisable() {
        tgbridge.shutdown()
    }
}
