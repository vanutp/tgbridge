package dev.vanutp.tgbridge.paper.compat

import de.maxhenkel.voicechat.api.BukkitVoicechatService
import dev.vanutp.tgbridge.common.compat.VoiceChatPlugin
import dev.vanutp.tgbridge.common.compat.VoiceMessagesCompat
import dev.vanutp.tgbridge.paper.PaperTelegramBridge

class VoiceMessagesPaperCompat(override val bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge) {
    override val paperId = "voicemessages"

    override fun shouldEnable() = VoiceMessagesCompat.voiceMessagesExists()

    override fun onPluginEnable() {
        val service = bridge.plugin.server.servicesManager.load(BukkitVoicechatService::class.java)
        service!!.registerPlugin(VoiceChatPlugin())
    }
}
