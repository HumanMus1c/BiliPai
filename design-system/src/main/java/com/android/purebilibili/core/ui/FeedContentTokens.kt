package com.android.purebilibili.core.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object AppTypographyTokens {
    val ZeroLetterSpacing = 0.sp
}

/** Typography roles shared by feed cards regardless of their visual composition. */
data class FeedContentTypography(
    val title: TextStyle,
    val author: TextStyle,
    val statistic: TextStyle,
    val coverBadge: TextStyle,
)

enum class FeedTitleHierarchy {
    Compact,
    Standard,
    Prominent,
}

@Composable
fun feedContentTypography(
    titleHierarchy: FeedTitleHierarchy = FeedTitleHierarchy.Compact,
): FeedContentTypography {
    val titleStyle = when (titleHierarchy) {
        FeedTitleHierarchy.Compact ->
            MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
        FeedTitleHierarchy.Standard ->
            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        FeedTitleHierarchy.Prominent ->
            MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
    }
    return FeedContentTypography(
        title = titleStyle,
        author = MaterialTheme.typography.labelMedium,
        statistic = MaterialTheme.typography.labelSmall,
        coverBadge = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
    )
}
