package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.asString
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TgbridgePlayer
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun Player.toTgbridge() = TgbridgePlayer(
    uniqueId,
    name,
    displayName().asString()
)


fun CommandSender.toTgbridge() = TBCommandContext(
    source = server.getPlayer(name)?.toTgbridge(),
    reply = this::sendMessage
)