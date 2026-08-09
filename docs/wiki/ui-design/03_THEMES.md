# 03 主题系统（MIUIX / Material 3 两主题）

> 文档编号：UI-03  
> 规范版本：1.0.0-draft  
> 状态：草案  
> 最后核对日期：2026-08-02  
> 适用提交：4443e72ff  
> 维护角色：设计系统维护者  
> 相关文档：[设计方向](01_DIRECTION.md) · [基础令牌](02_FOUNDATIONS.md) · [前端架构与主题精简优化计划](../FRONTEND_ARCHITECTURE_THEME_SIMPLIFICATION_PLAN.md)

## 初学者解释

主题不是一张固定配色表，而是一套“角色到颜色/形状/文字/动效”的映射。页面只说“这是背景”“这是主操作”“这是错误”，主题再决定 Miuix 或 Material 3 中具体怎样显示。这样切换主题时，业务逻辑不需要重写。

## 规范要求

### 主题树

- **必须**保持一个可供 Compose 使用的共同主题合同，业务页面通过 Material 角色与 `App*` 入口消费它。
- **必须**让 `AppUiStyle.MIUIX` 映射到 Miuix 渲染器，`AppUiStyle.MATERIAL3` 映射到 Material 3 渲染器；iOS 只属于历史迁移兼容来源，不产生任何运行时分支。
- **禁止**在 feature 层直接根据 `AppUiStyle` 复制整个页面；差异应放在设计系统渲染器或小型策略中。
- **禁止**在 feature 层直接依赖 `MiuixTheme.colorScheme`；Miuix 色彩先桥接到公共语义角色。

### 主题模式

| 模式 | What | Why | How |
|---|---|---|---|
| 浅色 | 亮背景主题 | 日间可读性 | 检查表面层级、弱文字和品牌色 |
| 深色 | 暗背景主题 | 夜间舒适与系统偏好 | 避免纯白大面积高亮，保持内容层级 |
| AMOLED | 以纯黑背景为主的暗色变体 | 用户偏好及部分屏幕功耗 | 仅改变背景/表面策略，不改变组件结构 |
| 动态取色 | 从系统壁纸获得角色色 | 个性化 | 品牌、错误、成功等语义不能因取色失真 |

### 主题映射矩阵

| 设计维度 | MIUIX（主基准） | Material 3（兼容） |
|---|---|---|
| 顶部栏 | Miuix chrome 与标题节奏 | Material TopAppBar 语义 |
| 导航 | Miuix NavigationBar/Rail | Material NavigationBar/Rail |
| Preference | Miuix Preference 组件优先 | Material Preference 语义渲染 |
| 圆角 | Miuix squircle/风格 Token | Material 形状 Token |
| 动效 | Miuix duration/easing | Material duration/easing |
| 字体 | Miuix 主题映射 | Material typography 映射 |
| 功能与状态 | 完整覆盖 | 必须相同 |

### 颜色角色

- `background`：页面最底层，不代表可点击。
- `surface` / `surfaceContainer*`：承载列表、卡片、面板等内容层。
- `primary`：主操作与当前选择，不等于所有品牌内容。
- `on*`：位于对应背景上的文字/图标颜色，不应任意降低透明度。
- `error`：失败、危险或破坏性操作，不用于普通提醒。
- `outline`：边界和分隔，不能取代内容层级。

### 状态一致性

两主题中 enabled、selected、checked、loading、error 等状态**必须**具有相同数据含义。颜色、边框或动画可以不同，但不能只靠颜色表达状态；至少再提供图标、形状、文字或可访问状态说明之一。

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

业务代码声明语义角色，不询问当前是 Miuix 还是 Material 3。

## 代码映射

- UI 设置枚举：`AppUiStyle`（MIUIX / MATERIAL3），运行时唯一主题类型
- 遗留预设（仅迁移与旧文件兼容边界）：`UiPreset.kt` 中的 `UiPreset` / `AndroidNativeVariant` / `UiStyle.IOS`
- 渲染器决策：`PresetPrimitiveRenderer.kt`
- 表面颜色桥接：`AppSurfaceTokens.kt`
- Miuix 现状：[MIUIX_ALIGNMENT.md](../MIUIX_ALIGNMENT.md)

## 当前差距

部分页面直接使用 `MaterialTheme.colorScheme` 是正常的公共主题消费；问题在于个别页面绕过语义层使用固定颜色或直接读取具体风格主题。当前 AMOLED 和动态取色在所有业务状态上的覆盖证据不足。

## 验收方法

1. Miuix 在浅色、深色、AMOLED 下完整检查所有组件状态和关键页面。
2. Material 3 至少在手机浅/深色与一个平板流程中检查关键页面；历史 iOS 用户自动迁移为 MIUIX。
3. 切换主题后页面不重置业务状态、不改变按钮含义、不产生不可读文字。
4. 关闭模糊或动态取色后仍能清楚区分页面背景、内容表面和浮层。

