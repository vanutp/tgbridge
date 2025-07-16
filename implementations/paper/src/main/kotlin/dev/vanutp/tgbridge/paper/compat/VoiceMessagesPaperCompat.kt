package dev.vanutp.tgbridge.paper.compat

import de.maxhenkel.voicechat.api.BukkitVoicechatService
import dev.vanutp.tgbridge.common.compat.AbstractCompat
import dev.vanutp.tgbridge.common.compat.VoiceChatPlugin
import dev.vanutp.tgbridge.paper.PaperTelegramBridge

class VoiceMessagesPaperCompat(override val bridge: PaperTelegramBridge) : AbstractCompat(bridge) {
    override val paperId = "voicemessages"
    override fun enable() {
        val service = bridge.plugin.server.servicesManager.load(BukkitVoicechatService::class.java)
        service!!.registerPlugin(VoiceChatPlugin())
    }
}
