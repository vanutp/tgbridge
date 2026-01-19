package dev.vanutp.tgbridge.common

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import java.nio.file.Path
import kotlin.io.path.createDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@Serializable
data class AuthData(
    val players: MutableMap<String, Long> = mutableMapOf(),
)

class AuthStorage(private val dataDir: Path) {
    private val authFile = dataDir.resolve("tg_auth.yml")
    private var data: AuthData = AuthData()

    init {
        load()
    }

    private fun load() {
        if (!dataDir.exists()) {
            dataDir.createDirectory()
        }
        if (authFile.exists()) {
            val text = authFile.readText()
            data = Yaml.default.decodeFromString(AuthData.serializer(), text)
        } else {
            save()
        }
    }

    private fun save() {
        val text = Yaml.default.encodeToString(AuthData.serializer(), data)
        authFile.writeText(text)
    }

    fun getTelegramId(minecraftUsername: String): Long? {
        return data.players[minecraftUsername.lowercase()]
    }

    fun getMinecraftUsername(telegramId: Long): String? {
        return data.players.entries.find { it.value == telegramId }?.key
    }

    fun linkPlayer(minecraftUsername: String, telegramId: Long) {
        data.players[minecraftUsername.lowercase()] = telegramId
        save()
    }

    fun unlinkPlayer(minecraftUsername: String) {
        data.players.remove(minecraftUsername.lowercase())
        save()
    }
}
