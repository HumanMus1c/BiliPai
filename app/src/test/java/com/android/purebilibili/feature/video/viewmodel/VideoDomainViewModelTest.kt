package com.android.purebilibili.feature.video.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VideoDomainViewModelTest {

    @Test
    fun `engagement keeps transient state for same generation and resets for next video`() {
        val viewModel = VideoEngagementViewModel()
        val first = subject("BV1", generation = 1L)
        viewModel.bindSubject(first, VideoEngagementSeed(isLiked = false))
        viewModel.setCoinDialogVisible(true)

        viewModel.bindSubject(first, VideoEngagementSeed(isLiked = true))
        assertTrue(viewModel.uiState.value.coinDialogVisible)
        assertTrue(viewModel.uiState.value.isLiked)

        viewModel.bindSubject(subject("BV2", generation = 2L), VideoEngagementSeed())
        assertFalse(viewModel.uiState.value.coinDialogVisible)
        assertFalse(viewModel.uiState.value.isLiked)
    }

    @Test
    fun `composer drops drafts when subject generation changes`() {
        val viewModel = VideoComposerViewModel()
        val first = subject("BV1", generation = 1L)
        viewModel.bindSubject(first)
        viewModel.updateCommentDraft("draft")
        viewModel.bindSubject(first)
        assertEquals("draft", viewModel.uiState.value.commentDraft)

        viewModel.bindSubject(subject("BV2", generation = 2L))
        assertEquals("", viewModel.uiState.value.commentDraft)
    }

    @Test
    fun `supplement updates payload without replacing subject generation`() {
        val viewModel = VideoSupplementViewModel()
        val subject = subject("BV1", generation = 1L)
        viewModel.bindSubject(subject, VideoSupplementSeed(onlineCount = "1"))
        viewModel.bindSubject(subject, VideoSupplementSeed(onlineCount = "2"))

        assertEquals(subject, viewModel.uiState.value.subject)
        assertEquals("2", viewModel.uiState.value.onlineCount)
    }

    private fun subject(bvid: String, generation: Long) = VideoSubjectSnapshot(
        bvid = bvid,
        cid = generation,
        aid = generation,
        ownerMid = 1L,
        title = bvid,
        coverUrl = "",
        durationMs = 1_000L,
        generation = generation
    )
}
