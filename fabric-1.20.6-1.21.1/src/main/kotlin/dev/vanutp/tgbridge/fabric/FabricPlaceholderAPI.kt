package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.PlaceholderAPI
import dev.vanutp.tgbridge.common.Platform
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.Placeholders
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1
import eu.pb4.placeholders.api.parsers.NodeParser
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.minecraft.server.MinecraftServer

object FabricPlaceholderAPI: PlaceholderAPI() {

    override fun parse(text: String, platform: Platform): Component {
        return (platform as FabricPlatform).minecraftToAdventure(NodeParser.merge(MarkdownLiteParserV1.ALL, Placeholders.DEFAULT_PLACEHOLDER_PARSER).parseNode(text).toText(
            PlaceholderContext.of(platform.server)
        ))
    }
}