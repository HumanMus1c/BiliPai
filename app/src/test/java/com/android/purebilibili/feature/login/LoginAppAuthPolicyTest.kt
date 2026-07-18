package com.android.purebilibili.feature.login

import com.android.purebilibili.core.network.AppSignUtils
import kotlin.test.Test
import kotlin.test.assertEquals

class LoginAppAuthPolicyTest {

    @Test
    fun `sms send uses Android HD credential and device parameters`() {
        val params = buildAndroidSmsSendParams(
            phone = "13800138000",
            countryCode = 86,
            token = "recaptcha-token",
            challenge = "challenge",
            validate = "validate",
            seccode = "seccode",
            buvid = "buvid",
            loginSessionId = "session-id",
            timestampSeconds = 123L
        )

        assertEquals(AppSignUtils.ANDROID_HD_APP_KEY, params["appkey"])
        assertEquals("android_hd", params["mobi_app"])
        assertEquals("buvid", params["buvid"])
        assertEquals("recaptcha-token", params["recaptcha_token"])
        assertEquals("session-id", params["login_session_id"])
    }

    @Test
    fun `sms login uses Android HD credential parameters`() {
        val params = buildAndroidSmsLoginParams(
            phone = "13800138000",
            countryCode = 86,
            code = 123456,
            captchaKey = "captcha-key",
            buvid = "buvid",
            deviceId = "device-id",
            encryptedDeviceToken = "encrypted-token",
            timestampSeconds = 123L
        )

        assertEquals(AppSignUtils.ANDROID_HD_APP_KEY, params["appkey"])
        assertEquals("android_hd", params["mobi_app"])
        assertEquals("captcha-key", params["captcha_key"])
        assertEquals("123456", params["code"])
        assertEquals("device-id", params["device_id"])
        assertEquals("encrypted-token", params["dt"])
    }
}
