package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptivePullToRefreshPolicyTest {

    @Test
    fun `miuix variant routes to miuix bridged renderer`() {
        assertEquals(
            PresetPrimitiveRenderer.MIUIX_BRIDGED,
            resolveAdaptivePullToRefreshRenderer(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
    }

    @Test
    fun `material md3 variant keeps material renderer`() {
        assertEquals(
            PresetPrimitiveRenderer.MATERIAL3,
            resolveAdaptivePullToRefreshRenderer(
                uiPreset = UiPreset.MD3,
                androidNativeVariant = AndroidNativeVariant.MATERIAL3
            )
        )
    }

    @Test
    fun `ios preset keeps ios renderer`() {
        assertEquals(
            PresetPrimitiveRenderer.IOS,
            resolveAdaptivePullToRefreshRenderer(
                uiPreset = UiPreset.IOS,
                androidNativeVariant = AndroidNativeVariant.MIUIX
            )
        )
    }

    @Test
    fun `miuix refresh texts use localized home hints`() {
        assertEquals(
            listOf("下拉刷新...", "松手刷新", "正在刷新...", "刷新完成"),
            resolveMiuixPullToRefreshTexts()
        )
    }

    @Test
    fun `pull refresh indicator top inset clamps overlay chrome height`() {
        assertEquals(0f, resolvePullRefreshIndicatorTopInsetDp(-8f), 0.001f)
        assertEquals(0f, resolvePullRefreshIndicatorTopInsetDp(0f), 0.001f)
        assertEquals(96f, resolvePullRefreshIndicatorTopInsetDp(96f), 0.001f)
        assertEquals(0f, resolveScaffoldedPullRefreshIndicatorTopInsetDp(), 0.001f)
    }
}
