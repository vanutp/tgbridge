package dev.vanutp.tgbridge.paper.compat

import dev.vanutp.tgbridge.common.*
import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.paper.PaperTelegramBridge
import github.scarsz.discordsrv.DiscordSRV
import github.scarsz.discordsrv.api.Subscribe
import github.scarsz.discordsrv.api.events.DiscordGuildMessagePostProcessEvent
import github.scarsz.discordsrv.util.DiscordUtil
import github.scarsz.discordsrv.util.MessageUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

class DiscordSRVCompat(bridge: PaperTelegramBridge) : AbstractPaperCompat(bridge) {
    override val paperId = "DiscordSRV"

    companion object {
        fun adventureToDsrv(adventure: Component) =
            github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer
                .gson()
                .deserialize(
                    GsonComponentSerializer.gson().serialize(adventure)
                )

        fun dsrvToAdventure(dsrv: github.scarsz.discordsrv.dependencies.kyori.adventure.text.Component) =
            GsonComponentSerializer.gson()
                .deserialize(
                    github.scarsz.discordsrv.dependencies.kyori.adventure.text.serializer.gson.GsonComponentSerializer
                        .gson()
                        .serialize(dsrv)
                )
    }

    @Subscribe
    fun onDiscordMessage(e: DiscordGuildMessagePostProcessEvent) = bridge.wrapMinecraftHandler {
        val chatName = DiscordSRV.getPlugin().getDestinationGameChannelNameForTextChannel(e.channel)
        if (chatName == null || chatName.equals("link", ignoreCase = true)) return@wrapMinecraftHandler
        val chat = config.getChat(chatName) ?: return@wrapMinecraftHandler
        val text = dsrvToAdventure(MessageUtil.reserializeToMinecraft(e.message.contentRaw))
        val message = lang.discord.toTelegram.formatMiniMessage(
            Placeholders(
                mapOf("sender" to (e.member?.effectiveName ?: e.author.name)),
                mapOf("text" to text),
            )
        )
        val tgMessage = MinecraftToTelegramConverter.convert(message)
        bridge.merger.lastMessages.remove(chat.name)
        bridge.bot.sendMessage(
            chat.chatId,
            tgMessage.text,
            tgMessage.entities,
            parseMode = null,
            replyToMessageId = chat.topicId,
        )
    }

    override fun enable() {
        super.enable()
        DiscordSRV.api.subscribe(this)
        TgbridgeEvents.TG_CHAT_MESSAGE.addListener { e ->
            val dsrv = DiscordSRV.getPlugin()
            val channel = dsrv.getDestinationTextChannelForGameChannelName(e.chat.name)
                ?: dsrv.mainTextChannel.takeIf { e.chat.isDefault }
                ?: return@addListener
            val component = e.placeholders.component["text"] ?: return@addListener

            val text = MessageUtil
                .reserializeToDiscord(adventureToDsrv(component))
                .replace("@", "@\u200B")
            val message = lang.discord.toDiscord.formatLang(
                Placeholders(
                    mapOf(
                        "sender" to e.message.senderName,
                        "text" to text,
                    )
                )
            )

            DiscordUtil.sendMessage(channel, message)
        }
    }
}
