package dev.vanutp.tgbridge.common.compat

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.opus.OpusDecoder
import de.maxhenkel.voicechat.api.opus.OpusEncoder
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.Placeholders
import dev.vanutp.tgbridge.common.TelegramBridge
import dev.vanutp.tgbridge.common.TgbridgeEvents
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.formatMiniMessage
import dev.vanutp.tgbridge.common.getDefaultChat
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

class VoiceMessagesCompat(bridge: TelegramBridge) : AbstractCompat(bridge) {
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

            val emptyUuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
            val targetUuids = bridge.platform.getChatRecipients(e.chat)?.map { it.uuid } ?: emptyList()
            val fmtString = if (e.chat.isDefault) lang.minecraft.format else lang.minecraft.formatChat
            val senderNameMsg = fmtString.formatMiniMessage(
                Placeholders(
                    mapOf("sender" to msg.senderName, "chat_name" to e.chat.name),
                    mapOf("text" to Component.text("")),
                )
            )
            bridge.platform.broadcastMessage(e.chat, senderNameMsg)
            voiceMessages.sendVoiceMessage(emptyUuid, targetUuids, transcoded, "all")
            e.isCancelled = true
        }
        VoiceMessageReceivedCallback.EVENT.register { player, message, target ->
            if (target != "all") return@register false
            val oggData = createOgg(message)
            val tgText = MinecraftToTelegramConverter.convert(
                lang.telegram.chatMessage.formatMiniMessage(
                    Placeholders(
                        mapOf("username" to (bridge.platform.playerToTgbridge(player)?.getName() ?: "???")),
                    )
                )
            )
            val chat = config.getDefaultChat()
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
