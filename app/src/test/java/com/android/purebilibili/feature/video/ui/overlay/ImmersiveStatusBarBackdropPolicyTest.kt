package com.android.purebilibili.feature.video.ui.overlay

import java.io.File
import kotlin.test.Test
import kotlin.test.assertTrue

class ImmersiveStatusBarBackdropPolicyTest {

    @Test
    fun `ambient haze keeps captured video colors free of theme tint`() {
        // Haze 2 的 HazeBlurStyle 是不透明程序，无法逐属性断言；改为核对样式工厂的关键写入。
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/ui/overlay/ImmersiveStatusBarBackdrop.kt"
        ).readText()
        val styleBody = source.substringAfter("fun resolveVideoStatusBarAmbientHazeStyle()")
            .substringBefore("}")

        assertTrue(styleBody.contains("backgroundColor(Color.Black)"))
        assertTrue(styleBody.contains("colorEffects(emptyList())"))
        assertTrue(styleBody.contains("blurRadius(24.dp)"))
        assertTrue(styleBody.contains("noiseFactor(0f)"))
        assertTrue(styleBody.contains("fallbackColorEffect(HazeColorEffect.tint(Color.Black))"))
    }

    @Test
    fun `ambient capture balances visual freshness with low cpu and battery overhead`() {
        assertTrue(VIDEO_STATUS_BAR_AMBIENT_CAPTURE_INTERVAL_MS >= 500L)
        assertEquals(96, VIDEO_STATUS_BAR_AMBIENT_SAMPLE_WIDTH_PX)
        assertEquals(54, VIDEO_STATUS_BAR_AMBIENT_SAMPLE_HEIGHT_PX)
    }

    @Test
    fun `ambient letterbox clips its blur and does not depend on global header blur`() {
        val source = File(
            "src/main/java/com/android/purebilibili/feature/video/ui/overlay/ImmersiveStatusBarBackdrop.kt"
        ).readText()
        val letterboxBody = source.substringAfter("internal fun ImmersiveAmbientLetterboxBackdrop(")
            .substringBefore("internal fun resolvePortraitLetterboxBarHeightPx(")

        assertTrue(letterboxBody.contains(".clipToBounds()"))
        assertTrue(letterboxBody.contains("surfaceType = BlurSurfaceType.GENERIC"))
    }
}
