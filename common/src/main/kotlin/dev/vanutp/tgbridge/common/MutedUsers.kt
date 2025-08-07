package dev.vanutp.tgbridge.common

import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 *
 * @author sibmaks
 */
object MutedUsers {
    private val mutedUsers = ConcurrentHashMap.newKeySet<UUID>()

    fun mute(uuid: UUID) {
        mutedUsers.add(uuid)
    }

    fun unmute(uuid: UUID) {
        mutedUsers.remove(uuid)
    }

    fun isMuted(uuid: UUID) = mutedUsers.contains(uuid)
}