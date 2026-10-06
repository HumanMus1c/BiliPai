package com.android.purebilibili.core.ui.components

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import java.util.concurrent.atomic.AtomicInteger
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class PageAwareAsyncImageTest {
    @get:Rule val composeRule = createComposeRule()

    @Test
    fun inactivePageDefersRequestAndKeepsDecodedImageWhenHiddenAgain() {
        val allowed = mutableStateOf(false)
        val successes = AtomicInteger()
        val bitmap = solidBitmap(android.graphics.Color.RED)
        composeRule.setContent {
            CompositionLocalProvider(LocalPageImageLoadingAllowed provides allowed.value) {
                PageAwareAsyncImage(
                    model = bitmap,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).background(Color.Gray).testTag("page-image"),
                    onSuccess = { successes.incrementAndGet() },
                )
            }
        }
        composeRule.waitForIdle()
        assertEquals(0, successes.get())
        assertPixel(Color.Gray)
        composeRule.runOnIdle { allowed.value = true }
        composeRule.waitUntil(5_000) { successes.get() == 1 }
        composeRule.waitForIdle()
        assertPixel(Color.Red)
        composeRule.runOnIdle { allowed.value = false }
        composeRule.waitForIdle()
        assertEquals(1, successes.get())
        assertPixel(Color.Red)
    }

    @Test
    fun changedImageIdentityWhileInactiveDoesNotDisplayOldResult() {
        val allowed = mutableStateOf(true)
        val image = mutableStateOf(solidBitmap(android.graphics.Color.RED))
        val successes = AtomicInteger()
        composeRule.setContent {
            CompositionLocalProvider(LocalPageImageLoadingAllowed provides allowed.value) {
                PageAwareAsyncImage(
                    model = image.value,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).background(Color.Gray).testTag("page-image"),
                    onSuccess = { successes.incrementAndGet() },
                )
            }
        }
        composeRule.waitUntil(5_000) { successes.get() == 1 }
        composeRule.runOnIdle {
            allowed.value = false
            image.value = solidBitmap(android.graphics.Color.BLUE)
        }
        composeRule.waitForIdle()
        assertEquals(1, successes.get())
        assertPixel(Color.Gray)
        composeRule.runOnIdle { allowed.value = true }
        composeRule.waitUntil(5_000) { successes.get() == 2 }
        composeRule.waitForIdle()
        assertPixel(Color.Blue)
    }

    private fun solidBitmap(color: Int) = Bitmap.createBitmap(8, 8, Bitmap.Config.ARGB_8888).apply {
        eraseColor(color)
    }

    private fun assertPixel(expected: Color) {
        val image = composeRule.onNodeWithTag("page-image").captureToImage()
        val pixel = image.toPixelMap()[image.width / 2, image.height / 2]
        assertEquals(expected.red, pixel.red, 0.02f)
        assertEquals(expected.green, pixel.green, 0.02f)
        assertEquals(expected.blue, pixel.blue, 0.02f)
    }
}
