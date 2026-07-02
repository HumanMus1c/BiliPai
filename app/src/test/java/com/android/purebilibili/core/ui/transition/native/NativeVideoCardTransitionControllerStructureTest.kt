package com.android.purebilibili.core.ui.transition.native

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NativeVideoCardTransitionControllerStructureTest {

    @Test
    fun openTransitionAnimatesHomeBackgroundBeforeCommittingNavigation() {
        val source = loadSource()
        val startOpenBlock = source
            .substringAfter("fun startOpen(")
            .substringBefore("fun startClose(")
        val validSourceBlock = startOpenBlock.substringAfter("if (isRunning) return")

        val animateIndex = validSourceBlock.indexOf("animate(")
        val navigateIndex = validSourceBlock.indexOf("navigateAction()")

        assertTrue(animateIndex >= 0)
        assertTrue(navigateIndex > animateIndex)
        assertTrue(validSourceBlock.contains("onEnd = {"))
    }

    @Test
    fun predictiveCloseCanPreviewFinishAndCancelWithoutStartingFromZero() {
        val source = loadSource()

        assertTrue(source.contains("fun previewClose("))
        assertTrue(source.contains("fun finishPreviewClose("))
        assertTrue(source.contains("fun cancelPreviewClose("))
        assertTrue(source.contains("startProgress = previewCloseProgress"))
    }

    @Test
    fun openNavigationHandoffDoesNotFreezeAtFullBlur() {
        val source = loadSource()

        assertTrue(source.contains("private const val OPEN_NAVIGATION_HANDOFF_DELAY_MS = 64L"))
    }

    @Test
    fun nativeTransitionRendersBlackCardWithoutCoverBitmapPipeline() {
        val controllerSource = loadSource()
        val overlaySource = loadOverlaySource()

        assertFalse(controllerSource.contains("coverUrl"))
        assertFalse(controllerSource.contains("loadCoverBitmap("))
        assertFalse(controllerSource.contains("setCoverBitmap("))
        assertFalse(overlaySource.contains("coverBitmap"))
        assertFalse(overlaySource.contains("drawCover("))
        assertFalse(overlaySource.contains("coverAlpha"))
    }

    private fun loadSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/core/ui/transition/native/NativeVideoCardTransitionController.kt"),
            File("src/main/java/com/android/purebilibili/core/ui/transition/native/NativeVideoCardTransitionController.kt")
        ).first { it.exists() }.readText()
    }

    private fun loadOverlaySource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/core/ui/transition/native/NativeVideoCardTransitionOverlayView.kt"),
            File("src/main/java/com/android/purebilibili/core/ui/transition/native/NativeVideoCardTransitionOverlayView.kt")
        ).first { it.exists() }.readText()
    }
}
