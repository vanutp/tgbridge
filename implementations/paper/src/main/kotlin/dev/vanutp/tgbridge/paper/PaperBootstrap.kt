package dev.vanutp.tgbridge.paper

import java.util.concurrent.TimeUnit
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : JavaPlugin() {
    val tgbridge = PaperTelegramBridge(this)

    override fun onEnable() {
        EventManager(this).register()
        Bukkit.getAsyncScheduler().runDelayed(this, { (tgbridge::onServerStarted)() }, 5, TimeUnit.SECONDS)
        tgbridge.init()
    }

    override fun onDisable() {
        tgbridge.shutdown()
    }
}
