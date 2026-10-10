package com.android.purebilibili.feature.video

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.purebilibili.core.store.PlayerProgressPlacement
import com.android.purebilibili.feature.video.ui.overlay.BottomControlBar
import com.android.purebilibili.feature.video.ui.overlay.PlayerProgress
import com.android.purebilibili.feature.video.ui.overlay.TopControlBar
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PlayerControlsUiRegressionTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun castButton_respectsVisibilitySetting() {
        val castVisible = mutableStateOf(false)
        composeTestRule.setContent {
            MaterialTheme {
                TopControlBar(
                    title = "测试视频",
                    isFullscreen = false,
                    showCurrentTime = false,
                    showInteractiveActions = false,
                    showCastButton = castVisible.value,
                    onBack = {}
                )
            }
        }
        composeTestRule.onNodeWithContentDescription("投屏").assertDoesNotExist()

        composeTestRule.runOnIdle { castVisible.value = true }
        composeTestRule.onNodeWithContentDescription("投屏").assertExists()
    }

    @Test
    fun progressPlacement_movesProgressAcrossControlRow() {
        val placement = mutableStateOf(PlayerProgressPlacement.ABOVE_CONTROLS)
        composeTestRule.setContent {
            MaterialTheme {
                Box(
                    modifier = Modifier
                        .size(width = 800.dp, height = 180.dp)
                        .background(Color.Black)
                ) {
                    BottomControlBar(
                        isPlaying = true,
                        progress = PlayerProgress(current = 30_000, duration = 120_000),
                        isFullscreen = false,
                        progressPlacement = placement.value,
                        onPlayPauseClick = {},
                        onSeek = {},
                        onToggleFullscreen = {}
                    )
                }
            }
        }
        assertProgressIsAboveControls()

        composeTestRule.runOnIdle { placement.value = PlayerProgressPlacement.BOTTOM_EDGE }
        val progress = composeTestRule.onNodeWithTag("player_progress").fetchSemanticsNode().boundsInRoot
        val controls = composeTestRule.onNodeWithTag("player_control_row").fetchSemanticsNode().boundsInRoot
        assertTrue(progress.top >= controls.bottom)
    }

    @Test
    fun progressProviders_updatePausedTimeAndDurationWithSeekOverride() {
        val progress = mutableStateOf(PlayerProgress(current = 30_000, duration = 120_000))
        val seekOverride = mutableStateOf<Long?>(null)
        composeTestRule.setContent {
            MaterialTheme {
                Box(Modifier.size(width = 800.dp, height = 180.dp)) {
                    BottomControlBar(
                        isPlaying = false,
                        progressProvider = { progress.value },
                        seekPositionProvider = { seekOverride.value ?: progress.value.current },
                        isFullscreen = false,
                        onPlayPauseClick = {},
                        onSeek = {},
                        onToggleFullscreen = {},
                    )
                }
            }
        }
        composeTestRule.onNodeWithText("00:30 / 02:00").assertIsDisplayed()

        // Progress must remain live even though neither the host nor the play button changes.
        composeTestRule.runOnIdle {
            progress.value = PlayerProgress(current = 45_000, duration = 180_000, buffered = 90_000)
        }
        composeTestRule.onNodeWithText("00:45 / 03:00").assertIsDisplayed()

        composeTestRule.runOnIdle { seekOverride.value = 91_000L }
        composeTestRule.onNodeWithText("01:31 / 03:00").assertIsDisplayed()

        composeTestRule.runOnIdle {
            progress.value = progress.value.copy(current = 46_000)
        }
        composeTestRule.onNodeWithText("01:31 / 03:00").assertIsDisplayed()

        composeTestRule.runOnIdle { seekOverride.value = null }
        composeTestRule.onNodeWithText("00:46 / 03:00").assertIsDisplayed()
    }

    private fun assertProgressIsAboveControls() {
        val progress = composeTestRule.onNodeWithTag("player_progress").fetchSemanticsNode().boundsInRoot
        val controls = composeTestRule.onNodeWithTag("player_control_row").fetchSemanticsNode().boundsInRoot
        assertTrue(progress.bottom <= controls.top)
    }
}
