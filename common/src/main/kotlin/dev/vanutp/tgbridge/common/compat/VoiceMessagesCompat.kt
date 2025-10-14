package dev.vanutp.tgbridge.common.compat

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.opus.OpusDecoder
import de.maxhenkel.voicechat.api.opus.OpusEncoder
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.formatMiniMessage
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
import ru.dimaskama.voicemessages.api.VoiceMessageReceivedCallback
import ru.dimaskama.voicemessages.api.VoiceMessagesApi
import ru.dimaskama.voicemessages.api.VoiceMessagesApiInitCallback
import java.util.*


@ForgeVoicechatPlugin
class VoiceChatPlugin : VoicechatPlugin {
    override fun getPluginId() = "tgbridge"

    companion object {
        internal lateinit var api: VoicechatApi private set
        internal lateinit var decoder: OpusDecoder
        internal lateinit var encoder: OpusEncoder
    }

    override fun initialize(api: VoicechatApi) {
        Companion.api = api
        decoder = api.createDecoder()
        encoder = api.createEncoder()
    }
}

private fun voiceMessagesExists() = try {
    Class.forName("ru.dimaskama.voicemessages.api.VoiceMessagesApiInitCallback")
    true
} catch (_: ClassNotFoundException) {
    false
}

class VoiceMessagesCompat(bridge: TelegramBridge) : AbstractCompat(bridge) {
    override val fabricId = "voicemessages"
    override val forgeId = "voicemessages"
    override val paperId = "voicemessages"
    private lateinit var voiceMessages: VoiceMessagesApi

    private fun transcodeOpus(packets: List<ByteArray>) =
        packets.map {
            VoiceChatPlugin.encoder.encode(VoiceChatPlugin.decoder.decode(it))
        }

    init {
        if (voiceMessagesExists()) {
            VoiceMessagesApiInitCallback.EVENT.register {
                voiceMessages = it
            }
        }
    }

    override fun enable() {
        TgbridgeEvents.TG_CHAT_MESSAGE.addListener { msg ->
            if (msg.voice?.mimeType != "audio/ogg") return@addListener

            val fileResponse = bridge.bot.downloadFile(msg.voice.fileId)
            val bytes = fileResponse.body()!!.bytes()
            val frames = extractOpusPackets(bytes)
            val transcoded = transcodeOpus(frames)

            val emptyUuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
            val targetUuids = bridge.platform.getOnlinePlayers().map { it.uuid }
            val senderNameMsg = lang.minecraft.format.formatMiniMessage(
                listOf("sender" to msg.senderName),
                listOf("text" to Component.text(""))
            )
            bridge.platform.broadcastMessage(senderNameMsg)
            voiceMessages.sendVoiceMessage(emptyUuid, targetUuids, transcoded, "all")
            e.isCancelled = true
        }
        VoiceMessageReceivedCallback.EVENT.register { player, message, target ->
            if (target != "all") return@register false
            val oggData = createOgg(message)
            val tgText = MinecraftToTelegramConverter.convert(
                lang.telegram.chatMessage.formatMiniMessage(
                    listOf("username" to (bridge.platform.playerToTgbridge(player)?.getName() ?: "???"))
                )
            )
            bridge.coroutineScope.launch {
                bridge.bot.sendVoice(
                    config.general.chatId,
                    oggData,
                    tgText.text,
                    tgText.entities,
                    config.general.topicId,
                    null,
                )
            }
            bridge.lastMessage = null
            return@register false
        }
    }
}
