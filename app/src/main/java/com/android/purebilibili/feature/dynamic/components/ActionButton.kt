// 文件路径: feature/dynamic/components/ActionButton.kt
package com.android.purebilibili.feature.dynamic.components
import com.android.purebilibili.core.ui.components.AppIcon
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppSpacingTokens

import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.AppTypographyTokens
import com.android.purebilibili.core.ui.motion.AppMotionTokens
import com.android.purebilibili.feature.dynamic.DynamicStatusPalette
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.rememberAppCommentIcon
import com.android.purebilibili.core.ui.rememberAppLikeFilledIcon
import com.android.purebilibili.core.ui.rememberAppLikeIcon
import com.android.purebilibili.core.ui.rememberAppShareIcon
import com.android.purebilibili.core.theme.AppUiStyle
import com.android.purebilibili.core.theme.LocalAppUiStyle
import com.android.purebilibili.feature.dynamic.resolveDynamicActionButtonText
import top.yukonga.miuix.kmp.basic.Button as MiuixButton
/**
 *  iOS 风格操作按钮 - 现代化胶囊设计
 * 
 * @param icon 图标
 * @param count 数量
 * @param label 标签（点赞/评论/转发）
 * @param isActive 是否激活状态（如已点赞）
 * @param onClick 点击回调
 */
@Composable
fun ActionButton(
    count: Int,
    label: String,
    isActive: Boolean = false,
    enabled: Boolean = true,
    onClick: () -> Unit = {},
    activeColor: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f),
    modifier: Modifier = Modifier
) {
    val isLike = label == "点赞"
    val isForward = label == "转发"
    val isComment = label == "评论"
    
    //  iOS 风格按压动画
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "actionButtonScale"
    )
    
    //  统一主题颜色 - 根据激活状态调整
    val buttonColor = when {
        !enabled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(0.45f)
        isLike && isActive -> DynamicStatusPalette.liked()
        isLike -> MaterialTheme.colorScheme.onSurfaceVariant.copy(0.6f)
        isForward -> MaterialTheme.colorScheme.primary  // 使用主题色替代硬编码
        isComment -> MaterialTheme.colorScheme.primary
        else -> activeColor
    }
    
    //  优雅的图标 - 根据状态切换填充/描边
    val buttonIcon = when {
        isLike && isActive -> rememberAppLikeFilledIcon()
        isLike -> rememberAppLikeIcon()
        isForward -> rememberAppShareIcon()
        isComment -> rememberAppCommentIcon()
        else -> rememberAppCommentIcon()
    }
    val countFadeAnimationSpec = AppMotionTokens.standardSpec<Float>()
    val countSlideAnimationSpec = AppMotionTokens.standardSpec<IntOffset>()
    BoxWithConstraints(modifier = modifier) {
        val slotWidthDp = maxWidth.value.toInt()
        val actionText = remember(label, count, slotWidthDp) {
            resolveDynamicActionButtonText(
                label = label,
                count = count,
                slotWidthDp = slotWidthDp
            )
        }

        if (isForward || isComment) {
            when (LocalAppUiStyle.current) {
                AppUiStyle.MATERIAL3 -> FilledTonalButton(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    shape = AppShapes.container(ContainerLevel.Card),
                    contentPadding = PaddingValues(
                        horizontal = AppSpacingTokens.Small,
                        vertical = AppSpacingTokens.Small
                    )
                ) {
                    Icon(
                        imageVector = buttonIcon,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    DynamicNativeActionText(
                        actionText = actionText,
                        countFadeAnimationSpec = countFadeAnimationSpec,
                        countSlideAnimationSpec = countSlideAnimationSpec,
                        spacing = ButtonDefaults.IconSpacing
                    )
                }

                AppUiStyle.MIUIX -> MiuixButton(
                    onClick = onClick,
                    enabled = enabled,
                    modifier = Modifier.fillMaxWidth(),
                    insideMargin = PaddingValues(
                        horizontal = AppSpacingTokens.Small,
                        vertical = AppSpacingTokens.Medium
                    )
                ) {
                    AppIcon(
                        imageVector = buttonIcon,
                        contentDescription = null,
                        modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.Micro)
                    )
                    DynamicNativeActionText(
                        actionText = actionText,
                        countFadeAnimationSpec = countFadeAnimationSpec,
                        countSlideAnimationSpec = countSlideAnimationSpec,
                        spacing = AppSpacingTokens.ExtraSmall
                    )
                }
            }
            return@BoxWithConstraints
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = AppChromeSizeTokens.MinimumTouchTarget)
                .scale(scale)
                .clip(AppShapes.container(ContainerLevel.Pill))
                .background(
                    color = buttonColor.copy(alpha = if (isActive && isLike) 0.15f else 0.08f)
                )
                .clickable(
                    enabled = enabled,
                    interactionSource = interactionSource,
                    indication = null
                ) { onClick() }
                .padding(horizontal = AppSpacingTokens.Small + AppSpacingTokens.Micro, vertical = AppSpacingTokens.Small)
        ) {
            AppIcon(
                imageVector = buttonIcon,
                contentDescription = label,
                modifier = Modifier.size(AppSpacingTokens.Large + AppSpacingTokens.Micro),
                tint = buttonColor
            )

            if (actionText != null) {
                Spacer(modifier = Modifier.width(AppSpacingTokens.ExtraSmall))
                //  计数变化动画（对齐 BiliPai AnimatedSwitcher + ScaleTransition）
                AnimatedContent(
                    targetState = actionText,
                    transitionSpec = {
                        (fadeIn(animationSpec = countFadeAnimationSpec) +
                            slideInVertically(animationSpec = countSlideAnimationSpec) { it / 3 })
                            .togetherWith(
                                fadeOut(animationSpec = countFadeAnimationSpec) +
                                    slideOutVertically(animationSpec = countSlideAnimationSpec) { -it / 3 }
                            )
                    },
                    label = "actionButtonCount"
                ) { text ->
                    AppText(
                        text = text,
                        fontSize = MaterialTheme.typography.labelMedium.fontSize,
                        fontWeight = FontWeight.Medium,
                        color = buttonColor,
                        letterSpacing = AppTypographyTokens.ZeroLetterSpacing,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun DynamicNativeActionText(
    actionText: String?,
    countFadeAnimationSpec: androidx.compose.animation.core.FiniteAnimationSpec<Float>,
    countSlideAnimationSpec: androidx.compose.animation.core.FiniteAnimationSpec<IntOffset>,
    spacing: androidx.compose.ui.unit.Dp
) {
    if (actionText == null) return
    Spacer(modifier = Modifier.width(spacing))
    AnimatedContent(
        targetState = actionText,
        transitionSpec = {
            (fadeIn(animationSpec = countFadeAnimationSpec) +
                slideInVertically(animationSpec = countSlideAnimationSpec) { it / 3 })
                .togetherWith(
                    fadeOut(animationSpec = countFadeAnimationSpec) +
                        slideOutVertically(animationSpec = countSlideAnimationSpec) { -it / 3 }
                )
        },
        label = "nativeActionButtonCount"
    ) { text ->
        AppText(
            text = text,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}
