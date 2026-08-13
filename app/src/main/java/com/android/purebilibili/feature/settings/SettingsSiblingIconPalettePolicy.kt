package com.android.purebilibili.feature.settings

import androidx.compose.ui.graphics.Color
import com.android.purebilibili.core.theme.ThemeColors
import com.android.purebilibili.core.theme.iOSBlue
import com.android.purebilibili.core.theme.iOSCoral
import com.android.purebilibili.core.theme.iOSGreen
import com.android.purebilibili.core.theme.iOSOrange
import com.android.purebilibili.core.theme.iOSPink
import com.android.purebilibili.core.theme.iOSPurple
import com.android.purebilibili.core.theme.iOSRed
import com.android.purebilibili.core.theme.iOSTeal

/**
 * 设置页同级条目的多彩色板。前八项延续现有视觉语言，后八项用于较长列表，
 * 从而在单个可见设置组内最多支持 16 个互不重复的图标色。
 */
internal val SettingsSiblingIconPalette: List<Color> = listOf(
    iOSPink,
    iOSOrange,
    iOSBlue,
    iOSGreen,
    iOSPurple,
    iOSTeal,
    iOSRed,
    iOSCoral,
    ThemeColors[8],
    ThemeColors[9],
    ThemeColors[5],
    ThemeColors[6],
    ThemeColors[12],
    ThemeColors[15],
    ThemeColors[17],
    ThemeColors[18],
)

internal fun resolveSettingsSiblingIconTint(
    siblingIndex: Int,
    paletteOffset: Int = 0,
): Color {
    val paletteIndex = Math.floorMod(siblingIndex + paletteOffset, SettingsSiblingIconPalette.size)
    return SettingsSiblingIconPalette[paletteIndex]
}

internal fun resolveSettingsSiblingIconTints(
    siblingCount: Int,
    paletteOffset: Int = 0,
): List<Color> {
    require(siblingCount in 0..SettingsSiblingIconPalette.size) {
        "A visible settings group supports at most ${SettingsSiblingIconPalette.size} unique icon colors"
    }
    return List(siblingCount) { index ->
        resolveSettingsSiblingIconTint(index, paletteOffset)
    }
}
