package dev.vanutp.tgbridge.fabric

import net.minecraft.SharedConstants

object VersionInfo {
    val version: String = run {
        val worldVersion = SharedConstants.getCurrentVersion()
        val getters = listOf(
            { worldVersion.id() },
            { worldVersion.javaClass.getMethod("method_48018").invoke(worldVersion) as String },
            { worldVersion.javaClass.getMethod("getId").invoke(worldVersion) as String }
        )
        for (getter in getters) {
            try {
                return@run getter()
            } catch (_: NoSuchMethodError) {
            } catch (_: NoSuchMethodException) {
            }
        }
        throw IllegalStateException("Could not determine Minecraft version")
    }
    val IS_192 = arrayOf("1.19", "1.19.1", "1.19.2").contains(version)
    val IS_19 = arrayOf("1.19", "1.19.1", "1.19.2", "1.19.3", "1.19.4").contains(version)
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
    ).contains(version)
    val IS_205_215 = arrayOf(
        "1.20.5",
        "1.20.6",
        "1.21",
        "1.21.1",
        "1.21.2",
        "1.21.3",
        "1.21.4",
        "1.21.5",
    ).contains(version)
    val IS_216_211 = arrayOf(
        "1.21.6",
        "1.21.7",
        "1.21.8",
        "1.21.9",
        "1.21.10",
        "1.21.11",
    ).contains(version)
    val IS_2111 = arrayOf(
        "1.21.11",
    ).contains(version)
}
