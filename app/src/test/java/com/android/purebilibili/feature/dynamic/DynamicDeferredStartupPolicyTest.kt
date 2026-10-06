package com.android.purebilibili.feature.dynamic

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DynamicDeferredStartupPolicyTest {
    @Test
    fun sameAccountWithNoRequestCanUseCachePlaceholder() {
        assertTrue(shouldApplyDeferredDynamicCache(true, null, null, false))
    }

    @Test
    fun switchingAccountsDuringCacheReadRejectsOldCache() {
        assertFalse(shouldApplyDeferredDynamicCache(false, null, null, false))
    }

    @Test
    fun newerTimelineRequestRejectsCacheEvenBeforeItReturnsItems() {
        assertFalse(shouldApplyDeferredDynamicCache(true, null, 1L, false))
        assertFalse(shouldApplyDeferredDynamicCache(true, 1L, 2L, false))
    }

    @Test
    fun requestAlreadyStartedBeforeCacheReadKeepsItsLoadingState() {
        assertFalse(shouldApplyDeferredDynamicCache(true, 1L, 1L, false))
    }

    @Test
    fun existingTimelineItemsCannotBeOverwrittenByStartupCache() {
        assertFalse(shouldApplyDeferredDynamicCache(true, null, null, true))
    }
}
