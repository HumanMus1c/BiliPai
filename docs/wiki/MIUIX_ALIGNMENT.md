# Miuix 对齐记录

最后更新：2026-09-01

## 无玻璃 Miuix 适配（2026-08-31）

本轮按 [UI 设计规范](ui-design/README.md) 1.1 执行，仅更改 MIUIX 且液态玻璃关闭的视觉。材质状态由宿主统一传递，官方组件优先，保留普通模糊和导航自定义。旧迁移进度是历史记录，不能作为当前完成率。

## 背景

本页记录 Miuix 技术接入状态；面向设计与页面实施的正式规则见 [UI 设计规范](ui-design/README.md)。两者发生差异时，前者回答“当前接入到哪里”，后者回答“目标应该怎样”。

本仓库通过 GitHub Packages 引入 `top.yukonga.miuix.kmp`（当前钉扎上游主线快照
**0.9.4-4f86de92-SNAPSHOT**），并在
`AppUiStyle.MIUIX` 下经由设计系统 facade 分发到官方组件或 Miuix 基元组合。

Navigation3 需单独看待：项目已经通过本地依赖替换接入 Miuix navigation，
`BiliPaiNavDisplayHost` 实际使用 Miuix `NavDisplay`。该宿主及配套 back stack、transition、
预测返回和手势桥接由 MD3 与 MIUIX 两主题共用，是本轮“各主题使用各自原生可见组件”规则的
固定导航子系统例外。

上游发布说明：<https://github.com/compose-miuix-ui/miuix/releases/tag/v0.9.3>

## 本地结论（相对上游能力）

- `Miuix` 不是“只换颜色”的封装：有独立主题、颜色槽位、文字样式与 squircle / smooth rounding。
- 壳层组件包括 `TopAppBar`、`NavigationBar` / `FloatingNavigationBar`、`NavigationRail`、`TabRow`、
  `BasicComponent` / Preference、以及 0.9.3 新增的 `Badge` / `Tooltip`。
- 正文文字分层更接近 `17 / 16 / 14 / 13 / 11sp`，不是 Material 3 token 的直接镜像。

## 对 BiliPai 当前实现的判断

主题、壳层、Preference 与少量高频组件已经接入 Miuix，但普通原语和大量 feature 仍直接依赖
Material 3；因此不能再将当前状态描述为“主路径已完成”。完整迁移进度见
[双主题原生组件迁移](NATIVE_THEME_COMPONENT_MIGRATION.md)。

- 颜色通过 `Material ColorScheme -> Miuix Colors` 桥接 + `AppSurfaceTokens` 消费。
- 壳层 / Preference / 内容卡 / 播放器设置与迷你播放器壳 / Tooltip 均已挂 `MIUIX_BRIDGED`。
- 可选：首页视频卡更深 squircle、更多长按 Tooltip 面。

## 已落地

- Miuix 变体独立 typography / shapes / corner scale / smooth rounding。
- `AppSurfaceTokens` 语义色；feature 层禁止直读 `MiuixTheme.colorScheme`（结构测试守门）。
- 设置 Scaffold、分段 `TabRow`、搜索 `InputField`、列表 `BasicComponent` / `SwitchPreference` / `SliderPreference` / `ArrowPreference`。
- 首页 `AdaptivePullToRefreshBox` → 官方 `PullToRefresh`。
- 底栏官方 `NavigationBar` + `Badge`；0.9.3 起 `TextOnly` 映射为 `IconWithSelectedLabel`。
- 平板 `FrostedSideBar` / `AdaptiveSideNavigationRail` → 官方 `NavigationRail`（Expanded 可展开）。
- 播放器 `VideoSettingsPanel`：可点击项走 `ArrowPreference`，开关行走 `SwitchPreference`。
- 迷你播放器壳：`MiniPlayerOverlayShellPolicy`（更圆角、更扁 elevation、`AppSurfaceTokens.primary` 强调色）。
- 设置外观说明卡：`AdaptivePlainTooltipBox` → 官方 `TooltipBox`（长按/悬停）。
- 工具链：Kotlin `2.4.0` + KSP `2.3.10` + miuix `0.9.4-4f86de92-SNAPSHOT`（含 `miuix-shader`）。
- `TextOnly`：MD3 设置保留；Miuix 路径映射为 `IconWithSelectedLabel`（非死分支，属 0.9.3 兼容）。

## 后续对齐顺序

以[双主题原生组件迁移](NATIVE_THEME_COMPONENT_MIGRATION.md)的阶段 0–9 为唯一执行顺序。
现有壳层、Preference、播放器和 Tooltip 接入视为可复用基础，不代表其余 App 原语已经完成双渲染。

## 非目标

- 改造液态玻璃效果
- 为迁移新增无关依赖
- 改变主题持久化或业务状态语义
- 将 48dp 触控下限写死为所有组件的视觉尺寸

上条不适用于锁定版 Miuix `TabRow`：其内部 selectable 与组件高度相同，API 不提供独立触摸扩展，因此必须把组件实际高度设为至少 48dp；图标和非交互装饰仍可保持更小的视觉尺寸。
