package dev.vanutp.tgbridge.common

import dev.vanutp.tgbridge.common.ConfigManager.config
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.*
import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeoutException
import kotlin.reflect.KClass


@Serializable
data class TgUser(
    val id: Long,
    @SerialName("first_name")
    val firstName: String,
    @SerialName("last_name")
    val lastName: String? = null,
    val username: String? = null,
) {
    val fullName
        get() = (firstName + " " + (lastName ?: "")).trim()
}

@Serializable
data class TgChat(
    val id: Long,
    val title: String = "",
    val username: String? = null,
)

@Serializable
class TgAny

@Serializable
data class TgPoll(
    val question: String,
)

@Serializable
data class TgVoice(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    @SerialName("duration")
    val duration: Int,
    @SerialName("mime_type")
    val mimeType: String? = null,
    @SerialName("file_size")
    val fileSize: Int? = null,
)

@Serializable
data class TgMessageOrigin(
    @SerialName("sender_user")
    val senderUser: TgUser? = null,
    @SerialName("sender_user_name")
    val senderUserName: String? = null,
    @SerialName("sender_chat")
    val senderChat: TgChat? = null,
    val chat: TgChat? = null,
)

interface TgMessageMedia {
    val animation: TgAny?
    val photo: List<TgAny>?
    val audio: TgAny?
    val document: TgAny?
    val sticker: TgAny?
    val video: TgAny?
    val videoNote: TgAny?
    val voice: TgVoice?
    val poll: TgPoll?
}

@Serializable
data class TgExternalReplyInfo(
    val origin: TgMessageOrigin,
    val chat: TgChat? = null,
    override val animation: TgAny? = null,
    override val photo: List<TgAny>? = null,
    override val audio: TgAny? = null,
    override val document: TgAny? = null,
    override val sticker: TgAny? = null,
    override val video: TgAny? = null,
    @SerialName("video_note")
    override val videoNote: TgAny? = null,
    override val voice: TgVoice? = null,
    override val poll: TgPoll? = null,
) : TgMessageMedia {
    val senderName
        get() = origin.senderUser?.fullName
            ?: origin.senderUserName
            ?: origin.senderChat?.title
            ?: origin.chat?.title
            ?: ""
}

@Serializable
data class TgTextQuote(
    val text: String,
    val entities: List<TgEntity>? = emptyList(),
)

@Serializable
enum class TgEntityType {
    @SerialName("mention")
    MENTION,

    @SerialName("hashtag")
    HASHTAG,

    @SerialName("cashtag")
    CASHTAG,

    @SerialName("bot_command")
    BOT_COMMAND,

    @SerialName("url")
    URL,

    @SerialName("email")
    EMAIL,

    @SerialName("phone_number")
    PHONE_NUMBER,

    @SerialName("bold")
    BOLD,

    @SerialName("italic")
    ITALIC,

    @SerialName("underline")
    UNDERLINE,

    @SerialName("strikethrough")
    STRIKETHROUGH,

    @SerialName("spoiler")
    SPOILER,

    @SerialName("blockquote")
    BLOCKQUOTE,

    @SerialName("expandable_blockquote")
    EXPANDABLE_BLOCKQUOTE,

    @SerialName("code")
    CODE,

    @SerialName("pre")
    PRE,

    @SerialName("text_link")
    TEXT_LINK,

    @SerialName("text_mention")
    TEXT_MENTION,

    @SerialName("custom_emoji")
    CUSTOM_EMOJI,
}

@Serializable
data class TgEntity(
    val type: TgEntityType,
    val offset: Int,
    val length: Int,
    val url: String? = null,
)

@Serializable
data class TgVideoChatParticipantsInvited(
    val users: List<TgUser>,
)

@Serializable
data class TgMessage(
    val chat: TgChat,
    @SerialName("message_id")
    val messageId: Int,
    val from: TgUser? = null,
    @SerialName("sender_chat")
    val senderChat: TgChat? = null,
    @SerialName("forward_from")
    val forwardFrom: TgUser? = null,
    @SerialName("forward_from_chat")
    val forwardFromChat: TgChat? = null,
    @SerialName("reply_to_message")
    val replyToMessage: TgMessage? = null,
    @SerialName("external_reply")
    val externalReply: TgExternalReplyInfo? = null,
    val quote: TgTextQuote? = null,
    @SerialName("via_bot")
    val viaBot: TgUser? = null,
    @SerialName("message_thread_id")
    val messageThreadId: Int? = null,
    @SerialName("is_topic_message")
    val isTopicMessage: Boolean? = null,
    @SerialName("author_signature")
    val authorSignature: String? = null,
    val text: String? = null,
    @SerialName("entities")
    val textEntities: List<TgEntity>? = null,
    val caption: String? = null,
    @SerialName("caption_entities")
    val captionEntities: List<TgEntity>? = null,
    override val animation: TgAny? = null,
    override val photo: List<TgAny>? = null,
    override val audio: TgAny? = null,
    override val document: TgAny? = null,
    override val sticker: TgAny? = null,
    override val video: TgAny? = null,
    @SerialName("video_note")
    override val videoNote: TgAny? = null,
    override val voice: TgVoice? = null,
    override val poll: TgPoll? = null,
    @SerialName("pinned_message")
    val pinnedMessage: TgMessage? = null,

    @SerialName("new_chat_members")
    val newChatMembers: List<TgUser>? = null,
    @SerialName("left_chat_member")
    val leftChatMember: TgUser? = null,

    @SerialName("video_chat_scheduled")
    val videoChatScheduled: TgAny? = null,
    @SerialName("video_chat_started")
    val videoChatStarted: TgAny? = null,
    @SerialName("video_chat_ended")
    val videoChatEnded: TgAny? = null,
    @SerialName("video_chat_participants_invited")
    val videoChatParticipantsInvited: TgVideoChatParticipantsInvited? = null,
) : TgMessageMedia {
    val senderName
        get() = authorSignature
            ?: senderChat?.title
            ?: from?.fullName
            ?: ""
    val effectiveText
        get() = text ?: caption
    val entities
        get() = textEntities ?: captionEntities ?: emptyList()
}

@Serializable
data class TgFile(
    @SerialName("file_id")
    val fileId: String,
    @SerialName("file_unique_id")
    val fileUniqueId: String,
    @SerialName("file_size")
    val fileSize: Int? = null,
    @SerialName("file_path")
    val filePath: String? = null,
)

@Serializable
data class TgUpdate(
    @SerialName("update_id")
    val updateId: Int,
    val message: TgMessage? = null,
)

@Serializable
data class TgResponse<T>(
    val ok: Boolean,
    val result: T? = null,
    val description: String? = null,
)

@Serializable
data class TgSendMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("text")
    val text: String,
    @SerialName("entities")
    val entities: List<TgEntity>? = null,
    @SerialName("reply_to_message_id")
    val replyToMessageId: Int? = null,
    @SerialName("parse_mode")
    val parseMode: String? = null,
    @SerialName("disable_web_page_preview")
    val disableWebPagePreview: Boolean,
)

@Serializable
data class TgEditMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("message_id")
    val messageId: Int,
    @SerialName("text")
    val text: String,
    @SerialName("entities")
    val entities: List<TgEntity>? = null,
    @SerialName("parse_mode")
    val parseMode: String? = null,
    @SerialName("disable_web_page_preview")
    val disableWebPagePreview: Boolean,
)

@Serializable
data class TgDeleteMessageRequest(
    @SerialName("chat_id")
    val chatId: Long,
    @SerialName("message_id")
    val messageId: Int,
)

interface TgApi {
    @GET("getMe")
    suspend fun getMe(): TgResponse<TgUser>

    @POST("sendMessage")
    suspend fun sendMessage(@Body data: TgSendMessageRequest): TgResponse<TgMessage>

    @Multipart
    @POST("sendVoice")
    suspend fun sendVoice(
        @Part("chat_id") chatId: RequestBody,
        @Part voice: MultipartBody.Part,
        @Part("caption") caption: RequestBody?,
        @Part("caption_entities") captionEntities: RequestBody?,
        @Part("reply_to_message_id") replyToMessageId: RequestBody?,
        @Part("parse_mode") parseMode: RequestBody?,
    ): TgResponse<TgMessage>

    @POST("editMessageText")
    suspend fun editMessageText(@Body data: TgEditMessageRequest): TgResponse<TgMessage>

    @POST("deleteMessage")
    suspend fun deleteMessage(@Body data: TgDeleteMessageRequest): TgResponse<Boolean>

    @GET("getUpdates")
    suspend fun getUpdates(
        @Query("offset") offset: Int,
        @Query("timeout") timeout: Int,
        @Query("allowed_updates") allowedUpdates: List<String> = listOf("message"),
    ): TgResponse<List<TgUpdate>>

    @POST("deleteWebhook")
    suspend fun deleteWebhook(): TgResponse<Boolean>

    @GET("getFile")
    suspend fun getFile(@Query("file_id") fileId: String): TgResponse<TgFile>

    @Streaming
    @GET
    suspend fun downloadFile(@Url filePath: String): Response<ResponseBody>
}

const val POLL_TIMEOUT_SECONDS = 60

class TelegramBot(botApiUrl: String, botToken: String, private val logger: ILogger, private val scope: CoroutineScope) {
    private val okhttpClient = OkHttpClient.Builder()
        .readTimeout(Duration.ofSeconds((POLL_TIMEOUT_SECONDS + 10).toLong()))
        .build()
    private val fileBaseUrl = "$botApiUrl/file/bot$botToken/"
    private val json = Json {
        ignoreUnknownKeys = true

    }
    private val client = Retrofit.Builder()
        .client(okhttpClient)
        .baseUrl("$botApiUrl/bot$botToken/")
        .addConverterFactory(
            json.asConverterFactory("application/json; charset=UTF8".toMediaType())
        )
        .build()
        .create(TgApi::class.java)
    private var pollTask: Job? = null
    private val commandHandlers: MutableList<suspend (TgMessage) -> Boolean> = mutableListOf()
    private val messageHandlers: MutableList<suspend (TgMessage) -> Unit> = mutableListOf()
    lateinit var me: TgUser
        private set

    fun registerMessageHandler(handler: suspend (TgMessage) -> Unit) {
        messageHandlers.add(handler)
    }

    fun registerMessageHandler(handler: Function1<TgMessage>) {
        messageHandlers.add(handler::apply)
    }

    fun registerCommandHandler(command: String, handler: suspend (TgMessage) -> Unit) {
        val cmdRegex = Regex("^/$command(@${me.username})?(\\s|$)", RegexOption.IGNORE_CASE)
        commandHandlers.add {
            if (cmdRegex.matches(it.effectiveText ?: "")) {
                handler(it)
                return@add true
            } else {
                return@add false
            }
        }
    }

    fun registerCommandHandler(command: String, handler: Function1<TgMessage>) {
        val suspendHandler: suspend (TgMessage) -> Unit = handler::apply
        registerCommandHandler(command, suspendHandler)
    }

    suspend fun init() {
        retriableCall { client.deleteWebhook() }
        me = retriableCall { client.getMe() }
    }

    fun startPolling() {
        if (pollTask != null) {
            throw IllegalStateException("polling already started")
        }
        pollTask = scope.launch {
            var offset = -1
            logger.info("pollTask started")
            while (true) {
                try {
                    val updates = call {
                        client.getUpdates(
                            offset,
                            timeout = POLL_TIMEOUT_SECONDS,
                        )
                    }
                    if (updates.isEmpty()) {
                        continue
                    }
                    offset = updates.last().updateId + 1
                    updates.forEach { update ->
                        if (update.message == null) {
                            return@forEach
                        }
                        for (handler in commandHandlers) {
                            if (handler.invoke(update.message)) {
                                return@forEach
                            }
                        }
                        messageHandlers.forEach {
                            it.invoke(update.message)
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is CancellationException -> break
                        else -> {
                            logger.error(e.message.toString(), e)
                            delay(1000)
                        }
                    }
                }
            }
            logger.info("pollTask finished")
        }
    }

    suspend fun recoverPolling() {
        val task = pollTask
        if (task != null) {
            if (!task.isCompleted) {
                task.cancelAndJoin()
            }
            pollTask = null
        }
        startPolling()
    }

    suspend fun shutdown() {
        pollTask?.cancelAndJoin()
        okhttpClient.dispatcher.executorService.shutdown()
        okhttpClient.connectionPool.evictAll()
    }

    private suspend fun <T> call(f: suspend () -> TgResponse<T>): T {
        try {
            return f().result!!
        } catch (e: HttpException) {
            val resp = e.response() ?: throw e
            // TODO: replace with custom exception class
            throw Exception("Telegram exception: ${resp.errorBody()?.string() ?: "no response body"}")
        }
    }

    private suspend fun <T> retriableCall(f: suspend () -> TgResponse<T>): T {
        val retryConf = config.advanced.connectionRetry
        return withRetry(
            maxAttempts = retryConf.maxAttempts,
            initialDelay = retryConf.initialDelay,
            maxDelay = retryConf.maxDelay,
            retryExceptions = setOf(
                UnknownHostException::class,
                ConnectException::class,
                SocketTimeoutException::class,
                SocketException::class,
                TimeoutException::class,
            )
        ) {
            call(f)
        }
    }

    private suspend fun <T> withRetry(
        maxAttempts: Int = 3,
        initialDelay: Long = 1000L,
        maxDelay: Long = 300000L,
        retryExceptions: Set<KClass<out Exception>> = setOf(Exception::class),
        operation: suspend () -> T
    ): T {
        var attempt = 0
        val infiniteRetries = maxAttempts <= 0

        while (true) {
            try {
                return operation()
            } catch (e: Exception) {
                if (!retryExceptions.contains(e::class)) {
                    logger.error("Not retriable exception", e)
                    throw e
                }
                attempt++

                if (!infiniteRetries && attempt >= maxAttempts) {
                    logger.error("Operation failed after $maxAttempts attempts", e)
                    throw e
                }

                val delay = minOf(initialDelay * (1L shl (attempt - 1)), maxDelay)
                val attemptText = if (infiniteRetries) "attempt $attempt" else "attempt $attempt/$maxAttempts"
                logger.warn("Operation failed ($attemptText), retrying in ${delay / 1000} seconds: ${e.javaClass.canonicalName}: ${e.message}")
                delay(delay)
            }
        }
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        entities: List<TgEntity>? = null,
        replyToMessageId: Int? = null,
        parseMode: String? = null,
        disableWebPagePreview: Boolean = true,
    ): TgMessage = retriableCall {
        client.sendMessage(
            TgSendMessageRequest(
                chatId,
                text,
                entities,
                replyToMessageId,
                parseMode,
                disableWebPagePreview
            )
        )
    }

    fun sendMessageAsync(
        chatId: Long,
        text: String,
        entities: List<TgEntity>? = null,
        replyToMessageId: Int? = null,
        parseMode: String? = null,
        disableWebPagePreview: Boolean = true,
    ) = scope.future { sendMessage(chatId, text, entities, replyToMessageId, parseMode, disableWebPagePreview) }

    // TODO: wtf
    suspend fun sendVoice(
        chatId: Long,
        voice: ByteArray,
        caption: String? = null,
        captionEntities: List<TgEntity>? = null,
        replyToMessageId: Int? = null,
        parseMode: String? = null,
    ): TgMessage = retriableCall {
        val requestVoiceFile = voice.toRequestBody(MultipartBody.FORM, 0, voice.size)
        val currentDateString = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"))
        val requestVoice = MultipartBody.Part.createFormData(
            "voice",
            "voice_$currentDateString.ogg",
            requestVoiceFile
        )
        val chatIdBody = chatId.toString().toRequestBody(MultipartBody.FORM)
        val captionBody = caption?.toRequestBody(MultipartBody.FORM)
        val captionEntitiesBody =
            captionEntities?.let { json.encodeToString(it).toRequestBody(MultipartBody.FORM) }
        val replyToMessageIdBody = replyToMessageId?.toString()?.toRequestBody(MultipartBody.FORM)
        val parseModeBody = parseMode?.toRequestBody(MultipartBody.FORM)
        client.sendVoice(
            chatIdBody,
            requestVoice,
            captionBody,
            captionEntitiesBody,
            replyToMessageIdBody,
            parseModeBody,
        )
    }

    suspend fun editMessageText(
        chatId: Long,
        messageId: Int,
        text: String,
        entities: List<TgEntity>? = null,
        parseMode: String? = null,
        disableWebPagePreview: Boolean = true,
    ) = retriableCall {
        client.editMessageText(
            TgEditMessageRequest(
                chatId,
                messageId,
                text,
                entities,
                parseMode,
                disableWebPagePreview
            )
        )
    }

    fun editMessageTextAsync(
        chatId: Long,
        messageId: Int,
        text: String,
        entities: List<TgEntity>? = null,
        parseMode: String? = null,
        disableWebPagePreview: Boolean = true,
    ) = scope.future {
        editMessageText(chatId, messageId, text, entities, parseMode, disableWebPagePreview)
    }

    suspend fun deleteMessage(chatId: Long, messageId: Int) = retriableCall {
        client.deleteMessage(TgDeleteMessageRequest(chatId, messageId))
    }

    fun deleteMessageAsync(chatId: Long, messageId: Int) = scope.future {
        deleteMessage(chatId, messageId)
    }

    suspend fun downloadFile(fileId: String): Response<ResponseBody> {
        val file = call { client.getFile(fileId) }
        val filePath = file.filePath
        if (filePath == null) {
            // TODO: replace with custom exception class
            throw Exception("File path is null for $fileId")
        }
        return client.downloadFile(fileBaseUrl + filePath)
    }

    fun downloadFileAsync(fileId: String) = scope.future {
        downloadFile(fileId)
    }
}
