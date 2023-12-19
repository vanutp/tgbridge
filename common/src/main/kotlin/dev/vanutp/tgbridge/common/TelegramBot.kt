package dev.vanutp.tgbridge.common

import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.time.Duration
import kotlin.coroutines.coroutineContext

data class TgUser(
    val id: Long,
    @SerializedName("first_name")
    val firstName: String,
    @SerializedName("last_name")
    val lastName: String?,
    val username: String?,
)

data class TgChat(
    val id: Long,
    val title: String = "",
    val username: String? = null,
)

class TgAny

data class TgMessage(
    val chat: TgChat,
    @SerializedName("message_id")
    val messageId: Int,
    val from: TgUser? = null,
    @SerializedName("sender_chat")
    val senderChat: TgChat? = null,
    @SerializedName("forward_from")
    val forwardFrom: TgUser? = null,
    @SerializedName("forward_from_chat")
    val forwardFromChat: TgChat? = null,
    @SerializedName("reply_to_message")
    val replyToMessage: TgMessage? = null,
    @SerializedName("message_thread_id")
    val messageThreadId: Int? = null,
    val text: String? = null,
    val caption: String? = null,
    val animation: TgAny? = null,
    val photo: List<TgAny>? = null,
    val audio: TgAny? = null,
    val document: TgAny? = null,
    val sticker: TgAny? = null,
    val video: TgAny? = null,
    @SerializedName("video_note")
    val videoNote: TgAny? = null,
    val voice: TgAny? = null,
    val poll: TgAny? = null,
) {
    val senderName: String
        get() {
            return from?.let { _ ->
                (from.firstName + " " + (from.lastName ?: "")).trim()
            } ?: senderChat?.title ?: ""
        }
    val effectiveText: String
        get() {
            return text ?: caption ?: ""
        }
}

data class TgUpdate(
    @SerializedName("update_id")
    val updateId: Int,
    val message: TgMessage? = null,
)

data class TgResponse<T>(
    val ok: Boolean,
    val result: T?,
    val description: String?,
)

data class TgSendMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("text")
    val text: String,
    @SerializedName("reply_to_message_id")
    val replyToMessageId: Int? = null,
    @SerializedName("parse_mode")
    val parseMode: String = "HTML",
    @SerializedName("disable_web_page_preview")
    val disableWebPagePreview: Boolean = true,
)

data class TgEditMessageRequest(
    @SerializedName("chat_id")
    val chatId: Long,
    @SerializedName("message_id")
    val messageId: Int,
    @SerializedName("text")
    val text: String,
    @SerializedName("parse_mode")
    val parseMode: String = "HTML",
    @SerializedName("disable_web_page_preview")
    val disableWebPagePreview: Boolean = true,
)

interface TgApi {
    @POST("sendMessage")
    suspend fun sendMessage(@Body data: TgSendMessageRequest): TgResponse<TgMessage>

    @POST("editMessageText")
    suspend fun editMessageText(@Body data: TgEditMessageRequest): TgResponse<TgMessage>

    @GET("getUpdates")
    suspend fun getUpdates(
        @Query("offset") offset: Int,
        @Query("timeout") timeout: Int,
        @Query("allowed_updates") allowedUpdates: List<String> = listOf("message"),
    ): TgResponse<List<TgUpdate>>
}

const val POLL_TIMEOUT_SECONDS = 60

class TelegramBot(private val config: TBConfig, private val logger: AbstractLogger) {
    private val client = Retrofit.Builder()
        .client(
            OkHttpClient.Builder()
                .readTimeout(Duration.ofSeconds((POLL_TIMEOUT_SECONDS + 10).toLong()))
                .build()
        )
        .baseUrl("https://api.telegram.org/bot${config.botToken}/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(TgApi::class.java)
    private var pollTask: Job? = null
    private val messageHandlers: MutableList<suspend (TgMessage) -> Unit> = mutableListOf()


    fun registerMessageHandler(handler: suspend (TgMessage) -> Unit) {
        messageHandlers.add(handler)
    }

    suspend fun startPolling() {
        if (pollTask != null) {
            throw Exception("polling already started")
        }
        pollTask = CoroutineScope(coroutineContext).launch {
            var offset = -1
            while (true) {
                try {
                    client.getUpdates(
                        offset,
                        timeout = POLL_TIMEOUT_SECONDS,
                    ).result?.let { updates ->
                        if (updates.isEmpty()) {
                            return@let
                        }
                        offset = updates.last().updateId + 1
                        updates.forEach { update ->
                            update.message?.let {
                                messageHandlers.forEach {
                                    it.invoke(update.message)
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    when (e) {
                        is CancellationException -> break
                        else -> {
                            logger.error(e.message.toString())
                            delay(1000)
                        }
                    }
                }
            }
        }
    }

    suspend fun stopPolling() {
        pollTask?.cancelAndJoin()
    }

    private suspend fun <T> call(f: suspend () -> TgResponse<T>): T {
        try {
            return f().result!!
        } catch (e: HttpException) {
            val resp = e.response() ?: throw e
            throw Exception("Telegram exception: ${resp.errorBody()?.string() ?: "no response body"}")
        }
    }

    suspend fun sendMessage(
        chatId: Long,
        text: String,
        replyToMessageId: Int? = null,
        parseMode: String = "HTML",
        disableWebPagePreview: Boolean = true,
    ): TgMessage = call {
        client.sendMessage(TgSendMessageRequest(chatId, text, replyToMessageId, parseMode, disableWebPagePreview))
    }

    suspend fun editMessageText(
        chatId: Long,
        messageId: Int,
        text: String,
        parseMode: String = "HTML",
        disableWebPagePreview: Boolean = true,
    ): TgMessage = call {
        client.editMessageText(TgEditMessageRequest(chatId, messageId, text, parseMode, disableWebPagePreview))
    }
}
