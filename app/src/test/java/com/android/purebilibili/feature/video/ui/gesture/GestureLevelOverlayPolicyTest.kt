package com.android.purebilibili.feature.video.ui.gesture

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeDown
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.BrightnessLow
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.ui.Alignment
import com.android.purebilibili.core.ui.AppTopTabPresentation
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GestureLevelOverlayPolicyTest {

    @Test
    fun overlayStyle_mapsChromePresentation() {
        assertEquals(
            GestureLevelOverlayStyle.Md3,
            resolveGestureLevelOverlayStyle(AppTopTabPresentation.MATERIAL_UNDERLINE)
        )
        assertEquals(
            GestureLevelOverlayStyle.Ios,
            resolveGestureLevelOverlayStyle(AppTopTabPresentation.MOVING_CAPSULE)
        )
        assertEquals(
            GestureLevelOverlayStyle.Miuix,
            resolveGestureLevelOverlayStyle(AppTopTabPresentation.TONAL_CAPSULE)
        )
    }

    @Test
    fun overlaySpec_differsByThemeLayout() {
        val md3 = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Md3,
            kind = GestureLevelKind.Volume,
            percent = 0.5f
        )
        val ios = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Ios,
            kind = GestureLevelKind.Volume,
            percent = 0.5f
        )
        val md3Brightness = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Md3,
            kind = GestureLevelKind.Brightness,
            percent = 0.5f
        )
        val miuixVolume = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Miuix,
            kind = GestureLevelKind.Volume,
            percent = 0.5f
        )
        val miuixBrightness = resolveGestureLevelOverlaySpec(
            style = GestureLevelOverlayStyle.Miuix,
            kind = GestureLevelKind.Brightness,
            percent = 0.5f
        )

        assertTrue(md3.verticalRail)
        assertFalse(ios.verticalRail)
        assertTrue(miuixVolume.verticalRail)
        assertEquals(Alignment.CenterEnd, md3.alignment)
        assertEquals(Alignment.CenterStart, md3Brightness.alignment)
        assertEquals(Alignment.Center, ios.alignment)
        assertEquals(Alignment.CenterEnd, miuixVolume.alignment)
        assertEquals(Alignment.CenterStart, miuixBrightness.alignment)
        assertTrue(ios.showLabel)
        assertFalse(md3.showLabel)
        assertFalse(miuixVolume.showPercentText)
    }

    @Test
    fun icons_resolveByLevelForAllStyles() {
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeOff,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Md3, GestureLevelKind.Volume, 0f)
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeUp,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Md3, GestureLevelKind.Volume, 1f)
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeOff,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Ios, GestureLevelKind.Volume, 0f)
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeUp,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Ios, GestureLevelKind.Volume, 1f)
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeOff,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Miuix, GestureLevelKind.Volume, 0f)
        )
        assertEquals(
            Icons.AutoMirrored.Filled.VolumeUp,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Miuix, GestureLevelKind.Volume, 1f)
        )
        assertEquals(
            Icons.Filled.BrightnessLow,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Md3, GestureLevelKind.Brightness, 0.2f)
        )
        assertEquals(
            Icons.Filled.LightMode,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Ios, GestureLevelKind.Brightness, 0.5f)
        )
        assertEquals(
            Icons.Filled.BrightnessHigh,
            resolveGestureLevelIcon(GestureLevelOverlayStyle.Md3, GestureLevelKind.Brightness, 0.9f)
        )
    }
}
