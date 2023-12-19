package dev.vanutp.tgbridge.common

import io.github.xn32.json5k.Json5
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Path
import kotlin.io.path.*

@Serializable
data class TBConfig(
    val botToken: String = "your bot token",
    val chatId: Long = 0,
    val threadId: Int? = null,
    val bluemapHost: String? = null,
    val requirePrefixInMinecraft: String? = null,
    val messageMergeWindowSeconds: Int? = null,
) {
    companion object {
        private val json5 = Json5 {
            encodeDefaults = true
            prettyPrint = true
        }

        fun load(configDir: Path): TBConfig {
            if (!configDir.exists()) {
                configDir.createDirectory()
            }
            val configPath = configDir.resolve("config.json5")
            if (!configDir.exists()) {
                configDir.createDirectory()
            }
            if (!configPath.exists()) {
                val defaultConfig = TBConfig()
                configPath.writeText(json5.encodeToString(defaultConfig))
                return defaultConfig
            }
            return json5.decodeFromString<TBConfig>(configPath.readText())
        }
    }
}
