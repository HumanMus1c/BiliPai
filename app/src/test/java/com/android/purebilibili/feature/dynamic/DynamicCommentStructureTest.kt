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

    @Test
    fun `dynamic comment fan decoration is top-end overlay not inline in name row`() {
        val sheetSource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCommentSheet.kt"
        ).readText()
        val commentItem = sheetSource
            .substringAfter("private fun CommentItem(")
            .substringBefore("private fun formatTime(")

        assertTrue(commentItem.contains("FanGroupDecorationBadge("))
        assertTrue(commentItem.contains("Alignment.TopEnd"))
        assertTrue(commentItem.contains("decorationEndReserve"))
        // Must not keep badge inline after username/time in the name Row.
        val nameRow = commentItem
            .substringAfter("// 用户名 + 时间")
            .substringBefore("// 评论内容")
        assertTrue(!nameRow.contains("FanGroupDecorationBadge("))
    }

    @Test
    fun `dynamic comment avatar and name open user space`() {
        val sheetSource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCommentSheet.kt"
        ).readText()
        val commentItem = sheetSource
            .substringAfter("private fun CommentItem(")
            .substringBefore("private fun formatTime(")

        assertTrue(commentItem.contains("ReplyMemberAvatar("))
        assertTrue(commentItem.contains("onClick = memberMid?.let"))
        assertTrue(commentItem.contains("onUserClick(memberMid)"))
        assertTrue(commentItem.contains("onUserClick(mid)"))
    }

    @Test
    fun `dynamic comments expose like reply and thread tap interactions`() {
        val sheetSource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicCommentSheet.kt"
        ).readText()
        val viewModelSource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/DynamicViewModel.kt"
        ).readText()
        val commentItem = sheetSource
            .substringAfter("private fun CommentItem(")
            .substringBefore("private fun formatTime(")

        assertTrue(sheetSource.contains("resolveDynamicCommentCountLabel("))
        assertTrue(sheetSource.contains("resolveDynamicCommentEmptyLabel()"))
        assertTrue(commentItem.contains("shouldOpenDynamicCommentThreadOnTap(reply)"))
        assertTrue(commentItem.contains("onReply(reply)"))
        assertTrue(commentItem.contains("onLike(reply)"))
        assertTrue(viewModelSource.contains("fun likeComment(rpid: Long"))
        assertTrue(viewModelSource.contains("fun startCommentReply("))
        assertTrue(viewModelSource.contains("root = replyTarget?.rootRpid ?: 0L"))
    }

    @Test
    fun `feed and space comment buttons open dynamic detail`() {
        val screenSource = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/DynamicScreen.kt"
        ).readText()
        val spaceSource = File(
            "src/main/java/com/android/purebilibili/feature/space/SpaceScreen.kt"
        ).readText()
        val topicSource = File(
            "src/main/java/com/android/purebilibili/feature/search/TopicDetailScreen.kt"
        ).readText()

        assertTrue(screenSource.contains("onCommentClick = onDynamicDetailClick"))
        assertTrue(!screenSource.contains("onCommentClick = { viewModel.openCommentSheet(it) }"))
        assertTrue(spaceSource.contains("onCommentClick = { onDynamicDetailClick(dynamic.id_str) }"))
        assertTrue(topicSource.contains("onCommentClick = onDynamicDetailClick"))
    }
}
