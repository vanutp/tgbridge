package dev.vanutp.tgbridge.common.modules

import dev.vanutp.tgbridge.common.models.ChatConfig
import dev.vanutp.tgbridge.common.models.ITgbridgePlayer

interface IChatModule {
    /**
     * Get a list of players that should receive messages
     * in the specified chat.
     *
     * @param chat
     * @return A list of ITgbridgePlayer objects, or null if the chat was not found
     */
    fun getChatRecipients(chat: ChatConfig): List<ITgbridgePlayer>?
}
