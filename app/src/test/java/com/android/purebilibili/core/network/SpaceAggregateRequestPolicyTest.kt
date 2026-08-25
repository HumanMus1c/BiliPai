package com.android.purebilibili.core.network

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SpaceAggregateRequestPolicyTest {

    @Test
    fun `guest space aggregate request is app signed without access key`() {
        val params = buildSpaceAggregateParams(mid = 2L, accessToken = null)

        assertEquals("2", params["vmid"])
        assertEquals(AppSignUtils.ANDROID_APP_KEY, params["appkey"])
        assertTrue(params["ts"].orEmpty().isNotBlank())
        assertTrue(params["sign"].orEmpty().isNotBlank())
        assertFalse(params.containsKey("access_key"))
    }

    @Test
    fun `authenticated space aggregate request includes access key before signing`() {
        val params = buildSpaceAggregateParams(mid = 2L, accessToken = "token")

        assertEquals("token", params["access_key"])
        assertTrue(params["sign"].orEmpty().isNotBlank())
    }
}
