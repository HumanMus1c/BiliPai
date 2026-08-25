package com.android.purebilibili.core.ui.renderer.material3

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.takeOrElse
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.components.AppCardColors
import com.android.purebilibili.core.ui.components.AppCardShape
import com.android.purebilibili.core.ui.components.AppCardVariant

@Composable
internal fun AppMaterial3Card(
    modifier: Modifier,
    shape: AppCardShape?,
    colors: AppCardColors?,
    variant: AppCardVariant,
    content: @Composable ColumnScope.() -> Unit,
) {
    val resolvedShape = shape?.toMaterial3Shape()
    when (variant) {
        AppCardVariant.Filled -> Card(
            modifier = modifier,
            shape = resolvedShape ?: CardDefaults.shape,
            colors = colors?.let {
                CardDefaults.cardColors(
                    containerColor = it.containerColor,
                    contentColor = it.contentColor.takeOrElse {
                        contentColorFor(it.containerColor)
                    },
                )
            } ?: CardDefaults.cardColors(),
            content = content,
        )
        AppCardVariant.Elevated -> ElevatedCard(
            modifier = modifier,
            shape = resolvedShape ?: CardDefaults.elevatedShape,
            colors = colors?.let {
                CardDefaults.elevatedCardColors(
                    containerColor = it.containerColor,
                    contentColor = it.contentColor.takeOrElse {
                        contentColorFor(it.containerColor)
                    },
                )
            } ?: CardDefaults.elevatedCardColors(),
            content = content,
        )
    }
}

private fun AppCardShape.toMaterial3Shape(): Shape = when (this) {
    is AppCardShape.Semantic -> AppShapes.resolveContainerShape(
        level = level,
        uiStyle = AppUiStyle.MATERIAL3,
    )
    is AppCardShape.Uniform -> RoundedCornerShape(cornerRadius)
}
