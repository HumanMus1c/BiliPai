package com.android.purebilibili.core.performance

import kotlin.test.Test
import kotlin.test.assertEquals

class Android17DiagnosticsPolicyTest {
    @Test
    fun `retention keeps newest three artifacts within size cap`() {
        val keep = selectProfilingArtifactPathsToKeep(
            artifacts = listOf(
                ProfilingArtifactSnapshot("old", 20, 1),
                ProfilingArtifactSnapshot("newest", 30, 4),
                ProfilingArtifactSnapshot("newer", 30, 3),
                ProfilingArtifactSnapshot("new", 30, 2)
            ),
            maxArtifacts = 3,
            maxTotalBytes = 100
        )

        assertEquals(setOf("newest", "newer", "new"), keep)
    }

    @Test
    fun `oversized artifact is skipped without blocking smaller files`() {
        val keep = selectProfilingArtifactPathsToKeep(
            artifacts = listOf(
                ProfilingArtifactSnapshot("oversized", 200, 3),
                ProfilingArtifactSnapshot("small", 40, 2),
                ProfilingArtifactSnapshot("smallest", 20, 1)
            ),
            maxArtifacts = 3,
            maxTotalBytes = 60
        )

        assertEquals(setOf("small", "smallest"), keep)
    }
}
