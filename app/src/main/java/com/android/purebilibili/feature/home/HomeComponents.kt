// 文件路径: feature/home/HomeComponents.kt
// 此文件包含对话框和错误状态展示
// UserState 定义在 HomeViewModel.kt 中
package com.android.purebilibili.feature.home
import com.android.purebilibili.core.ui.components.AppText

import com.android.purebilibili.core.ui.AppSpacingTokens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppFilterChip
import com.android.purebilibili.core.ui.components.AppTextButton

// ==========================================
// 对话框组件
// ==========================================

/**
 * 欢迎对话框
 */
@Composable
fun WelcomeDialog(githubUrl: String, onConfirm: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    AppAlertDialog(
        onDismissRequest = { },
        title = { AppText("欢迎") },
        text = {
            Column {
                AppText("本应用仅供学习使用。")
                AppTextButton(onClick = { uriHandler.openUri(githubUrl) }) {
                    AppText("开源地址: $githubUrl", fontSize = MaterialTheme.typography.labelSmall.fontSize, color = MaterialTheme.colorScheme.primary)
                }
            }
        },
        confirmButton = {
            AppButton(onClick = onConfirm, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) {
                AppText("进入")
            }
        },
        containerColor = AppSurfaceTokens.cardContainer()
    )
}

/**
 * 错误状态展示
 */
@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppText(text = message, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(AppSpacingTokens.Large))
            AppButton(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                AppText("重试")
            }
        }
    }
}

// ==========================================
// 直播子分类组件
// ==========================================

/**
 *  直播子分类行（关注/热门切换）
 */
@Composable
fun LiveSubCategoryRow(
    selectedSubCategory: LiveSubCategory,
    onSubCategorySelected: (LiveSubCategory) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppSpacingTokens.Small),
        horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium)
    ) {
        LiveSubCategory.entries.forEach { subCategory ->
            val isSelected = selectedSubCategory == subCategory
            AppFilterChip(
                selected = isSelected,
                onClick = { onSubCategorySelected(subCategory) },
                label = { AppText(stringResource(resolveLiveSubCategoryLabelRes(subCategory))) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,  //  使用主题色
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary     //  使用主题对应的前景色
                )
            )
        }
    }
}
