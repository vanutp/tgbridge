package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.asString
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import org.bukkit.entity.Player

fun Player.toTgbridge() = TgbridgePlayer(
    uniqueId,
    name,
    displayName().asString()
)
