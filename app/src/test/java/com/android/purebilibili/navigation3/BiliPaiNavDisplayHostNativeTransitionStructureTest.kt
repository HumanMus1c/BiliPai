package com.android.purebilibili.navigation3

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class BiliPaiNavDisplayHostNativeTransitionStructureTest {

    @Test
    fun hostForwardsPredictiveBackProgressAndCancellationToNativeVideoTransition() {
        val source = loadSource()

        assertTrue(source.contains("onNativeVideoBackProgress:"))
        assertTrue(source.contains("NavigationEventTransitionState.InProgress"))
        assertTrue(source.contains("latestEvent?.progress"))
        assertTrue(source.contains("onNativeVideoBackCancelled("))
    }

    @Test
    fun scopedEntryContentRefreshesItsExposureProviderBeforePredictiveBack() {
        val source = loadSource()
        val scopedContentRememberKeys = source
            .substringAfter("val scopedContent:")
            .substringAfter("remember(")
            .substringBefore(") {")

        assertTrue(scopedContentRememberKeys.contains("videoCardExposureProvider"))
        // sourceMetadata 经 rememberUpdatedState 保持最新且不触发 scopedContent 重建，
        // 不再作为 remember key（避免预测返回期间 entry lambda 重建打断 seek）。
        assertTrue(source.contains("rememberUpdatedState(sourceMetadata)"))
    }

    private fun loadSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt"),
            File("src/main/java/com/android/purebilibili/navigation3/BiliPaiNavDisplayHost.kt")
        ).first { it.exists() }.readText()
    }
}
