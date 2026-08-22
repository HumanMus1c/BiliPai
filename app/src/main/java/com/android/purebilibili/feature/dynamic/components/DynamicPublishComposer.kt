package com.android.purebilibili.feature.dynamic.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.AppChromeSizeTokens
import com.android.purebilibili.core.ui.AppDialogAction
import com.android.purebilibili.core.ui.AppShapes
import com.android.purebilibili.core.ui.AppSpacingTokens
import com.android.purebilibili.core.ui.ContainerLevel
import com.android.purebilibili.core.ui.components.AppText
import com.android.purebilibili.core.ui.components.AppTextButton
import com.android.purebilibili.core.ui.components.AppTextField
import com.android.purebilibili.feature.home.components.BottomBarLiquidSegmentedControl
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import com.android.purebilibili.data.model.response.DynamicCreatedReserve
import com.android.purebilibili.data.model.response.DynamicCreatedVote
import com.android.purebilibili.data.model.response.DynamicPublishDraft

@Composable
fun DynamicPublishComposer(
    initialText: String,
    isEditing: Boolean,
    submitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSubmit: (DynamicPublishDraft) -> Unit
) {
    var text by remember(initialText) { mutableStateOf(initialText) }
    var title by remember { mutableStateOf("") }
    var imageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var vote by remember { mutableStateOf<DynamicCreatedVote?>(null) }
    var reserve by remember { mutableStateOf<DynamicCreatedReserve?>(null) }
    var privatePublish by remember { mutableStateOf(false) }
    var showVoteDialog by remember { mutableStateOf(false) }
    var showReserveDialog by remember { mutableStateOf(false) }
    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris ->
        if (uris.isNotEmpty()) {
            imageUris = (imageUris + uris).distinct().take(9)
        }
    }

    AppAlertDialog(
        onDismissRequest = onDismiss,
        title = { AppText(if (isEditing) "编辑动态" else "发布动态") },
        text = {
            val publishChromeBackdrop = rememberLayerBackdrop()
            val visibilityLabels = remember { listOf("公开", "仅自己可见") }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .layerBackdrop(publishChromeBackdrop),
                verticalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)
            ) {
                if (!isEditing) {
                    AppTextField(
                        value = title,
                        onValueChange = { if (it.length <= 20) title = it },
                        placeholder = "标题，选填 20 字",
                        singleLine = true
                    )
                }
                AppTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = "说点什么吧…",
                    singleLine = false,
                    minLines = 4
                )
                if (!isEditing) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
                        items(imageUris, key = { it.toString() }) { uri ->
                            AsyncImage(
                                model = uri,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(AppShapes.container(ContainerLevel.Chip)),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacingTokens.Small)) {
                        AppTextButton(
                            onClick = {
                                picker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                        ) { AppText(if (imageUris.isEmpty()) "添加图片" else "再加图片") }
                        AppTextButton(onClick = { showVoteDialog = true }) {
                            AppText(vote?.title?.let { "投票：$it" } ?: "发起投票")
                        }
                        AppTextButton(onClick = { showReserveDialog = true }) {
                            AppText(reserve?.title?.let { "预约：$it" } ?: "直播预约")
                        }
                    }
                    BottomBarLiquidSegmentedControl(
                        items = visibilityLabels,
                        selectedIndex = if (privatePublish) 1 else 0,
                        onSelected = { index -> privatePublish = index == 1 },
                        itemWidth = 88.dp,
                        height = AppChromeSizeTokens.BottomBarMatchedSegmentedControlHeightDp.dp,
                        indicatorHeight = AppChromeSizeTokens.BottomBarMatchedSegmentedIndicatorHeightDp.dp,
                        labelFontSize = 13.sp,
                        miuixBackdrop = publishChromeBackdrop,
                        forceLiquidChrome = false,
                        liquidGlassEffectsEnabled = true,
                        tapPressRefractionEnabled = true,
                    )
                }
                errorMessage?.let { AppText(it) }
            }
        },
        confirmButton = {
            AppDialogAction(
                onClick = {
                    if (submitting) return@AppDialogAction
                    onSubmit(
                        DynamicPublishDraft(
                            text = text,
                            title = title,
                            imageUris = imageUris.map(Uri::toString),
                            voteId = vote?.voteId ?: 0L,
                            voteTitle = vote?.title.orEmpty(),
                            reserveId = reserve?.reserveId ?: 0L,
                            private = privatePublish
                        )
                    )
                }
            ) {
                AppText(
                    when {
                        submitting -> "发布中…"
                        isEditing -> "保存"
                        else -> "发布"
                    }
                )
            }
        },
        dismissButton = {
            AppDialogAction(onClick = onDismiss) { AppText("取消") }
        }
    )

    if (showVoteDialog) {
        DynamicCreateVoteDialog(
            onDismiss = { showVoteDialog = false },
            onCreated = { created ->
                vote = created
                showVoteDialog = false
            }
        )
    }
    if (showReserveDialog) {
        DynamicCreateReserveDialog(
            onDismiss = { showReserveDialog = false },
            onCreated = { created ->
                reserve = created
                showReserveDialog = false
            }
        )
    }
}