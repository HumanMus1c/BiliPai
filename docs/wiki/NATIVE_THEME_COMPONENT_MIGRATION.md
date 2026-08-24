# 双主题原生组件迁移

最后更新：2026-08-24  
状态：实施中（阶段 0）

## 目标合同

业务页面只依赖中性的 `App*` 入口。设计系统根据 `AppUiStyle` 选择渲染器：

```text
feature UI
    -> App* facade
        -> renderer/material3 -> Material 3 原生组件
        -> renderer/miuix     -> Miuix 原生组件或 Miuix 基元组合
```

- `MATERIAL3` 必须使用 Material 3 原生组件。
- `MIUIX` 必须使用 Miuix 原生组件；上游无等价组件时，以 Miuix `Surface`、`Text`、
  `Icon`、`BasicComponent` 等基元组合，不回退到 Material 3 可见组件。
- feature 不读取 `LocalAppUiStyle`，不复制整页主题分支，不直接导入可见的 Material 3 / Miuix
  组件。
- 公共 `App*` API 不暴露 `ButtonColors`、`DrawerState`、`SnackbarHostState`、
  `TabPosition` 等厂商类型；状态、结果、颜色和变体由应用自有类型表达。
- 两个主题保持相同业务语义、状态和事件，允许布局密度、圆角、图标、动效和视觉层级不同。

## 固定例外

本轮只迁移普通可见组件，不借机改造下列能力：

1. 液态玻璃继续使用现有 Miuix blur / shader / squircle 链路。阶段 0 以精确文件集合冻结，
   不允许借组件迁移扩大使用范围。
2. `BiliPaiNavDisplayHost` 继续使用 Miuix `NavDisplay`，相关 back stack、transition 与手势桥接
   作为 MD3 与 MIUIX 共用的导航子系统；当前 12 个源码文件以精确集合冻结。
3. `AndroidView`、Media3 播放表面、系统 UI 与第三方插件内部 UI 不属于本次组件迁移。

## 尺寸与圆角合同

`48.dp` 不是统一的视觉尺寸。迁移时必须区分：

- **视觉几何**：优先由原生组件自身规格决定；自建基元则由主题、组件角色和语义圆角共同解析。
- **触控几何**：不足时在交互/语义层扩展到无障碍最小命中区域，不放大视觉容器。
- **胶囊与圆形**：尺寸和圆角共同决定几何；圆角受控于高度，不能在调用点分别写两个无关常量。
- **普通圆角矩形**：通过比例上限避免 `radius >= height / 2` 后意外变为全胶囊。

页面不得使用 `AppChromeSizeTokens.MinimumTouchTarget` 充当视觉 `.size(...)`。现有
`AppShapes`、`resolveCompactCapsuleChromeSpec` 与 `resolveRoundedControlVisualGeometry` 是迁移起点；
后续统一收敛到按主题和组件角色解析的几何策略。

## 阶段 0 基线（2026-08-24）

生产源码统计：

| 检查项 | 文件数 | import / 分支行数 | 退出目标 |
|---|---:|---:|---:|
| feature 直接 Material 3 import | 237 | 341 | 0 个可见组件 import |
| feature Material 3 通配 import | 89 | 89 | 0 |
| feature 直接 Miuix 可见组件 import | 1 | 1 | 0 |
| feature 直接 Miuix icon import | 5 | 26 | 0 |
| feature 主题分支 | 8 | 34 | 0 |
| `AppPrimitiveComponents.kt` Material 3 import | 1 | 78 | 0（迁入 MD3 renderer） |
| 液态玻璃 Miuix effect 文件 | 45 | 冻结集合 | 保持集合身份不变 |

静态门禁位于
`app/src/test/java/com/android/purebilibili/core/ui/migration/NativeThemeMigrationBoundaryTest.kt`：

- 存量预算只能下降；每完成一批迁移，必须同步降低上限。
- MD3 renderer 禁止导入 Miuix；Miuix renderer 禁止导入 Material 3。
- 液态玻璃文件集合使用路径摘要冻结。
- Miuix navigation 只能出现在已冻结的共享导航子系统文件集合。

## 组件映射

| 中性入口 | Material 3 renderer | Miuix renderer |
|---|---|---|
| Text / Icon | `Text` / `Icon` | `Text` / `Icon` |
| Surface / Card / Divider | 原生 Surface / Card / Divider | 原生 Surface / Card / Divider |
| Button / IconButton | 原生各变体 | 原生 Button / IconButton 与 Miuix 基元组合 |
| Checkbox / Radio / Switch / Slider | 原生选择控件 | 原生选择控件 |
| Progress / Loading | 原生进度组件 | 原生进度组件 |
| TextField / Search | 原生输入组件 | `TextField` / `InputField`；前后缀由 Miuix 基元组合 |
| ListItem | `ListItem` | `BasicComponent` |
| Preference | ListItem + 原生控件 | Miuix Preference 家族 |
| Menu / Spinner | Dropdown / Dialog | Miuix overlay / window spinner |
| Dialog | Alert / BasicAlertDialog | `OverlayDialog` |
| Bottom sheet | `ModalBottomSheet` | `OverlayBottomSheet` |
| Navigation / Tabs | 原生 MD3 chrome | 原生 Miuix chrome |
| Chip / Drawer | 原生组件 | Miuix 基元组合 |
| Date selection | `DatePicker` | NumberPicker 组合 + `OverlayDialog` |

## 执行波次

### 0. 基线与门禁

- 冻结 import、主题分支、液态玻璃与导航例外。
- 修正设计文档与真实依赖不一致的描述。
- 所有后续批次先降低棘轮，再删除旧入口。

### 1. 渲染器骨架

- 拆分 `AppPrimitiveComponents.kt`，建立 `renderer/material3` 与 `renderer/miuix`。
- 引入应用自有变体、颜色、状态和结果类型。
- 公共 facade 是唯一读取 `LocalAppUiStyle` 的位置；renderer 接收已解析的中性参数。
- 建立按主题、组件角色与语义圆角解析的视觉几何策略；触控下限独立处理。

### 2. 基础视觉

迁移 Text、Icon、Surface、Card、Divider、Badge/Tag。目标是 MIUIX 分支不再通过 Material 3
可见组件渲染，同时保持现有文本复制、语义颜色与内容 slot 合同。

### 3. 操作、选择与反馈

迁移 Button、IconButton、Checkbox、Radio、Switch、Slider、FAB、Progress、Loading、Tooltip、
Snackbar。按钮视觉尺寸按原生规格或几何策略解析，不能把 48dp 触控下限写进视觉容器。

### 4. 输入、列表、Preference 与菜单

迁移 TextField/Search、ListItem、Preference、Dropdown/Spinner 和 Chips。移除 MIUIX 输入框因
prefix/suffix 回退 Material 3 的路径；复杂内容统一使用 slot API。

### 5. Dialog 与 Sheet

- 紧凑宽度：MD3 `ModalBottomSheet` / Miuix `OverlayBottomSheet`。
- 中宽及以上：MD3 `BasicAlertDialog` / Miuix `OverlayDialog`。
- 统一返回、点外关闭、拖拽、IME、焦点、旋转恢复与并发弹层行为。
- 替换当前禁止 Miuix overlay 的旧结构测试。

### 6. 导航与复合组件

迁移 Scaffold、TopBar、NavigationBar/Rail、Tabs、Drawer、DatePicker 和非玻璃 FAB/nav。
液态玻璃实现与共享 Miuix NavDisplay 保持不动，重点检查 inset 不重复消费。

### 7. 双主题图标

feature 只消费 `AppIcons` 语义入口。MD3 使用 Material Symbols；MIUIX 使用 Miuix Icons 或
项目自有 vector。Miuix 缺失图标不得回退 Material Symbols。

### 8. Feature 清理

按以下顺序逐批清除直接组件调用、通配 import 与主题分支：

1. settings（试点）
2. home / search / dynamic
3. message / profile / download / list
4. live / bangumi / audio
5. video / player
6. login / onboarding / cast / web / plugin / 长尾页面

每批独立提交并推送；出现问题只回滚当前批次。

### 9. 收尾

- 删除兼容 overload 和旧厂商类型 API。
- 可见组件、图标与 feature 主题分支预算降到零。
- 更新组件目录、主题规范、弹层规范与验收手册。

## 验证规则

默认只运行 `rg`、源文件检查等静态验证。Gradle 单测、Kotlin 编译、打包、安装和设备验收，
仅在用户明确授权后执行。

人工验收至少覆盖 360 / 700 / 840 / 1000 / 1600dp，浅色、深色、AMOLED，1.3 倍字体，
以及触摸、键盘、鼠标和 D-pad 输入。视觉组件大小与实际点击命中区域分别验收。
