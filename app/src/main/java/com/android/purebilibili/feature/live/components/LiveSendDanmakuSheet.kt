package com.android.purebilibili.feature.live.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.android.purebilibili.core.ui.components.AppButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import com.android.purebilibili.core.ui.AppModalBottomSheet
import com.android.purebilibili.core.ui.components.AppOutlinedTextField
import com.android.purebilibili.core.ui.components.AppSurface
import com.android.purebilibili.core.ui.components.AppText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.AppSurfaceTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.data.repository.LiveDanmakuPermission
import com.android.purebilibili.feature.live.LiveDanmakuItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveSendDanmakuSheet(
    onDismiss: () -> Unit,
    onSend: (String) -> Unit,
    permission: LiveDanmakuPermission = LiveDanmakuPermission(),
    replyTarget: LiveDanmakuItem? = null
) {
    var message by remember { mutableStateOf("") }
    val maxLength = permission.maxLength.takeIf { it > 0 } ?: 40
    AppModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = AppSpacingTokens.ExtraLarge,
                    vertical = AppSpacingTokens.Small,
                ),
            verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Large)
        ) {
            AppText(
                text = if (replyTarget == null) "发弹幕" else "回复 @${replyTarget.uname.ifBlank { replyTarget.uid.toString() }}",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            AppSurface(
                color = AppSurfaceTokens.cardContainer(),
                shape = AppShapes.container(ContainerLevel.Card)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppSpacingTokens.Large),
                    verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Medium)
                ) {
                    AppOutlinedTextField(
                        value = message,
                        onValueChange = { message = it.take(maxLength) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 4,
                        placeholder = { AppText(if (replyTarget == null) "输入弹幕内容" else "输入回复内容") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = {
                            val content = message.trim()
                            if (content.isNotEmpty() && permission.canSend) onSend(content)
                        })
                    )
                    AppText(
                        text = buildString {
                            append(permission.statusText)
                            append(" · ")
                            append(message.length)
                            append("/")
                            append(maxLength)
                            if (permission.availableColors.isNotEmpty()) {
                                append(" · ")
                                append(permission.availableColors.take(3).joinToString("、") { it.name })
                            }
                            if (permission.availableModes.isNotEmpty()) {
                                append(" · ")
                                append(permission.availableModes.take(2).joinToString("、") { it.name })
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        AppButton(
                            enabled = permission.canSend && message.trim().isNotEmpty(),
                            onClick = { onSend(message.trim()) }
                        ) {
                            AppText("发送")
                        }
                    }
                }
            }
        }
    }
}
