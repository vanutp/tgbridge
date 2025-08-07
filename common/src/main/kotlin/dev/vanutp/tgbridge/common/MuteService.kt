package dev.vanutp.tgbridge.common

import com.google.gson.Gson
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object MuteService {
    private val gson = Gson()
    private val mutedUsers = ConcurrentHashMap.newKeySet<UUID>()
    private val executor: ExecutorService = Executors.newSingleThreadExecutor()

    private lateinit var filePath: Path

    /**
     * Mute user
     */
    fun mute(uuid: UUID) {
        mutedUsers.add(uuid)
        saveAsync()
    }

    /**
     * Unmute user
     */
    fun unmute(uuid: UUID) {
        mutedUsers.remove(uuid)
        saveAsync()
    }

    /**
     * Check if a user is muted
     */
    fun isMuted(uuid: UUID): Boolean = mutedUsers.contains(uuid)

    /**
     * Initialize storage
     */
    fun init(path: Path) {
        filePath = path.resolve("muted_users.json")
        if (!Files.exists(filePath)) return

        try {
            Files.newBufferedReader(filePath).use { reader ->
                val loaded = gson.fromJson(reader, Array<UUID>::class.java) ?: return
                mutedUsers.clear()
                mutedUsers.addAll(loaded)
            }
        } catch (e: Exception) {
            println("Failed to load muted users: ${e.message}")
        }
    }

    /**
     * Async file saver
     */
    private fun saveAsync() {
        val snapshot = mutedUsers.toSet()

        executor.submit {
            try {
                Files.createDirectories(filePath.parent)
                Files.newBufferedWriter(filePath).use { writer ->
                    gson.toJson(snapshot, writer)
                }
            } catch (e: Exception) {
                println("Failed to save muted users: ${e.message}")
            }
        }
    }

    /**
     * Shutdown muted users storage
     */
    fun shutdown() {
        executor.shutdown()
    }
}
