package com.android.purebilibili.data.repository

import com.android.purebilibili.data.model.response.DynamicContentModule
import com.android.purebilibili.data.model.response.DynamicItem
import com.android.purebilibili.data.model.response.DynamicMajor
import com.android.purebilibili.data.model.response.DynamicModules
import com.android.purebilibili.data.model.response.OpusContentBlock
import com.android.purebilibili.data.model.response.OpusMajor
import com.android.purebilibili.data.model.response.OpusPic
import com.android.purebilibili.feature.article.ArticleContentBlock
import com.android.purebilibili.feature.article.scoreOpusContentBlocks

internal fun shouldFetchStandardDetailForPlainTextDynamic(item: DynamicItem): Boolean {
    val content = item.modules.module_dynamic ?: return false
    return item.type == "DYNAMIC_TYPE_WORD" && content.desc?.text?.isNotBlank() == true
}

internal fun mergeDynamicDetailWithLongerDesc(
    desktopItem: DynamicItem,
    standardItem: DynamicItem,
): DynamicItem {
    val desktopContent = desktopItem.modules.module_dynamic ?: return desktopItem
    val standardDesc = standardItem.modules.module_dynamic?.desc ?: return desktopItem
    val desktopDesc = desktopContent.desc
    if (standardDesc.text.length <= desktopDesc?.text?.length ?: 0) return desktopItem

    return desktopItem.copy(
        modules = desktopItem.modules.copy(
            module_dynamic = desktopContent.copy(desc = standardDesc)
        )
    )
}

internal fun isFoldedDynamicLinkPlaceholder(text: String): Boolean {
    return text.trim() == "网页链接"
}

internal fun shouldFallbackForDynamicDetail(item: DynamicItem): Boolean {
    val modules = item.modules
    val descText = modules.module_dynamic?.desc?.text.orEmpty()
    val hasDescText = descText.isNotBlank() && !isFoldedDynamicLinkPlaceholder(descText)
    val hasMajorContent = modules.module_dynamic?.major != null
    val hasOrig = item.orig != null

    // 可渲染内容都没有时，说明解析结构可能不兼容，应该走 fallback
    val hasRenderableContent = hasDescText || hasMajorContent || hasOrig
    return !hasRenderableContent
}

internal fun shouldFetchDynamicDetailByRid(
    current: DynamicItem?,
    rid: String
): Boolean {
    val cleanedRid = rid.trim()
    if (cleanedRid.isEmpty()) return false
    return current == null || shouldFallbackForDynamicDetail(current)
}

internal fun resolvePreferredDynamicDetailItem(
    candidates: List<DynamicItem>
): DynamicItem? {
    val renderable = candidates.filter { !shouldFallbackForDynamicDetail(it) }
    if (renderable.isEmpty()) return candidates.firstOrNull()
    return renderable.maxByOrNull(::resolveDynamicOpusContentScore) ?: renderable.first()
}

internal fun resolveDynamicOpusContentScore(item: DynamicItem): Int {
    val opus = item.modules.module_dynamic?.major?.opus ?: return 0
    val blockScore = scoreOpusContentBlocks(opus.contentBlocks)
    if (blockScore > 0) {
        return 100_000 + blockScore
    }
    return opus.pics.size + (opus.summary?.text?.length ?: 0)
}

internal fun mergeRicherOpusDetailContent(
    base: DynamicItem,
    candidates: List<DynamicItem>
): DynamicItem {
    val richest = candidates.maxByOrNull(::resolveDynamicOpusContentScore) ?: return base
    if (resolveDynamicOpusContentScore(richest) <= resolveDynamicOpusContentScore(base)) {
        return base
    }
    val richestOpus = richest.modules.module_dynamic?.major?.opus ?: return base
    val baseContent = base.modules.module_dynamic ?: return richest
    val baseMajor = baseContent.major
    val baseOpus = baseMajor?.opus
    val mergedOpus = OpusMajor(
        jump_url = baseOpus?.jump_url?.takeIf { it.isNotBlank() } ?: richestOpus.jump_url,
        pics = if (richestOpus.pics.size >= (baseOpus?.pics?.size ?: 0)) {
            richestOpus.pics
        } else {
            baseOpus?.pics.orEmpty()
        },
        summary = if ((richestOpus.summary?.text?.length ?: 0) >= (baseOpus?.summary?.text?.length ?: 0)) {
            richestOpus.summary
        } else {
            baseOpus?.summary
        },
        title = richestOpus.title?.takeIf { it.isNotBlank() } ?: baseOpus?.title,
        contentBlocks = if (richestOpus.contentBlocks.size >= (baseOpus?.contentBlocks?.size ?: 0)) {
            richestOpus.contentBlocks
        } else {
            baseOpus?.contentBlocks.orEmpty()
        }
    )
    val mergedMajor = (baseMajor ?: DynamicMajor(
        type = "MAJOR_TYPE_OPUS"
    )).copy(
        type = baseMajor?.type?.takeIf { it.isNotBlank() } ?: "MAJOR_TYPE_OPUS",
        opus = mergedOpus
    )
    return base.copy(
        modules = base.modules.copy(
            module_dynamic = baseContent.copy(major = mergedMajor)
        )
    )
}

/** Retains feed-defined comment metadata when detail/opus responses omit it. */
internal fun mergeDynamicDetailInteractionMetadata(
    detailItem: DynamicItem,
    seedItem: DynamicItem?
): DynamicItem {
    if (seedItem == null) return detailItem
    val detailBasic = detailItem.basic?.takeIf {
        it.comment_type > 0 && it.comment_id_str.toLongOrNull()?.let { oid -> oid > 0L } == true
    }
    val seedBasic = seedItem.basic?.takeIf {
        it.comment_type > 0 && it.comment_id_str.toLongOrNull()?.let { oid -> oid > 0L } == true
    }
    return detailItem.copy(
        basic = detailBasic ?: seedBasic ?: detailItem.basic,
        modules = detailItem.modules.copy(
            module_stat = detailItem.modules.module_stat ?: seedItem.modules.module_stat
        )
    )
}

internal fun shouldFetchOpusDetailForDynamicDetail(item: DynamicItem): Boolean {
    val major = item.modules.module_dynamic?.major
    if (major?.type == "MAJOR_TYPE_OPUS" || major?.opus != null) return true
    // Space/web 详情把长图文当成 DRAW 九宫格预览；正文在 opus/detail。
    if (major?.type == "MAJOR_TYPE_DRAW" || major?.draw != null) return true
    if (item.type.equals("DYNAMIC_TYPE_DRAW", ignoreCase = true)) return true
    return item.basic?.comment_type == 11
}

internal fun resolveOpusArticleFallbackCvId(
    fallbackId: Long?,
    commentType: Int,
    commentIdStr: String
): Long? {
    val fallback = fallbackId?.takeIf { it > 0L }
    if (fallback != null) return fallback
    if (commentType != 12) return null
    return commentIdStr.toLongOrNull()?.takeIf { it > 0L }
}

internal fun articleContentBlocksToOpusBlocks(
    blocks: List<ArticleContentBlock>
): List<OpusContentBlock> {
    return blocks.map { block ->
        when (block) {
            is ArticleContentBlock.Heading -> OpusContentBlock.Text(block.text)
            is ArticleContentBlock.Paragraph -> OpusContentBlock.Text(block.text)
            is ArticleContentBlock.Quote -> OpusContentBlock.Text(block.text)
            is ArticleContentBlock.ListBlock -> OpusContentBlock.Text(
                block.items.mapIndexed { index, item ->
                    if (block.ordered) "${index + 1}. $item" else "• $item"
                }.joinToString("\n")
            )
            is ArticleContentBlock.Code -> OpusContentBlock.Text(block.content)
            is ArticleContentBlock.Image -> OpusContentBlock.Image(
                OpusPic(url = block.url, width = block.width, height = block.height)
            )
        }
    }
}

internal fun mergeArticleDetailIntoOpus(
    base: DynamicItem,
    title: String,
    blocks: List<ArticleContentBlock>
): DynamicItem {
    val opusBlocks = articleContentBlocksToOpusBlocks(blocks)
    if (opusBlocks.isEmpty()) return base
    return mergeRicherOpusDetailContent(
        base = base,
        candidates = listOf(
            base,
            DynamicItem(
                id_str = base.id_str,
                modules = DynamicModules(
                    module_dynamic = DynamicContentModule(
                        major = DynamicMajor(
                            type = "MAJOR_TYPE_OPUS",
                            opus = OpusMajor(
                                title = title.takeIf { it.isNotBlank() },
                                contentBlocks = opusBlocks,
                                pics = opusBlocks.mapNotNull { block ->
                                    (block as? OpusContentBlock.Image)?.pic
                                }
                            )
                        )
                    )
                )
            )
        )
    )
}
