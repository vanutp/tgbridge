package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.TgbridgeJvm21
import dev.vanutp.tgbridge.paper.compat.*
import kotlin.io.path.deleteIfExists

class PaperTelegramBridge(val plugin: PaperBootstrap) : TelegramBridge() {
    override val logger = PaperLogger(plugin)
    override val platform = PaperPlatform(plugin)

    init {
        val configPath = platform.configDir.resolve("config-paper.yml")
        configPath.deleteIfExists()
        init()

        addIntegration(ChattyV2Compat(this))
        addIntegration(ChattyV3Compat(this))
        addIntegration(HeroChatCompat(this))
        addIntegration(CarbonChatCompat(this))
        addIntegration(DiscordSRVCompat(this))
        addIntegration(EssentialsVanishCompat(this))
        addIntegration(GenericVanishCompat(this))
        addIntegration(SuperVanishCompat(this))
        addIntegration(IncompatibleChatPluginCompat(this))
        addIntegration(VoiceMessagesPaperCompat(this))
        TgbridgeJvm21.register(this)
    }

    internal fun onEnable() {
        availableIntegrations
            .asSequence()
            .filter { it.shouldEnable() }
            .filterIsInstance<AbstractPaperCompat>()
            .forEach { it.onPluginEnable() }
    }
}
