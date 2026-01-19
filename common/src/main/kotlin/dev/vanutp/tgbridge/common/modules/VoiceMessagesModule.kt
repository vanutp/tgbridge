package dev.vanutp.tgbridge.common.modules

//import dev.vanutp.tgbridge.common.IPlatform
//import dev.vanutp.tgbridge.common.TgbridgeEvents
//import dev.vanutp.tgbridge.common.models.TgMessageMedia
//import dev.vanutp.tgbridge.common.models.TgbridgeTgVoiceMessageEvent
//import io.ktor.client.*
//import io.ktor.client.engine.cio.*
//import io.ktor.client.request.*
//import io.ktor.client.statement.*
//import kotlinx.coroutines.launch
//import ru.dimaskama.voicemessages.network.packets.client.VoiceMessagePacket
//import ru.dimaskama.voicemessages.network.packets.server.SendVoiceMessagePacket
//import ru.dimaskama.voicemessages.network.packets.server.VoiceMessageReceivedPacket
//
//class VoiceMessagesModule(bridge: dev.vanutp.tgbridge.common.TelegramBridge) :
//    dev.vanutp.tgbridge.common.modules.AbstractTgbridgeModule(bridge) {
//    override fun shouldEnable(): Boolean {
//        return platform.isModLoadedMulti("voicemessages", "voicemessages", "voicemessages")
//    }
//
//    override fun enable() {
//        TgbridgeEvents.TG_VOICE_MESSAGE.addListener(this::onTelegramVoiceMessage)
//    }
//
//    private fun onTelegramVoiceMessage(e: TgbridgeTgVoiceMessageEvent) = bridge.coroutineScope.launch {
//        val httpClient = HttpClient(CIO)
//        val response = httpClient.get(e.url)
//        val bytes = response.bodyAsChannel().toByteArray()
//        httpClient.close()
//        val packet = SendVoiceMessagePacket(bytes, e.chat.isDefault, e.message.messageId)
//        platform.getOnlinePlayers().forEach {
//            it.toNative()?.connection?.send(packet)
//        }
//    }
//}
