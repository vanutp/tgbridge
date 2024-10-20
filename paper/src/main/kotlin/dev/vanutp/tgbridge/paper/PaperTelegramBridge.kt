package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.PaperConfigManager
import dev.vanutp.tgbridge.common.TelegramBridge
import org.bukkit.plugin.java.JavaPlugin

class PaperTelegramBridge(plugin: JavaPlugin) : TelegramBridge() {
    override val logger = PaperLogger(plugin)
    override val platform = PaperPlatform(plugin)

    override fun platformInit() {
        PaperConfigManager.init(platform.configDir)
    }
}
