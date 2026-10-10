package com.android.purebilibili.feature.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay

@Composable
internal fun SettingsItemAnchor(
    settingId: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val request by SettingsSearchFocusController.request.collectAsStateWithLifecycle()
    val bringIntoView = remember { BringIntoViewRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    val view = LocalView.current
    var highlighted by remember { mutableStateOf(false) }
    LaunchedEffect(request?.token, settingId) {
        val current = request ?: return@LaunchedEffect
        if (current.settingId != settingId) return@LaunchedEffect
        try {
            keyboard?.hide()
            withFrameNanos { }
            bringIntoView.bringIntoView()
            highlighted = true
            view.announceForAccessibility("已定位到${settingsItemDirectory.firstOrNull { it.settingId == settingId }?.title.orEmpty()}")
            delay(1600)
        } finally {
            highlighted = false
            SettingsSearchFocusController.clear(current.token)
        }
    }
    Box(
        modifier.fillMaxWidth()
            .bringIntoViewRequester(bringIntoView)
            .background(if (highlighted) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent),
    ) { content() }
}
