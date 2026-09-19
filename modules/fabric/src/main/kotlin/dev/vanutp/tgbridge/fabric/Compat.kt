package dev.vanutp.tgbridge.fabric

import net.minecraft.advancements.AdvancementType
import net.minecraft.advancements.DisplayInfo
import net.minecraft.network.chat.Component


private val oldShouldAnnounceChat = if (!VersionInfo.IS_263_PLUS) {
    DisplayInfo::class.java.getMethod("shouldAnnounceChat")
} else {
    null
}

fun DisplayInfo.tgbridgeAnnounceToChat(): Boolean =
    if (oldShouldAnnounceChat == null) {
        announceToChat()
    } else {
        oldShouldAnnounceChat.invoke(this) as Boolean
    }

private val oldType = if (!VersionInfo.IS_263_PLUS) {
    DisplayInfo::class.java.getMethod("getType")
} else {
    null
}

fun DisplayInfo.tgbridgeType(): AdvancementType? =
    if (oldType == null) {
        type()
    } else {
        oldType.invoke(this) as AdvancementType
    }

private val oldTitle = if (!VersionInfo.IS_263_PLUS) {
    DisplayInfo::class.java.getMethod("getTitle")
} else {
    null
}

fun DisplayInfo.tgbridgeTitle(): Component =
    if (oldTitle == null) {
        title()
    } else {
        oldTitle.invoke(this) as Component
    }

private val oldDescription = if (!VersionInfo.IS_263_PLUS) {
    DisplayInfo::class.java.getMethod("getDescription")
} else {
    null
}

fun DisplayInfo.tgbridgeDescription(): Component =
    if (oldDescription == null) {
        title()
    } else {
        oldDescription.invoke(this) as Component
    }
