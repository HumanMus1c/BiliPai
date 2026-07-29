package com.android.purebilibili.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.theme.UiPreset
import com.android.purebilibili.core.ui.IosContinuousRoundedCornerShape
import kotlin.test.Test
import kotlin.test.assertIs
import kotlin.test.assertSame

class ResolveAdaptiveGroupSurfaceShapeTest {

    private val defaultShape = RoundedCornerShape(12.dp)
    private val continuousShape = IosContinuousRoundedCornerShape(14.dp)
    private val border = BorderStroke(0.6.dp, Color.Black)

    @Test
    fun iosBorderedGroup_fallsBackToStandardRoundedCorners() {
        val shape = resolveAdaptiveGroupSurfaceShape(
            uiPreset = UiPreset.IOS,
            requestedShape = continuousShape,
            defaultShape = defaultShape,
            border = border
        )
        assertSame(defaultShape, shape)
    }

    @Test
    fun iosBorderlessGroup_keepsContinuousCorners() {
        val shape = resolveAdaptiveGroupSurfaceShape(
            uiPreset = UiPreset.IOS,
            requestedShape = continuousShape,
            defaultShape = defaultShape,
            border = null
        )
        assertSame(continuousShape, shape)
    }

    @Test
    fun md3BorderedGroup_keepsRequestedShape() {
        val shape = resolveAdaptiveGroupSurfaceShape(
            uiPreset = UiPreset.MD3,
            requestedShape = continuousShape,
            defaultShape = defaultShape,
            border = border
        )
        assertSame(continuousShape, shape)
    }

    @Test
    fun nullRequestedShape_usesDefault() {
        val shape = resolveAdaptiveGroupSurfaceShape(
            uiPreset = UiPreset.IOS,
            requestedShape = null,
            defaultShape = defaultShape,
            border = border
        )
        assertIs<RoundedCornerShape>(shape)
        assertSame(defaultShape, shape)
    }
}
