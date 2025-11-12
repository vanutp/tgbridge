package dev.vanutp.tgbridge.paper.compat

import de.maxhenkel.voicechat.api.BukkitVoicechatService
import dev.vanutp.tgbridge.common.compat.AbstractCompat
import dev.vanutp.tgbridge.common.compat.VoiceChatPlugin
import dev.vanutp.tgbridge.common.compat.VoiceMessagesCompat
import dev.vanutp.tgbridge.paper.PaperTelegramBridge

class VoiceMessagesPaperCompat(override val bridge: PaperTelegramBridge) : AbstractCompat(bridge) {
    override val paperId = "voicemessages"

    init {
        // Registration must be done in onEnable
        if (VoiceMessagesCompat.voiceMessagesExists()) {
            val service = bridge.plugin.server.servicesManager.load(BukkitVoicechatService::class.java)
            service!!.registerPlugin(VoiceChatPlugin())
        }
    }

    override fun enable() {}
}
