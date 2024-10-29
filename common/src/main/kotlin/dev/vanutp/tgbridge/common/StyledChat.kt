package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent


abstract class StyledChat {

    abstract fun registerOnPreMessage(onEvent: (String, Any) -> Unit)

    abstract fun registerMessageEvent(handler: (TBPlayerEventData) -> Unit)

}