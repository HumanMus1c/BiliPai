package com.android.purebilibili.feature.dynamic

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class DynamicCommentStructureTest {

    @Test
    fun `dynamic comments expose sort mode state and reset pagination when it changes`() {
        val source = File("src/main/java/com/android/purebilibili/feature/dynamic/DynamicViewModel.kt")
            .readText()

        assertTrue(source.contains("val dynamicCommentSortMode: StateFlow<CommentSortMode>"))
        assertTrue(source.contains("fun setDynamicCommentSortMode(mode: CommentSortMode)"))
        assertTrue(source.contains("commentNextPage = 1"))
        assertTrue(source.contains("commentGrpcNextOffset = null"))
        assertTrue(source.contains("loadCommentsForDynamic(item)"))
    }

    @Test
    fun `dynamic sub replies use rest pn paging for detail list`() {
        val source = File("src/main/java/com/android/purebilibili/feature/dynamic/DynamicViewModel.kt")
            .readText()

        assertTrue(source.contains("paginationOffset = state.grpcNextOffset"))
        assertTrue(source.contains("restPage = data.page"))
        assertTrue(source.contains("grpcNextOffset = null"))
    }

    @Test
    fun `dynamic detail reloads comments for each screen entry`() {
        val source = File("src/main/java/com/android/purebilibili/feature/dynamic/DynamicDetailScreen.kt")
            .readText()

        assertTrue(source.contains("interactionViewModel.openCommentSheet("))
        assertTrue(!source.contains("hasAutoOpenedComments"))
        assertTrue(!source.contains("rememberSaveable(\n        dynamicId"))
    }

    @Test
    fun `dynamic comment reload cancels and ignores the previous request`() {
        val source = File("src/main/java/com/android/purebilibili/feature/dynamic/DynamicViewModel.kt")
            .readText()

        assertTrue(source.contains("private var commentLoadJob: Job? = null"))
        assertTrue(source.contains("val requestId = ++commentLoadRequestId"))
        assertTrue(source.contains("commentLoadJob?.cancel()"))
        assertTrue(source.contains("if (requestId != commentLoadRequestId) return@launch"))
    }

    @Test
    fun `dynamic comment mentions forward user navigation through every comment surface`() {
        val sheetSource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCommentSheet.kt"
        ).readText()
        val subReplySource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicSubReplyPreviewHost.kt"
        ).readText()
        val screenSource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/DynamicScreen.kt"
        ).readText()
        val detailSource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/DynamicDetailScreen.kt"
        ).readText()

        assertTrue(sheetSource.contains("content = reply.content"))
        assertTrue(sheetSource.contains("content = subReply.content"))
        assertTrue(sheetSource.contains("onUserClick = onUserClick"))
        assertTrue(subReplySource.contains("onAvatarClick = { mid -> mid.toLongOrNull()?.let(onUserClick) }"))
        assertTrue(screenSource.contains("DynamicCommentOverlayHost("))
        assertTrue(screenSource.contains("onUserClick = onUserClick"))
        assertTrue(detailSource.contains("dynamicInlineCommentItems("))
        assertTrue(detailSource.contains("onUserClick = onUserClick"))
    }
}
