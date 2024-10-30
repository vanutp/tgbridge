package dev.vanutp.tgbridge.common.integration

import dev.vanutp.tgbridge.common.Platform
import net.kyori.adventure.text.Component


abstract class PlaceholderAPI {

    abstract fun parse(text: String, platform: Platform): Component
    abstract fun parse(text: String, platform: Platform, context: Any): Component

}