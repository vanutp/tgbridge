package dev.vanutp.tgbridge.fabric

import net.minecraft.SharedConstants

object VersionInfo {
    val version: String = SharedConstants.getCurrentVersion().id()
    val versionType = version.split("-").let {
        if (it.size > 1) {
            it[1]
        } else {
            "release"
        }
    }
    val snapshotIndex = version.split("-").let {
        if (it.size > 2) {
            it[2]
        } else {
            null
        }
    }
    val baseVersion = version.split("-")[0].split(".").map { it.toInt() }
    val IS_263_PLUS = baseVersion[0] > 26 || baseVersion[0] == 26 && baseVersion[1] >= 3
}
