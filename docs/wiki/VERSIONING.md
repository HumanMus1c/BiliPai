# BiliPai 版本规范

最后更新：2026-08-05

## 当前选择

BiliPai 采用日历日构建号：

```text
YY.MMDD.N
```

| 段 | 含义 | 示例 |
| --- | --- | --- |
| `YY` | 两位公元年 | `26` = 2026 |
| `MMDD` | 月日（补零） | `0805` = 8 月 5 日 |
| `N` | **自然日**内第几次正式构建（从 1 起） | `1`、`2`… |

当前构建：`26.0805.1` / `versionCode 283`。

- 应用 ID、签名和用户配置格式不变。
- Android `versionCode` **独立于** `versionName`，每次发布必须单调 +1，永不回退或复用。
- 应用内更新优先比较发布元数据中的 `versionCode`；不要用 `versionName` 字符串单独判断新旧。
- 时区：按 `Asia/Shanghai` 取「自然日」。

### 展示与追溯

| 位置 | 内容 |
| --- | --- |
| `versionName` / APK 名 | `26.0805.1` |
| 关于页建议 | `v26.0805.1 · 283`，并可附短 commit |
| Git 短 SHA / 完整 sha256 | 关于页、Telegram 说明、日志；**不**写入主 `versionName` |

历史 9.x / 0.1.0 安装升级仍以 `versionCode` 为准。

## 递增规则

1. **同一天再打正式包**：`N + 1`，同时 `versionCode + 1`  
   - 例：`26.0805.1` → `26.0805.2`（code 283 → 284）
2. **换日**：日期改为当天 `YY.MMDD`，`N` 从 `1` 起，同时 `versionCode + 1`  
   - 例：`26.0805.2` → `26.0806.1`
3. 仅工程变体后缀（不改变正式 `versionName` 主体）：  
   - debug：`versionNameSuffix = "-debug"`  
   - dev：`versionNameSuffix = "-dev"`  
   - smooth：`versionNameSuffix = "-smooth"`
4. 稳定版 Git 标签：`v<versionName>`，例如 `v26.0805.1`。

## 示例

```text
26.0805.1    # 2026-08-05 第 1 包
26.0805.2    # 同日第 2 包
26.0806.1    # 次日第 1 包
26.1231.3
27.0101.1
```

## 与其它方案的关系

| 方向 | 示例 | BiliPai |
| --- | --- | --- |
| 两位年日历构建 | `26.0805.1` | **当前采用** |
| 四位年 | `2026.0805.1` | 不采用（过长） |
| `0.x` / `1.x` SemVer | `0.1.0` | 已结束该纪元展示 |
| 完整 sha256 进 versionName | — | 不采用 |

## 发布一致性

一次正式发布中下列值必须一致：

- `app/build.gradle.kts` 的 `versionName`；
- Git 标签去掉前缀 `v` 后的版本；
- `CHANGELOG.md` 顶部版本；
- `build-metadata.json` 中的 `versionName`；
- 交付 APK 文件名中的版本。

Release APK：`BiliPai-<versionName>.apk`，例如 `BiliPai-26.0805.1.apk`。  
Dev 验证包：`BiliPai-<versionName>-dev.apk`。

### 交付路径（不要拿 AGP 默认名）

| 命令 | 用户交付文件 |
| --- | --- |
| `./gradlew :app:assembleRelease` | `app/build/outputs/bilipai/release/BiliPai-26.0805.1.apk` |
| `./gradlew :app:assembleDev` | `app/build/outputs/bilipai/dev/BiliPai-26.0805.1-dev.apk` |

- `app/build/outputs/apk/**/app-*.apk` 或带 `-release` 后缀的中间产物**不是**对外交付名。
- `assembleRelease` / `assembleDev` 会 `finalizedBy` 导出任务，强制写成 `BiliPai-` 前缀；命名校验会拒绝 `app-release` 一类默认名。
- AGP 基名已设为 `BiliPai-<versionName>`，进一步避免默认 `app` 工程名。
