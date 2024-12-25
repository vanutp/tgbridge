package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.TelegramBridge

class PaperTelegramBridge(private val plugin: PaperBootstrap) : TelegramBridge() {
    override val logger = PaperLogger(plugin)
    override val platform = PaperPlatform(plugin)
    private val eventManager = EventManager(plugin)

    override fun platformInit() {
        PaperConfigManager.init(platform.configDir)
        eventManager.register()
    }
}
