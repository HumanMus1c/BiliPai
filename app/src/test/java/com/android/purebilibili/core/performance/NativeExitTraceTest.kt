package com.android.purebilibili.core.performance

import java.util.Base64
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NativeExitTraceTest {
    @Test
    fun binaryTraceSurvivesInvalidUtf8AndEmbeddedNewlines() {
        val bytes = ByteArray(4096) { it.toByte() }
        val trace = requireNotNull(encodeNativeExitTrace(bytes.inputStream()))
        assertTrue(trace.contains("truncated=false"))
        assertContentEquals(bytes, Base64.getDecoder().decode(trace.lines()[2]))
    }

    @Test
    fun oversizedTraceIsExplicitlyMarkedAndBounded() {
        val trace = requireNotNull(encodeNativeExitTrace(ByteArray(32).inputStream(), maxBytes = 16))
        assertTrue(trace.contains("truncated=true; bytes=16"))
        assertTrue(Base64.getDecoder().decode(trace.lines()[2]).size == 16)
        assertNull(encodeNativeExitTrace(byteArrayOf().inputStream()))
    }
}
