package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.paper.compat.*
import kotlin.io.path.deleteIfExists

class PaperTelegramBridge(private val plugin: PaperBootstrap) : TelegramBridge() {
    override val logger = PaperLogger(plugin)
    override val platform = PaperPlatform(plugin)
    private val eventManager = EventManager(plugin)

    lateinit var integrations: List<AbstractCompat> private set

    init {
        val configPath = platform.configDir.resolve("config-paper.yml")
        configPath.deleteIfExists()
    }

    fun asyncInit() {
        // Running in a scheduled task to wait for all the plugins
        // to load to reliably test if a plugin is enabled
        integrations = listOf(
            EssentialsVanishCompat(plugin),
            SuperVanishCompat(plugin),
            ChattyV2Compat(plugin),
            ChattyV3Compat(plugin),
            UnsupportedChatPluginCompat(plugin),
        ).filter {
            it.shouldEnable()
        }
        for (integration in integrations) {
            logger.info("Using ${integration::class.simpleName}")
            integration.enable()
        }

        eventManager.register()

        onServerStarted()
    }
}
