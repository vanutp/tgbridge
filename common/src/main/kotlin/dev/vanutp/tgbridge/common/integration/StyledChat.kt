package dev.vanutp.tgbridge.common.integration

import dev.vanutp.tgbridge.common.models.TBPlayerEventData


abstract class StyledChat {

    abstract fun registerOnPreMessage(onEvent: (String, Any) -> Unit)

    abstract fun registerMessageEvent(handler: (TBPlayerEventData) -> Unit)

}