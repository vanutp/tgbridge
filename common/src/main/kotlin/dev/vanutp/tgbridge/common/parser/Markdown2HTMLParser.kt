package dev.vanutp.tgbridge.common.parser

import java.util.*

object Markdown2HTMLParser {

    private val textLinkAllTextRegex = Regex("((.*?)(?<!\\\\)\\[(.*?)(?<!\\\\)]\\((.*?)(?<!\\\\)\\))*?(.*?)")
    private val textLinkRegex = Regex("(?<!\\\\)\\[(.*?)(?<!\\\\)]\\((.*?)(?<!\\\\)\\)")
    private val textLinkTextRegex = Regex("(?<!\\\\)\\[(.*?)(?<!\\\\)]\\(")
    private val textLinkURLRegex = Regex("(?<!\\\\)]\\((.*?)(?<!\\\\)\\)")

    fun parse(
        markdown: String,
        tagMap: Map<String, String> = mapOf(
            "**" to "b",
            "*" to "i",
            "__" to "u",
            "~~" to "s",
            "||" to "tg-spoiler",
            "```" to "pre",
            "`" to "code",
        ),
        textLinkAllTextRegex: Regex = this.textLinkAllTextRegex,
        textLinkRegex: Regex = this.textLinkRegex,
        textLinkTextRegex: Regex = this.textLinkTextRegex,
        textLinkURLRegex: Regex = this.textLinkURLRegex,
    ): String {
        val codeOrPreBlocksMap = mutableMapOf<Int, Int>()
        val codeOrPreList = listOf("```", "`")
        var codeOrPreBlockOpenedAt = 0

        val stack = Stack<String>()
        val result = StringBuilder()
        var i = 0

        while (i < markdown.length) {
            when {
                markdown[i] == '\\' && i + 1 < markdown.length -> {
                    result.append(markdown[i + 1])
                    i += 2
                }
                else -> {
                    var matched = false
                    for ((key, tag) in tagMap.entries.sortedByDescending { it.key.length }) {
                        if (markdown.startsWith(key, i)) {
                            if (stack.isNotEmpty() && codeOrPreList.contains(stack.last()) && stack.last() != key) result.append(key)
                            else if (stack.isNotEmpty() && stack.last() == key) {
                                result.append("</${tag}>")
                                stack.removeAt(stack.lastIndex)
                                if (codeOrPreList.contains(key)) {
                                    stack.forEach { result.append("<${tagMap[it]}>") }
                                    codeOrPreBlocksMap[codeOrPreBlockOpenedAt] = result.length
                                }
                            } else if (stack.isEmpty() || (stack.last() != key && !stack.contains(key))) {
                                if (codeOrPreList.contains(key)) {
                                    stack.asReversed()
                                        .forEach { result.append("</${tagMap[it]}>") }
                                    codeOrPreBlockOpenedAt = result.length
                                }
                                result.append("<${tag}>")
                                stack.add(key)
                            } else {
                                result.append(key)
                            }
                            i += key.length
                            matched = true
                            break
                        }
                    }
                    if (!matched) {
                        result.append(markdown[i])
                        i++
                    }
                }
            }
        }
        while (stack.isNotEmpty()) {
            val key = stack.removeAt(stack.lastIndex)
            val tag = tagMap[key] ?: ""
            result.append("</${tag}>")
            if (codeOrPreList.contains(key)) stack.clear()
        }
        var parsed = result.toString()
        if (parsed.matches(textLinkAllTextRegex)) parsed =
            textLinkRegex.replace(result.toString()) { matched ->
                var isInCodeOrPreBlock = false
                codeOrPreBlocksMap.forEach { (start, end) -> if (matched.range.first>start && matched.range.last<end) isInCodeOrPreBlock = true }
                if (isInCodeOrPreBlock) return@replace matched.value
                val linkMatched = textLinkURLRegex.find(matched.value) ?: return@replace matched.value
                val displayTextMatched = textLinkTextRegex.find(matched.value) ?: return@replace matched.value
                "<a href=\"${
                    linkMatched.value.substring(2, linkMatched.value.length - 1)
                }\">${
                    displayTextMatched.value.substring(1, displayTextMatched.value.length - 2)
                }</a>"
            }
        return parsed
    }

}