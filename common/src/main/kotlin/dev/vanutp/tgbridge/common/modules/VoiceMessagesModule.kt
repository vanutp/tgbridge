package dev.vanutp.tgbridge.common.modules

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.opus.OpusDecoder
import de.maxhenkel.voicechat.api.opus.OpusEncoder
import dev.vanutp.tgbridge.common.*
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.models.TgbridgeRecipientsEvent
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

        fun transcodeOpus(packets: List<ByteArray>) =
            packets.map {
                encoder.encode(decoder.decode(it))
            }
    }

    override fun initialize(api: VoicechatApi) {
        Companion.api = api
        decoder = api.createDecoder()
        encoder = api.createEncoder()
    }
}

class VoiceMessagesModule(bridge: TelegramBridge) : AbstractModule(bridge) {
    override val fabricId = "voicemessages"
    override val forgeId = "voicemessages"
    override val paperId = "voicemessages"
    private lateinit var voiceMessages: VoiceMessagesApi

    companion object {
        fun voiceMessagesExists() = try {
            Class.forName("ru.dimaskama.voicemessages.api.VoiceMessagesApiInitCallback")
            true
        } catch (_: ClassNotFoundException) {
            false
        }
    }

    init {
        if (voiceMessagesExists()) {
            VoiceMessagesApiInitCallback.EVENT.register {
                voiceMessages = it
            }
        }
    }

    override fun enable() {
        TgbridgeEvents.TG_CHAT_MESSAGE.addListener { e ->
            val msg = e.message
            if (msg.voice?.mimeType != "audio/ogg") return@addListener

            val fileResponse = bridge.bot.downloadFile(msg.voice.fileId)
            val bytes = fileResponse.body()!!.bytes()
            val frames = extractOpusPackets(bytes)
            val transcoded = VoiceChatPlugin.transcodeOpus(frames)

            val recipientsEvt = TgbridgeRecipientsEvent(e.chat, originalEvent = e)
            TgbridgeEvents.RECIPIENTS.invoke(recipientsEvt)

            val senderNameMsg = e.chat.minecraftFormat.formatMiniMessage(
                Placeholders(
                    mapOf("sender" to msg.senderName),
                    mapOf("text" to Component.text("")),
                )
            )
            bridge.platform.broadcastMessage(recipientsEvt.recipients, senderNameMsg)
            val emptyUuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
            voiceMessages.sendVoiceMessage(emptyUuid, recipientsEvt.recipients.map { it.uuid }, transcoded, "all")
            e.isCancelled = true
        }
        VoiceMessageReceivedCallback.EVENT.register { player, message, target ->
            if (target != "all") return@register false
            val oggData = createOgg(message)
            val chat = config.getDefaultChat()
            val tgText = MinecraftToTelegramConverter.convert(
                chat.telegramFormat.formatMiniMessage(
                    Placeholders(
                        mapOf("username" to (bridge.platform.playerToTgbridge(player)?.getName() ?: "???")),
                        mapOf("text" to Component.text(""))
                    )
                )
            )
            bridge.coroutineScope.launch {
                bridge.bot.sendVoice(
                    chat.chatId,
                    oggData,
                    tgText.text,
                    tgText.entities,
                    chat.topicId,
                    null,
                )
            }
            bridge.merger.lastMessages.remove(chat.name)
            return@register false
        }
    }
}
