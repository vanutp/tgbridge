package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.PlaceholderAPI
import dev.vanutp.tgbridge.common.Platform
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1.MarkdownFormat
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.tag.TagRegistry
import eu.pb4.placeholders.api.parsers.tag.TextTag
import net.kyori.adventure.text.Component


object FabricPlaceholderAPI: PlaceholderAPI() {

    override fun parse(text: String, platform: Platform): Component = parse(text, platform, PlaceholderContext.of((platform as FabricPlatform).server))

    override fun parse(text: String, platform: Platform, context: Any): Component {
        context as PlaceholderContext
        platform as FabricPlatform
        val registry = TagRegistry.create()
        TagRegistry.DEFAULT.tags.forEach { registry.register(it) }
        registry.register(
            TextTag.enclosing(
                "spoiler",
                listOf("hide"),
                "tgbridge",
                true,
                ({ nodes, _, _ -> SpoilerPlaceholderNode(nodes) })
            ))
        val form = ArrayList<MarkdownFormat>()

        if (registry.getTag("bold") != null) form.add(MarkdownFormat.BOLD)
        if (registry.getTag("italic") != null) form.add(MarkdownFormat.ITALIC)
        if (registry.getTag("underline") != null) form.add(MarkdownFormat.UNDERLINE)
        if (registry.getTag("strikethrough") != null) form.add(MarkdownFormat.STRIKETHROUGH)
        if (registry.getTag("spoiler") != null) form.add(MarkdownFormat.SPOILER)
        if (registry.getTag("link") != null) form.add(MarkdownFormat.URL)

        val output = platform.minecraftToAdventure(NodeParser.builder()
            .markdown(::SpoilerPlaceholderNode, MarkdownLiteParserV1::defaultQuoteFormatting, MarkdownLiteParserV1::defaultUrlFormatting, form)
            .globalPlaceholders()
            .build()
            .parseNode(text).toText(context))
        return output
    }
}