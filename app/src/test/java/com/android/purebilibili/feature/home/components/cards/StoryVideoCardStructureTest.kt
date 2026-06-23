package com.android.purebilibili.feature.home.components.cards

import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StoryVideoCardStructureTest {

    @Test
    fun storySharedMetadataUsesShorterBoundsTimelineWithoutSpringOvershoot() {
        val source = File("src/main/java/com/android/purebilibili/feature/home/components/cards/StoryVideoCard.kt")
            .readText()
        val sharedTimelineCall = "videoMetadataSharedElementBoundsTransformSpec(cardSharedTransitionMotionSpec)"

        assertTrue(
            source.windowed(sharedTimelineCall.length).count { it == sharedTimelineCall } >= 5
        )
        assertFalse(source.contains("spring(dampingRatio = 0.8f, stiffness = 200f)"))
    }

    @Test
    fun storyCoverContainerClipsPlaceholderAndImageTogether() {
        val source = File("src/main/java/com/android/purebilibili/feature/home/components/cards/StoryVideoCard.kt")
            .readText()
        val coverContainerSource = source
            .substringAfter("// 卡片容器 (封面)")
            .substringBefore("// 封面比例由首页卡片样式统一配置。")

        assertTrue(source.contains("val coverShape = RoundedCornerShape(cardCornerRadius)"))
        assertTrue(coverContainerSource.contains(".testTag(\"home_story_video_cover\")"))
        assertTrue(coverContainerSource.contains(".aspectRatio(coverAspectRatio)"))
        assertTrue(coverContainerSource.contains(".clip(coverShape)"))
        assertTrue(coverContainerSource.contains(".background(MaterialTheme.colorScheme.surfaceVariant)"))
    }
}
