package com.android.purebilibili.feature.dynamic.components

import com.android.purebilibili.data.model.response.DynamicBasic
import com.android.purebilibili.data.model.response.DynamicItem
import com.android.purebilibili.data.model.response.DynamicModules
import com.android.purebilibili.data.model.response.DynamicMoreModule
import com.android.purebilibili.data.model.response.DynamicThreePointItem
import com.android.purebilibili.data.model.response.DynamicThreePointParams
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DynamicMenuActionPolicyTest {

    @Test
    fun pinnedMenuLabelFollowsCurrentTopState() {
        assertEquals("置顶", resolveDynamicPinnedMenuLabel(isCurrentlyTop = false))
        assertEquals("取消置顶", resolveDynamicPinnedMenuLabel(isCurrentlyTop = true))
    }

    @Test
    fun visibilityMenuLabelFollowsPrivateState() {
        assertEquals("设为仅自己可见", resolveDynamicVisibilityMenuLabel(isPrivate = false))
        assertEquals("设为公开", resolveDynamicVisibilityMenuLabel(isPrivate = true))
    }

    @Test
    fun visibilityActionMapsToApiValues() {
        assertEquals("private_pub", resolveDynamicVisibilityAction(isPrivate = true))
        assertEquals("public_pub", resolveDynamicVisibilityAction(isPrivate = false))
    }

    @Test
    fun replySelectionActionMapsToApiValues() {
        assertEquals(2, resolveDynamicReplySelectionAction(isCurrentlyEnabled = true))
        assertEquals(1, resolveDynamicReplySelectionAction(isCurrentlyEnabled = false))
    }

    @Test
    fun replyOpenActionMapsToApiValues() {
        assertEquals(3, resolveDynamicReplyOpenAction(isCurrentlyEnabled = true))
        assertEquals(4, resolveDynamicReplyOpenAction(isCurrentlyEnabled = false))
    }

    @Test
    fun replySubjectOidParsesFromBasicCommentId() {
        val item = DynamicItem(
            basic = DynamicBasic(comment_id_str = "112981396619958", comment_type = 17)
        )
        assertEquals(112981396619958L, resolveDynamicReplySubjectOid(item))
        assertEquals(17, resolveDynamicReplySubjectType(item))
    }

    @Test
    fun replySubjectOidRejectsBlankOrInvalidId() {
        assertNull(resolveDynamicReplySubjectOid(DynamicItem()))
        assertNull(resolveDynamicReplySubjectOid(DynamicItem(basic = DynamicBasic(comment_id_str = "abc"))))
        assertNull(resolveDynamicReplySubjectOid(DynamicItem(basic = DynamicBasic(comment_id_str = "-5"))))
    }

    @Test
    fun visibilityObjectIdBuildsJsonPayload() {
        assertEquals(
            """{"dyn_id":"123","dyn_type":1}""",
            buildDynamicVisibilityObjectId(dynamicId = "123", dynType = 1)
        )
    }

    @Test
    fun dynTypeFallsBackToThreePointParams() {
        val item = DynamicItem(
            modules = DynamicModules(
                module_more = DynamicMoreModule(
                    three_point_items = listOf(
                        DynamicThreePointItem(
                            type = "THREE_POINT_DELETE",
                            params = DynamicThreePointParams(dyn_type = 17)
                        )
                    )
                )
            )
        )
        assertEquals(17, resolveDynamicDynType(item))
    }

    @Test
    fun dynTypeDefaultsToZeroWhenUnavailable() {
        assertEquals(0, resolveDynamicDynType(DynamicItem()))
    }
}
