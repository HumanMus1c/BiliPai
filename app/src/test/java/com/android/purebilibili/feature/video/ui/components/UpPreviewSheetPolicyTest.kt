package com.android.purebilibili.feature.video.ui.components

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpPreviewSheetPolicyTest {

    @Test
    fun surfaceColors_lightAndDarkRemainReadable() {
        val light = resolveUpPreviewSheetSurfaceColors(lightColorScheme())
        val dark = resolveUpPreviewSheetSurfaceColors(darkColorScheme())

        assertTrue(light.sheetColor.luminance() > 0.5f)
        assertTrue(dark.sheetColor.luminance() < 0.5f)
        assertTrue(light.titleColor.luminance() < light.sheetColor.luminance())
        assertTrue(dark.titleColor.luminance() > dark.sheetColor.luminance())
        assertNotNull(light.followFillColor)
        assertNotNull(dark.followFillColor)
    }

    @Test
    fun statLine_formatsCountsAndSkipsNulls() {
        assertEquals(
            "1.5万粉丝  128投稿  6.1万获赞",
            resolveUpPreviewStatLine(
                followerCount = 15_291,
                videoCount = 128,
                likeCount = 61_000,
            )
        )
        assertEquals(
            "999粉丝",
            resolveUpPreviewStatLine(
                followerCount = 999,
                videoCount = null,
                likeCount = null,
            )
        )
        assertEquals("", resolveUpPreviewStatLine(null, null, null))
    }

    @Test
    fun videoClickTarget_rejectsBlankBvid() {
        assertNull(resolveUpPreviewVideoClickTarget("  "))
        assertEquals("BV1xx" to 0L, resolveUpPreviewVideoClickTarget("BV1xx"))
        assertEquals("BV1yy" to 12L, resolveUpPreviewVideoClickTarget(" BV1yy ", 12L))
    }
}
