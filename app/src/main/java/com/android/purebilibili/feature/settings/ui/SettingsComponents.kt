package com.android.purebilibili.feature.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.theme.iOSBlue
import com.android.purebilibili.core.ui.blur.BlurIntensity
import com.android.purebilibili.core.ui.components.AppSegmentOption
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome

@Composable
fun BlurIntensitySelector(
    selectedIntensity: BlurIntensity,
    onIntensityChange: (BlurIntensity) -> Unit,
    modifier: Modifier = Modifier,
) {
    SettingsSingleChoicePreference(
        title = "模糊强度",
        subtitle = when (selectedIntensity) {
            BlurIntensity.THIN -> "平衡美观与性能（推荐）"
            BlurIntensity.APPLE_DOCK -> "强烈模糊，完全遮盖背景"
            BlurIntensity.THICK -> "背景颜色透出并增强磨砂质感"
        },
        options = listOf(
            AppSegmentOption(BlurIntensity.THIN, "标准"),
            AppSegmentOption(BlurIntensity.APPLE_DOCK, "玻璃拟态"),
            AppSegmentOption(BlurIntensity.THICK, "浓郁"),
        ),
        selectedValue = selectedIntensity,
        modifier = modifier,
        icon = Icons.Outlined.AutoAwesome,
        iconTint = iOSBlue,
        onSelectionChange = onIntensityChange,
    )
}
