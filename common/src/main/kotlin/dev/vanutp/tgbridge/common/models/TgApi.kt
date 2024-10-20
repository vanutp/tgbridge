package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.dataclass.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TgApi {
    @GET("getMe")
    suspend fun getMe(): TgResponse<TgUser>

    @POST("sendMessage")
    suspend fun sendMessage(@Body data: TgSendMessageRequest): TgResponse<TgMessage>

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
}