package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.converters.MinecraftToTelegramConverter
import dev.vanutp.tgbridge.common.converters.TelegramFormattedText
import dev.vanutp.tgbridge.common.converters.TelegramToMinecraftConverter
import dev.vanutp.tgbridge.common.models.LastMessage
import dev.vanutp.tgbridge.common.models.LastMessageType
import dev.vanutp.tgbridge.common.models.TBCommandContext
import dev.vanutp.tgbridge.common.models.TBPlayerEventData
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslatableComponent
import java.time.Clock
import java.time.temporal.ChronoUnit
import kotlin.math.max


abstract class TelegramBridge {
    private val coroutineScope = CoroutineScope(Dispatchers.IO).plus(SupervisorJob())
    protected abstract val logger: AbstractLogger
    protected abstract val platform: Platform
    private var initialized: Boolean = false
    private lateinit var bot: TelegramBot

    private var lastMessage: LastMessage? = null
    private val lastMessageLock = Mutex()

    open fun platformInit() {}

    fun init() {
        logger.info("tgbridge starting on ${platform.name}")
        platformInit()
        ConfigManager.init(platform.configDir, platform::getLanguageKey)
        if (config.hasDefaultValues()) {
            logger.error("Can't start with default config values: please fill in botToken and chatId")
            return
        }
        bot = TelegramBot(config.advanced.botApiUrl, config.general.botToken, logger)

        runBlocking {
            bot.init()
        }
        registerTelegramHandlers()
        coroutineScope.launch {
            bot.startPolling(coroutineScope)
        }
        initialized = true
    }

    fun onServerStarted() = wrapMinecraftHandler {
        if (config.events.enableStartMessages) {
            sendMessage(lang.telegram.serverStarted)
        }
    }

    fun shutdown() {
        if (!initialized) {
            return
        }
        runBlocking {
            if (config.events.enableStopMessages) {
                sendMessage(lang.telegram.serverStopped)
            }
            bot.shutdown()
        }
        coroutineScope.cancel()
    }

    private fun registerTelegramHandlers() {
        bot.registerCommandHandler("list", this::onTelegramListCommand)
        bot.registerMessageHandler(this::onTelegramMessage)
    }

    private fun checkMessageChat(msg: TgMessage): Boolean {
        val isTopicMessage = msg.isTopicMessage ?: false
        val messageThreadId = if (isTopicMessage) {
            msg.messageThreadId!!
        } else {
            // Replies in "General" topic have threadId set to the reply message id
            1
        }
        val chatValid = msg.chat.id == config.general.chatId
        val topicValid = config.general.topicId == null || messageThreadId == config.general.topicId
        return chatValid && topicValid
    }

    private suspend fun onTelegramListCommand(msg: TgMessage) {
        if (!checkMessageChat(msg)) {
            return
        }
        val onlinePlayerNames = platform.getOnlinePlayerNames()
        if (onlinePlayerNames.isNotEmpty()) {
            sendMessage(
                lang.telegram.playerList.formatLang(
                    "count" to onlinePlayerNames.size.toString(),
                    "usernames" to onlinePlayerNames.joinToString(),
                )
            )
        } else {
            sendMessage(lang.telegram.playerListZeroOnline)
        }
    }

    private suspend fun onTelegramMessage(msg: TgMessage) {
        if (!checkMessageChat(msg)) {
            return
        }
        lastMessageLock.withLock {
            lastMessage = null
        }
        platform.broadcastMessage(TelegramToMinecraftConverter.convert(msg, bot.me.id))
    }

    fun onReloadCommand(ctx: TBCommandContext): Boolean {
        if (!initialized) {
            try {
                if (config.hasDefaultValues()) {
                    ctx.reply("A restart is required after initially filling in the bot token and chat ID")
                    return false
                }
            } catch (_: UninitializedPropertyAccessException) {
            }
            ctx.reply("Mod was not properly initialized, please restart the server")
            return false
        }
        try {
            ConfigManager.reload()
        } catch (e: Exception) {
            ctx.reply("Error reloading config: " + (e.message ?: e.javaClass.name))
            return false
        }
        runBlocking {
            bot.recoverPolling(coroutineScope)
            if (lastMessageLock.isLocked) {
                lastMessageLock.unlock()
            }
        }
        ctx.reply("Config reloaded. Note that the bot token can't be changed without a restart")
        return true
    }

    fun onChatMessage(e: TBPlayerEventData) = wrapMinecraftHandler {
        var telegramText = MinecraftToTelegramConverter.convert(e.text)
        val bluemapLink = telegramText.text.asBluemapLinkOrNone()
        val prefix = config.integrations.incompatiblePluginChatPrefix
            ?: config.messages.requirePrefixInMinecraft
            ?: ""
        if (bluemapLink == null && !telegramText.text.startsWith(prefix)) {
            return@wrapMinecraftHandler
        }

        if (bluemapLink != null) {
            telegramText = bluemapLink
        } else if (!config.messages.keepPrefix) {
            telegramText = TelegramFormattedText(
                telegramText.text.removePrefix(prefix),
                telegramText.entities.map { it.copy(offset = max(it.offset - prefix.length, 0)) },
            )
        }

        val tgPrefix = MinecraftToTelegramConverter.convert(
            lang.telegram.chatMessage.formatMiniMessage(listOf("username" to e.username))
                .append(Component.text(" "))
        )

        val currText = tgPrefix + telegramText
        val currDate = Clock.systemUTC().instant()

        val lm = lastMessage
        if (
            lm != null
            && lm.type == LastMessageType.TEXT
            && (lm.text!! + "\n" + currText).text.length <= 4000
            && currDate.minus((config.messages.mergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            lm.text = lm.text!! + "\n" + currText
            lm.date = currDate
            editMessageText(lm.id, lm.text!!.text, lm.text!!.entities)
        } else {
            val newMsg = sendMessage(currText.text, currText.entities)
            lastMessage = LastMessage(
                LastMessageType.TEXT,
                newMsg.messageId,
                currDate,
                text = currText
            )
        }
    }

    fun onPlayerDeath(e: TBPlayerEventData) = wrapMinecraftHandler {
        if (!config.events.enableDeathMessages) {
            return@wrapMinecraftHandler
        }
        val telegramText = MinecraftToTelegramConverter.convert(
            lang.telegram.playerDied.formatMiniMessage(listOf(), listOf("death_message" to e.text))
        )
        sendMessage(telegramText.text, telegramText.entities)
        lastMessage = null
    }

    fun onPlayerJoin(username: String, hasPlayedBefore: Boolean) = wrapMinecraftHandler {
        if (!config.events.enableJoinMessages) {
            return@wrapMinecraftHandler
        }
        val lm = lastMessage
        val currDate = Clock.systemUTC().instant()
        if (
            lm != null
            && lm.type == LastMessageType.LEAVE
            && lm.leftPlayer!! == username
            && currDate.minus((config.events.leaveJoinMergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            deleteMessage(lm.id)
        } else {
            val message = if (hasPlayedBefore) {
                lang.telegram.playerJoined
            } else {
                lang.telegram.playerJoinedFirstTime
            }
            sendMessage(message.formatLang("username" to username))
        }
        lastMessage = null
    }

    fun onPlayerLeave(username: String) = wrapMinecraftHandler {
        if (!config.events.enableLeaveMessages) {
            return@wrapMinecraftHandler
        }
        val newMsg = sendMessage(lang.telegram.playerLeft.formatLang("username" to username))
        lastMessage = LastMessage(
            LastMessageType.LEAVE,
            newMsg.messageId,
            Clock.systemUTC().instant(),
            leftPlayer = username
        )
    }

    fun onPlayerAdvancement(e: TBPlayerEventData) = wrapMinecraftHandler {
        val advancementsCfg = config.events.advancementMessages
        if (!advancementsCfg.enable) {
            return@wrapMinecraftHandler
        }
        val component = e.text as TranslatableComponent
        val advancementTypeKey = component.key()
        val squareBracketsComponent = component.args()[1] as TranslatableComponent
        val advancementNameComponent = squareBracketsComponent.args()[0]
        val advancementName = advancementNameComponent.asString()
        val advancementDescription = if (advancementsCfg.showDescription) {
            advancementNameComponent.style().hoverEvent()?.let {
                val advancementTooltipComponent = it.value() as Component
                if (advancementTooltipComponent.children().size < 2) {
                    return@let null
                }
                advancementTooltipComponent.children()[1].asString()
            } ?: ""
        } else {
            ""
        }
        val langKey = when (advancementTypeKey) {
            "chat.type.advancement.task" -> {
                if (!advancementsCfg.enableTask) return@wrapMinecraftHandler
                lang.telegram.advancements.regular
            }

            "chat.type.advancement.goal" -> {
                if (!advancementsCfg.enableGoal) return@wrapMinecraftHandler
                lang.telegram.advancements.goal
            }

            "chat.type.advancement.challenge" -> {
                if (!advancementsCfg.enableChallenge) return@wrapMinecraftHandler
                lang.telegram.advancements.challenge
            }

            else -> throw TBAssertionFailed("Unknown advancement type $advancementTypeKey.")
        }
        sendMessage(
            langKey.formatLang(
                "username" to e.username,
                "title" to advancementName.escapeHTML(),
                "description" to advancementDescription.escapeHTML(),
            )
        )
        lastMessage = null
    }

    private fun wrapMinecraftHandler(fn: suspend () -> Unit) {
        if (!initialized) {
            return
        }
        coroutineScope.launch {
            lastMessageLock.withLock {
                fn()
            }
        }
    }

    private suspend fun sendMessage(text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.sendMessage(
            config.general.chatId,
            text,
            entities,
            parseMode = if (entities == null) "HTML" else null,
            replyToMessageId = config.general.topicId,
        )
    }

    private suspend fun editMessageText(messageId: Int, text: String, entities: List<TgEntity>? = null): TgMessage {
        return bot.editMessageText(
            config.general.chatId,
            messageId,
            text,
            entities,
            parseMode = if (entities == null) "HTML" else null,
        )
    }

    private suspend fun deleteMessage(messageId: Int) {
        bot.deleteMessage(config.general.chatId, messageId)
    }
}
