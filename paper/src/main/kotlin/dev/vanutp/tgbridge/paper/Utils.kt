package dev.vanutp.tgbridge.paper

import org.bukkit.entity.Player

fun Player.isVanished() = getMetadata("vanished").any { it.asBoolean() }
