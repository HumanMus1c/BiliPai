package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridScope
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Keeps the feed's chrome inset in item space instead of the staggered grid's before-padding
 * region. Uneven lanes can otherwise retain different anchors inside that region when reversing
 * a slow scroll. A full-line inset gives all lanes the same origin and still scrolls under chrome.
 *
 * The inset is a real grid item: callers must use grid indices (including this item) for scroll
 * restoration, and item keys when mapping a visible item back to feed data.
 */
@Composable
internal fun FeedVerticalStaggeredGrid(
    columns: StaggeredGridCells,
    state: LazyStaggeredGridState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    verticalItemSpacing: Dp = 0.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(0.dp),
    content: LazyStaggeredGridScope.() -> Unit,
) {
    val direction = LocalLayoutDirection.current
    val topInset = contentPadding.calculateTopPadding()
    // Small decorative padding needs no header; do not add an extra gap when inset < spacing.
    val hasInsetItem = topInset > verticalItemSpacing && topInset > 0.dp
    LazyVerticalStaggeredGrid(
        columns = columns,
        state = state,
        modifier = modifier,
        contentPadding = PaddingValues(
            start = contentPadding.calculateStartPadding(direction),
            top = if (hasInsetItem) 0.dp else topInset,
            end = contentPadding.calculateEndPadding(direction),
            bottom = contentPadding.calculateBottomPadding(),
        ),
        verticalItemSpacing = verticalItemSpacing,
        horizontalArrangement = horizontalArrangement,
    ) {
        if (hasInsetItem) {
            item(
                key = "feed_chrome_top_inset",
                contentType = "feed_chrome_top_inset",
                span = StaggeredGridItemSpan.FullLine,
            ) {
                Spacer(Modifier.height(topInset - verticalItemSpacing))
            }
        }
        content()
    }
}
