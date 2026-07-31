package com.android.purebilibili.feature.dynamic.components

import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import com.android.purebilibili.core.util.BilibiliNavigationTarget
import com.android.purebilibili.core.util.BilibiliNavigationTargetParser
import com.android.purebilibili.data.model.response.DynamicDesc
import com.android.purebilibili.data.model.response.EmojiInfo
import com.android.purebilibili.data.model.response.RichTextNode

internal const val DYNAMIC_RICH_TEXT_URL_TAG = "URL"
internal const val DYNAMIC_RICH_TEXT_USER_TAG = "USER"

internal enum class DynamicRichTextOpenMode {
    IN_APP,
    EXTERNAL
}

internal data class DynamicRichTextBuildResult(
    val annotatedString: AnnotatedString,
    val emojiUrlById: Map<String, String>
)

private val DYNAMIC_RICH_TEXT_URL_PATTERN =
    """((https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])""".toRegex()
private val DYNAMIC_EMOTE_TOKEN_PATTERN = """\[[^\[\]]+\]""".toRegex()
private val DYNAMIC_IMAGE_PLACEHOLDERS = setOf("[图片]", "【图片】")

internal fun buildDynamicRichTextAnnotatedString(
    desc: DynamicDesc,
    primaryColor: Color,
    textColor: Color,
    extraEmoteUrlMap: Map<String, String> = emptyMap()
): AnnotatedString {
    return buildDynamicRichText(desc, primaryColor, textColor, extraEmoteUrlMap).annotatedString
}

internal fun buildDynamicRichText(
    desc: DynamicDesc,
    primaryColor: Color,
    textColor: Color,
    extraEmoteUrlMap: Map<String, String> = emptyMap()
): DynamicRichTextBuildResult {
    val nodeEmoteMap = collectDynamicEmojiUrlMap(desc.rich_text_nodes)
    val emoteUrlMap = buildMap {
        putAll(extraEmoteUrlMap)
        putAll(nodeEmoteMap)
    }
    val usedEmojiIds = linkedMapOf<String, String>()
    val annotated = buildAnnotatedString {
        if (shouldUseDynamicRichTextNodes(desc)) {
            desc.rich_text_nodes.forEach { node ->
                appendDynamicRichTextNode(
                    node = node,
                    primaryColor = primaryColor,
                    textColor = textColor,
                    emoteUrlMap = emoteUrlMap,
                    usedEmojiIds = usedEmojiIds
                )
            }
        } else {
            appendDynamicRichTextExpandableText(
                text = desc.text,
                primaryColor = primaryColor,
                textColor = textColor,
                emoteUrlMap = emoteUrlMap,
                usedEmojiIds = usedEmojiIds
            )
        }
    }
    return DynamicRichTextBuildResult(
        annotatedString = annotated,
        emojiUrlById = usedEmojiIds
    )
}

/**
 * Prefer structured nodes when they can render richer content (especially emoji images).
 * Fall back to plain text only when nodes are clearly truncated and have no emoji.
 */
internal fun shouldUseDynamicRichTextNodes(desc: DynamicDesc): Boolean {
    if (desc.rich_text_nodes.isEmpty()) return false
    if (desc.rich_text_nodes.any(::isRenderableDynamicEmojiNode)) return true
    if (desc.text.isBlank()) return true
    return resolveDynamicRichTextNodeDisplayText(desc.rich_text_nodes)
        .length >= desc.text.length
}

internal fun collectDynamicEmojiUrlMap(nodes: List<RichTextNode>): Map<String, String> {
    if (nodes.isEmpty()) return emptyMap()
    val result = linkedMapOf<String, String>()
    nodes.forEach { node ->
        val iconUrl = resolveDynamicEmojiIconUrl(node.emoji) ?: return@forEach
        val tokens = listOf(
            node.text,
            node.orig_text,
            node.emoji?.text.orEmpty()
        ).map { it.trim() }.filter { it.isNotEmpty() }
        tokens.forEach { token ->
            result.putIfAbsent(token, iconUrl)
        }
    }
    return result
}

internal fun resolveDynamicEmojiIconUrl(emoji: EmojiInfo?): String? {
    if (emoji == null) return null
    val raw = sequenceOf(
        emoji.icon_url,
        emoji.webp_url,
        emoji.gif_url
    ).map { it.trim() }.firstOrNull { it.isNotEmpty() } ?: return null
    return normalizeDynamicImageUrl(raw)
}

internal fun isRenderableDynamicEmojiNode(node: RichTextNode): Boolean {
    val nodeType = node.type.trim().removePrefix("RICH_TEXT_NODE_TYPE_")
    if (!nodeType.equals("EMOJI", ignoreCase = true)) return false
    return resolveDynamicEmojiIconUrl(node.emoji) != null
}

internal fun resolveDynamicRichTextNodeDisplayText(nodes: List<RichTextNode>): String {
    return nodes.joinToString(separator = "") { node ->
        resolveDynamicRichTextNodeToken(node)
    }
}

internal fun resolveDynamicRichTextNodeToken(node: RichTextNode): String {
    return when {
        node.text.isNotBlank() -> node.text
        node.orig_text.isNotBlank() -> node.orig_text
        node.emoji?.text?.isNotBlank() == true -> node.emoji.text
        else -> ""
    }
}

internal fun resolveDynamicDescForImages(
    desc: DynamicDesc,
    hasImages: Boolean
): DynamicDesc {
    if (!hasImages) return desc
    return desc.copy(
        text = stripDynamicImagePlaceholders(desc.text),
        rich_text_nodes = desc.rich_text_nodes.filterNot { node ->
            isDynamicStandaloneImagePlaceholder(resolveDynamicRichTextNodeToken(node))
        }.map { node ->
            node.copy(
                text = stripDynamicImagePlaceholders(node.text),
                orig_text = stripDynamicImagePlaceholders(node.orig_text)
            )
        }.filterNot { node ->
            resolveDynamicRichTextNodeToken(node).isBlank() &&
                node.emoji == null &&
                node.jump_url.isNullOrBlank() &&
                node.rid.isNullOrBlank()
        }
    )
}

internal fun resolveDynamicOpusSummaryDescForImages(
    text: String,
    richTextNodes: List<RichTextNode>,
    hasImages: Boolean
): DynamicDesc? {
    val desc = resolveDynamicDescForImages(
        desc = DynamicDesc(
            text = text,
            rich_text_nodes = richTextNodes
        ),
        hasImages = hasImages
    )
    return desc.takeIf(::shouldRenderDynamicRichText)
}

/**
 * Prefer the description that can render emoji images when both desc and opus summary exist.
 */
internal fun resolvePreferredDynamicDesc(
    primary: DynamicDesc?,
    fallback: DynamicDesc?
): DynamicDesc? {
    if (primary == null) return fallback
    if (fallback == null) return primary
    val primaryHasEmoji = primary.rich_text_nodes.any(::isRenderableDynamicEmojiNode)
    val fallbackHasEmoji = fallback.rich_text_nodes.any(::isRenderableDynamicEmojiNode)
    return when {
        primaryHasEmoji -> primary
        fallbackHasEmoji -> fallback
        shouldUseDynamicRichTextNodes(primary) -> primary
        shouldUseDynamicRichTextNodes(fallback) -> fallback
        primary.text.isNotBlank() -> primary
        else -> fallback
    }
}

internal fun shouldRenderDynamicRichText(desc: DynamicDesc?): Boolean {
    if (desc == null) return false
    if (desc.text.isNotBlank()) return true
    return desc.rich_text_nodes.any { node ->
        val token = resolveDynamicRichTextNodeToken(node)
        token.isNotBlank() && !isDynamicStandaloneImagePlaceholder(token)
    }
}

private fun isDynamicStandaloneImagePlaceholder(text: String): Boolean {
    return text.trim() in DYNAMIC_IMAGE_PLACEHOLDERS
}

private fun stripDynamicImagePlaceholders(text: String): String {
    if (text.isBlank()) return text
    if (!DYNAMIC_IMAGE_PLACEHOLDERS.any { placeholder -> text.contains(placeholder) }) {
        return text
    }
    var sanitized = text
    // B 站图片动态会把真实图片另外放在媒体区，正文里的占位符不应再重复显示。
    DYNAMIC_IMAGE_PLACEHOLDERS.forEach { placeholder ->
        sanitized = sanitized.replace(placeholder, "")
    }
    return sanitized
        .lines()
        .map { line -> line.trimEnd() }
        .filterNot { line -> line.isBlank() }
        .joinToString(separator = "\n")
}

internal fun resolveDynamicRichTextOpenMode(
    rawUrl: String
): DynamicRichTextOpenMode? {
    val url = rawUrl.trim()
    if (url.isBlank()) return null

    if (BilibiliNavigationTargetParser.parse(url) != null || isDynamicRichTextInAppHost(url)) {
        return DynamicRichTextOpenMode.IN_APP
    }
    return DynamicRichTextOpenMode.EXTERNAL
}

private fun AnnotatedString.Builder.appendDynamicRichTextNode(
    node: RichTextNode,
    primaryColor: Color,
    textColor: Color,
    emoteUrlMap: Map<String, String>,
    usedEmojiIds: MutableMap<String, String>
) {
    val nodeType = node.type.trim().removePrefix("RICH_TEXT_NODE_TYPE_")
    val displayToken = resolveDynamicRichTextNodeToken(node)
    when {
        nodeType.equals("EMOJI", ignoreCase = true) -> {
            val iconUrl = resolveDynamicEmojiIconUrl(node.emoji)
                ?: emoteUrlMap[displayToken]
            if (!iconUrl.isNullOrBlank() && displayToken.isNotBlank()) {
                usedEmojiIds[displayToken] = iconUrl
                appendInlineContent(id = displayToken, alternateText = displayToken)
            } else if (displayToken.isNotBlank()) {
                withStyle(SpanStyle(color = textColor)) {
                    append(displayToken)
                }
            }
        }

        shouldRenderDynamicRichTextLink(nodeType, node) -> {
            appendDynamicRichTextLink(
                displayText = displayToken,
                targetUrl = resolveDynamicRichTextLinkTarget(node),
                primaryColor = primaryColor
            )
        }

        nodeType.equals("AT", ignoreCase = true) -> {
            appendDynamicRichTextAtMention(
                node = node,
                primaryColor = primaryColor
            )
        }

        nodeType.equals("TOPIC", ignoreCase = true) -> {
            withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
                append(displayToken)
            }
        }

        else -> {
            appendDynamicRichTextExpandableText(
                text = displayToken,
                primaryColor = primaryColor,
                textColor = textColor,
                emoteUrlMap = emoteUrlMap,
                usedEmojiIds = usedEmojiIds
            )
        }
    }
}

internal fun resolveDynamicRichTextUserMid(node: RichTextNode): Long? {
    node.rid
        ?.trim()
        ?.toLongOrNull()
        ?.takeIf { it > 0L }
        ?.let { return it }

    // Space feeds often put the mid only on jump_url (//space.bilibili.com/{mid}).
    when (val target = BilibiliNavigationTargetParser.parse(node.jump_url.orEmpty())) {
        is BilibiliNavigationTarget.Space -> return target.mid.takeIf { it > 0L }
        else -> Unit
    }
    return null
}

private fun AnnotatedString.Builder.appendDynamicRichTextAtMention(
    node: RichTextNode,
    primaryColor: Color
) {
    val mid = resolveDynamicRichTextUserMid(node)
    if (mid != null) {
        pushStringAnnotation(tag = DYNAMIC_RICH_TEXT_USER_TAG, annotation = mid.toString())
    }
    withStyle(SpanStyle(color = primaryColor, fontWeight = FontWeight.Medium)) {
        append(resolveDynamicRichTextNodeToken(node))
    }
    if (mid != null) {
        pop()
    }
}

private fun shouldRenderDynamicRichTextLink(
    nodeType: String,
    node: RichTextNode
): Boolean {
    val normalized = nodeType.uppercase()
    if (normalized in setOf("AT", "TOPIC", "EMOJI")) return false
    if (normalized in setOf("WEB", "LINK", "URL")) return true
    val display = resolveDynamicRichTextNodeToken(node)
    return !resolveDynamicRichTextLinkTarget(node).isNullOrBlank() &&
        DYNAMIC_RICH_TEXT_URL_PATTERN.containsMatchIn(display)
}

private fun resolveDynamicRichTextLinkTarget(node: RichTextNode): String? {
    normalizeDynamicRichTextUrl(node.jump_url)?.let { return it }
    return DYNAMIC_RICH_TEXT_URL_PATTERN.find(resolveDynamicRichTextNodeToken(node))?.value
}

/**
 * Expand known emote shortcodes and plain URLs inside free text.
 */
private fun AnnotatedString.Builder.appendDynamicRichTextExpandableText(
    text: String,
    primaryColor: Color,
    textColor: Color,
    emoteUrlMap: Map<String, String>,
    usedEmojiIds: MutableMap<String, String>
) {
    if (text.isEmpty()) return
    if (emoteUrlMap.isEmpty()) {
        withStyle(SpanStyle(color = textColor)) {
            appendDynamicRichTextPlainText(
                text = text,
                primaryColor = primaryColor
            )
        }
        return
    }

    var lastIndex = 0
    DYNAMIC_EMOTE_TOKEN_PATTERN.findAll(text).forEach { match ->
        if (match.range.first > lastIndex) {
            withStyle(SpanStyle(color = textColor)) {
                appendDynamicRichTextPlainText(
                    text = text.substring(lastIndex, match.range.first),
                    primaryColor = primaryColor
                )
            }
        }
        val token = match.value
        val iconUrl = emoteUrlMap[token]
        if (!iconUrl.isNullOrBlank()) {
            usedEmojiIds[token] = iconUrl
            appendInlineContent(id = token, alternateText = token)
        } else {
            withStyle(SpanStyle(color = textColor)) {
                append(token)
            }
        }
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) {
        withStyle(SpanStyle(color = textColor)) {
            appendDynamicRichTextPlainText(
                text = text.substring(lastIndex),
                primaryColor = primaryColor
            )
        }
    }
}

private fun AnnotatedString.Builder.appendDynamicRichTextPlainText(
    text: String,
    primaryColor: Color
) {
    var lastIndex = 0
    DYNAMIC_RICH_TEXT_URL_PATTERN.findAll(text).forEach { match ->
        if (match.range.first > lastIndex) {
            append(text.substring(lastIndex, match.range.first))
        }
        appendDynamicRichTextLink(
            displayText = match.value,
            targetUrl = match.value,
            primaryColor = primaryColor
        )
        lastIndex = match.range.last + 1
    }
    if (lastIndex < text.length) {
        append(text.substring(lastIndex))
    }
}

private fun AnnotatedString.Builder.appendDynamicRichTextLink(
    displayText: String,
    targetUrl: String?,
    primaryColor: Color
) {
    val resolvedUrl = targetUrl?.trim().takeUnless { it.isNullOrEmpty() } ?: displayText
    pushStringAnnotation(tag = DYNAMIC_RICH_TEXT_URL_TAG, annotation = resolvedUrl)
    withStyle(
        SpanStyle(
            color = primaryColor,
            fontWeight = FontWeight.Medium,
            textDecoration = TextDecoration.Underline
        )
    ) {
        append(displayText)
    }
    pop()
}

private fun normalizeDynamicRichTextUrl(rawUrl: String?): String? {
    val url = rawUrl?.trim().orEmpty()
    if (url.isBlank()) return null
    return when {
        url.startsWith("//") -> "https:$url"
        else -> url
    }
}

private fun normalizeDynamicImageUrl(rawUrl: String): String {
    return when {
        rawUrl.startsWith("//") -> "https:$rawUrl"
        rawUrl.startsWith("http://") -> rawUrl.replaceFirst("http://", "https://")
        else -> rawUrl
    }
}

private fun isDynamicRichTextInAppHost(url: String): Boolean {
    val normalized = normalizeDynamicRichTextUrl(url) ?: return false
    val host = runCatching { java.net.URI(normalized) }
        .getOrNull()
        ?.host
        ?.lowercase()
        .orEmpty()
    return host.contains("b23.tv") || host.contains("bilibili.com")
}
