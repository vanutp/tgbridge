package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : JavaPlugin() {
    val tgbridge = PaperTelegramBridge(this)

    override fun onEnable() {
        EventManager(this).register()
        tgbridge.init()
        server.asyncScheduler.runNow(this) {
            tgbridge.onServerStarted()
        }
    }

    override fun onDisable() {
        tgbridge.shutdown()
    }
}
