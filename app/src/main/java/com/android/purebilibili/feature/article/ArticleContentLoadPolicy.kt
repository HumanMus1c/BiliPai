package com.android.purebilibili.feature.article

import com.android.purebilibili.data.model.response.OpusContentBlock

internal const val ARTICLE_CONTENT_SPARSE_SCORE = 800
internal const val ARTICLE_HISTORY_REPORT_TYPE = 5

internal fun scoreArticleBlocks(blocks: List<ArticleContentBlock>): Int {
    if (blocks.isEmpty()) return 0
    val textLength = blocks.sumOf { block ->
        when (block) {
            is ArticleContentBlock.Heading -> block.text.length
            is ArticleContentBlock.Paragraph -> block.text.length
            is ArticleContentBlock.Quote -> block.text.length
            is ArticleContentBlock.ListBlock -> block.items.sumOf { it.length }
            is ArticleContentBlock.Code -> block.content.length
            is ArticleContentBlock.Image -> 0
        }
    }
    val imageCount = blocks.count { it is ArticleContentBlock.Image }
    return blocks.size * 100 + imageCount * 20 + textLength
}

internal fun selectRicherArticleBlocks(
    vararg candidates: List<ArticleContentBlock>
): List<ArticleContentBlock> {
    return candidates.maxByOrNull(::scoreArticleBlocks).orEmpty()
}

internal fun scoreOpusContentBlocks(blocks: List<OpusContentBlock>): Int {
    if (blocks.isEmpty()) return 0
    val textLength = blocks.sumOf { block ->
        when (block) {
            is OpusContentBlock.Text -> block.text.length
            is OpusContentBlock.LinkCard ->
                block.card.title.length + block.card.description.length
            is OpusContentBlock.Image -> 0
        }
    }
    val imageCount = blocks.count { it is OpusContentBlock.Image }
    return blocks.size * 100 + imageCount * 20 + textLength
}

internal fun shouldFetchArticleFallbackForOpus(
    opusBlocks: List<OpusContentBlock>,
    fallbackCvId: Long?
): Boolean {
    if (fallbackCvId != null && fallbackCvId > 0L) return true
    if (opusBlocks.isEmpty()) return true
    return scoreOpusContentBlocks(opusBlocks) < ARTICLE_CONTENT_SPARSE_SCORE
}

internal fun opusContentBlocksToArticleBlocks(
    blocks: List<OpusContentBlock>
): List<ArticleContentBlock> {
    return blocks.mapNotNull { block ->
        when (block) {
            is OpusContentBlock.Text ->
                block.text.takeIf { it.isNotBlank() }?.let(ArticleContentBlock::Paragraph)
            is OpusContentBlock.Image -> {
                val url = block.pic.url.trim()
                if (url.isBlank()) {
                    null
                } else {
                    ArticleContentBlock.Image(
                        url = url,
                        width = block.pic.width,
                        height = block.pic.height
                    )
                }
            }
            is OpusContentBlock.LinkCard -> {
                val title = block.card.title.ifBlank { block.card.description }
                title.takeIf { it.isNotBlank() }?.let(ArticleContentBlock::Paragraph)
            }
        }
    }
}

internal fun buildArticleHistoryReportFields(
    articleId: Long,
    csrf: String
): Map<String, String>? {
    if (articleId <= 0L || csrf.isBlank()) return null
    return mapOf(
        "aid" to articleId.toString(),
        "type" to ARTICLE_HISTORY_REPORT_TYPE.toString(),
        "csrf" to csrf
    )
}
