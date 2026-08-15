package com.android.purebilibili.core.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppTextButton

@Composable
fun CopySelectionDialog(
    text: String,
    title: String,
    onDismiss: () -> Unit
) {
    if (text.isBlank()) return
    val context = LocalContext.current

    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(text = title) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AppText(
                    text = "长按并拖拽选择需要复制的内容",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                SelectionContainer {
                    AppText(
                        text = text,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        },
        confirmButton = {
            AppTextButton(
                onClick = {
                    copyPlainTextToClipboard(context, text, title)
                    onDismiss()
                }
            ) {
                AppText("复制全部")
            }
        },
        dismissButton = {
            AppTextButton(onClick = onDismiss) {
                AppText("关闭")
            }
        }
    )
}
