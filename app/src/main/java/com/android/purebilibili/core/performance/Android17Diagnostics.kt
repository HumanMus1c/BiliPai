package com.android.purebilibili.core.performance

import android.app.ActivityManager
import android.app.ApplicationExitInfo
import android.content.Context
import android.os.Build
import android.os.ProfilingManager
import android.os.ProfilingResult
import android.os.ProfilingTrigger
import androidx.annotation.RequiresApi
import com.android.purebilibili.BuildConfig
import com.android.purebilibili.core.util.CrashReporter
import com.android.purebilibili.core.util.Logger
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.function.Consumer

internal data class ProfilingArtifactSnapshot(
    val path: String,
    val sizeBytes: Long,
    val lastModifiedMillis: Long
)

internal fun selectProfilingArtifactPathsToKeep(
    artifacts: List<ProfilingArtifactSnapshot>,
    maxArtifacts: Int,
    maxTotalBytes: Long
): Set<String> {
    var keptBytes = 0L
    return artifacts
        .sortedByDescending(ProfilingArtifactSnapshot::lastModifiedMillis)
        .filter { artifact ->
            val canKeep = artifact.sizeBytes >= 0L &&
                artifact.sizeBytes <= maxTotalBytes - keptBytes
            if (canKeep) keptBytes += artifact.sizeBytes
            canKeep
        }
        .take(maxArtifacts.coerceAtLeast(0))
        .mapTo(linkedSetOf(), ProfilingArtifactSnapshot::path)
}

internal object Android17Diagnostics {
    private const val TAG = "Android17Diagnostics"
    private const val PROFILE_DIRECTORY = "android17_profiles"
    private const val MAX_ARTIFACTS = 3
    private const val MAX_TOTAL_BYTES = 128L * 1024L * 1024L
    private const val PREFS_NAME = "android17_diagnostics"
    private const val KEY_LAST_EXIT_TIMESTAMP = "last_exit_timestamp"

    private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    @Volatile private var initialized = false
    private var resultConsumer: Consumer<ProfilingResult>? = null

    fun initialize(context: Context, enabled: Boolean) {
        if (Build.VERSION.SDK_INT < 37) return
        updateEnabled(context.applicationContext, enabled)
    }

    fun updateEnabled(context: Context, enabled: Boolean) {
        val appContext = context.applicationContext
        if (Build.VERSION.SDK_INT < 37) {
            if (!enabled) deleteRetainedArtifacts(appContext)
            return
        }
        if (enabled) {
            enableApi37(appContext)
        } else {
            disableApi37(appContext)
        }
    }

    fun retainedArtifacts(context: Context): List<File> =
        profileDirectory(context).listFiles()
            ?.filter(File::isFile)
            ?.sortedByDescending(File::lastModified)
            .orEmpty()

    fun copyRetainedArtifactsTo(context: Context, targetDirectory: File): List<File> {
        targetDirectory.mkdirs()
        return retainedArtifacts(context).mapNotNull { source ->
            runCatching {
                val target = File(targetDirectory, source.name)
                source.inputStream().buffered().use { input ->
                    target.outputStream().buffered().use(input::copyTo)
                }
                target
            }.onFailure { Logger.w(TAG, "Failed to prepare profiling artifact for export", it) }
                .getOrNull()
        }
    }

    @RequiresApi(37)
    private fun enableApi37(context: Context) {
        if (initialized) return
        val manager = context.getSystemService(ProfilingManager::class.java) ?: return
        val consumer = Consumer<ProfilingResult> { result ->
            executor.execute { handleProfilingResult(context, result) }
        }
        resultConsumer = consumer
        manager.registerForAllProfilingResults(executor, consumer)
        manager.clearProfilingTriggers()
        manager.addProfilingTriggers(
            listOf(
                ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_OOM)
                    .setRateLimitingPeriodHours(24)
                    .build(),
                ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_ANOMALY)
                    .setRateLimitingPeriodHours(24)
                    .build(),
                ProfilingTrigger.Builder(ProfilingTrigger.TRIGGER_TYPE_KILL_EXCESSIVE_CPU_USAGE)
                    .setRateLimitingPeriodHours(24)
                    .build()
            )
        )
        initialized = true
        executor.execute { recordMemoryLimiterExitIfPresent(context) }
        Logger.i(TAG, "Android 17 profiling triggers enabled")
    }

    @RequiresApi(37)
    private fun disableApi37(context: Context) {
        context.getSystemService(ProfilingManager::class.java)?.let { manager ->
            resultConsumer?.let(manager::unregisterForAllProfilingResults)
            manager.clearProfilingTriggers()
        }
        resultConsumer = null
        initialized = false
        executor.execute { deleteRetainedArtifacts(context) }
        Logger.i(TAG, "Android 17 profiling triggers disabled and local artifacts cleared")
    }

    @RequiresApi(37)
    private fun handleProfilingResult(context: Context, result: ProfilingResult) {
        if (result.errorCode != ProfilingResult.ERROR_NONE) {
            Logger.w(TAG, "Profiling failed: code=${result.errorCode}, trigger=${result.triggerType}")
            return
        }
        val sourcePath = result.resultFilePath?.takeIf(String::isNotBlank) ?: return
        val source = File(sourcePath).takeIf(File::isFile) ?: return
        val safeExtension = source.extension.take(16).filter { it.isLetterOrDigit() || it == '-' }
        val targetName = buildString {
            append("profile_")
            append(result.triggerType)
            append('_')
            append(System.currentTimeMillis())
            if (safeExtension.isNotBlank()) append('.').append(safeExtension)
        }
        val directory = profileDirectory(context).apply { mkdirs() }
        val temporary = File(directory, "$targetName.tmp")
        val target = File(directory, targetName)
        runCatching {
            source.inputStream().buffered().use { input ->
                temporary.outputStream().buffered().use(input::copyTo)
            }
            check(temporary.renameTo(target)) { "Unable to finalize profiling artifact" }
            if (source.absolutePath != target.absolutePath) source.delete()
            trimRetainedArtifacts(directory)
            CrashReporter.setCustomKey("android17_profile_trigger", result.triggerType)
            CrashReporter.setCustomKey("android17_profile_timestamp_ms", target.lastModified())
            CrashReporter.setCustomKey("android17_profile_size_bytes", target.length())
            CrashReporter.setCustomKey("android17_profile_app_version", BuildConfig.VERSION_NAME)
            val activityManager = context.getSystemService(ActivityManager::class.java)
            CrashReporter.setCustomKey(
                "android17_profile_memory_class_mb",
                activityManager?.memoryClass ?: 0
            )
            Logger.i(TAG, "Stored private profiling artifact: trigger=${result.triggerType}, bytes=${target.length()}")
        }.onFailure { error ->
            temporary.delete()
            Logger.w(TAG, "Failed to retain profiling artifact", error)
        }
    }

    @RequiresApi(37)
    private fun recordMemoryLimiterExitIfPresent(context: Context) {
        val manager = context.getSystemService(ActivityManager::class.java) ?: return
        val latest = manager.getHistoricalProcessExitReasons(context.packageName, 0, 10)
            .asSequence()
            .filter { it.reason == ApplicationExitInfo.REASON_OTHER }
            .firstOrNull { it.description?.contains("MemoryLimiter:AnonSwap", ignoreCase = true) == true }
            ?: return
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (latest.timestamp <= prefs.getLong(KEY_LAST_EXIT_TIMESTAMP, 0L)) return
        prefs.edit().putLong(KEY_LAST_EXIT_TIMESTAMP, latest.timestamp).apply()
        CrashReporter.setCustomKey("android17_memory_limiter_exit", true)
        CrashReporter.setCustomKey("android17_memory_limiter_pss_kb", latest.pss)
        CrashReporter.setCustomKey("android17_memory_limiter_rss_kb", latest.rss)
        Logger.w(
            TAG,
            "Previous process was stopped by Android 17 memory limiter; pssKb=${latest.pss}, rssKb=${latest.rss}"
        )
    }

    private fun trimRetainedArtifacts(directory: File) {
        val files = directory.listFiles()?.filter(File::isFile).orEmpty()
        val pathsToKeep = selectProfilingArtifactPathsToKeep(
            artifacts = files.map { file ->
                ProfilingArtifactSnapshot(file.absolutePath, file.length(), file.lastModified())
            },
            maxArtifacts = MAX_ARTIFACTS,
            maxTotalBytes = MAX_TOTAL_BYTES
        )
        files.forEach { file ->
            if (file.absolutePath !in pathsToKeep) file.delete()
        }
    }

    private fun deleteRetainedArtifacts(context: Context) {
        profileDirectory(context).listFiles()?.forEach(File::delete)
        profileDirectory(context).delete()
    }

    private fun profileDirectory(context: Context): File = File(context.filesDir, PROFILE_DIRECTORY)
}
