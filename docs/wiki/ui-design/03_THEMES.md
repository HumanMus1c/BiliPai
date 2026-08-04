# 03 主题与三风格

> 文档编号：UI-03  
> 规范版本：1.0.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-02  
> 适用提交：4443e72ff  
> 维护角色：设计系统维护者  
> 相关文档：[设计方向](01_DIRECTION.md) · [基础令牌](02_FOUNDATIONS.md)

## 初学者解释

主题不是一张固定配色表，而是一套“角色到颜色/形状/文字/动效”的映射。页面只说“这是背景”“这是主操作”“这是错误”，主题再决定浅色 Miuix、深色 iOS 或 Material 3 中具体怎样显示。这样切换风格时，业务逻辑不需要重写。

## 规范要求

### 主题树

- **必须**保持一个可供 Compose 使用的共同主题合同，业务页面通过 Material 角色与 `App*` 入口消费它。
- **必须**让 `UiStyle.MIUIX` 映射到 Miuix 渲染器，`UiStyle.IOS` 映射到 iOS 预设，`UiStyle.MATERIAL3` 映射到 Material 3。
- **禁止**在 feature 层直接根据 `UiStyle` 复制整个页面；差异应放在设计系统渲染器或小型策略中。
- **禁止**在 feature 层直接依赖 `MiuixTheme.colorScheme`；Miuix 色彩先桥接到公共语义角色。

### 主题模式

| 模式 | What | Why | How |
|---|---|---|---|
| 浅色 | 亮背景主题 | 日间可读性 | 检查表面层级、弱文字和品牌色 |
| 深色 | 暗背景主题 | 夜间舒适与系统偏好 | 避免纯白大面积高亮，保持内容层级 |
| AMOLED | 以纯黑背景为主的暗色变体 | 用户偏好及部分屏幕功耗 | 仅改变背景/表面策略，不改变组件结构 |
| 动态取色 | 从系统壁纸获得角色色 | 个性化 | 品牌、错误、成功等语义不能因取色失真 |

### 风格映射矩阵

| 设计维度 | Miuix（主基准） | iOS（兼容） | Material 3（兼容） |
|---|---|---|---|
| 顶部栏 | Miuix chrome 与标题节奏 | iOS 标题与连续圆角倾向 | Material TopAppBar 语义 |
| 导航 | Miuix NavigationBar/Rail | iOS 风格导航反馈 | Material NavigationBar/Rail |
| Preference | Miuix Preference 组件优先 | 公共入口的 iOS 渲染 | Material Preference 语义渲染 |
| 圆角 | Miuix squircle/风格 Token | 连续圆角 | Material 形状 Token |
| 动效 | Miuix duration/easing | 弹簧物理倾向 | Material duration/easing |
| 字体 | Miuix 主题映射 | iOS 预设映射 | Material typography 映射 |
| 功能与状态 | 完整覆盖 | 必须相同 | 必须相同 |

### 颜色角色

- `background`：页面最底层，不代表可点击。
- `surface` / `surfaceContainer*`：承载列表、卡片、面板等内容层。
- `primary`：主操作与当前选择，不等于所有品牌内容。
- `on*`：位于对应背景上的文字/图标颜色，不应任意降低透明度。
- `error`：失败、危险或破坏性操作，不用于普通提醒。
- `outline`：边界和分隔，不能取代内容层级。

### 状态一致性

三风格中 enabled、selected、checked、loading、error 等状态**必须**具有相同数据含义。颜色、边框或动画可以不同，但不能只靠颜色表达状态；至少再提供图标、形状、文字或可访问状态说明之一。

## Compose 短示例

```kotlin
val colors = MaterialTheme.colorScheme
AppSurface(
    color = AppSurfaceTokens.contentSurface(colors),
    contentColor = colors.onSurface
) {
    AppText(text = title)
}
```

业务代码声明语义角色，不询问当前是 Miuix 还是 iOS。

## 代码映射

- UI 设置枚举：应用侧 `UiStyle`
- 设计系统预设：`UiPreset.kt` 中的 `UiPreset`
- Android 原生分支：`AndroidNativeVariant`
- 渲染器决策：`PresetPrimitiveRenderer.kt`
- 表面颜色桥接：`AppSurfaceTokens.kt`
- Miuix 现状：[MIUIX_ALIGNMENT.md](../MIUIX_ALIGNMENT.md)

## 当前差距

部分页面直接使用 `MaterialTheme.colorScheme` 是正常的公共主题消费；问题在于个别页面绕过语义层使用固定颜色或直接读取具体风格主题。当前 AMOLED 和动态取色在所有业务状态上的覆盖证据不足。

## 验收方法

1. Miuix 在浅色、深色、AMOLED 下完整检查所有组件状态和关键页面。
2. iOS、Material 3 至少在手机浅/深色与一个平板流程中检查关键页面。
3. 切换主题后页面不重置业务状态、不改变按钮含义、不产生不可读文字。
4. 关闭模糊或动态取色后仍能清楚区分页面背景、内容表面和浮层。

