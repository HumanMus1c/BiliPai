package com.android.purebilibili.feature.home

import com.android.purebilibili.core.store.HomeCardBadgeEffectMode
import com.android.purebilibili.core.store.HomeCardInfoGlassMode

/**
 * 壁纸 Haze source 的挂载判定。
 *
 * ### 问题
 *
 * `wallpaperHazeState` 此前是**无条件创建并注册**的，而且注册了两次全屏 source：
 * 一次在 `AppNavigation`（包住全局壁纸 + 全部导航内容），一次在 `HomeScreen`
 * （只包住首页壁纸）。每个 haze source 都意味着对应子树每帧要被 record 一遍。
 *
 * 但它的消费者只有两个，且**都门控在默认关闭的档位上**：
 * - `HomeCardBadgeEffectMode.LIGHT_BLUR` —— 卡片角标实时模糊
 * - `HomeCardInfoGlassMode` 的 `usesRealtimeBlur` —— 卡片信息区实时模糊
 *
 * 两者的默认值分别是 `SOFT_GLASS` 和 `OFF`，且所有会用到实时采样的档位在设置页里
 * 都明确标着「开发中，请勿使用」。也就是说**绝大多数用户每一帧都在为两层
 * 全屏 record 付费，而没有任何东西在消费它**。
 *
 * ### 为什么是条件挂载，而不是删掉其中一次注册
 *
 * 两次注册的采样范围不同：外层是「全局壁纸 + 导航 chrome」，内层是「首页壁纸」。
 * haze 里后注册者赢，所以卡片实际采样的是内层那份。删掉任意一边都会在
 * `LIGHT_BLUR` 档下改变角标的观感，而那需要真机截图比对才能确认哪个是设计意图。
 *
 * 条件挂载则完全绕开这个判断：需要采样时两边都保持原样、观感逐像素不变；
 * 不需要时两边一起消失。**默认档零观感风险，且收益更大**（省掉的是两层而不是一层）。
 *
 * 消费侧不需要任何改动——`VideoCard` 与 `HomeGlassVisualPolicy` 本来就有
 * `hazeState != null` 分支，拿不到 state 时自动退回非实时路径。
 */
internal fun shouldMountWallpaperHazeSource(
    badgeEffectMode: HomeCardBadgeEffectMode,
    infoGlassMode: HomeCardInfoGlassMode
): Boolean =
    badgeEffectMode == HomeCardBadgeEffectMode.LIGHT_BLUR || infoGlassMode.usesRealtimeBlur
