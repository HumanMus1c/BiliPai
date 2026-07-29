package com.android.purebilibili.core.ui.blur

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials

/**
 *  模糊样式管理
 * 
 * 模糊 + 饱和度增强 + 半透明底色 + 顶部高光 + 精细边框
 */
object BlurStyles {
    
    @OptIn(ExperimentalHazeMaterialsApi::class)
    @Composable
    fun getBlurStyle(intensity: BlurIntensity): HazeStyle {
        return when (resolveBlurHazeMaterial(intensity)) {
            BlurHazeMaterial.THIN -> HazeMaterials.thin()
            BlurHazeMaterial.ULTRA_THIN -> HazeMaterials.ultraThin()
            BlurHazeMaterial.THICK -> HazeMaterials.thick()
        }
    }

    @OptIn(ExperimentalHazeMaterialsApi::class)
    @Composable
    fun getBlurStyle(
        intensity: BlurIntensity,
        budget: BlurBudget?
    ): HazeStyle {
        val effectiveIntensity = if (budget != null) {
            resolveBudgetedBlurIntensity(intensity, budget)
        } else {
            intensity
        }
        return getBlurStyle(effectiveIntensity)
    }
    
    fun getBackgroundAlpha(intensity: BlurIntensity): Float {
        return resolveBlurBackgroundAlpha(intensity)
    }

    fun getBackgroundAlpha(
        intensity: BlurIntensity,
        budget: BlurBudget?
    ): Float {
        val effectiveIntensity = if (budget != null) {
            resolveBudgetedBlurIntensity(intensity, budget)
        } else {
            intensity
        }
        val alpha = getBackgroundAlpha(effectiveIntensity)
        val multiplier = budget?.backgroundAlphaMultiplier ?: 1f
        return (alpha * multiplier).coerceIn(0f, 1f)
    }
}
