package com.android.purebilibili.feature.profile

import com.android.purebilibili.data.model.response.FavFolder
import com.android.purebilibili.data.model.response.FollowBangumiItem
import com.android.purebilibili.data.model.response.SpaceAggregateArchiveItem
import com.android.purebilibili.data.model.response.SpaceVideoItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProfileSpacePolicyTest {

    @Test
    fun `profile space tabs match self space order`() {
        assertEquals(
            listOf("主页", "动态", "投稿", "收藏", "追番"),
            defaultProfileSpaceTabs().map { it.title }
        )
        assertEquals(ProfileSpaceMainTab.HOME, defaultProfileSpaceTabs().first().tab)
        assertEquals(ProfileSpaceMainTab.BANGUMI, defaultProfileSpaceTabs().last().tab)
    }

    @Test
    fun `home sections keep reference order and hide empty sections`() {
        val sections = resolveProfileSpaceHomeSections(
            favoriteFolders = listOf(FavFolder(id = 1, title = "默认收藏夹", media_count = 8)),
            bangumiItems = listOf(FollowBangumiItem(seasonId = 2, title = "追番")),
            coinVideos = listOf(SpaceAggregateArchiveItem(aid = 3, title = "投币")),
            likeVideos = listOf(SpaceAggregateArchiveItem(aid = 4, title = "点赞")),
            contributionVideos = listOf(SpaceVideoItem(aid = 5, bvid = "BV1xx", title = "投稿"))
        )

        assertEquals(
            listOf(
                ProfileSpaceHomeSection.FAVORITES,
                ProfileSpaceHomeSection.BANGUMI,
                ProfileSpaceHomeSection.COIN_VIDEOS,
                ProfileSpaceHomeSection.LIKE_VIDEOS,
                ProfileSpaceHomeSection.CONTRIBUTIONS,
                ProfileSpaceHomeSection.SERVICES
            ),
            sections
        )
    }

    @Test
    fun `home sections always keep services even when content is empty`() {
        assertEquals(
            listOf(ProfileSpaceHomeSection.SERVICES),
            resolveProfileSpaceHomeSections(
                favoriteFolders = emptyList(),
                bangumiItems = emptyList(),
                coinVideos = emptyList(),
                likeVideos = emptyList(),
                contributionVideos = emptyList()
            )
        )
    }

    @Test
    fun `editable account state trims sign and marks only sign as editable`() {
        val state = ProfileEditableAccountState(
            name = "测试用户",
            birthday = "2000-01-01",
            sex = "保密",
            sign = "  这个人很神秘  "
        )

        assertEquals("这个人很神秘", state.normalizedSign)
        assertTrue(state.canSubmitSign)
        assertFalse(state.canEditName)
        assertFalse(state.canEditBirthday)
        assertFalse(state.canEditSex)
    }

    @Test
    fun `sign validation allows empty sign and blocks over length`() {
        assertEquals(null, validateProfileSign(""))
        assertEquals(null, validateProfileSign("a".repeat(70)))
        assertEquals("签名最多支持 70 个字符", validateProfileSign("a".repeat(71)))
    }
}
