package dev.vanutp.tgbridge.common.models

import dev.vanutp.tgbridge.common.dataclass.TgAny
import dev.vanutp.tgbridge.common.dataclass.TgPoll

interface TgMessageMedia {
    val animation: TgAny?
    val photo: List<TgAny>?
    val audio: TgAny?
    val document: TgAny?
    val sticker: TgAny?
    val video: TgAny?
    val videoNote: TgAny?
    val voice: TgAny?
    val poll: TgPoll?
}