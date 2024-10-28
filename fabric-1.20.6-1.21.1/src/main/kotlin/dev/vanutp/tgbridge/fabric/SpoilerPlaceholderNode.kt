package dev.vanutp.tgbridge.fabric

import dev.vanutp.tgbridge.common.ConfigManager
import dev.vanutp.tgbridge.common.FormattingParser
import eu.pb4.placeholders.api.ParserContext
import eu.pb4.placeholders.api.node.TextNode
import eu.pb4.placeholders.api.node.parent.ParentNode
import eu.pb4.placeholders.api.node.parent.ParentTextNode
import net.minecraft.text.MutableText
import net.minecraft.text.Text

class SpoilerPlaceholderNode(children: Array<TextNode>): ParentNode() {

    override fun applyFormatting(out: MutableText, context: ParserContext): Text {
        val lang = ConfigManager.lang
        val obj = FabricPlatform.instance?.adventureToMinecraft(FormattingParser.getAsSpoilerComponent(lang.minecraft.messageFormatting.spoilerReplaceWith?:""))
        return obj as Text
    }

    public override fun copyWith(children: Array<TextNode>): ParentTextNode = SpoilerPlaceholderNode(this.children)
}