# 架构说明

最后更新：2026-08-15（按当前工作区与构建配置校对）

## 构建与运行基线

| 项目 | 当前值 |
| --- | --- |
| 应用版本 | 当前构建与最近完整发布记录 `v0.2.3-beta.7` / `versionCode 296` |
| Android | minSdk 26、targetSdk 35、compileSdk 37、arm64-v8a |
| 工具链 | AGP 9.3.1、Gradle 9.5、Kotlin 2.4、JDK 21 |
| Compose | BOM 2026.06.00、Material3 1.5.0-alpha25、Lifecycle 2.11.0 |
| 导航 | Navigation3 runtime/UI 1.2.0-alpha07、NavigationEvent 1.2.0-alpha03 |
| 媒体 | Media3 1.10.1、DASH/HLS、MediaSession、Texture surface 连续返回 |
| 视觉 | Miuix 0.9.4-4f86de92-SNAPSHOT、Haze 2.0.0-alpha03、Miuix Backdrop / Liquid Glass、Compose Cupertino |

## Gradle 模块

```text
app
├── design-system
├── settings-core
├── network-core
└── plugin-sdk

baselineprofile ──(benchmark target)──> app
```

| 模块 | 职责 | 当前边界 |
| --- | --- | --- |
| `app/` | Application、Activity、业务 UI、导航、播放器、Repository、UseCase 与应用测试 | 绝大多数产品逻辑仍在此模块，新增代码优先沿既有 feature/core 分层 |
| `design-system/` | MD3/Miuix/iOS 主题、语义 token、组件 facade、动效、模糊预算与自适应策略 | 不承载网络、业务状态或页面 ViewModel |
| `settings-core/` | 可复用播放速度等设置策略 | 只放跨页面、可独立测试的偏好域逻辑 |
| `network-core/` | 网络 fallback 与首页推荐匿名化策略 | 不依赖 feature UI |
| `plugin-sdk/` | 推荐、播放器、弹幕插件接口与能力 manifest | 作为外部插件稳定边界，不暴露 app 内部实现 |
| `danmaku-engine/` | 普通 Canvas 弹幕引擎、共享 XML/Protobuf/Mode 7 解析及 Mode 9 BAS 语法与时间线 | BAS 解析/求值不依赖 App；原生 BAS 视口绘制与播放器交互由 App 覆盖层承担 |
| `baselineprofile/` | 启动、首页、底部 Pager、设置返回、视频详情与非实时 surface 基准 | 只负责 benchmark/profile，不承载产品代码 |

## App 源码分层

主路径：`app/src/main/java/com/android/purebilibili/`

| 目录 | 职责 | 典型内容 |
| --- | --- | --- |
| `app/` | 应用启动与全局装配 | `PureApplication`、初始化、遥测、进程级 owner |
| `core/` | 跨业务公共能力 | network、store、database、player、plugin、theme、ui、cache、lifecycle |
| `data/` | 数据模型与数据访问 | API/数据库 model、Repository、加载与缓存策略 |
| `domain/` | 可复用业务规则 | UseCase 与不依赖 Compose 的业务决策 |
| `feature/` | 业务场景 | home、video、bangumi、live、dynamic、message、download、settings 等 |
| `navigation/` | 兼容与顶层入口 | legacy route 映射、首页 Pager、链接解析、入口/外观/播放策略 |
| `navigation3/` | 当前页面导航内核 | 61 个 NavKey、返回栈策略、59 个显式 Entry、Scene、预测返回和整卡会话 |
| `androidx/navigationevent/compose/` | 本地 NavigationEvent Compose 兼容层 | 保留完成/取消提交时序与关闭跟手预览能力；需随 NavigationEvent 版本核对 |

## 导航与整卡过渡

1. `AppNavigation` 持有应用级 `List<BiliPaiNavKey>`，业务事件通过 policy 转换成 push、replace 或 pop。
2. `BiliPaiNavEntryProvider` 把 NavKey 解析为 Entry，并注入来源路由、ViewModel owner 和视觉状态。
3. `BiliPaiNavDisplayHost` 使用官方 Navigation3 runtime/UI `1.2.0-alpha07` 生成 `SceneState`，统一普通返回与预测返回。
4. 视频入口创建不可变 `VideoCardTransitionSession`，冻结 bvid、来源 key/route、边界、圆角、方向与封面身份。
5. 整卡几何只由一个 shell/shared bounds 所有；封面、标题、UP 和统计跟随卡片，不创建竞争的独立 bounds。
6. 转场时钟负责 Opening、SettledHidden、BackPreview、Returning、Restoring；详情稳态保留返回会话，但停止无收益的模糊、Miuix Backdrop 和来源重录。

Miuix `0.9.4-4f86de92-SNAPSHOT` 继续用于组件与视觉；当前项目仍不混用其 NavDisplay，
Navigation3 runtime/UI 必须保持官方同版。

## 播放主链路

```text
Feature UI
  -> screen state holder / ViewModel
  -> domain UseCase / playback policy
  -> data Repository
  -> core network + account/auth state
  -> ViewInfo / PlayUrlData / player intent
  -> Media3 player + surface + overlay
```

- 登录态由 Cookie、CSRF 与 access token 共同参与；画质由用户偏好、账号权限、编码能力和 fallback 策略决定。
- 播放器覆盖普通视频、番剧、直播、离线、竖屏 feed 与听视频模式，页面必须明确 player/surface 的 owner。
- 视频整卡返回保留 Texture 实时画面或最后一帧；从未播放时由封面承担转场，不为动画启动无意义播放器帧。
- PiP、后台播放与系统媒体控制通过 MediaSession/通知继续共享播放状态。

### 高级弹幕分流与 BAS

- Mode 7 是单文本 JSON 数组，保留现有兼容解析和 `AdvancedDanmakuOverlay`；它不等同于 BAS 脚本。
- Mode 9 由 `danmaku-engine/.../parser/bas/BasScriptParser` 独立解析。支持 `def text/button/path`、`let` 对象构造/复制、默认参数模板、位置/具名实参、复合时间、转义字符串和 `//` 注释；不执行 JavaScript。
- 数字词法直接扫描原字符串的当前 token，不对完整脚本反复创建正则匹配器。Android ICU 的 [`MatcherState::updateInput`](https://android.googlesource.com/platform/external/icu/+/refs/heads/android16-release/android_icu4j/libcore_bridge/src/native/MatcherState.cpp) 会复制完整输入到原生堆；即使 Java 堆与播放窗口不大，这类按 token 的全输入复制仍可能触发系统低内存杀进程。
- `BasTimeline` 预编译各属性轨道，递归串行组取时长之和、并行组取最大值。空 `set` 提供延时，文本/字号在 `set` 开始时改变；重叠同属性及变换通道按源码后声明优先，保留无冲突的透明度/颜色动画。
- 位置保留像素/百分比单位，到帧布局时按视口或父文本尺寸求值；字号百分比基于视口宽度。播放进度可直接前进/倒退求值，无需重放动画历史。父层级生命周期结束后隐藏其子树。
- `DanmakuParser` 的 `ParsedDanmaku.basList` 与 `DanmakuManager.basDanmakuFlow` 将脚本场景送到普通视频的 `BasDanmakuOverlay`。文本、按钮、路径使用保留的原生 Canvas 场景；SVG 使用 AndroidX `PathParser`，`viewBox` 按等比居中映射，支持锚点、三轴旋转、层级透明度及描边。
- 在线特殊分片由 `SpecialDanmakuIndexReader` 用最多 1 KiB 的 HTTP Range 头部读取建立偏移/开始时间索引，跳过脚本文本；索引逐片就绪即可接入 `SpecialDanmakuWindow`，不依赖文件名或分片的时间顺序。窗口最多 4 个载荷并发，只保留当前位置仍有效及未来 3 秒的场景；不缓存完整线上分片，也不把整段视频写入磁盘缓存。
- Protobuf 没有结束时间。冷跳转复用已知生命周期；此前未知的 BAS 使用同一语法的 `parseDurationMs` 检查时长，不保留 SVG/文本字符串或生成可绘制对象。检查过期载荷后立即丢弃，只将重叠场景完整编译。冷跳转仍可能需要读取此前未知的脚本，不能用固定回看秒数丢掉长持续弹幕。
- BAS 预取检查与普通弹幕校时分开计时，按播放倍速调整刷新间隔；普通滚动时间线不因 BAS 窗口更新而重启。暂停时场景继续使用播放器固定的 `currentPosition`；切视频、释放及后台裁剪取消过期请求并释放场景，提交检查会话/窗口代际。SVG 路径支持 `alpha`，包括零时长的逐帧显隐切换。
- 离线播放对已有特殊分片文件使用相同索引与随机读取窗口。用户主动离线导出仍可保存完整资产，但使用 Retrofit `@Streaming` 和逐片写盘，不返回全量特殊分片 `List<ByteArray>`。
- BAS 特殊/彩色/权重/关键词/用户屏蔽复用应用设置；关键词匹配定义与后续动画文本而非标识符。Mode 9 插件接收原始脚本，改写后重新解析；无效脚本丢弃，不回退滚动显示源码。
- 按钮仅支持 `seek`、`av`/BV、`bangumi` 类型。seek 使用当前播放器；其他目标暂停播放后在外部浏览器打开完整分 P/时间链接，避免现有 App 深链入口丢失这些参数。未命中按钮时不消费播放器手势。
- `apply`、`clone`、成员/算术表达式、块注释与单引号不是此可执行 BAS 契约；解析失败携带行列信息。未知/不可变的 `set` 属性被忽略。
- 字体由设备字体和系统替代决定；真实设备上的字体、透视、SVG 抗锯齿与点击透传仍需视觉验证。共享解析不意味着所有播放宿主都已接入 BAS 绘制，TV 覆盖层保持现有普通弹幕职责。
- `BV1Wneiz5EpD` 的真实载荷 JVM 冒烟在 `-Xmx192m` 下通过：17 个分片、735 条索引、原始 265.74 MiB，索引实际读取约 0.71 MiB；检查了 0.1 秒、6.1 秒、141.29 秒及回退窗口，相同暂停位置不改变场景或重复读取载荷。冷跳转检查未知旧脚本仍有 I/O 成本，JVM 结果不代表 Android 原生绘制的像素或内存。
- Android 36 模拟器已完成该视频的 BAS 实际显示、暂停保持与前后跳转冒烟。暂停在 104.531 秒时，两次截图的 1080×607 视口像素完全一致；前跳约 140 秒、回退约 65 秒后重新加载相应窗口并显示场景，同一进程也经历了 3 倍速播放。数字扫描修复后，三次整个调试 App 的 RSS 采样约 565、582、564 MiB，末次 PSS 约 433 MiB、Java 堆已分配约 40 MiB、Native Heap RSS 约 90 MiB、Swap PSS 约 0.28 MiB，未再次观察到 OOM；这是采样值，不是峰值或 BAS 独占内存。引擎 76 项回归、手机调试 APK 构建及 TV Kotlin 编译通过；字体/透视/按钮手势与其他设备仍不在本轮完整视觉验收范围。

已执行的模板与串并行样例：

```text
def text T(c="default" x=0) { content=c x=x }
let a=T("模板", x=50%,)
let b=(a {content="复制" x=100})
{ set a {alpha=0} 2s set b {} 1s then set b {y=50%} 3s }
then set a {rotateY=90} 1s
```

实现依据：[官方 BAS 属性与语法](https://github.com/bilibili/bas/blob/2ef488f1e8403fa023eeeff1c30851e6908b60f1/docs/src/pages/docs.md)、[官方使用指南](https://github.com/bilibili/bas/blob/2ef488f1e8403fa023eeeff1c30851e6908b60f1/docs/src/pages/guide.md)；[PiliPlus 2.1.5](https://github.com/bggRGjQaUbCoE/PiliPlus/tree/2.1.5) 的锁定 `canvas_danmaku` 只提供 Mode 7 参考，不作为完整 Mode 9 解析器。BAS 语法与求值为独立 Kotlin 实现，未复制未标明许可的官方 JavaScript。


## 视觉与自适应

正式 UI 设计合同、三风格边界、组件入口和页面档案见 [UI 设计规范](ui-design/README.md)。本页继续说明技术架构，不重复设计规则。

- `design-system` 提供 MD3、Miuix 与 iOS facade，feature 只消费语义 token 和能力接口。
- Haze、Miuix blur 与 Miuix Backdrop 都受平台能力、运行时视觉预算和转场安全门控约束。
- 手机使用底栏/单栏为主；平板和折叠屏使用 rail、双栏或影院布局。
- 液态玻璃复用遵循 sibling/combined backdrop 拓扑，避免控件采样自身造成黑边或 RenderThread 问题。

## 插件边界

| 形态 | 当前能力 |
| --- | --- |
| 内置插件 | `PureApplication` 注册 10 个实现，可接入推荐、播放器、弹幕与投屏链路 |
| JSON / `.bp` | URL 导入、规则预览、启停与本地执行 |
| 外部 `.bpplugin` | manifest、SHA-256、签名状态、能力声明和包预览；外部 Dex 执行仍未正式开放 |
| 源码示例/皮肤 | `plugins/samples/` 提供源码插件和数据型皮肤包示例 |

外部执行正式化前必须先定义签名信任、能力授权、隔离、版本兼容与失败回滚，不能由 UI 预览状态推导为“可安全执行”。

## 测试与性能

- App policy/structure tests：`app/src/test/`。
- Design system、network、settings、plugin SDK 各模块拥有独立纯 Kotlin 测试。
- `baselineprofile/` 覆盖 Startup、FrameTiming、视频详情、首页、底部 Pager 和设置返回。
- 性能相关改动按“目标测试 → `:app:compileDebugKotlin` → Macrobenchmark/真机”逐级验证。
- 当前 AGP 9 迁移后的 app 单元测试注解解析仍是路线图 P0；恢复前不得把生产编译成功等同于测试全绿。

## 结构维护原则

- screen composable 消费不可变状态与事件 lambda，ViewModel 不向叶子组件传播。
- 可测试决策优先抽成 `Policy`/UseCase；业务 I/O 不放入 Composable。
- 跨 feature 的视觉能力先进入 `design-system`，业务公共能力进入 `core`/独立模块。
- 新模块只在边界稳定且能减少反向依赖时建立，不以模块数量代替架构质量。
- 更完整的目录归属与依赖规则见 `STRUCTURE_GUIDELINES.adoc`。

## 事实入口

- 当前优先级：`docs/wiki/ROADMAP.md`
- 版本与依赖：`app/build.gradle.kts`、根 `build.gradle.kts`、Gradle wrapper
- 发布历史：`CHANGELOG.md`
- 功能状态：`docs/wiki/FEATURE_MATRIX.md`
- 回归标准：`docs/wiki/QA.md`
