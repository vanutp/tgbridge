package dev.vanutp.tgbridge.common.modules

import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.asString
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer
import dev.vanutp.tgbridge.common.models.TgbridgeMcChatMessageEvent
import net.draycia.carbon.api.CarbonChatProvider
import net.draycia.carbon.api.channels.ChannelRegistry
import net.draycia.carbon.api.channels.ChatChannel
import net.draycia.carbon.api.event.events.CarbonChatEvent
import net.draycia.carbon.api.users.CarbonPlayer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import java.util.*

private object CarbonHelpers {
    private val NET = "net."
    private val componentCls = Class.forName(NET + "kyori.adventure.text.Component")
    private val serializerCls = Class.forName(NET + "kyori.adventure.text.serializer.gson.GsonComponentSerializer")
    private val serializer = serializerCls.getMethod("gson").invoke(null)
    private val serialize = serializerCls.getMethod("serialize", componentCls)
    private val deserialize = serializerCls.getMethod("deserialize", Object::class.java)
    private val usingNativeAdventure = componentCls == Component::class.java

    val listContains = List::class.java.getMethod("contains", Object::class.java)
    val channelGetKey = ChatChannel::class.java.getMethod("key")
    val keyCls = Class.forName(NET + "kyori.adventure.key.Key")
    val createKey = keyCls.getMethod("key", String::class.java)
    val keyAsAsting = keyCls.getMethod("asString")
    val eventGetMessage = CarbonChatEvent::class.java.getMethod("message")
    val registryGetChannel = ChannelRegistry::class.java.getMethod("channel", keyCls)

    fun adventureToNative(adventure: Component): Any =
        adventure
            .takeIf { usingNativeAdventure }
            ?: deserialize.invoke(serializer, GsonComponentSerializer.gson().serialize(adventure))

    fun nativeToAdventure(native: Any): Component =
        if (usingNativeAdventure) {
            native as Component
        } else {
            GsonComponentSerializer.gson()
                .deserialize(
                    serialize.invoke(serializer, native) as String
                )
        }
}

private class CarbonTgbridgePlayer(
    override val uuid: UUID,
    override val username: String,
    override val displayName: String?,
) : ITgbridgePlayer {
    override val nativePlayer = null

    companion object {
        private val carbonPlayerCls = CarbonPlayer::class.java
        private val carbonPlayerGetDisplayName = carbonPlayerCls.getMethod("displayName")
        fun fromCarbon(carbonPlayer: CarbonPlayer) =
            CarbonTgbridgePlayer(
                carbonPlayer.uuid(),
                carbonPlayer.username(),
                carbonPlayerGetDisplayName.invoke(carbonPlayer)
                    .let { CarbonHelpers.nativeToAdventure(it) }
                    .asString(),
            )
    }
}

class CarbonChatModule(bridge: TelegramBridge) : AbstractModule(bridge), IChatModule {
    override val fabricId = "carbonchat"
    override val paperId = "CarbonChat"

    fun onCarbonChatMessage(e: CarbonChatEvent) {
        val channel = e.chatChannel()
            .let { CarbonHelpers.channelGetKey(it) }
            .let { CarbonHelpers.keyAsAsting(it) as String }
            .removePrefix("carbon:")
        if (e.cancelled()) {
            return
        }
        val message = CarbonHelpers.eventGetMessage(e)
            .let { CarbonHelpers.nativeToAdventure(it) }
        bridge.onChatMessage(
            TgbridgeMcChatMessageEvent(
                CarbonTgbridgePlayer.fromCarbon(e.sender()),
                message,
                channel,
                e,
            )
        )
    }

    override fun getChatRecipients(chat: ChatConfig): List<ITgbridgePlayer>? {
        val cc = CarbonChatProvider.carbonChat()
        val channelKey = CarbonHelpers.createKey.invoke(
            null,
            if (chat.name.contains(":")) {
                chat.name
            } else {
                "carbon:${chat.name}"
            }
        )

        val channel = CarbonHelpers
            .registryGetChannel(cc.channelRegistry(), channelKey) as? ChatChannel
            ?: return null
        return cc.server().players()
            .asSequence()
            .filter { channel.permissions().hearingPermitted(it).permitted() }
            .filterNot { CarbonHelpers.listContains(it.leftChannels(), channelKey) as Boolean }
            .map(CarbonTgbridgePlayer::fromCarbon)
            .toList()
    }

    override fun enable() {
        CarbonChatProvider.carbonChat()
            .eventHandler()
            .subscribe(CarbonChatEvent::class.java, this::onCarbonChatMessage)
    }
}
