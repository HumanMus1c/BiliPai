package com.android.purebilibili.feature.settings

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse

class AnimationSettingsScreenStructureTest {

    @Test
    fun animationSettingsScreen_leavesPredictiveBackToThePlatform() {
        val source = animationSettingsSource()

        assertFalse(source.contains("setPredictiveBackEnabled"))
        assertFalse(source.contains("setPredictiveBackAnimationStyle"))
        assertFalse(source.contains("setPredictiveBackExitDirection"))
        assertFalse(source.contains("resolvePredictiveBackStyleOptions"))
        assertFalse(source.contains("resolvePredictiveBackExitDirectionOptions"))
    }

    private fun animationSettingsSource(): String {
        return listOf(
            File("app/src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
            File("src/main/java/com/android/purebilibili/feature/settings/screen/AnimationSettingsScreen.kt"),
        ).first { it.exists() }.readText()
    }
}
