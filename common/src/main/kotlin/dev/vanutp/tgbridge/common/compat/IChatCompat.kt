package dev.vanutp.tgbridge.common.compat

import dev.vanutp.tgbridge.common.models.ChatConfig

interface IChatCompat {
    /**
     * Get a list of players that should receive messages
     * in the specified chat.
     *
     * @param chatName
     * @return A list of platform player objects, or null if the chat was not found
     */
    fun getChatRecipients(chat: ChatConfig): List<Any>?

    fun <T> getChatRecipients(chat: ChatConfig, cls: Class<T>): List<T>? = getChatRecipients(chat)
        ?.map {
            if (cls.isInstance(it)) {
                cls.cast(it)!!
            } else {
                throw IllegalArgumentException(
                    "${this::class.qualifiedName}::getChatRecipients returned an object of type " +
                        "${it::class.qualifiedName}, expected ${cls.name}"
                )
            }
        }
}
