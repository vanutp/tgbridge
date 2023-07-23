package dev.vanutp.tgbridge.common

import com.google.gson.GsonBuilder
import java.nio.file.Path
import kotlin.io.path.*

data class TBConfig(
    val botToken: String = "your bot token",
    val chatId: Long = 0,
    val bluemapHost: String = "",
) {
    companion object {
        private val gson = GsonBuilder().setPrettyPrinting().create()
        fun load(path: Path): TBConfig {
            if (!path.parent.exists()) {
                path.parent.createDirectory()
            }
            if (!path.exists()) {
                val defaultConfig = TBConfig()
                path.writeText(gson.toJson(defaultConfig))
                return defaultConfig
            }
            return gson.fromJson(path.readText(), TBConfig::class.java)
        }
    }

    fun save(path: Path) {
        path.writeText(gson.toJson(this))
    }
}
