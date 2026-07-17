package dev.vanutp.tgbridge.paper.modules

import de.maxhenkel.voicechat.api.BukkitVoicechatService
import dev.vanutp.tgbridge.common.modules.VoiceChatPlugin
import dev.vanutp.tgbridge.common.modules.VoiceMessagesModule
import dev.vanutp.tgbridge.paper.PaperTelegramBridge

class VoiceMessagesPaperModule(override val bridge: PaperTelegramBridge) : AbstractPaperModule(bridge) {
    override val paperId = "voicemessages"

    override fun shouldEnable() = VoiceMessagesModule.voiceMessagesExists()

    override fun onPluginEnable() {
        val service = bridge.plugin.server.servicesManager.load(BukkitVoicechatService::class.java)
        service!!.registerPlugin(VoiceChatPlugin())
    }
}
