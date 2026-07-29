package com.android.purebilibili.feature.home.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

internal fun <T> iosTopTabCapsuleMotionSpec(): SpringSpec<T> = spring(
    dampingRatio = 0.68f,
    stiffness = Spring.StiffnessMediumLow,
)
