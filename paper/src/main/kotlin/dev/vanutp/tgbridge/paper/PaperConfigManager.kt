package dev.vanutp.tgbridge.paper

import java.nio.file.Path
import kotlin.io.path.deleteIfExists

object PaperConfigManager {
    private lateinit var configDir: Path

    fun init(configDir: Path) {
        this.configDir = configDir
        reload()
    }

    private fun reload() {
        val configPath = configDir.resolve("config-paper.yml")
        configPath.deleteIfExists()
    }
}
