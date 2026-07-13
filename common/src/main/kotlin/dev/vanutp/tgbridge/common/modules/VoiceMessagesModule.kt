package dev.vanutp.tgbridge.common.modules

import de.maxhenkel.voicechat.api.ForgeVoicechatPlugin
import de.maxhenkel.voicechat.api.VoicechatApi
import de.maxhenkel.voicechat.api.VoicechatPlugin
import de.maxhenkel.voicechat.api.opus.OpusDecoder
import de.maxhenkel.voicechat.api.opus.OpusEncoder
import dev.vanutp.tgbridge.common.*
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.converters.TelegramFormattedText
import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.TgbridgeRecipientsEvent
import kotlinx.coroutines.launch
import net.kyori.adventure.text.Component
//import ru.dimaskama.voicemessages.api.VoiceMessageReceivedCallback
//import ru.dimaskama.voicemessages.api.VoiceMessagesApi
//import ru.dimaskama.voicemessages.api.VoiceMessagesApiInitCallback
import java.time.Clock
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

class MessageContentVoice(
    val oggData: ByteArray,
    val text: TelegramFormattedText,
) : MessageContent() {
    override suspend fun send(chat: ChatConfig, lastMessage: TgbridgeTgMessage?): TgbridgeTgMessage {
        val tgMessage = TelegramBridge.INSTANCE.bot.sendVoice(
            chat.chatId,
            oggData,
            text.text,
            text.entities,
            chat.topicId,
            null,
        )
        return TgbridgeTgMessage(
            chat, tgMessage.messageId, Clock.systemUTC().instant(), this
        )
    }
}

class VoiceMessagesModule(bridge: TelegramBridge) : AbstractModule(bridge) {
    override val fabricId = "voicemessages"
    override val forgeId = "voicemessages"
    override val paperId = "voicemessages"

    companion object {
        fun voiceMessagesExists() = false
    }

    override fun shouldEnable() = false

    override fun enable() {
    }
}
