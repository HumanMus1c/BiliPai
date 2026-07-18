package com.android.purebilibili.feature.login

import com.android.purebilibili.core.network.AppSignUtils

internal fun buildAndroidSmsSendParams(
    phone: String,
    countryCode: Int,
    token: String,
    challenge: String,
    validate: String,
    seccode: String,
    buvid: String,
    loginSessionId: String,
    timestampSeconds: Long
): Map<String, String> = buildMap {
    putAll(androidLoginBaseParams(timestampSeconds))
    put("buvid", buvid)
    put("local_id", buvid)
    put("login_session_id", loginSessionId)
    put("cid", countryCode.toString())
    put("tel", phone)
    put("recaptcha_token", token)
    put("gee_challenge", challenge)
    put("gee_validate", validate)
    put("gee_seccode", seccode)
}

internal fun buildAndroidSmsLoginParams(
    phone: String,
    countryCode: Int,
    code: Int,
    captchaKey: String,
    buvid: String,
    deviceId: String,
    encryptedDeviceToken: String,
    timestampSeconds: Long
): Map<String, String> = buildMap {
    putAll(androidLoginBaseParams(timestampSeconds))
    put("bili_local_id", deviceId)
    put("buvid", buvid)
    put("device", "phone")
    put("device_id", deviceId)
    put("device_name", "android_hd")
    put("device_platform", "Android")
    put("dt", encryptedDeviceToken)
    put("local_id", buvid)
    put("cid", countryCode.toString())
    put("tel", phone)
    put("code", code.toString())
    put("captcha_key", captchaKey)
}

private fun androidLoginBaseParams(timestampSeconds: Long): Map<String, String> = mapOf(
    "appkey" to AppSignUtils.ANDROID_HD_APP_KEY,
    "build" to "2001100",
    "c_locale" to "zh_CN",
    "channel" to "master",
    "disable_rcmd" to "0",
    "mobi_app" to "android_hd",
    "platform" to "android",
    "s_locale" to "zh_CN",
    "statistics" to "{\"appId\":5,\"platform\":3,\"version\":\"2.0.1\",\"abtest\":\"\"}",
    "ts" to timestampSeconds.toString()
)
