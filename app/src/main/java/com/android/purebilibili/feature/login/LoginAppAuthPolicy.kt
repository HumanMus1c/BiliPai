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
    putAll(androidLoginDeviceParams(buvid, deviceId, encryptedDeviceToken))
    put("cid", countryCode.toString())
    put("tel", phone)
    put("code", code.toString())
    put("captcha_key", captchaKey)
    put("from_pv", "main.my-information.my-login.0.click")
    put("from_url", "bilibili%3A%2F%2Fuser_center%2Fmine")
}

internal fun buildAndroidPasswordLoginParams(
    username: String,
    encryptedPassword: String,
    token: String,
    challenge: String,
    validate: String,
    seccode: String,
    buvid: String,
    deviceId: String,
    encryptedDeviceToken: String,
    timestampSeconds: Long
): Map<String, String> = buildMap {
    putAll(androidLoginBaseParams(timestampSeconds))
    putAll(androidLoginDeviceParams(buvid, deviceId, encryptedDeviceToken))
    put("username", username)
    put("password", encryptedPassword)
    put("permission", "ALL")
    put("recaptcha_token", token)
    put("gee_challenge", challenge)
    put("gee_validate", validate)
    put("gee_seccode", seccode)
    put("from_pv", "main.homepage.avatar-nologin.all.click")
    put("from_url", "bilibili%3A%2F%2Fpegasus%2Fpromo")
}

private fun androidLoginDeviceParams(
    buvid: String,
    deviceId: String,
    encryptedDeviceToken: String
): Map<String, String> = mapOf(
    "bili_local_id" to deviceId,
    "buvid" to buvid,
    "device" to "phone",
    "device_id" to deviceId,
    "device_name" to "android_hd",
    "device_platform" to "Android",
    "dt" to encryptedDeviceToken,
    "local_id" to buvid
)

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
