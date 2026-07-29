package com.android.purebilibili.feature.home

import com.android.purebilibili.core.store.HomeCardBadgeEffectMode
import com.android.purebilibili.core.store.HomeCardInfoGlassMode
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HomeWallpaperHazeSourcePolicyTest {

    /**
     * 最重要的一条：默认配置下不挂载。
     *
     * 这正是这次改动的全部收益来源——默认档省掉两层全屏 haze source。
     * 如果哪天有人把某个默认值改成实时档，这条会立刻变红，
     * 提醒他这不只是「换个观感」，而是给每一帧加回两次全屏 record。
     */
    @Test
    fun defaultSettings_doNotMountWallpaperHazeSource() {
        assertFalse(
            shouldMountWallpaperHazeSource(
                badgeEffectMode = DEFAULT_BADGE_MODE,
                infoGlassMode = DEFAULT_INFO_GLASS_MODE
            ),
            "默认档（$DEFAULT_BADGE_MODE / $DEFAULT_INFO_GLASS_MODE）不应挂载壁纸 haze source",
        )
    }

    /** 默认值本身也要守住：这条策略的价值完全建立在「默认是非实时档」之上。 */
    @Test
    fun defaultsAreStillTheNonRealtimeModes() {
        assertFalse(DEFAULT_BADGE_MODE == HomeCardBadgeEffectMode.LIGHT_BLUR)
        assertFalse(DEFAULT_INFO_GLASS_MODE.usesRealtimeBlur)
    }

    @Test
    fun badgeLightBlur_mountsSource() {
        assertTrue(
            shouldMountWallpaperHazeSource(
                badgeEffectMode = HomeCardBadgeEffectMode.LIGHT_BLUR,
                infoGlassMode = HomeCardInfoGlassMode.OFF
            ),
        )
    }

    @Test
    fun everyRealtimeBlurInfoMode_mountsSource() {
        HomeCardInfoGlassMode.entries.filter { it.usesRealtimeBlur }.forEach { mode ->
            assertTrue(
                shouldMountWallpaperHazeSource(
                    badgeEffectMode = HomeCardBadgeEffectMode.SOFT_GLASS,
                    infoGlassMode = mode
                ),
                "$mode 使用实时模糊，必须挂载 source，否则卡片会静默退回非实时路径",
            )
        }
    }

    /**
     * 液态玻璃走的是 LayerBackdrop，不是 Haze——不应该因为它去挂 haze source。
     * 这条防止有人「顺手」把条件放宽成 `infoGlassMode != OFF`。
     */
    @Test
    fun liquidGlassOnlyMode_doesNotMountHazeSource() {
        assertFalse(
            shouldMountWallpaperHazeSource(
                badgeEffectMode = HomeCardBadgeEffectMode.SOFT_GLASS,
                infoGlassMode = HomeCardInfoGlassMode.REALTIME_LIQUID_GLASS
            ),
            "REALTIME_LIQUID_GLASS 走 LayerBackdrop 折射路径，与 Haze 无关",
        )
    }

    /**
     * 结构守卫：两处注册点都必须真的受这个判定控制。
     *
     * 策略函数写对了但没接上，是这类改动最典型的失效方式——而且不会有任何测试变红，
     * 因为策略自己的单测照样全绿。
     */
    @Test
    fun bothRegistrationSitesStayGuardedByNullState() {
        val nav = source("navigation/AppNavigation.kt")
        assertTrue(
            nav.contains("shouldMountWallpaperHazeSource"),
            "AppNavigation 没有调用 shouldMountWallpaperHazeSource，条件挂载并未生效",
        )
        assertTrue(
            nav.contains("if (wallpaperHazeState != null)"),
            "AppNavigation 的 hazeSourceCompat 注册点丢失了 null 判断",
        )

        val home = source("feature/home/HomeScreen.kt")
        assertTrue(
            home.contains("if (wallpaperHazeState != null)"),
            "HomeScreen 的 hazeSourceCompat 注册点丢失了 null 判断——" +
                "state 为 null 时这里会崩，或者退化成无条件注册",
        )
    }

    private fun source(relative: String): String {
        val roots = listOf(
            "src/main/java/com/android/purebilibili",
            "app/src/main/java/com/android/purebilibili",
        )
        val file = roots.map { File("$it/$relative") }.firstOrNull { it.exists() }
            ?: error("找不到 $relative，cwd=" + File(".").absoluteFile.canonicalPath)
        return file.readText()
    }

    private companion object {
        val DEFAULT_BADGE_MODE = com.android.purebilibili.core.store.HomeSettings().homeCardBadgeEffectMode
        val DEFAULT_INFO_GLASS_MODE = com.android.purebilibili.core.store.HomeSettings().homeCardInfoGlassMode
    }
}
