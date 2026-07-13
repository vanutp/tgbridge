package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.file.Path
import kotlin.io.path.*

@Serializable
data class AuthPlayer(
    val username: String, // lowercase
    var tgId: Long? = null,
    var tgUsername: String? = null,
    val allowedIps: MutableSet<String> = mutableSetOf(),
    var currentCode: String? = null,
    var pendingIpToConfirm: String? = null,
    var pendingIpMessageId: Int? = null,
)

@Serializable
data class AuthDatabase(
    val players: MutableMap<String, AuthPlayer> = mutableMapOf()
)

object AuthManager {
    private val json = Json { prettyPrint = true; ignoreUnknownKeys = true }
    private lateinit var dbPath: Path
    lateinit var db: AuthDatabase
        private set

    fun init(configDir: Path) {
        dbPath = configDir.resolve("auth_db.json")
        db = if (dbPath.exists()) {
            try {
                json.decodeFromString<AuthDatabase>(dbPath.readText())
            } catch (e: Exception) {
                TelegramBridge.INSTANCE.logger.error("Failed to load auth_db.json, recreating", e)
                AuthDatabase()
            }
        } else {
            AuthDatabase()
        }
        save()
    }

    fun save() {
        try {
            dbPath.writeText(json.encodeToString(AuthDatabase.serializer(), db))
        } catch (e: Exception) {
            TelegramBridge.INSTANCE.logger.error("Failed to save auth_db.json", e)
        }
    }

    fun getOrCreatePlayer(username: String): AuthPlayer {
        val lower = username.lowercase()
        return db.players.getOrPut(lower) {
            AuthPlayer(username = lower)
        }
    }

    fun generateCode(player: AuthPlayer): String {
        val code = (100000..999999).random().toString()
        player.currentCode = code
        save()
        return code
    }
}
