package dev.vanutp.tgbridge.common.modules

interface ITgbridgeModule {
    /**
     * If true, the module may be disabled or enabled during reload
     * depending on [shouldEnable] result. Must be a constant value
     */
    val canBeDisabled: Boolean
        get() = false
    fun shouldEnable() = true

    /**
     * Enabled the module.
     *
     * This method is called after the server is started, right before start message is sent to Telegram.
     * It is also called during plugin reload if the module was previously disabled and [shouldEnable] returns true.
     */
    fun enable()

    /**
     * Disables the module. This method should be implemented if [canBeDisabled] is true
     *
     * This method will be called during plugin reload
     * if [canBeDisabled] is true and [shouldEnable] returns false.
     */
    fun disable() {}
}
