package com.android.purebilibili.feature.home.components

import kotlin.test.Test
import kotlin.test.assertNull
import kotlin.test.assertSame
import top.yukonga.miuix.kmp.blur.Backdrop

class LiquidReuseIndicatorContentBackdropTest {

    private object PageBackdrop : Backdrop {
        override val isCoordinatesDependent: Boolean = false
        override fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBackdrop(
            density: androidx.compose.ui.unit.Density,
            coordinates: androidx.compose.ui.layout.LayoutCoordinates?,
            layerBlock: (androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit)?,
            downscaleFactor: Int,
        ) = Unit
    }

    private object ExportBackdrop : Backdrop {
        override val isCoordinatesDependent: Boolean = false
        override fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBackdrop(
            density: androidx.compose.ui.unit.Density,
            coordinates: androidx.compose.ui.layout.LayoutCoordinates?,
            layerBlock: (androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit)?,
            downscaleFactor: Int,
        ) = Unit
    }

    private object Combined : Backdrop {
        override val isCoordinatesDependent: Boolean = false
        override fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBackdrop(
            density: androidx.compose.ui.unit.Density,
            coordinates: androidx.compose.ui.layout.LayoutCoordinates?,
            layerBlock: (androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit)?,
            downscaleFactor: Int,
        ) = Unit
    }

    private object CoordinateDependentPageBackdrop : Backdrop {
        override val isCoordinatesDependent: Boolean = true
        override fun androidx.compose.ui.graphics.drawscope.DrawScope.drawBackdrop(
            density: androidx.compose.ui.unit.Density,
            coordinates: androidx.compose.ui.layout.LayoutCoordinates?,
            layerBlock: (androidx.compose.ui.graphics.GraphicsLayerScope.() -> Unit)?,
            downscaleFactor: Int,
        ) = Unit
    }

    @Test
    fun inContentReuseRejectsCoordinateDependentPageBackdrop() {
        assertNull(
            resolveInContentLiquidSamplingBackdrop(CoordinateDependentPageBackdrop)
        )
    }

    @Test
    fun inContentReuseKeepsCoordinateIndependentBackdrop() {
        assertSame(
            PageBackdrop,
            resolveInContentLiquidSamplingBackdrop(PageBackdrop)
        )
    }

    @Test
    fun prefersCombinedWhenPageExportAndCombinedProvided() {
        val result = resolveLiquidReuseIndicatorContentBackdrop(
            pageBackdrop = PageBackdrop,
            exportBackdrop = ExportBackdrop,
            useCombined = true,
            combinedBackdrop = Combined,
        )
        assertSame(Combined, result)
    }

    @Test
    fun prefersPageWhenCombinedNotRequested() {
        val result = resolveLiquidReuseIndicatorContentBackdrop(
            pageBackdrop = PageBackdrop,
            exportBackdrop = ExportBackdrop,
            useCombined = false,
            combinedBackdrop = Combined,
        )
        assertSame(PageBackdrop, result)
    }

    @Test
    fun neverFallsBackToExportOnlyToAvoidBlackSample() {
        val result = resolveLiquidReuseIndicatorContentBackdrop(
            pageBackdrop = null,
            exportBackdrop = ExportBackdrop,
            useCombined = true,
            combinedBackdrop = null,
        )
        assertNull(result)
    }

    @Test
    fun usesAlignedLocalExportWhenCallerGuaranteesItIsSafe() {
        val result = resolveLiquidReuseIndicatorContentBackdrop(
            pageBackdrop = null,
            exportBackdrop = ExportBackdrop,
            useCombined = false,
            combinedBackdrop = null,
            allowExportOnly = true,
        )
        assertSame(ExportBackdrop, result)
    }

    @Test
    fun returnsNullWhenNoPage() {
        val result = resolveLiquidReuseIndicatorContentBackdrop(
            pageBackdrop = null,
            exportBackdrop = null,
            useCombined = true,
            combinedBackdrop = null,
        )
        assertNull(result)
    }
}
