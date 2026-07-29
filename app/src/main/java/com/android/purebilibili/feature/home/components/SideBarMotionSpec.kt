package com.android.purebilibili.feature.home.components

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

internal fun <T> sideBarSelectionScaleMotionSpec(): SpringSpec<T> = spring(
    dampingRatio = 0.35f,
    stiffness = 300f,
)

internal fun <T> sideBarWobbleMotionSpec(): SpringSpec<T> = spring(
    dampingRatio = 0.2f,
    stiffness = 600f,
)
