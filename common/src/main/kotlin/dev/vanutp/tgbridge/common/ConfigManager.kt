package dev.vanutp.tgbridge.common

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.YamlMap
import com.charleskorn.kaml.yamlMap
import dev.vanutp.tgbridge.common.models.Config
import dev.vanutp.tgbridge.common.models.Lang
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.kyori.adventure.text.minimessage.MiniMessage
import java.nio.file.Path
import kotlin.io.path.*

object ConfigManager {
    private const val LATEST_CONFIG_VERSION = 3
    private const val LATEST_LANG_VERSION = 3

    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
    private lateinit var configDir: Path
    lateinit var config: Config
        private set
    lateinit var lang: Lang
        private set
    private lateinit var fallbackMinecraftLangGetter: (String) -> String?
    private var minecraftLang: Map<String, String>? = null
    private val hardcodedDefaultMinecraftLang = mapOf(
        "gui.xaero-deathpoint-old" to "Old Death",
        "gui.xaero-deathpoint" to "Death",
    )

    fun getMinecraftLangKey(key: String): String? {
        return minecraftLang?.get(key)
            ?: fallbackMinecraftLangGetter(key)
            ?: hardcodedDefaultMinecraftLang[key]
    }

    fun init(configDir: Path, fallbackMinecraftLangGetter: (String) -> String?) {
        this.configDir = configDir
        this.fallbackMinecraftLangGetter = fallbackMinecraftLangGetter
        reload()
    }

    private fun migrateLang() {
        if (lang.version == 1) {
            val mm = MiniMessage.miniMessage()
            lang = lang.copy(
                version = 2,
                telegram = lang.telegram.copy(
                    chatMessage = lang.telegram.chatMessage
                        .replace("{sender}", "<sender>")
                        .replace("{text}", "")
                        .trim()
                ),
                minecraft = lang.minecraft.copy(
                    messageMeta = lang.minecraft.messageMeta.copy(
                        reply = "<blue>" + mm.escapeTags(lang.minecraft.messageMeta.reply)
                            .replace("{sender}", "<sender>")
                            .replace("{text}", "<text>"),
                        replyToMinecraft = "<blue>" + mm.escapeTags(lang.minecraft.messageMeta.replyToMinecraft)
                            .replace("{text}", "<text>"),
                        forward = "<gray>" + mm.escapeTags(lang.minecraft.messageMeta.forward)
                            .replace("{from}", "<from>"),
                        gif = mm.escapeTags(lang.minecraft.messageMeta.gif),
                        document = mm.escapeTags(lang.minecraft.messageMeta.document),
                        photo = mm.escapeTags(lang.minecraft.messageMeta.photo),
                        audio = mm.escapeTags(lang.minecraft.messageMeta.audio),
                        sticker = mm.escapeTags(lang.minecraft.messageMeta.sticker),
                        video = mm.escapeTags(lang.minecraft.messageMeta.video),
                        videoMessage = mm.escapeTags(lang.minecraft.messageMeta.videoMessage),
                        voiceMessage = mm.escapeTags(lang.minecraft.messageMeta.voiceMessage),
                        poll = mm.escapeTags(lang.minecraft.messageMeta.poll)
                            .replace("{title}", "<title>"),
                        pin = "<dark_aqua>" + mm.escapeTags(lang.minecraft.messageMeta.pin) + "</dark_aqua> <message>",
                    )
                )
            )
        } else if (lang.version == 2) {
            lang = lang.copy(
                version = 3,
                telegram = lang.telegram.copy(
                    playerDied = lang.telegram.playerDied
                        .replace("{deathMessage}", "<death_message>")
                ),
            )
        } else if (lang.version != LATEST_LANG_VERSION) {
            throw Exception("Unsupported lang version ${lang.version}")
        }
    }

    private fun migrateConfig(configPath: Path) {
        if (config.version == 1) {
            config = config.copy(
                version = 2,
                messages = config.messages.copy(
                    requirePrefixInMinecraft = null,
                ),
            )
        } else if (config.version == 2) {
            val data = yaml.parseToYamlNode(configPath.readText())
            config = config.copy(
                version = 3,
                integrations = config.integrations.copy(
                    bluemapUrl = data
                        .yamlMap.get<YamlMap>("messages")
                        ?.getScalar("bluemapUrl")?.content ?: ""
                )
            )
        } else if (config.version != LATEST_CONFIG_VERSION) {
            throw Exception("Unsupported config version ${config.version}")
        }
        if (config.general.chatId > 0) {
            config = config.copy(
                general = config.general.copy(
                    chatId = -1000000000000 - config.general.chatId
                )
            )
        }
    }

    fun reload() {
        if (configDir.notExists()) {
            configDir.createDirectory()
        }

        val configPath = configDir.resolve("config.yml")
        if (configPath.notExists()) {
            configPath.writeText(yaml.encodeToString(Config(version = LATEST_CONFIG_VERSION)))
        }
        config = yaml.decodeFromString<Config>(configPath.readText())
        migrateConfig(configPath)
        // write new keys & update docs
        configPath.writeText(yaml.encodeToString(config))

        val langPath = configDir.resolve("lang.yml")
        if (langPath.notExists()) {
            langPath.writeText(yaml.encodeToString(Lang(version = LATEST_LANG_VERSION)))
        }
        lang = yaml.decodeFromString<Lang>(langPath.readText())
        migrateLang()
        // write new keys & update docs
        langPath.writeText(yaml.encodeToString(lang))

        val minecraftLangPath = configDir.resolve("minecraft_lang.json")
        if (minecraftLangPath.exists()) {
            minecraftLang = Json.decodeFromString<Map<String, String>>(minecraftLangPath.readText())
        }
    }
}
