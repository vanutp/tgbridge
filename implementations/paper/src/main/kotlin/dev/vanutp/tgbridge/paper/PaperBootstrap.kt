package dev.vanutp.tgbridge.paper

import org.bukkit.plugin.java.JavaPlugin

class PaperBootstrap : JavaPlugin() {
    val tgbridge = PaperTelegramBridge(this)

    private fun isFolia(): Boolean {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
            return true
        } catch (e: ClassNotFoundException) {
            return false
        }
    }

    override fun onEnable() {
        EventManager(this).register()
        tgbridge.init()
        if (isFolia()) {
            server.asyncScheduler.runNow(this) {
                tgbridge.onServerStarted()
            }
        } else {
            server.scheduler.runTaskAsynchronously(this, tgbridge::onServerStarted)
        }
    }

    override fun onDisable() {
        tgbridge.shutdown()
    }
}
