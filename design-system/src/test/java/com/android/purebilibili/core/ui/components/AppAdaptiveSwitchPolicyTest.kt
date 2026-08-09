package com.android.purebilibili.core.ui.components

import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.theme.AppUiStyle
import kotlin.test.Test
import kotlin.test.assertEquals

class AppAdaptiveSwitchPolicyTest {

    @Test
    fun `material3 style uses material switch treatment`() {
        assertEquals(
            AppAdaptiveSwitchTreatment.MATERIAL,
            resolveAppAdaptiveSwitchTreatment(uiStyle = AppUiStyle.MATERIAL3)
        )
    }

    @Test
    fun `miuix style uses miuix switch treatment`() {
        assertEquals(
            AppAdaptiveSwitchTreatment.MIUIX,
            resolveAppAdaptiveSwitchTreatment(uiStyle = AppUiStyle.MIUIX)
        )
    }

    @Test
    fun `checked thumb stays light when onPrimary is dark on bright themes`() {
        // 亮主题色下 onPrimary 可能被判为黑色 → 回退白色,避免选中后 thumb 变黑
        assertEquals(
            Color.White,
            resolveSwitchCheckedThumbColor(onPrimary = Color.Black),
        )
        assertEquals(
            Color.White,
            resolveSwitchCheckedThumbColor(onPrimary = Color(0xFF1C1B1F)),
        )
    }

    @Test
    fun `checked thumb keeps onPrimary when it is light`() {
        val lightOnPrimary = Color(0xFFFFFFFF)
        assertEquals(lightOnPrimary, resolveSwitchCheckedThumbColor(onPrimary = lightOnPrimary))
        assertEquals(
            Color(0xFFF5EFFF),
            resolveSwitchCheckedThumbColor(onPrimary = Color(0xFFF5EFFF)),
        )
    }
}

