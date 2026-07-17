package dev.vanutp.tgbridge.paper

import dev.vanutp.tgbridge.common.models.TBCommandContext
import org.bukkit.command.CommandSender


fun CommandSender.toTgbridge() = TBCommandContext(
    source = server.getPlayer(name)?.toTgbridge(),
    reply = this::sendMessage
)
