package com.android.purebilibili.feature.video.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoDetailShareSheetStructureTest {

    @Test
    fun ordinaryVideoDetailShareEntrypoints_openVideoShareSheet() {
        val source = loadVideoDetailSource()
        val phoneContentSource = loadVideoDetailPhoneContentSource()
        val overlayAdapterSource = loadVideoDetailCommonOverlayAdapterSource()

        assertTrue(
            source.contains("pendingVideoShare"),
            "VideoDetailScreen should keep a local sheet state for ordinary video sharing"
        )
        assertTrue(source.contains("VideoDetailCommonOverlayAdapter("))
        assertTrue(source.contains("pendingVideoShare = pendingVideoShare"))
        assertTrue(source.contains("onDismissShare = { pendingVideoShare = null }"))
        assertFalse(
            source.contains("VideoShareSheet("),
            "VideoDetailScreenStateHolder should only coordinate the sheet state",
        )
        assertTrue(
            overlayAdapterSource.contains("pendingVideoShare?.let { payload ->") &&
                overlayAdapterSource.contains("VideoShareSheet(payload = payload, onDismiss = onDismissShare)"),
            "VideoDetailCommonOverlayAdapter should render and dismiss the shared video share sheet",
        )

        val detailActionShare = phoneContentSource
            .substringAfter("onDownloadClick = playbackActions.openDownloadDialog")
            .substringBefore("onTimestampClick = {")
        val bottomInputShare = phoneContentSource
            .substringAfter("BottomInputBar(")
            .substringBefore("onCommentClick = {")

        assertTrue(
            detailActionShare.contains("onShareVideo(") &&
                detailActionShare.contains("buildVideoSharePayload"),
            "Detail action row share should emit unified share payload"
        )
        assertTrue(
            detailActionShare.contains("coverUrl = success.info.pic"),
            "Detail action row share should include the current video cover"
        )
        assertTrue(
            bottomInputShare.contains("onShareVideo(") &&
                bottomInputShare.contains("buildVideoSharePayload"),
            "Bottom input bar share should emit unified share payload"
        )
        assertTrue(
            bottomInputShare.contains("coverUrl = success.info.pic"),
            "Bottom input bar share should include the current video cover"
        )
        assertFalse(
            detailActionShare.contains("ShareUtils.shareVideo("),
            "Detail action row share should not directly invoke the system chooser"
        )
        assertFalse(
            bottomInputShare.contains("Intent.createChooser"),
            "Bottom input bar share should not directly invoke the system chooser"
        )
    }

    private fun loadVideoDetailSource(): String {
        val candidates = listOf(
            File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt"),
            File("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailScreenStateHolder.kt")
        )
        val sourceFile = candidates.firstOrNull { it.exists() }
            ?: error("Cannot locate VideoDetailScreen.kt from ${File(".").absolutePath}")
        return sourceFile.readText()
    }

    private fun loadVideoDetailPhoneContentSource(): String {
        val candidates = listOf(
            File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailPhoneContent.kt"),
            File("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailPhoneContent.kt")
        )
        val sourceFile = candidates.firstOrNull { it.exists() }
            ?: error("Cannot locate VideoDetailPhoneContent.kt from ${File(".").absolutePath}")
        return sourceFile.readText()
    }

    private fun loadVideoDetailCommonOverlayAdapterSource(): String {
        val candidates = listOf(
            File("src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailCommonOverlayAdapter.kt"),
            File("app/src/main/java/com/android/purebilibili/feature/video/screen/VideoDetailCommonOverlayAdapter.kt"),
        )
        val sourceFile = candidates.firstOrNull { it.exists() }
            ?: error("Cannot locate VideoDetailCommonOverlayAdapter.kt from ${File(".").absolutePath}")
        return sourceFile.readText()
    }
}
