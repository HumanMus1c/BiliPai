package com.android.purebilibili.feature.audio.screen

import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MusicPlayerLayoutPolicyTest {

    @Test
    fun `compact portrait uses horizontal pager`() {
        assertEquals(
            MusicPlayerLayout.COMPACT_PAGER,
            resolveMusicPlayerLayout(widthDp = 393, isInPipMode = false)
        )
    }

    @Test
    fun `wide screen keeps artwork and lyrics visible together`() {
        assertEquals(
            MusicPlayerLayout.EXPANDED_SPLIT,
            resolveMusicPlayerLayout(widthDp = 900, isInPipMode = false)
        )
    }

    @Test
    fun `pip always renders artwork only`() {
        assertEquals(
            MusicPlayerLayout.PIP_ARTWORK,
            resolveMusicPlayerLayout(widthDp = 900, isInPipMode = true)
        )
    }

    @Test
    fun `compact pager tabs stay on cover and lyrics`() {
        assertEquals(listOf("封面", "歌词"), resolveMusicPlayerPageTabs())
        assertEquals(70, MUSIC_PLAYER_COMPACT_DOCK_BOTTOM_PADDING_DP)
    }

    @Test
    fun `chrome follows md3 and miuix spacing and only immerses when glass is on`() {
        val md3 = resolveMusicPlayerChromeSpec(AppUiStyle.MATERIAL3, glassEnabled = false)
        assertEquals(18, md3.horizontalPaddingDp)
        assertEquals(80, md3.playButtonSizeDp)
        assertFalse(md3.usePaletteImmersiveBackdrop)
        assertTrue(md3.coverShapeIsCircle)

        val miuix = resolveMusicPlayerChromeSpec(AppUiStyle.MIUIX, glassEnabled = false)
        assertEquals(16, miuix.horizontalPaddingDp)
        assertEquals(72, miuix.playButtonSizeDp)
        assertFalse(miuix.usePaletteImmersiveBackdrop)

        val glass = resolveMusicPlayerChromeSpec(AppUiStyle.MATERIAL3, glassEnabled = true)
        assertTrue(glass.usePaletteImmersiveBackdrop)
        assertTrue(glass.coverShapeIsCircle)
    }

    @Test
    fun `compact artwork respects available height`() {
        assertEquals(
            320,
            resolveMusicArtworkSizeDp(
                availableWidthDp = 393,
                availableHeightDp = 720,
                layout = MusicPlayerLayout.COMPACT_PAGER
            )
        )
        assertEquals(
            240,
            resolveMusicArtworkSizeDp(
                availableWidthDp = 393,
                availableHeightDp = 320,
                layout = MusicPlayerLayout.COMPACT_PAGER
            )
        )
    }
}
