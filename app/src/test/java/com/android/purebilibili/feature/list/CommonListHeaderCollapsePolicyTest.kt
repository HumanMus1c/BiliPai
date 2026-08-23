package com.android.purebilibili.feature.list

import com.android.purebilibili.core.store.CommonListHeaderCollapseMode
import com.android.purebilibili.core.store.HomeHeaderCollapseMode
import kotlin.test.Test
import kotlin.test.assertEquals

class CommonListHeaderCollapsePolicyTest {

    @Test
    fun `collapse mode falls back to reverse scroll behavior`() {
        assertEquals(
            CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL,
            CommonListHeaderCollapseMode.fromValue(999)
        )
    }

    @Test
    fun `favorite header only returns after reaching the top`() {
        assertEquals(
            CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY,
            resolveCommonListHeaderCollapseModeForScreen(
                configuredMode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL,
                isFavoritePage = true
            )
        )
        assertEquals(
            CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL,
            resolveCommonListHeaderCollapseModeForScreen(
                configuredMode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL,
                isFavoritePage = false
            )
        )
    }

    @Test
    fun `history header follows home top bar toggle`() {
        // 首页顶部折叠开启 → 历史页使用同款下滑收起、回顶恢复。
        assertEquals(
            CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY,
            resolveCommonListHeaderCollapseModeForScreen(
                configuredMode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL,
                isFavoritePage = false,
                isHistoryPage = true,
                homeHeaderCollapseMode = HomeHeaderCollapseMode.BOTH
            )
        )
        // 首页「首页顶栏显示」开关关闭（始终显示）→ 历史页顶栏始终显示
        assertEquals(
            CommonListHeaderCollapseMode.ALWAYS_VISIBLE,
            resolveCommonListHeaderCollapseModeForScreen(
                configuredMode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL,
                isFavoritePage = false,
                isHistoryPage = true,
                homeHeaderCollapseMode = HomeHeaderCollapseMode.OFF
            )
        )
        // 历史页不再受「列表顶部栏」独立配置影响
        assertEquals(
            CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY,
            resolveCommonListHeaderCollapseModeForScreen(
                configuredMode = CommonListHeaderCollapseMode.ALWAYS_VISIBLE,
                isFavoritePage = false,
                isHistoryPage = true,
                homeHeaderCollapseMode = HomeHeaderCollapseMode.BOTH
            )
        )
    }

    @Test
    fun `always visible mode ignores scroll deltas`() {
        assertEquals(
            0f,
            resolveCommonListHeaderOffsetPx(
                currentOffsetPx = -80f,
                scrollDeltaYPx = -40f,
                maxCollapsePx = 160f,
                isAtTop = false,
                mode = CommonListHeaderCollapseMode.ALWAYS_VISIBLE
            )
        )
    }

    @Test
    fun `reverse scroll mode collapses and restores within bounds`() {
        assertEquals(
            -32f,
            resolveCommonListHeaderOffsetPx(
                currentOffsetPx = 0f,
                scrollDeltaYPx = -32f,
                maxCollapsePx = 160f,
                isAtTop = true,
                mode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
            )
        )
        assertEquals(
            -100f,
            resolveCommonListHeaderOffsetPx(
                currentOffsetPx = -60f,
                scrollDeltaYPx = -40f,
                maxCollapsePx = 160f,
                isAtTop = false,
                mode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
            )
        )
        assertEquals(
            -20f,
            resolveCommonListHeaderOffsetPx(
                currentOffsetPx = -60f,
                scrollDeltaYPx = 40f,
                maxCollapsePx = 160f,
                isAtTop = false,
                mode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
            )
        )
    }

    @Test
    fun `top only mode stays collapsed until list reaches top`() {
        assertEquals(
            -160f,
            resolveCommonListHeaderOffsetPx(
                currentOffsetPx = -160f,
                scrollDeltaYPx = 48f,
                maxCollapsePx = 160f,
                isAtTop = false,
                mode = CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY
            )
        )
        assertEquals(
            0f,
            resolveCommonListHeaderOffsetPx(
                currentOffsetPx = -160f,
                scrollDeltaYPx = 0f,
                maxCollapsePx = 160f,
                isAtTop = true,
                mode = CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY
            )
        )
    }

    @Test
    fun `settled page at top expands header`() {
        assertEquals(
            0f,
            resolveCommonListHeaderOffsetForSettledContent(
                firstVisibleItemIndex = 0,
                firstVisibleItemScrollOffset = 0,
                maxCollapsePx = 240f,
                mode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
            )
        )
    }

    @Test
    fun `settled page away from top keeps header collapsed`() {
        assertEquals(
            -240f,
            resolveCommonListHeaderOffsetForSettledContent(
                firstVisibleItemIndex = 1,
                firstVisibleItemScrollOffset = 0,
                maxCollapsePx = 240f,
                mode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
            )
        )
        assertEquals(
            -240f,
            resolveCommonListHeaderOffsetForSettledContent(
                firstVisibleItemIndex = 0,
                firstVisibleItemScrollOffset = 1,
                maxCollapsePx = 240f,
                mode = CommonListHeaderCollapseMode.SHOW_AT_TOP_ONLY
            )
        )
    }

    @Test
    fun `always visible mode stays expanded after page switch`() {
        assertEquals(
            0f,
            resolveCommonListHeaderOffsetForSettledContent(
                firstVisibleItemIndex = 8,
                firstVisibleItemScrollOffset = 120,
                maxCollapsePx = 240f,
                mode = CommonListHeaderCollapseMode.ALWAYS_VISIBLE
            )
        )
    }

    @Test
    fun `header ignores gesture delta when list consumes no vertical scroll`() {
        assertEquals(
            -40f,
            resolveCommonListHeaderOffsetAfterContentScroll(
                currentOffsetPx = -40f,
                contentConsumedDeltaYPx = 0f,
                maxCollapsePx = 240f,
                isAtTop = false,
                mode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
            )
        )
    }

    @Test
    fun `header follows vertical scroll consumed by list`() {
        assertEquals(
            -100f,
            resolveCommonListHeaderOffsetAfterContentScroll(
                currentOffsetPx = -40f,
                contentConsumedDeltaYPx = -60f,
                maxCollapsePx = 240f,
                isAtTop = false,
                mode = CommonListHeaderCollapseMode.SHOW_ON_REVERSE_SCROLL
            )
        )
    }
}
