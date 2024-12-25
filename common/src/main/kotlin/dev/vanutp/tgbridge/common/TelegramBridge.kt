package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
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
        try {
            ConfigManager.init(platform.configDir, platform::getLanguageKey)
        } catch (_: DefaultConfigUnchangedException) {
            logger.error("Can't start with default config values: please fill in botToken and chatId")
            return
        }
        bot = TelegramBot(config.advanced.botApiUrl, config.general.botToken, logger)

        runBlocking {
            bot.init()
            if (config.events.enableStartMessages) {
                sendMessage(lang.telegram.serverStarted)
            }
        }
        registerTelegramHandlers()
        coroutineScope.launch {
            bot.startPolling(coroutineScope)
        }
        initialized = true
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

    private suspend fun onTelegramListCommand(msg: TgMessage) {
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
        if (
            msg.chat.id != config.general.chatId
            || config.general.topicId != null && msg.messageThreadId != config.general.topicId
        ) {
            return
        }
        lastMessageLock.withLock {
            lastMessage = null
        }
        platform.broadcastMessage(msg.toMinecraft(bot.me.id))
    }

    fun onReloadCommand(ctx: TBCommandContext): Boolean {
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

    fun onChatMessage(e: TBPlayerEventData) = withScopeAndLock {
        val rawMinecraftText = e.text.asString()
        val bluemapLink = rawMinecraftText.asBluemapLinkOrNone()
        val prefix = config.messages.requirePrefixInMinecraft ?: ""
        if (bluemapLink == null && !rawMinecraftText.startsWith(prefix)) {
            return@withScopeAndLock
        }

        val textWithoutPrefix = if (config.messages.keepPrefix) {
            rawMinecraftText
        } else {
            rawMinecraftText.removePrefix(prefix)
        }
        val escapedText = textWithoutPrefix.escapeHTML()

        val currText = lang.telegram.chatMessage.formatLang(
            "username" to e.username,
            "text" to (bluemapLink ?: escapedText),
        )
        val currDate = Clock.systemUTC().instant()

        val lm = lastMessage
        if (
            lm != null
            && lm.type == LastMessageType.TEXT
            && (lm.text + "\n" + currText).length <= 4000
            && currDate.minus((config.messages.mergeWindow ?: 0).toLong(), ChronoUnit.SECONDS) < lm.date
        ) {
            lm.text += "\n" + currText
            lm.date = currDate
            editMessageText(lm.id, lm.text!!)
        } else {
            val newMsg = sendMessage(currText)
            lastMessage = LastMessage(
                LastMessageType.TEXT,
                newMsg.messageId,
                currDate,
                text = currText
            )
        }
    }

    fun onPlayerDeath(e: TBPlayerEventData) = withScopeAndLock {
        if (!config.events.enableDeathMessages) {
            return@withScopeAndLock
        }
        val component = e.text as TranslatableComponent
        sendMessage(lang.telegram.playerDied.formatLang("deathMessage" to component.asString().escapeHTML()))
        lastMessage = null
    }

    fun onPlayerJoin(username: String) = withScopeAndLock {
        if (!config.events.enableJoinMessages) {
            return@withScopeAndLock
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
            sendMessage(lang.telegram.playerJoined.formatLang("username" to username))
        }
        lastMessage = null
    }

    fun onPlayerLeave(username: String) = withScopeAndLock {
        if (!config.events.enableLeaveMessages) {
            return@withScopeAndLock
        }
        val newMsg = sendMessage(lang.telegram.playerLeft.formatLang("username" to username))
        lastMessage = LastMessage(
            LastMessageType.LEAVE,
            newMsg.messageId,
            Clock.systemUTC().instant(),
            leftPlayer = username
        )
    }

    fun onPlayerAdvancement(e: TBPlayerEventData) = withScopeAndLock {
        val advancementsCfg = config.events.advancementMessages
        if (!advancementsCfg.enable) {
            return@withScopeAndLock
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
                if (!advancementsCfg.enableTask) return@withScopeAndLock
                lang.telegram.advancements.regular
            }

            "chat.type.advancement.goal" -> {
                if (!advancementsCfg.enableGoal) return@withScopeAndLock
                lang.telegram.advancements.goal
            }

            "chat.type.advancement.challenge" -> {
                if (!advancementsCfg.enableChallenge) return@withScopeAndLock
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

    private fun withScopeAndLock(fn: suspend () -> Unit) {
        coroutineScope.launch {
            lastMessageLock.withLock {
                fn()
            }
        }
    }

    private suspend fun sendMessage(text: String): TgMessage {
        return bot.sendMessage(config.general.chatId, text, replyToMessageId = config.general.topicId)
    }

    private suspend fun editMessageText(messageId: Int, text: String): TgMessage {
        return bot.editMessageText(config.general.chatId, messageId, text)
    }

    private suspend fun deleteMessage(messageId: Int) {
        bot.deleteMessage(config.general.chatId, messageId)
    }
}
