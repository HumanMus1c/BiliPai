package com.android.purebilibili.feature.video.screen

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class VideoDetailVideoShareSheetStructureTest {

    @Test
    fun ordinaryVideoDetailShareEntrypoints_openVideoShareSheet() {
        val source = loadVideoDetailSource()
        val phoneContentSource = loadVideoDetailPhoneContentSource()

        assertTrue(
            source.contains("pendingVideoShare"),
            "VideoDetailScreen should keep a local sheet state for ordinary video sharing"
        )
        assertTrue(
            source.contains("VideoShareSheet("),
            "VideoDetailScreen should render the shared video share sheet"
        )

        val detailActionShare = phoneContentSource
            .substringAfter("onDownloadClick = playbackActions.openDownloadDialog")
            .substringBefore("//  [新增] 时间戳点击跳转")
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
}
