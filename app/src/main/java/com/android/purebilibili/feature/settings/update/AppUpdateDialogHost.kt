package com.android.purebilibili.feature.settings

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.android.purebilibili.core.ui.AppAlertDialog
import com.android.purebilibili.core.ui.components.AppButton
import com.android.purebilibili.core.ui.components.AppHorizontalDivider
import com.android.purebilibili.core.ui.components.AppLinearProgressIndicator
import com.android.purebilibili.core.ui.components.AppOutlinedButton
import com.android.purebilibili.core.ui.components.AppText
import kotlinx.coroutines.delay
import java.io.File

/** Style-neutral update dialog host backed by the adaptive dialog facade. */
@Composable
internal fun AppUpdateDialogHost(
    update: AppUpdateCheckResult,
    showReleaseNotesOnly: Boolean = false,
    onDismissRequest: () -> Unit,
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val preferredAsset = remember(update.assets) { selectPreferredAppUpdateAsset(update.assets) }
    val expectedSha256 = remember(preferredAsset, update.buildMetadata) {
        preferredAsset?.let { resolveAppUpdateExpectedSha256(it, update.buildMetadata) }
    }
    var downloadState by remember(update.latestVersion) { mutableStateOf(AppUpdateDownloadState()) }

    LaunchedEffect(update.latestVersion, expectedSha256) {
        while (true) {
            downloadState = AppUpdateDownloadCoordinator.readState(
                context = context,
                releaseVersion = update.latestVersion,
                checksumProvided = expectedSha256 != null,
            )
            delay(500)
        }
    }

    val onPrimaryAction: () -> Unit = {
        val completedFile = downloadState.filePath
            ?.takeIf { downloadState.status == AppUpdateDownloadStatus.COMPLETED }
            ?.let(::File)
            ?.takeIf(File::exists)
        when {
            completedFile != null -> {
                val action = installDownloadedAppUpdate(context, completedFile)
                if (action == AppUpdateInstallAction.OPEN_UNKNOWN_SOURCES_SETTINGS) {
                    Toast.makeText(context, "请先允许安装未知来源应用", Toast.LENGTH_SHORT).show()
                }
            }
            preferredAsset != null -> AppUpdateDownloadCoordinator.enqueue(
                context = context,
                asset = preferredAsset,
                releaseVersion = update.latestVersion,
                expectedSha256 = expectedSha256,
            )
            else -> uriHandler.openUri(update.releaseUrl)
        }
        Unit
    }
    val onCancelDownload: () -> Unit = {
        preferredAsset?.let { AppUpdateDownloadCoordinator.cancel(context, it.name) }
        Unit
    }
    val title = if (showReleaseNotesOnly) {
        "更新日志 v${update.latestVersion}"
    } else {
        "发现新版本 v${update.latestVersion}"
    }

    AppAlertDialog(
        onDismissRequest = onDismissRequest,
        title = { AppText(title) },
        text = {
            UpdateDialogBody(
                update = update,
                state = downloadState,
                showReleaseNotesOnly = showReleaseNotesOnly,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            if (!showReleaseNotesOnly) {
                AppButton(
                    onClick = onPrimaryAction,
                    enabled = downloadState.status != AppUpdateDownloadStatus.DOWNLOADING,
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) { AppText(updatePrimaryLabel(downloadState, preferredAsset != null)) }
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (downloadState.status == AppUpdateDownloadStatus.DOWNLOADING ||
                    downloadState.status == AppUpdateDownloadStatus.QUEUED
                ) {
                    AppOutlinedButton(
                        onClick = onCancelDownload,
                        modifier = Modifier.sizeIn(minHeight = 48.dp),
                    ) { AppText("取消下载") }
                }
                AppOutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.sizeIn(minHeight = 48.dp),
                ) { AppText(if (showReleaseNotesOnly) "关闭" else "稍后") }
            }
        },
    )
}

@Composable
private fun UpdateDialogBody(
    update: AppUpdateCheckResult,
    state: AppUpdateDownloadState,
    showReleaseNotesOnly: Boolean,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier.heightIn(max = 360.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        UpdateDialogText("当前版本 v${update.currentVersion}", MaterialTheme.typography.bodyMedium)
        if (!showReleaseNotesOnly) {
            selectPreferredAppUpdateAsset(update.assets)?.let { asset ->
                UpdateDialogText("安装包：${asset.name}", MaterialTheme.typography.bodySmall)
            }
            UpdateDownloadProgress(state)
        }
        AppHorizontalDivider()
        UpdateReleaseNotesContent(update.releaseNotes)
    }
}

@Composable
private fun UpdateDownloadProgress(state: AppUpdateDownloadState) {
    if (state.status == AppUpdateDownloadStatus.IDLE) return
    val statusText = when (state.status) {
        AppUpdateDownloadStatus.QUEUED -> "等待网络后开始下载"
        AppUpdateDownloadStatus.DOWNLOADING -> "下载中 ${(state.progress * 100).toInt()}%"
        AppUpdateDownloadStatus.COMPLETED -> "下载完成，可安装"
        AppUpdateDownloadStatus.FAILED -> state.errorMessage ?: "下载失败"
        AppUpdateDownloadStatus.IDLE -> ""
    }
    UpdateDialogText(statusText, MaterialTheme.typography.bodySmall)
    if (state.status == AppUpdateDownloadStatus.DOWNLOADING || state.status == AppUpdateDownloadStatus.QUEUED) {
        if (state.totalBytes > 0L) {
            AppLinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
        } else {
            AppLinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
    if (state.status == AppUpdateDownloadStatus.COMPLETED) {
        UpdateDialogText(
            if (state.checksumProvided) "SHA-256 校验通过" else "Release 未提供 SHA-256 校验信息",
            MaterialTheme.typography.bodySmall,
        )
    }
}

@Composable
private fun UpdateReleaseNotesContent(releaseNotes: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        parseUpdateReleaseNotes(releaseNotes).forEach { block ->
            when (block) {
                is AppUpdateReleaseNotesBlock.Heading -> UpdateDialogText(
                    block.text,
                    if (block.level == 1) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                    FontWeight.SemiBold,
                )
                is AppUpdateReleaseNotesBlock.Bullet -> UpdateDialogText(
                    text = if (block.ordered) "• ${block.text}" else "• ${block.text}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                AppUpdateReleaseNotesBlock.Divider -> AppHorizontalDivider()
                is AppUpdateReleaseNotesBlock.Paragraph -> UpdateDialogText(
                    block.text,
                    MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun UpdateDialogText(
    text: String,
    style: androidx.compose.ui.text.TextStyle,
    fontWeight: FontWeight? = null,
) {
    AppText(text = text, style = style, fontWeight = fontWeight)
}

private fun updatePrimaryLabel(state: AppUpdateDownloadState, hasAsset: Boolean): String = when {
    state.status == AppUpdateDownloadStatus.COMPLETED -> "安装更新"
    state.status == AppUpdateDownloadStatus.QUEUED -> "等待下载"
    state.status == AppUpdateDownloadStatus.DOWNLOADING -> "下载中"
    !hasAsset -> "前往下载"
    else -> "下载更新"
}
