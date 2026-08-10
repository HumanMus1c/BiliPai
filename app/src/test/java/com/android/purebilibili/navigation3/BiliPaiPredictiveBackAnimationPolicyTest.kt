package com.android.purebilibili.navigation3

import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackAnimationStyle
import com.android.purebilibili.navigation3.predictiveback.BiliPaiPredictiveBackExitDirection
import org.junit.Assert.assertEquals
import org.junit.Test

class BiliPaiPredictiveBackAnimationPolicyTest {
    @Test
    fun `all transition storage values are stable`() {
        assertEquals(
            listOf("none", "aosp", "miuix", "scale", "ksu_classic"),
            BiliPaiPredictiveBackAnimationStyle.entries.map { it.storageValue },
        )
    }

    @Test
    fun `legacy and unknown transition values migrate to miuix`() {
        assertEquals(
            BiliPaiPredictiveBackAnimationStyle.MIUIX,
            BiliPaiPredictiveBackAnimationStyle.fromStorageValue("default"),
        )
        assertEquals(
            BiliPaiPredictiveBackAnimationStyle.CLASSIC,
            BiliPaiPredictiveBackAnimationStyle.fromStorageValue("classic"),
        )
        assertEquals(
            BiliPaiPredictiveBackAnimationStyle.MIUIX,
            BiliPaiPredictiveBackAnimationStyle.fromStorageValue("unknown"),
        )
    }

    @Test
    fun `scale exit direction defaults to always right`() {
        assertEquals(
            BiliPaiPredictiveBackExitDirection.ALWAYS_RIGHT,
            BiliPaiPredictiveBackExitDirection.fromStorageValue(null),
        )
    }
}
