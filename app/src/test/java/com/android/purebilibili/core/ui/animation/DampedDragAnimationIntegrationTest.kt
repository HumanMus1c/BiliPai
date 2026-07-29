package com.android.purebilibili.core.ui.animation

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DampedDragAnimationIntegrationTest {

    @Test
    fun `bottom bar input layer keeps horizontal drag gesture wiring`() {
        val source = listOf(
            File("app/src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt"),
            File("src/main/java/com/android/purebilibili/feature/home/components/BottomBar.kt"),
        ).first { it.exists() }.readText()
        val inputLayerSource = source
            .substringAfter("private fun BoxScope.KernelSuBottomBarInputLayer(")
            .substringBefore("private fun KernelSuBottomBarSearchSlot(")

        assertTrue(inputLayerSource.contains(".horizontalDragGesture("))
        assertTrue(inputLayerSource.contains("dragState = dampedDragState"))
        assertTrue(inputLayerSource.contains("itemWidthPx = itemWidthPx"))
        assertTrue(inputLayerSource.contains("onPressChanged = dampedDragState::setPressed"))
        assertFalse(inputLayerSource.contains("pointerInput("))
    }
}
