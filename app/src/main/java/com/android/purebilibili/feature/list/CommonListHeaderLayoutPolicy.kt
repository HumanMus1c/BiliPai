package com.android.purebilibili.feature.list

/**
 * History always uses floating chrome, even when that chrome is configured to stay visible.
 * Other common lists retain the existing behavior and scroll under only while collapsible.
 */
internal fun shouldScrollCommonListUnderHeader(
    isHistoryPage: Boolean,
    headerCollapseEnabled: Boolean,
): Boolean = isHistoryPage || headerCollapseEnabled
