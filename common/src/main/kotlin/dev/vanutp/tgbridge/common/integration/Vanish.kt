package dev.vanutp.tgbridge.common.integration

import dev.vanutp.tgbridge.common.models.TBPlayerEventData


abstract class Vanish {

    abstract fun registerOnJoinMessage(handler: (TBPlayerEventData) -> Unit)
    abstract fun registerOnLeaveMessage(handler: (TBPlayerEventData) -> Unit)
    abstract fun isVanished(player: Any): Boolean

}