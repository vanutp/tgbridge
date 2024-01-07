package dev.vanutp.tgbridge.common

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.charleskorn.kaml.yamlMap
import dev.vanutp.tgbridge.common.models.Config
import dev.vanutp.tgbridge.common.models.Lang
import io.github.xn32.json5k.Json5
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.FileNotFoundException
import java.nio.file.Path
import kotlin.io.path.*

@Serializable
private data class OldConfig(
    val botToken: String = "your bot token",
    val chatId: Long = 0,
    val threadId: Int? = null,
    val bluemapHost: String? = null,
    val requirePrefixInMinecraft: String? = null,
    val messageMergeWindowSeconds: Int? = null,
)

class DefaultConfigUnchangedException : Exception("botToken or chatId is not set")

object ConfigManager {
    private val yaml = Yaml(configuration = YamlConfiguration())
    private lateinit var configDir: Path
    lateinit var config: Config
        private set
    lateinit var lang: Lang
        private set
    lateinit var minecraftLang: Map<String, String>
        private set

    fun init(configDir: Path) {
        this.configDir = configDir
        reload()
    }

    fun reload() {
        if (configDir.notExists()) {
            configDir.createDirectory()
        }

        migrateConfig(configDir)
        migrateMinecraftLang(configDir)

        val configPath = configDir.resolve("config.yml")
        if (configPath.notExists()) {
            configPath.writeText(yaml.encodeToString(Config()))
        }
        val loadedConfig = yaml.decodeFromString<Config>(configPath.readText())
        if (loadedConfig.botToken == Config().botToken || loadedConfig.chatId == Config().chatId) {
            throw DefaultConfigUnchangedException()
        }
        config = loadedConfig
        // write new keys & update docs
        configPath.writeText(yaml.encodeToString(config))

        val langPath = configDir.resolve("lang.yml")
        if (langPath.notExists()) {
            langPath.writeText(yaml.encodeToString(Lang()))
        }
        lang = yaml.decodeFromString<Lang>(langPath.readText())
        // write new keys & update docs
        langPath.writeText(yaml.encodeToString(lang))

        val minecraftLangPath = configDir.resolve("minecraft_lang.json")
        if (minecraftLangPath.notExists()) {
            throw FileNotFoundException("minecraft_lang.json not found")
        }
        minecraftLang = Json.decodeFromString<Map<String, String>>(minecraftLangPath.readText())

    }

    private fun migrateConfig(configDir: Path) {
        val oldConfigPath = configDir.resolve("config.json5")
        val newConfigPath = configDir.resolve("config.yml")
        if (newConfigPath.exists() || oldConfigPath.notExists()) {
            return
        }
        val json5 = Json5 {}
        val oldConfig = json5.decodeFromString<OldConfig>(oldConfigPath.readText())
        val newConfig = Config(
            botToken = oldConfig.botToken,
            chatId = oldConfig.chatId,
            topicId = oldConfig.threadId,
            bluemapUrl = oldConfig.bluemapHost,
            requirePrefixInMinecraft = oldConfig.requirePrefixInMinecraft ?: "",
            messageMergeWindow = oldConfig.messageMergeWindowSeconds,
            leaveJoinMergeWindow = oldConfig.messageMergeWindowSeconds,
        )
        newConfigPath.writeText(yaml.encodeToString(newConfig))
        oldConfigPath.deleteIfExists()
    }

    private fun migrateMinecraftLang(configDir: Path) {
        val oldLangPath = configDir.resolve("lang.json")
        val newLangPath = configDir.resolve("minecraft_lang.json")
        if (newLangPath.exists() || oldLangPath.notExists()) {
            return
        }
        oldLangPath.moveTo(newLangPath)
    }
}
