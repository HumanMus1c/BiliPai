// SPDX-License-Identifier: GPL-3.0-only
// Copyright (C) 2026 InstallerX Revived contributors
// Copyright (C) 2026 BiliPai contributors
package com.android.purebilibili.navigation3.predictiveback

import top.yukonga.miuix.kmp.nav.transition.NavTransition
import top.yukonga.miuix.kmp.nav.transition.NavTransitions

internal fun biliPaiMiuixNavTransition(
    animation: BiliPaiPredictiveBackAnimationStyle,
    exitDirection: BiliPaiPredictiveBackExitDirection,
): NavTransition = when (animation) {
    BiliPaiPredictiveBackAnimationStyle.NONE -> NoPredictiveBackTransition
    BiliPaiPredictiveBackAnimationStyle.MIUIX -> NavTransitions.MiuixDefault
    BiliPaiPredictiveBackAnimationStyle.AOSP -> AospNavTransition
    BiliPaiPredictiveBackAnimationStyle.SCALE -> scaleNavTransition(exitDirection)
    BiliPaiPredictiveBackAnimationStyle.CLASSIC -> ClassicNavTransition
}
