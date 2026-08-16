package com.android.purebilibili.data.model.response

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DynamicCreateModelsTest {

    @Test
    fun imageDraftUsesDrawScene() {
        assertEquals(2, resolveDynamicCreateScene(hasImages = true))
        assertEquals(1, resolveDynamicCreateScene(hasImages = false))
    }

    @Test
    fun voteContentUsesBizTypeFour() {
        val contents = buildDynamicCreateContents(
            text = "来投票",
            voteId = 99L,
            voteTitle = "晚饭"
        )

        assertEquals(1, contents.first().type)
        assertEquals("来投票", contents.first().raw_text)
        assertEquals(4, contents[1].type)
        assertEquals("99", contents[1].biz_id)
        assertEquals("晚饭", contents[1].raw_text)
    }

    @Test
    fun emptyTextAndVoteStillCreatesVoteNode() {
        val contents = buildDynamicCreateContents(
            text = "  ",
            voteId = 7L,
            voteTitle = ""
        )

        assertEquals(4, contents.first().type)
        assertEquals("投票", contents.first().raw_text)
        assertTrue(contents.last().raw_text.isNotEmpty())
    }

    @Test
    fun createdDynamicIdPrefersStringField() {
        assertEquals(
            "abc",
            resolveCreatedDynamicId(DynamicCreateFeedData(dyn_id = 1L, dyn_id_str = "abc"))
        )
        assertEquals(
            "1",
            resolveCreatedDynamicId(DynamicCreateFeedData(dyn_id = 1L))
        )
    }
}