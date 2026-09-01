# 02 基础令牌

> 文档编号：UI-02  
> 规范版本：1.1.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-31  
> 适用提交：4443e72ff  
> 维护角色：设计系统维护者  
> 相关文档：[主题规范](03_THEMES.md) · [组件目录](components/README.md)

## 初学者解释

Token（设计令牌）就是“有名字的设计数值”。写 `AppSpacingTokens.Large` 比写 `16.dp` 多了一层含义：前者说明这里是“大间距”，未来设计系统调整时也能统一变化。Token 不是为了让代码变长，而是防止几十个页面各自发明相近数值。

## 规范要求

基础数值必须先使用现有共享 Token；缺少语义时先登记差距并验证复用场景。以下间距、形状、颜色和尺寸规则共同组成 1.0 基线。

## 间距

| Token | 当前值 | 常见用途 |
|---|---:|---|
| `None` | 0dp | 明确无间距 |
| `Micro` | 2dp | 图标内部、紧凑媒体标记 |
| `ExtraSmall` | 4dp | 紧密文字与图标 |
| `Small` | 8dp | 同一组元素 |
| `Medium` | 12dp | 紧凑容器内边距 |
| `Large` | 16dp | 标准页面边距、卡片内边距 |
| `ExtraLarge` | 24dp | 区块间距 |
| `DoubleExtraLarge` | 32dp | 大区块分隔 |
| `TripleExtraLarge` | 48dp | 强分区或安全留白 |

**必须**先判断语义再选择 Token；**禁止**为了“看着顺眼”新增 10dp、14dp、18dp 等近似值。若确有媒体比例或第三方控件约束，记录理由并登记 Token 差距。

## 形状

| `ContainerLevel` | 语义 | 历史基础值；无玻璃 MIUIX 使用下述角色规格 |
|---|---|---:|
| `Tag` | 小标签、角标 | 4dp |
| `Chip` | 筛选项、小操作 | 6dp |
| `Field` | 输入框、搜索框 | 10dp |
| `Card` | 标准内容卡 | 12dp |
| `Dialog` | 确认与警告弹窗 | 14dp |
| `Sheet` | 底部面板顶部圆角 | 20dp |
| `Floating` | 悬浮栏、FAB | 28dp |
| `Pill` | 胶囊、分段选择 | 由风格 chrome token 决定 |

**必须**使用 `AppShapes.container(level)`；带描边的容器使用 `AppShapes.borderedContainer(level)`。**禁止**在新业务组件中直接写 `RoundedCornerShape(N.dp)`，除非形状由媒体裁切、进度轨道或外部格式严格定义。

## 无玻璃 Miuix 角色规格

以下仅在 MIUIX 且液态玻璃关闭时生效，玻璃开启与 MD3 保持现有值。官方控件的默认圆角优先；业务适配集中在 `AppShapes`、`AppChromeSizeTokens`，不得在页面复制。

| 角色 | 默认视觉规格 | 依据 |
|---|---|---|
| 普通分组/内容卡 | 圆角 16dp | [官方 Card](https://compose-miuix-ui.github.io/miuix/components/card) |
| 紧凑媒体封面 | 圆角 12dp | BiliPai 信息流密度 |
| 突出内容卡 | 圆角 20dp | BiliPai 内容层级 |
| 分类 TabRow | 可点击高至少 48dp、圆角 12dp；42dp 仅作上游视觉参考 | [官方 TabRow](https://compose-miuix-ui.github.io/miuix/components/tabrow) |
| 紧凑筛选 | 可点击高至少 48dp、圆角 10dp；36dp 仅作密度参考 | BiliPai 紧凑交互 |
| 普通页面 | 横向 16dp、组间 24dp、组内 8/12/16dp | BiliPai 页面节奏 |

48dp 是触摸区域下限，不是所有非交互图形的视觉尺寸。锁定版 Miuix `TabRow` 的点击区域与传入高度相同，不能用 42/36dp 的内部控件加 48dp 空外壳冒充触摸扩展，因此应用必须向它传入至少 48dp 的实际高度。字体放大时允许继续增高；短标签按文字宽度和 48dp 最小项宽均分，长标签空间不足时滚动，不缩字硬塞。官方组件内部留白优先保持默认，不给 Preference 和外层 Card 重复加内边距。

## 颜色与表面

颜色应按角色选择，而不是按十六进制选择：背景、内容表面、次级表面、主操作、错误、边框、文字各自表达含义。`AppSurfaceTokens` 负责把 Material `ColorScheme` 与双预设语义连接起来。

- **必须**使用主题角色或 `AppSurfaceTokens`，同时检查浅色、深色与 AMOLED。
- **必须**让错误色只表达错误/破坏性语义，品牌粉色不能自动代表危险。
- **应该**用层级、边框或留白区分容器，不依赖阴影一种手段。
- **禁止**把 feature 层直接读取 `MiuixTheme.colorScheme` 作为常规做法。
- **禁止**把页面里的偶然硬编码颜色升级成公共规范。

## 尺寸、图标和触摸

- 可点击区域**必须**至少为 `AppChromeSizeTokens.MinimumTouchTarget`，当前是 48dp。
- 图标视觉尺寸可以小于 48dp，但承载点击的父容器不能小于 48dp。
- 图标**必须**来自项目当前图标入口或已接入图标库；相同语义使用相同图标家族。
- 纯装饰图标的 `contentDescription` **必须**为 `null`；独立可点击图标必须提供动作名称。
- 紧凑控件可以保留小于 48dp 的图标或装饰外壳；若点击手势挂在内部控件本身，其实际测量尺寸必须达到 48dp，只有手势明确挂在更大的父容器时才可由父容器扩展触摸区。

## Compose 短示例

```kotlin
AppCard(
    onClick = onOpen,
    shape = AppShapes.container(ContainerLevel.Card),
    modifier = Modifier.padding(AppSpacingTokens.Large)
) {
    AppText(title)
}
```

## 代码映射

| 规范 | 代码入口 |
|---|---|
| 间距 | `AppSpacingTokens.kt` 中的 `AppSpacingTokens` |
| 圆角 | `AppShapes.kt` 中的 `ContainerLevel`、`AppShapes` |
| 触摸与 chrome 尺寸 | `AppChromeSizeTokens.kt` |
| 表面角色色 | `AppSurfaceTokens.kt` |
| 动画 | `AppMotionTokens.kt` |
| 排版 | `Type.kt` 与主题 typography |

## 当前差距

代码中仍有大量 `padding(N.dp)`、局部 `RoundedCornerShape` 和直接颜色值。它们要区分为合理领域值与重复设计值，不能机械替换。共享 Token 当前也未覆盖头像尺寸、媒体比例、列表密度和内容最大宽度。

## 验收方法

1. 新组件不出现无理由的布局、圆角、动画或颜色硬编码。
2. 360dp 宽度下文本不被 48dp 点击目标挤出容器。
3. 切换双预设与明暗主题，语义层级仍可辨认。
4. 缺少 Token 时先在[差距台账](10_GAP_LEDGER.md)登记，再由设计系统维护者决定是否新增。
