package com.android.purebilibili.core.performance

import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.Base64

/** Preserve binary tombstones without UTF-8 replacement or line-based truncation. */
internal fun encodeNativeExitTrace(stream: InputStream, maxBytes: Int = 4 * 1024 * 1024): String? {
    require(maxBytes > 0)
    val output = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    while (output.size() < maxBytes) {
        val count = stream.read(buffer, 0, minOf(buffer.size, maxBytes - output.size()))
        if (count < 0) break
        if (count == 0) continue
        output.write(buffer, 0, count)
    }
    if (output.size() == 0) return null
    val truncated = stream.read() != -1
    return buildString {
        appendLine("Native tombstone: protobuf/base64; truncated=$truncated; bytes=${output.size()}")
        appendLine("----- BEGIN TOMBSTONE PROTOBUF BASE64 -----")
        appendLine(Base64.getEncoder().encodeToString(output.toByteArray()))
        append("----- END TOMBSTONE PROTOBUF BASE64 -----")
    }
}
