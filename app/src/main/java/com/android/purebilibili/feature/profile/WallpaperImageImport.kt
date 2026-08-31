package com.android.purebilibili.feature.profile

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.File
import java.io.IOException
import java.io.InputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

/** Copy while the picker grant is valid; previews and saved wallpapers then use our own file. */
internal suspend fun importWallpaperImage(
    context: Context,
    source: Uri,
    destinationDirectory: File,
): File {
    var imported: File? = null
    try {
        return withContext(Dispatchers.IO) {
            val file = copyWallpaperImage(
                destinationDirectory = destinationDirectory,
                openSource = { context.contentResolver.openInputStream(source) },
                validateImage = { candidate ->
                    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    BitmapFactory.decodeFile(candidate.absolutePath, options)
                    options.outWidth > 0 && options.outHeight > 0
                },
            )
            imported = file
            ensureActive()
            file
        }
    } catch (error: Throwable) {
        // Also handles cancellation during the dispatch back to the UI thread.
        imported?.delete()
        throw error
    }
}

internal fun copyWallpaperImage(
    destinationDirectory: File,
    openSource: () -> InputStream?,
    validateImage: (File) -> Boolean,
): File {
    if (!destinationDirectory.isDirectory && !destinationDirectory.mkdirs()) {
        throw IOException("无法创建壁纸目录")
    }
    val destination = File.createTempFile("wallpaper_", ".img", destinationDirectory)
    try {
        val source = openSource() ?: throw IOException("无法读取所选图片，请重新从相册选择")
        source.use { input ->
            destination.outputStream().use { output -> input.copyTo(output) }
        }
        if (destination.length() == 0L || !validateImage(destination)) {
            throw IOException("图片为空、已损坏或格式不受支持，请选择其他图片")
        }
        return destination
    } catch (error: Throwable) {
        destination.delete()
        throw error
    }
}
