package com.android.purebilibili.core.ui

import com.android.purebilibili.core.theme.AndroidNativeVariant
import com.android.purebilibili.core.theme.UiPreset
import kotlin.test.Test
import kotlin.test.assertEquals

class AppPullRefreshIndicatorPolicyTest {

    @Test
    fun refreshIndicatorRendererSwapsToMaterialWhenNotIos() {
        assertEquals(
            AppPullRefreshIndicatorRenderer.CUPERTINO,
            resolveAppPullRefreshIndicatorRenderer(UiPreset.IOS, AndroidNativeVariant.MATERIAL3),
        )
        assertEquals(
            AppPullRefreshIndicatorRenderer.MATERIAL3,
            resolveAppPullRefreshIndicatorRenderer(UiPreset.MD3, AndroidNativeVariant.MATERIAL3),
        )
        assertEquals(
            AppPullRefreshIndicatorRenderer.MIUIX,
            resolveAppPullRefreshIndicatorRenderer(UiPreset.MD3, AndroidNativeVariant.MIUIX),
        )
    }
}
