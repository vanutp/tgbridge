package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.FormattingParser
import dev.vanutp.tgbridge.common.PlaceholderAPI
import dev.vanutp.tgbridge.common.Platform
import eu.pb4.placeholders.api.PlaceholderContext
import eu.pb4.placeholders.api.node.TextNode
import eu.pb4.placeholders.api.node.parent.HoverNode
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1
import eu.pb4.placeholders.api.parsers.MarkdownLiteParserV1.MarkdownFormat
import eu.pb4.placeholders.api.parsers.NodeParser
import eu.pb4.placeholders.api.parsers.TagLikeParser
import eu.pb4.placeholders.api.parsers.TextParserV1
import eu.pb4.placeholders.api.parsers.tag.TagRegistry
import eu.pb4.placeholders.api.parsers.tag.TextTag
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent


object FabricPlaceholderAPI: PlaceholderAPI() {

    val EMOTE_FORMAT: TagLikeParser.Format = TagLikeParser.Format.of(':', ':')

    override fun parse(text: String, platform: Platform): Component = parse(text, platform, PlaceholderContext.of((platform as FabricPlatform).server))

    override fun parse(text: String, platform: Platform, context: Any): Component {
        context as PlaceholderContext
        platform as FabricPlatform
//        val registry = TagRegistry.create()
//        TagRegistry.DEFAULT.tags.forEach { registry.register(it) }
//        registry.register(
//            TextTag.enclosing(
//                "spoiler",
//                listOf("hide"),
//                "tgbridge",
//                true,
//                ({ nodes, _, _ -> spoilerFormatting(nodes) })
//            ))
//        registry.register(
//            TextTag.enclosing(
//                "quote",
//                listOf("backtick"),
//                "tgbridge",
//                true,
//                ({ nodes, _, _ -> codeFormatting(nodes) })
//            ))
//        registry.register(
//            TextTag.enclosing(
//                "link",
//                listOf("url"),
//                "tgbridge",
//                true,
//                ({ nodes, _, _ -> linkFormatting(nodes) })
//            ))
        val form = ArrayList<MarkdownFormat>()

//        if (registry.getTag("bold") != null) form.add(MarkdownFormat.BOLD)
//        if (registry.getTag("italic") != null) form.add(MarkdownFormat.ITALIC)
//        if (registry.getTag("underline") != null) form.add(MarkdownFormat.UNDERLINE)
//        if (registry.getTag("strikethrough") != null) form.add(MarkdownFormat.STRIKETHROUGH)
//        if (registry.getTag("spoiler") != null) form.add(MarkdownFormat.SPOILER)
//        if (registry.getTag("quote") != null) form.add(MarkdownFormat.QUOTE)
//        if (registry.getTag("link") != null) form.add(MarkdownFormat.URL)

        form.add(MarkdownFormat.BOLD); form.add(MarkdownFormat.ITALIC)
        form.add(MarkdownFormat.UNDERLINE); form.add(MarkdownFormat.STRIKETHROUGH)
        form.add(MarkdownFormat.SPOILER); form.add(MarkdownFormat.QUOTE)
        form.add(MarkdownFormat.URL)

        val builder = NodeParser.builder().markdown(
                FabricPlaceholderAPI::spoilerFormatting,
                FabricPlaceholderAPI::codeFormatting,
                FabricPlaceholderAPI::linkFormatting,
                form
            ).globalPlaceholders()
//        val emotes = getEmotes(context)
//
//        if (!emotes.isEmpty()) {
//            builder.placeholders(EMOTE_FORMAT, emotes::get);
//        }

        val output = platform.minecraftToAdventure(NodeParser.merge(
            (builder.build()),
            /*TextParserV1.DEFAULT,*/
        )
            .parseNode(text).toText(context)
        )
        return output
    }

    fun spoilerFormatting(textNodes: Array<TextNode>): TextNode =
        TextNode.convert (
            FabricPlatform.instance?.adventureToMinecraft (
                FormattingParser.getAsSpoilerComponent (
                    FabricPlatform.instance?.minecraftToAdventure (
                        TextNode.asSingle(textNodes.asList()).toText()
                    ) as TextComponent
                )
            )
        )
    fun codeFormatting(textNodes: Array<TextNode>): TextNode =
        TextNode.convert (
            FabricPlatform.instance?.adventureToMinecraft (
                FormattingParser.getAsCodeComponent (
                    FabricPlatform.instance?.minecraftToAdventure (
                        TextNode.asSingle(textNodes.asList()).toText()
                    ) as TextComponent
                )
            )
        )
    fun linkFormatting(textNodes: Array<TextNode>, url: TextNode): TextNode =
        TextNode.convert (
            FabricPlatform.instance?.adventureToMinecraft (
                FormattingParser.getAsLinkComponent (
                    FabricPlatform.instance?.minecraftToAdventure (
                        TextNode.asSingle(textNodes.asList()).toText()
                    ) as TextComponent, url.toText().literalString.toString()
                )
            )
        )
}