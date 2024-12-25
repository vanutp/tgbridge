package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.paper.compat.AbstractCompat
import dev.vanutp.tgbridge.paper.compat.EssentialsVanishCompat
import dev.vanutp.tgbridge.paper.compat.SuperVanishCompat

class PaperTelegramBridge(private val plugin: PaperBootstrap) : TelegramBridge() {
    override val logger = PaperLogger(plugin)
    override val platform = PaperPlatform(plugin)
    private val eventManager = EventManager(plugin)

    lateinit var integrations: List<AbstractCompat> private set

    override fun platformInit() {
        PaperConfigManager.init(platform.configDir)
        eventManager.register()

        integrations = listOf(
            EssentialsVanishCompat(plugin),
            SuperVanishCompat(plugin),
        ).filter {
            plugin.server.pluginManager.getPlugin(it.pluginId)?.isEnabled == true
        }
        for (integration in integrations) {
            integration.enable()
        }
    }
}
