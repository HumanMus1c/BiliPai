package com.android.purebilibili.core.ui

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertTrue

class IosContinuousCornerShapeTest {

    @Test
    fun `default continuous corner controls stay curved instead of chamfered`() {
        val inset = resolveIosContinuousCornerControlInset(IOS_CONTINUOUS_CORNER_SMOOTHING)

        assertTrue(inset in 0.30f..0.50f)
    }

    private val density = Density(1f)

    @Test
    fun continuousShape_producesGenericOutline() {
        val shape = IosContinuousRoundedCornerShape(cornerRadius = 12.dp)
        val outline = shape.createOutline(
            size = Size(120f, 80f),
            layoutDirection = LayoutDirection.Ltr,
            density = density
        )
        assertIs<Outline.Generic>(outline)
    }

    @Test
    fun bottomSheetShape_supportsTopOnlyCorners() {
        val shape = IosContinuousRoundedCornerShape(
            topStart = 14.dp,
            topEnd = 14.dp
        )
        val outline = shape.createOutline(
            size = Size(200f, 120f),
            layoutDirection = LayoutDirection.Ltr,
            density = density
        )
        assertIs<Outline.Generic>(outline)
    }
}
