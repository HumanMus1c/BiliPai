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
    fun `dynamic detail auto opens comments on first successful load`() {
        val source = File("src/main/java/com/android/purebilibili/feature/dynamic/DynamicDetailScreen.kt")
            .readText()

        assertTrue(source.contains("shouldAutoOpenCommentsOnDynamicDetailEntry(hasAutoOpenedComments)"))
        assertTrue(source.contains("interactionViewModel.openCommentSheet("))
        assertTrue(source.contains("hasAutoOpenedComments = true"))
        // Must not gate auto-open on routed root reply only.
        assertTrue(!source.contains("openCommentRootRpid > 0L && !hasHandledRoutedComment"))
        assertTrue(!source.contains("openCommentRootRpid > 0L && !hasAutoOpenedComments"))
    }
}
