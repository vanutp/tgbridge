package dev.vanutp.tgbridge.common.modules

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.asString
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import net.flectone.pulse.FlectonePulseAPI
import net.flectone.pulse.annotation.Pulse
import net.flectone.pulse.listener.PulseListener
import net.flectone.pulse.model.entity.FEntity
import net.flectone.pulse.model.event.lifecycle.ReloadEvent
import net.flectone.pulse.model.event.message.MessagePrepareEvent
import net.flectone.pulse.module.message.chat.model.ChatMetadata
import net.flectone.pulse.platform.registry.ListenerRegistry
import net.flectone.pulse.service.FPlayerService
import net.flectone.pulse.util.constant.MessageType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.util.*

private class FlectonePulsePlayer(
    override val uuid: UUID,
    override val username: String,
    override val displayName: String?,
) : ITgbridgePlayer {
    override val nativePlayer = null

    companion object {
        private val fpulseComponentCls = try {
            Class.forName("net.flectone.pulse.library.adventure.text.Component")
        } catch (_: ClassNotFoundException) {
            null
        }
        private val fSerializerCls = try {
            Class.forName("net.flectone.pulse.library.adventure.text.serializer.gson.GsonComponentSerializer")
        } catch (_: ClassNotFoundException) {
            null
        }
        private val fSerializer = fSerializerCls?.getMethod("gson")?.invoke(null)
        private val fSerialize = fSerializerCls?.getMethod("serialize", fpulseComponentCls)
        private val fDeserialize = fSerializerCls?.getMethod("deserialize", Object::class.java)
        private val fEntity = Class.forName("net.flectone.pulse.model.entity.FEntity")
        private val fEntityGetShowEntityName = fEntity.getMethod("getShowEntityName")
        private val usingNativeAdventure = fpulseComponentCls == null && fSerializerCls == null

        fun adventureToFPulse(adventure: Component): Any =
            adventure
                .takeIf { usingNativeAdventure }
                ?: fDeserialize!!.invoke(fSerializer, GsonComponentSerializer.gson().serialize(adventure))

        fun fpulseToAdventure(fpulse: Any): Component =
            if (usingNativeAdventure) {
                fpulse as Component
            } else {
                GsonComponentSerializer.gson()
                    .deserialize(
                        fSerialize!!.invoke(fSerializer, fpulse) as String
                    )
            }

        fun fromFEntity(fPlayer: FEntity): FlectonePulsePlayer {
            return FlectonePulsePlayer(
                fPlayer.uuid,
                fPlayer.name,
                fEntityGetShowEntityName.invoke(fPlayer)
                    ?.let { fpulseToAdventure(it) }
                    ?.asString(),
            )
        }
    }
}

class FlectonePulseListener(private val bridge: TelegramBridge) : PulseListener {
    private val flectonePulse = FlectonePulseAPI.getInstance()
    private val fListenerRegistry = flectonePulse.get(ListenerRegistry::class.java)

    init {
        fListenerRegistry.register(this)
    }

    @Pulse
    fun onMessagePrepare(e: MessagePrepareEvent) {
        val meta = e.eventMetadata
        if (e.messageType != MessageType.CHAT || meta !is ChatMetadata) {
            return
        }

        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                FlectonePulsePlayer.fromFEntity(meta.sender),
                Component.text(meta.message ?: return),
                meta.chatName,
                e,
            )
        )
    }

    @Pulse
    fun onReload(e: ReloadEvent) {
        fListenerRegistry.register(this)
    }
}

class FlectonePulseModule(bridge: TelegramBridge) : AbstractModule(bridge), IChatModule {
    override val fabricId = "flectonepulse"
    override val paperId = "FlectonePulse"

    private lateinit var fPlayerService: FPlayerService

    override fun enable() {
        fPlayerService = FlectonePulseAPI.getInstance().get(FPlayerService::class.java)
        FlectonePulseListener(bridge)
    }

    override fun getChatRecipients(chat: ChatConfig): List<ITgbridgePlayer> =
        fPlayerService
            .onlineFPlayers
            .filter { it.isSetting(MessageType.FROM_TELEGRAM_TO_MINECRAFT) }
            .map { FlectonePulsePlayer.fromFEntity(it) }
}
