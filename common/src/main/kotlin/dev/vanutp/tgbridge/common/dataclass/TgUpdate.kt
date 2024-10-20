package dev.vanutp.tgbridge.common.dataclass

import com.google.gson.annotations.SerializedName

data class TgUpdate(
    @SerializedName("update_id")
    val updateId: Int,
    val message: TgMessage? = null,
)
