package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.TgbridgeJvm21
import dev.vanutp.tgbridge.paper.modules.*
import kotlin.io.path.deleteIfExists

class PaperTelegramBridge(val plugin: PaperBootstrap) : TelegramBridge() {
    override val logger = PaperLogger(plugin)
    override val platform = PaperPlatform(plugin)

    init {
        val configPath = platform.configDir.resolve("config-paper.yml")
        configPath.deleteIfExists()
        init()

        addModule(ChattyV2Module(this))
        addModule(ChattyV3Module(this))
        addModule(HeroChatModule(this))
        addModule(DiscordSRVModule(this))
        addModule(EssentialsVanishModule(this))
        addModule(GenericVanishModule(this))
        addModule(SuperVanishModule(this))
        addModule(IncompatibleChatPluginModule(this))
        addModule(VoiceMessagesPaperModule(this))
        TgbridgeJvm21.register(this)
    }

    internal fun onEnable() {
        availableModules
            .asSequence()
            .filter { it.shouldEnable() }
            .filterIsInstance<AbstractPaperModule>()
            .forEach { it.onPluginEnable() }
    }
}
