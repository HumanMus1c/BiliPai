package com.android.purebilibili.feature.dynamic.components

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DynamicPublishComposerPolicyTest {

    @Test
    fun composerTokenInsertionKeepsReadableSpacing() {
        assertEquals("@用户 ", appendDynamicComposerToken("", "@用户 "))
        assertEquals("正文 @用户 ", appendDynamicComposerToken("正文", "@用户 "))
        assertEquals("正文 [doge]", appendDynamicComposerToken("正文 ", "[doge]"))
    }

    @Test
    fun liquidDockReuseFollowsTheUserSetting() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/dynamic/components/DynamicPublishComposer.kt"
        ).readText()

        assertTrue(source.contains("reuseEnabled = liquidGlassEnabled"))
        assertTrue(source.contains("if (liquidGlassEnabled)"))
        assertTrue(source.contains("AppNativeSegmentedControl("))
    }
}
