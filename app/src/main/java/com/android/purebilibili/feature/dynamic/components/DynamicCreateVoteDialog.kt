package com.android.purebilibili.feature.dynamic.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppDialogAction
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.components.AppFilterChip
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppTextField
import com.android.purebilibili.data.model.response.DynamicCreatedVote
import com.android.purebilibili.data.repository.DynamicCreateRepository
import kotlinx.coroutines.launch

@Composable
fun DynamicCreateVoteDialog(
    onDismiss: () -> Unit,
    onCreated: (DynamicCreatedVote) -> Unit
) {
    val scope = rememberCoroutineScope()
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var optionOne by remember { mutableStateOf("") }
    var optionTwo by remember { mutableStateOf("") }
    var optionThree by remember { mutableStateOf("") }
    var choiceCount by remember { mutableIntStateOf(1) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText("发起投票") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)
            ) {
                AppTextField(value = title, onValueChange = { title = it }, placeholder = "投票标题", singleLine = true)
                AppTextField(value = description, onValueChange = { description = it }, placeholder = "补充说明（可选）", singleLine = false, minLines = 2)
                AppTextField(value = optionOne, onValueChange = { optionOne = it }, placeholder = "选项 1", singleLine = true)
                AppTextField(value = optionTwo, onValueChange = { optionTwo = it }, placeholder = "选项 2", singleLine = true)
                AppTextField(value = optionThree, onValueChange = { optionThree = it }, placeholder = "选项 3（可选）", singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
                    AppFilterChip(
                        selected = choiceCount == 1,
                        onClick = { choiceCount = 1 },
                        label = { AppText("单选") }
                    )
                    AppFilterChip(
                        selected = choiceCount > 1,
                        onClick = { choiceCount = 2 },
                        label = { AppText("多选") }
                    )
                }
                errorMessage?.let { AppText(it) }
            }
        },
        confirmButton = {
            AppDialogAction(
                onClick = {
                    if (submitting) return@AppDialogAction
                    submitting = true
                    errorMessage = null
                    scope.launch {
                        DynamicCreateRepository.createVote(
                            title = title,
                            options = listOf(optionOne, optionTwo, optionThree),
                            description = description,
                            choiceCount = choiceCount,
                            durationSeconds = 24 * 60 * 60
                        ).fold(
                            onSuccess = { created ->
                                submitting = false
                                onCreated(created)
                            },
                            onFailure = { error ->
                                submitting = false
                                errorMessage = error.message ?: "创建失败"
                            }
                        )
                    }
                }
            ) {
                AppText(if (submitting) "创建中…" else "创建")
            }
        },
        dismissButton = {
            AppDialogAction(onClick = onDismiss) { AppText("取消") }
        }
    )
}