package dev.vanutp.tgbridge.fabric.integration

import dev.vanutp.tgbridge.common.integration.StyledChat
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import dev.vanutp.tgbridge.fabric.FabricPlatform
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.styledchat.StyledChatEvents
import net.kyori.adventure.text.Component


object FabricStyledChat: StyledChat() {

    override fun registerOnPreMessage(onEvent: (String, Any) -> Unit) {
        val onStyledChatEvent = StyledChatEvents.PreMessageEvent { msg, ctx ->
            onEvent(msg, ctx)
            msg
        }
        StyledChatEvents.PRE_MESSAGE_CONTENT.register(onStyledChatEvent)
    }

    override fun registerMessageEvent(handler: (TBPlayerEventData) -> Unit) {
        registerOnPreMessage { message, context ->
            context as PlaceholderContext
            handler.invoke(
                if (FabricPlatform.instance?.vanishInstance == null || FabricPlatform.instance?.vanishInstance?.isVanished(context.source()?.player ?: return@registerOnPreMessage) == false)
                    TBPlayerEventData(
                        context.source().displayName?.string ?: return@registerOnPreMessage,
                        Component.text(message),
                    )
                else return@registerOnPreMessage
            )
        }
    }

}