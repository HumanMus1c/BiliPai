package com.android.purebilibili.feature.audio.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ListenVideoLayoutPolicyTest {

    @Test
    fun `compact widths use single column library list`() {
        assertEquals(ListenVideoLayout.COMPACT_LIST, resolveListenVideoLayout(393))
        assertEquals(ListenVideoLayout.COMPACT_LIST, resolveListenVideoLayout(599))
    }

    @Test
    fun `wide widths use adaptive music grid`() {
        assertEquals(ListenVideoLayout.WIDE_GRID, resolveListenVideoLayout(600))
        assertEquals(ListenVideoLayout.WIDE_GRID, resolveListenVideoLayout(1_024))
    }

    @Test
    fun `section tabs reuse the home liquid indicator without local chrome`() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/audio/screen/ListenVideoScreen.kt"
        ).readText()

        assertTrue(source.contains("BottomBarLiquidSegmentedControl("))
        assertTrue(!source.contains("forceLiquidChrome = true"))
        assertTrue(!source.contains("rememberLayerBackdrop"))
    }
}
