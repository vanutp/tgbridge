package dev.vanutp.tgbridge.common.modules

interface ITgbridgeModule {
    val canBeDisabled: Boolean
        get() = false
    fun shouldEnable() = true
    fun enable()
    fun disable() {}
}
