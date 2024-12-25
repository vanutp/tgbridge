package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.ConfigManager
import dev.vanutp.tgbridge.common.asString
import org.bukkit.entity.Player

fun Player.isVanished() = getMetadata("vanished").any { it.asBoolean() }

fun getPlayerName(player: Player) =
    if (ConfigManager.config.messages.useRealUsername) {
        player.name
    } else {
        player.displayName().asString()
    }
