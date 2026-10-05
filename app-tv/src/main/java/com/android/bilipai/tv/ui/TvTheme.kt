@file:OptIn(androidx.tv.material3.ExperimentalTvMaterial3Api::class)

package com.android.bilipai.tv.ui

import android.app.ActivityManager
import androidx.compose.ui.platform.LocalContext
import com.android.purebilibili.core.ui.motion.rememberSystemReduceMotion
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme
import com.android.purebilibili.core.theme.BiliPink
import com.android.purebilibili.core.theme.BiliPinkDark
import com.android.purebilibili.core.theme.DarkBackground
import com.android.purebilibili.core.theme.DarkSurface
import com.android.purebilibili.core.theme.DarkSurfaceVariant
import com.android.purebilibili.core.theme.TextPrimaryDark
import com.android.purebilibili.core.theme.TextSecondaryDark

@Composable
fun TvTheme(
    reduceMotion: Boolean = false,
    simpleEffects: Boolean = false,
    content: @Composable () -> Unit,
) {
    val systemReduceMotion = rememberSystemReduceMotion()
    val lowRam = (LocalContext.current.getSystemService(android.content.Context.ACTIVITY_SERVICE) as? ActivityManager)?.isLowRamDevice == true
    MaterialTheme(shapes = TvUiTokens.shapes, typography = TvUiTokens.typography, colorScheme = darkColorScheme(
        primary = BiliPink, onPrimary = DarkBackground,
        background = DarkBackground, onBackground = TextPrimaryDark,
        surface = DarkBackground, onSurface = TextPrimaryDark,
        surfaceVariant = DarkSurface, onSurfaceVariant = TextPrimaryDark,
        secondary = TextSecondaryDark, secondaryContainer = DarkSurfaceVariant,
        onSecondaryContainer = TextPrimaryDark,
        border = BiliPinkDark,
    )) {
        CompositionLocalProvider(
            LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            LocalTvReduceMotion provides (reduceMotion || systemReduceMotion),
            LocalTvSimpleEffects provides (simpleEffects || lowRam),
            content = content,
        )
    }
}
