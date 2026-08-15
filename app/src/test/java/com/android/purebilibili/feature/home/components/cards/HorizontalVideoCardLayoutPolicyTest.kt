package com.android.purebilibili.feature.home.components.cards

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class HorizontalVideoCardLayoutPolicyTest {

    @Test
    fun coverUsesSixteenByTen() {
        assertEquals(16f / 10f, HORIZONTAL_VIDEO_CARD_COVER_ASPECT_RATIO)
        assertEquals(87.5f, resolveHorizontalVideoCoverHeightDp(140f, 16f / 10f))
    }

    @Test
    fun coverHeightNeverUsesSubUnityAspect() {
        assertEquals(140f, resolveHorizontalVideoCoverHeightDp(140f, 0.5f))
    }

    @Test
    fun horizontalCardDoesNotLockInfoColumnToCoverHeight() {
        val source = java.io.File(
            "src/main/java/com/android/purebilibili/feature/home/components/cards/HomeStyleSingleColumnVideoCard.kt",
        ).let { file ->
            listOf(file, java.io.File("app/${file.path}")).first { it.exists() }.readText()
        }
        assertTrue(source.contains("HORIZONTAL_VIDEO_CARD_COVER_ASPECT_RATIO"))
        assertTrue(source.contains("HorizontalVideoStatRow("))
        assertTrue(source.contains("modifier = Modifier.fillMaxWidth()"))
        assertTrue(!source.contains(".height(coverHeight)"))
    }

    @Test
    fun statRowWrapsInsteadOfClippingTrailingUnits() {
        val source = java.io.File(
            "src/main/java/com/android/purebilibili/feature/home/components/cards/HorizontalVideoCardStats.kt",
        ).let { file ->
            listOf(file, java.io.File("app/${file.path}")).first { it.exists() }.readText()
        }

        assertTrue(source.contains("FlowRow("))
        assertTrue(source.contains("HORIZONTAL_VIDEO_STAT_WRAP_SPACING_DP"))
    }
}
