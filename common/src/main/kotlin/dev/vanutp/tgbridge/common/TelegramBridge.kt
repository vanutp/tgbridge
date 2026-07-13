package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import dev.vanutp.tgbridge.common.ConfigManager.lang
import dev.vanutp.tgbridge.common.adventure.args
import dev.vanutp.tgbridge.common.converters.TelegramFormattedText
import dev.vanutp.tgbridge.common.converters.TelegramToMinecraftConverter
import dev.vanutp.tgbridge.common.models.*
import dev.vanutp.tgbridge.common.modules.IChatModule
import dev.vanutp.tgbridge.common.modules.ITgbridgeModule
import dev.vanutp.tgbridge.common.modules.ReplacementsModule
import dev.vanutp.tgbridge.common.modules.VoiceMessagesModule
import kotlinx.coroutines.*
import kotlinx.serialization.SerializationException
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.TranslatableComponent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer

abstract class TelegramBridge {
    internal val coroutineScope = CoroutineScope(Dispatchers.IO).plus(SupervisorJob())
    abstract val logger: ILogger
    abstract val platform: IPlatform
    lateinit var bot: TelegramBot private set
    private var spark: SparkHelper? = null

    private var initialized = false
    private var canRetryFailedInit = false
    private var shouldStartAfterInit = false
    private var started = false

    val chatManager: ChatManager = ChatManager(coroutineScope)

    protected val availableModules = mutableListOf<ITgbridgeModule>()
    private val _enabledModules = mutableListOf<ITgbridgeModule>()
    val enabledModules: List<ITgbridgeModule>
        get() = _enabledModules
    val chatModule: IChatModule?
        get() = _enabledModules.find { it is IChatModule } as? IChatModule

    companion object {
        private var _INSTANCE: TelegramBridge? = null
        val INSTANCE: TelegramBridge
            get() = _INSTANCE ?: throw UninitializedPropertyAccessException("TelegramBridge is not initialized yet")
    }

    init {
        if (_INSTANCE != null) {
            throw IllegalStateException("TelegramBridge is already initialized")
        }
        _INSTANCE = this
    }

    fun init(sync: Boolean = false) {
        logger.info("tgbridge starting on ${platform.name}")
        canRetryFailedInit = false
        ConfigManager.init(platform.configDir)
        AuthManager.init(platform.configDir)
        config.getError()?.let {
            canRetryFailedInit = true
            logger.error(it)
            return
        }
        try {
            LanguageService.init()
        } catch (e: SerializationException) {
            canRetryFailedInit = true
            logger.error("Failed to load language data", e)
            return
        }
        MuteService.init(logger, platform.configDir)
        TgbridgeEvents.RECIPIENTS.addListener { e ->
            if (e.chat.isDefault && chatModule == null) {
                e.recipients += platform.getOnlinePlayers()
            }
        }
        bot = TelegramBot(config.advanced.botApiUrl, config.botToken, logger, coroutineScope)
        addModule(ReplacementsModule(this))
        addModule(VoiceMessagesModule(this))
        val initLambda = suspend {
            bot.init()
            logger.info("Logged in as @${bot.me.username}")
            registerTelegramHandlers()
            bot.startPolling()
            initialized = true
            if (shouldStartAfterInit) {
                onServerStarted()
            }
        }
        if (sync) {
            runBlocking {
                initLambda()
            }
        } else {
            coroutineScope.launch {
                initLambda()
            }
        }
    }

    fun addModule(module: ITgbridgeModule) {
        if (availableModules.contains(module)) {
            throw IllegalStateException("Module ${module::class.simpleName} is already added")
        }
        availableModules.add(module)
        if (started && module.shouldEnable()) {
            logger.info("Enabling module " + (module::class.simpleName ?: "unknown"))
            module.enable()
            _enabledModules.add(module)
        }
    }

    fun onServerStarted() {
        // TODO: this will fail if there is no internet on server start
        if (!initialized) {
            if (canRetryFailedInit) {
                logger.error("Error initializing the mod, check the server console for errors")
            }
            shouldStartAfterInit = true
            return
        }
        shouldStartAfterInit = false
        availableModules
            .filter { it.shouldEnable() }
            .also { toEnable ->
                toEnable.forEach { it.enable() }
                _enabledModules.addAll(toEnable)
            }
        logger.info("Loaded modules: " + enabledModules.joinToString { it::class.simpleName ?: "unknown" })
        // Doing this here to ensure spark is loaded
        spark = SparkHelper.createOrNull()
        if (config.events.enableStartMessages) {
            val disableNotification = config.messages.silentEvents.contains(TgMessageType.SERVER_STARTUP)
            coroutineScope.launch {
                chatManager.sendMessage(
                    config.getDefaultChat(),
                    MessageContentHTMLText(lang.telegram.serverStarted, disableNotification = disableNotification),
                )
            }
        }
        started = true
        coroutineScope.launch {
            LanguageService.updateLangIfNeeded()
        }
    }

    fun shutdown() {
        runBlocking {
            MuteService.shutdown()
            if (config.events.enableStopMessages && initialized) {
                val disableNotification = config.messages.silentEvents.contains(TgMessageType.SERVER_SHUTDOWN)
                chatManager.sendMessage(
                    config.getDefaultChat(),
                    MessageContentHTMLText(lang.telegram.serverStopped, disableNotification = disableNotification),
                )
            }
            bot.shutdown()
        }
        coroutineScope.cancel()
    }

    private fun registerTelegramHandlers() {
        bot.registerCommandHandler("tps", this::onTelegramTpsCommand)
        bot.registerCommandHandler("list", this::onTelegramListCommand)
        bot.registerMessageHandler(this::onTelegramMessage)
        bot.registerCallbackHandler(this::onTelegramCallbackQuery)
    }

    private fun getMessageChat(msg: TgMessage): ChatConfig? {
        val topicId = if (msg.isTopicMessage ?: false) {
            msg.messageThreadId!!
        } else {
            // Replies in "General" topic have threadId set to the reply message id
            1
        }
        return config.getChat(msg.chat.id, topicId)
    }

    private suspend fun onTelegramTpsCommand(msg: TgMessage) {
        val spark = spark ?: return
        val chat = getMessageChat(msg) ?: return
        val durations = spark.getPlaceholders()
        if (durations == null) {
            logger.error("Unable to get spark data")
            return
        }
        chatManager.sendMessage(
            chat,
            MessageContentHTMLText(lang.telegram.tps.formatLang(durations), disableNotification = true)
        )
    }

    private suspend fun onTelegramListCommand(msg: TgMessage) {
        val chat = getMessageChat(msg) ?: return
        val onlinePlayers = platform.getOnlinePlayers().filterNot { it.isVanished() }
        val text = if (onlinePlayers.isNotEmpty()) {
            lang.telegram.playerList.formatLang(
                Placeholders(
                    mapOf(
                        "count" to onlinePlayers.size.toString(),
                        "usernames" to onlinePlayers.joinToString { it.getName() },
                    ),
                )
            )
        } else {
            lang.telegram.playerListZeroOnline
        }
        chatManager.sendMessage(chat, MessageContentHTMLText(text, disableNotification = true))
    }

    private suspend fun onTelegramMessage(msg: TgMessage) {
        if (msg.chat.type == TgChatType.PRIVATE) {
            onTelegramPrivateMessage(msg)
            return
        }
        val chat = getMessageChat(msg) ?: return
        chatManager.clearLastMessage(chat)
        val textComponent = TelegramToMinecraftConverter.convert(msg, bot.me.id)
        val e = TgbridgeTgChatMessageEvent(
            chat,
            msg,
            Placeholders(
                mapOf(
                    "sender" to msg.senderName,
                    "full_name" to (msg.from?.fullName ?: msg.senderName),
                    "first_name" to (msg.from?.firstName ?: ""),
                    "last_name" to (msg.from?.lastName ?: ""),
                ),
                mapOf("text" to textComponent),
            )
        )
        if (!TgbridgeEvents.TG_CHAT_MESSAGE.invoke(e)) return

        val recipientsEvt = TgbridgeRecipientsEvent(chat, originalEvent = e)
        if (chat.useTellraw) {
            recipientsEvt.recipients = platform.getOnlinePlayers()
        } else {
            TgbridgeEvents.RECIPIENTS.invoke(recipientsEvt)
        }

        val format = if (chat.useTellraw) chat.tellrawFormat else chat.minecraftFormat
        platform.broadcastMessage(recipientsEvt.recipients, format.formatMiniMessage(e.placeholders))
    }

    private fun tryReinit(ctx: TBCommandContext): Boolean {
        init(sync = true)
        if (initialized) {
            ctx.reply("tgbridge initialized")
        } else {
            ctx.reply(config.getError() ?: "Error initializing the mod, check the server console for errors")
        }
        return initialized
    }

    fun onReloadCommand(ctx: TBCommandContext): Boolean {
        if (!initialized) {
            try {
                if (canRetryFailedInit) {
                    return tryReinit(ctx)
                }
            } catch (_: UninitializedPropertyAccessException) {
            }
            ctx.reply("Mod was not properly initialized, check the server console for errors")
            return false
        }
        try {
            ConfigManager.reload()

            enabledModules
                .filter { !it.shouldEnable() && it.canBeDisabled }
                .also { toDisable ->
                    if (toDisable.isEmpty()) return@also
                    logger.info("Disabling modules: " + toDisable.joinToString { it::class.simpleName ?: "unknown" })
                    toDisable.forEach { it.disable() }
                    _enabledModules.removeAll(toDisable)
                }
            availableModules
                .filter { it.shouldEnable() && !_enabledModules.contains(it) }
                .also { toEnable ->
                    if (toEnable.isEmpty()) return@also
                    logger.info("Enabling modules: " + toEnable.joinToString { it::class.simpleName ?: "unknown" })
                    toEnable.forEach { it.enable() }
                    _enabledModules.addAll(toEnable)
                }

            runBlocking {
                bot.recoverPolling()
                chatManager.unlock()
            }

            ctx.reply("Config reloaded. Note that the bot token and settings under \"advanced\" can't be changed without a restart")
        } catch (e: Exception) {
            logger.error("Reloading error", e)
            ctx.reply("Error reloading: " + (e.message ?: e.javaClass.name))
            return false
        }
        coroutineScope.launch {
            TgbridgeEvents.POST_RELOAD.invoke(Unit)
        }
        return true
    }

    fun onToggleMuteCommand(ctx: TBCommandContext): Boolean {
        val player = ctx.source?.uuid ?: return false
        if (MuteService.isMuted(player)) {
            if (MuteService.unmute(player)) {
                ctx.reply(lang.minecraft.serviceMessages.mute.unmuted)
                return true
            }
        } else if (MuteService.mute(player)) {
            ctx.reply(lang.minecraft.serviceMessages.mute.muted)
            return true
        }
        return false
    }

    fun onSendCommand(ctx: TBCommandContext, format: String, chatName: String, message: String): Boolean {
        val chat = config.getChat(chatName)
        if (chat == null) {
            ctx.reply("Chat '$chatName' not found")
            return false
        }
        val content = try {
            when (format) {
                "plain" -> MessageContentText(TelegramFormattedText(message))
                "mm" -> MessageContentText(message.formatMiniMessage())
                "html" -> MessageContentHTMLText(message)
                "json" -> MessageContentText(GsonComponentSerializer.gson().deserialize(message))
                else -> throw IllegalStateException("Unknown format")
            }
        } catch (e: Exception) {
            ctx.reply("Error parsing message: $e")
            return false
        }
        coroutineScope.launch {
            // TODO: do commands run on the main thread?
            try {
                chatManager.sendMessage(chat, content)
                ctx.reply("Message sent")
            } catch (e: TelegramException) {
                ctx.reply("Error sending message: ${e.responseBody}")
                return@launch
            } catch (e: Exception) {
                ctx.reply("Error sending message: $e")
                return@launch
            }
        }
        // TODO: is it ok to ever return false?
        return true
    }

    fun onChatMessage(e: TgbridgeMcChatMessageEvent) = wrapMinecraftHandler {
        if (!TgbridgeEvents.MC_CHAT_MESSAGE.invoke(e)) return@wrapMinecraftHandler

        val chat = config.getChat(e.chatName) ?: return@wrapMinecraftHandler

        val messageText = e.message.asString()
        val bluemapLink = messageText.asBluemapLinkOrNone()
        val prefix = config.integrations.incompatiblePluginChatPrefix
            ?: config.messages.requirePrefixInMinecraft
            ?: ""
        if (bluemapLink == null && !messageText.startsWith(prefix)) {
            return@wrapMinecraftHandler
        }

        val tgText = bluemapLink
            ?: e.message.takeIf { config.messages.keepPrefix || prefix.isEmpty() }
            ?: e.message.replaceText { it: TextReplacementConfig.Builder ->
                it.match("^" + Regex.escape(prefix)).replacement("")
            }

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.sender,
            Placeholders(mapOf("username" to e.sender.getName()), mapOf("text" to tgText)),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val text = chat.telegramFormat.formatMiniMessage(placeholdersEvt.placeholders)
        val disableNotification = config.messages.silentEvents.contains(TgMessageType.CHAT)
        chatManager.sendMessage(
            chat,
            MessageContentMergeableText(text, disableNotification = disableNotification),
        )
    }

    fun onPlayerDeath(e: TgbridgeDeathEvent) = wrapMinecraftHandler {
        if (!config.events.enableDeathMessages) return@wrapMinecraftHandler

        if (!TgbridgeEvents.DEATH.invoke(e)) return@wrapMinecraftHandler
        if (e.player.isVanished()) return@wrapMinecraftHandler

        val players = platform.getOnlinePlayers()
        val convertedMessage = when (val msg = e.message) {
            is TranslatableComponent -> {
                val args = msg.args().map { arg ->
                    val entity = arg.hoverEvent()
                        ?.value() as? HoverEvent.ShowEntity
                        ?: return@map arg
                    return@map players
                        .find { it.uuid == entity.id() }
                        ?.let { Component.text(it.getName()) }
                        ?: arg
                }
                msg.args(args)
            }

            null -> Component.translatable("death.attack.generic", Component.text(e.player.getName()))
            else -> msg
        }

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.player,
            Placeholders(component = mapOf("death_message" to convertedMessage)),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val chat = config.getDefaultChat()
        val text = lang.telegram.playerDied.formatMiniMessage(placeholdersEvt.placeholders)
        val disableNotification = config.messages.silentEvents.contains(TgMessageType.DEATH)
        chatManager.sendMessage(
            chat,
            MessageContentText(text, disableNotification = disableNotification),
        )
    }

    fun onPlayerJoin(e: TgbridgeJoinEvent) = wrapMinecraftHandler {
        if (!TgbridgeEvents.JOIN.invoke(e)) return@wrapMinecraftHandler
        if (!e.ignoreVanish && e.player.isVanished()) return@wrapMinecraftHandler
        if (
            config.events.joinMessages == JoinMessagesMode.DISABLED
            || config.events.joinMessages == JoinMessagesMode.FIRST_JOIN_ONLY
            && e.hasPlayedBefore
        )
            return@wrapMinecraftHandler

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.player,
            Placeholders(mapOf("username" to e.player.getName())),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val chat = config.getDefaultChat()
        val text = if (e.hasPlayedBefore) {
            lang.telegram.playerJoined
        } else {
            lang.telegram.playerJoinedFirstTime
        }.formatLang(placeholdersEvt.placeholders)
        chatManager.sendMessage(chat, MessageContentJoin(e.player, text))
    }

    fun onPlayerLeave(e: TgbridgeLeaveEvent) = wrapMinecraftHandler {
        if (!config.events.enableLeaveMessages) return@wrapMinecraftHandler
        if (!TgbridgeEvents.LEAVE.invoke(e)) return@wrapMinecraftHandler
        if (!e.ignoreVanish && e.player.isVanished()) return@wrapMinecraftHandler

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.player,
            Placeholders(mapOf("username" to e.player.getName())),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val chat = config.getDefaultChat()
        val text = lang.telegram.playerLeft.formatLang(placeholdersEvt.placeholders)
        chatManager.sendMessage(chat, MessageContentLeave(e.player, text))
    }

    fun onPlayerAdvancement(e: TgbridgeAdvancementEvent) = wrapMinecraftHandler {
        val advancementsCfg = config.events.advancementMessages
        if (!advancementsCfg.enable) return@wrapMinecraftHandler
        if (!TgbridgeEvents.ADVANCEMENT.invoke(e)) return@wrapMinecraftHandler
        if (e.player.isVanished()) return@wrapMinecraftHandler

        val advancementName = e.title.asString()
        val advancementDescription = if (advancementsCfg.showDescription) {
            e.description.asString()
        } else {
            ""
        }

        val placeholdersEvt = TgbridgePlayerPlaceholdersEvent(
            e.player,
            Placeholders(
                mapOf(
                    "username" to e.player.getName(),
                    "title" to advancementName.escapeHTML(),
                    "description" to advancementDescription.escapeHTML(),
                ),
            ),
            e,
        )
        TgbridgeEvents.PLAYER_PLACEHOLDERS.invoke(placeholdersEvt)

        val langKey = when (e.type) {
            "task" -> {
                if (!advancementsCfg.enableTask) return@wrapMinecraftHandler
                lang.telegram.advancements.regular
            }

            "goal" -> {
                if (!advancementsCfg.enableGoal) return@wrapMinecraftHandler
                lang.telegram.advancements.goal
            }

            "challenge" -> {
                if (!advancementsCfg.enableChallenge) return@wrapMinecraftHandler
                lang.telegram.advancements.challenge
            }

            else -> throw TBAssertionFailed("Unknown advancement type ${e.type}.")
        }
        val chat = config.getDefaultChat()
        val text = langKey.formatLang(placeholdersEvt.placeholders)
        val disableNotification = config.messages.silentEvents.contains(TgMessageType.ADVANCEMENT)
        chatManager.sendMessage(chat, MessageContentHTMLText(text, disableNotification = disableNotification))
    }

    fun wrapMinecraftHandler(fn: suspend () -> Unit) {
        if (!initialized) {
            return
        }
        coroutineScope.launch {
            fn()
        }
    }

    private fun getTgMentionHtml(user: TgUser): String {
        return if (user.username != null) {
            "@${user.username}"
        } else {
            "<a href=\"tg://user?id=${user.id}\">${user.firstName}</a>"
        }
    }

    private suspend fun announceLinking(username: String, user: TgUser) {
        val mention = getTgMentionHtml(user)
        val text = "🛫 Игрок <b>$username</b> привязал майнкрафт акк к тг $mention"
        // Send to Telegram group chat
        val chat = config.getDefaultChat()
        chatManager.sendMessage(chat, MessageContentHTMLText(text, disableNotification = false))

        // Also broadcast in Minecraft so online players see it
        val mm = net.kyori.adventure.text.minimessage.MiniMessage.miniMessage()
        val plainMention = if (user.username != null) "@${user.username}" else user.firstName
        val mcText = mm.deserialize("🛫 Игрок <green>$username</green> привязал майнкрафт акк к тг <aqua>$plainMention</aqua>")
        platform.broadcastMessage(platform.getOnlinePlayers(), mcText)
    }

    private suspend fun checkGroupMembership(tgId: Long): Boolean {
        val defaultChat = config.getDefaultChat()
        val authGroupId = defaultChat.chatId
        if (authGroupId == 0L) return true

        return try {
            val member = bot.getChatMember(authGroupId.toString(), tgId)
            member.status in listOf("creator", "administrator", "member", "restricted")
        } catch (e: Exception) {
            logger.error("Failed to check group membership for $tgId in default chat $authGroupId", e)
            false
        }
    }

    private suspend fun onTelegramPrivateMessage(msg: TgMessage) {
        val text = msg.effectiveText?.trim() ?: return
        val from = msg.from ?: return

        // 1. If `/start` command
        if (text.startsWith("/start", ignoreCase = true)) {
            bot.sendMessage(
                chatId = msg.chat.id,
                text = "Привет! Чтобы зайти на сервер, отправь мне код из 5-6 цифр, который ты видишь при входе в игру.\n\n" +
                       "Также тебе необходимо обязательно вступить в нашу группу: <b>@burmalda_mc_c</b>.\n" +
                       "После этого ты сможешь зайти на сервер!",
                parseMode = "HTML"
            )
            return
        }

        // 2. Check if the message is a 5-6 digit code
        val codeRegex = Regex("^\\d{5,6}$")
        if (codeRegex.matches(text)) {
            // Find player with this code (case insensitive lowercase keys)
            val playerEntry = AuthManager.db.players.values.find { it.currentCode == text }
            if (playerEntry == null) {
                bot.sendMessage(
                    chatId = msg.chat.id,
                    text = "Код не найден или уже устарел. Попробуй зайти на сервер еще раз, чтобы получить новый код."
                )
                return
            }

            // Check if user is in the group/channel
            val isMember = checkGroupMembership(from.id)
            if (!isMember) {
                // Link anyway, but keep them informed that they need to join the group
                playerEntry.tgId = from.id
                playerEntry.currentCode = null
                if (playerEntry.pendingIpToConfirm != null) {
                    playerEntry.allowedIp = playerEntry.pendingIpToConfirm
                    playerEntry.pendingIpToConfirm = null
                }
                AuthManager.save()

                bot.sendMessage(
                    chatId = msg.chat.id,
                    text = "Аккаунт <b>${playerEntry.username}</b> успешно привязан!\n\n" +
                           "⚠️ Но вы ещё не вступили в группу <b>@burmalda_mc_c</b>. Вступите в неё, чтобы иметь возможность войти на сервер.",
                    parseMode = "HTML"
                )
                return
            }

            // Account successfully linked and in group!
            playerEntry.tgId = from.id
            playerEntry.currentCode = null
            if (playerEntry.pendingIpToConfirm != null) {
                playerEntry.allowedIp = playerEntry.pendingIpToConfirm
                playerEntry.pendingIpToConfirm = null
            }
            AuthManager.save()

            bot.sendMessage(
                chatId = msg.chat.id,
                text = "Аккаунт <b>${playerEntry.username}</b> успешно привязан! Вы можете заходить на сервер.",
                parseMode = "HTML"
            )

            // Broadcast message about linking!
            announceLinking(playerEntry.username, from)
            return
        }

        // If not a code or start, we can remind them what to do
        bot.sendMessage(
            chatId = msg.chat.id,
            text = "Отправьте мне код из 5-6 цифр для привязки аккаунта или введите /start."
        )
    }

    private suspend fun onTelegramCallbackQuery(query: TgCallbackQuery): Boolean {
        val data = query.data ?: return false
        val from = query.from

        if (data.startsWith("ip_confirm:") || data.startsWith("ip_decline:")) {
            val parts = data.split(":")
            if (parts.size < 3) {
                bot.answerCallbackQuery(query.id, "Некорректные данные")
                return true
            }
            val username = parts[1]
            val ip = parts.drop(2).joinToString(":")

            val player = AuthManager.db.players[username.lowercase()]
            if (player == null) {
                bot.answerCallbackQuery(query.id, "Игрок не найден")
                return true
            }

            if (player.tgId != from.id) {
                bot.answerCallbackQuery(query.id, "Это не ваш аккаунт!", showAlert = true)
                return true
            }

            val chat_id = query.message?.chat?.id ?: from.id
            val message_id = query.message?.messageId

            if (data.startsWith("ip_confirm:")) {
                player.allowedIp = ip
                player.pendingIpToConfirm = null
                player.pendingIpMessageId = null
                AuthManager.save()

                bot.answerCallbackQuery(query.id, "Вход подтвержден!")

                if (message_id != null) {
                    bot.editMessageTextWithMarkup(
                        chatId = chat_id,
                        messageId = message_id,
                        text = "✅ Вход с IP <b>$ip</b> для игрока <b>${player.username}</b> подтвержден!",
                        replyMarkup = null,
                        parseMode = "HTML"
                    )
                }
            } else {
                player.pendingIpToConfirm = null
                player.pendingIpMessageId = null
                AuthManager.save()

                bot.answerCallbackQuery(query.id, "Вход отклонен!")

                if (message_id != null) {
                    bot.editMessageTextWithMarkup(
                        chatId = chat_id,
                        messageId = message_id,
                        text = "❌ Вход с IP <b>$ip</b> для игрока <b>${player.username}</b> отклонен!",
                        replyMarkup = null,
                        parseMode = "HTML"
                    )
                }
            }
            return true
        }

        return false
    }
}
