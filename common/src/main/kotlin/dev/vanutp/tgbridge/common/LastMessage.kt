package dev.vanutp.tgbridge.common

import kotlinx.datetime.Instant

data class LastMessage(
    val id: Int,
    var text: String,
    var date: Instant,
)
