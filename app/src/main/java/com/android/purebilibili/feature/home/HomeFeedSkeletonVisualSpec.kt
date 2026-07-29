package com.android.purebilibili.feature.home

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal fun resolveHomeSkeletonCoverShape(cornerRadius: Dp): Shape {
    return RoundedCornerShape(
        topStart = cornerRadius,
        topEnd = cornerRadius,
        bottomStart = 0.dp,
        bottomEnd = 0.dp,
    )
}

internal fun resolveHomeSkeletonInfoShape(cornerRadius: Dp): Shape {
    return RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = cornerRadius,
        bottomEnd = cornerRadius,
    )
}
