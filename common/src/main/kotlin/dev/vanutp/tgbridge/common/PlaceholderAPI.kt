package dev.vanutp.tgbridge.common

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent


abstract class PlaceholderAPI {

    abstract fun parse(text: String, platform: Platform): Component
    abstract fun parse(text: String, platform: Platform, context: Any): Component

}