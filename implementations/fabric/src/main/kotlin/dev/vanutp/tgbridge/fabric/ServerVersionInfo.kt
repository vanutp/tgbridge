package dev.vanutp.tgbridge.fabric

import net.minecraft.server.MinecraftServer

class ServerVersionInfo(server: MinecraftServer) {
    val IS_192 = arrayOf("1.19", "1.19.1", "1.19.2").contains(server.version)
    val IS_19 = arrayOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4").contains(server.version)
    val IS_19_204 = arrayOf(
        "1.19",
        "1.19.1",
        "1.19.2",
        "1.19.3",
        "1.19.4",
        "1.20",
        "1.20.1",
        "1.20.2",
        "1.20.3",
        "1.20.4",
    ).contains(server.version)
    val IS_205_215 = arrayOf(
        "1.20.5",
        "1.20.6",
        "1.21",
        "1.21.1",
        "1.21.2",
        "1.21.3",
        "1.21.4",
        "1.21.5",
    ).contains(server.version)
}
