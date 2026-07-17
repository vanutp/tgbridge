package dev.vanutp.tgbridge.fabric

import kotlin.reflect.KClass

data class FabricEventWrapper(
    val type: KClass<*>,
    val args: List<Any?>,
)
