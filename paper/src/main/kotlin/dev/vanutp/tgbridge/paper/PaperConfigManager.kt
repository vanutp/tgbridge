package dev.vanutp.tgbridge.common

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import dev.vanutp.tgbridge.paper.PaperConfig
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.notExists
import kotlin.io.path.readText
import kotlin.io.path.writeText

object PaperConfigManager {
    private val yaml = Yaml(configuration = YamlConfiguration(strictMode = false))
    private lateinit var configDir: Path
    lateinit var config: PaperConfig
        private set

    fun init(configDir: Path) {
        this.configDir = configDir
        reload()
    }

    private fun reload() {
        if (configDir.notExists()) {
            configDir.createDirectory()
        }
        val configPath = configDir.resolve("config-paper.yml")
        if (configPath.notExists()) {
            configPath.writeText(yaml.encodeToString(PaperConfig()))
        }
        config = yaml.decodeFromString<PaperConfig>(configPath.readText())
        // write new keys & update docs
        configPath.writeText(yaml.encodeToString(config))
    }
}
