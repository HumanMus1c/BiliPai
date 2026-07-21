package com.android.purebilibili.feature.video.ui.components

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * UP 半屏预览在深/浅色下的表面色。
 * 浅色：干净 surface + 浅容器；深色：高对比 surfaceContainer，避免半透明糊底。
 */
internal data class UpPreviewSheetSurfaceColors(
    val sheetColor: Color,
    val scrimColor: Color,
    val cardColor: Color,
    val titleColor: Color,
    val supportingColor: Color,
    val followFillColor: Color,
    val followContentColor: Color,
    val followingFillColor: Color,
    val followingContentColor: Color,
    val enterSpaceColor: Color,
    val dividerColor: Color,
    val coverPlaceholderColor: Color,
)

internal fun resolveUpPreviewSheetSurfaceColors(
    colorScheme: ColorScheme,
): UpPreviewSheetSurfaceColors {
    val isDark = colorScheme.surface.luminance() < 0.5f
    return UpPreviewSheetSurfaceColors(
        sheetColor = if (isDark) {
            colorScheme.surfaceContainerHigh
        } else {
            colorScheme.surface
        },
        scrimColor = Color.Black.copy(alpha = if (isDark) 0.55f else 0.4f),
        cardColor = if (isDark) {
            colorScheme.surfaceContainer
        } else {
            colorScheme.surfaceContainerLowest
        },
        titleColor = colorScheme.onSurface,
        supportingColor = colorScheme.onSurfaceVariant.copy(alpha = if (isDark) 0.82f else 0.75f),
        followFillColor = colorScheme.primary,
        followContentColor = colorScheme.onPrimary,
        followingFillColor = if (isDark) {
            colorScheme.surfaceVariant
        } else {
            colorScheme.surfaceContainerHigh
        },
        followingContentColor = colorScheme.onSurfaceVariant,
        enterSpaceColor = colorScheme.primary,
        dividerColor = colorScheme.outlineVariant.copy(alpha = if (isDark) 0.5f else 0.65f),
        coverPlaceholderColor = colorScheme.surfaceVariant,
    )
}

internal fun resolveUpPreviewStatLine(
    followerCount: Int?,
    videoCount: Int?,
    likeCount: Int?,
): String {
    val parts = buildList {
        followerCount?.takeIf { it >= 0 }?.let {
            add("${formatUpPreviewCount(it)}粉丝")
        }
        videoCount?.takeIf { it >= 0 }?.let {
            add("${formatUpPreviewCount(it)}投稿")
        }
        likeCount?.takeIf { it >= 0 }?.let {
            add("${formatUpPreviewCount(it)}获赞")
        }
    }
    return parts.joinToString("  ")
}

internal fun formatUpPreviewCount(count: Int): String {
    val safe = count.coerceAtLeast(0)
    return when {
        safe >= 100_000_000 -> String.format("%.1f亿", safe / 100_000_000f)
        safe >= 10_000 -> {
            val wan = safe / 10_000f
            if (wan >= 100f) {
                String.format("%.0f万", wan)
            } else {
                String.format("%.1f万", wan)
            }
        }
        else -> safe.toString()
    }
}

internal data class UpPreviewVideoItem(
    val bvid: String,
    val title: String,
    val coverUrl: String,
    val playCount: Int,
    val durationText: String,
    val createdAtSeconds: Long,
)

internal fun resolveUpPreviewVideoClickTarget(
    bvid: String,
    cid: Long = 0L,
): Pair<String, Long>? {
    val normalized = bvid.trim()
    if (normalized.isEmpty()) return null
    return normalized to cid.coerceAtLeast(0L)
}
