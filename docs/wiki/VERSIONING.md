# BiliPai 版本规范

最后更新：2026-08-04

## 当前选择

BiliPai 从 `0.1.0 / versionCode 282` 开始启用新的 `0.x` 语义化版本纪元。应用 ID、签名和用户配置格式不变，Android `versionCode` 独立于展示版本并永久保持单调递增。

历史 9.x 安装中的旧更新器会把 9.x 视为大于 0.x，因此用户首次进入新纪元时需要手动下载安装 `BiliPai-0.1.0.apk`。进入 0.x 后，应用内更新优先比较发布元数据中的 `versionCode`。

## 版本递增规则

- `0.MINOR.PATCH`：当前开发阶段。新增兼容功能递增 `MINOR`，兼容修复递增 `PATCH`。
- `1.0.0`：核心功能、数据格式和兼容承诺稳定后的首个正式稳定纪元。
- 预发布版本：使用 `0.2.0-alpha.1`、`0.2.0-beta.1`、`0.2.0-rc.1`，顺序为 alpha、beta、rc、stable。
- 每次发布都必须增加 `versionCode`；不得因 `versionName` 降低、换纪元或回滚功能而降低或复用构建号。
- 稳定版 Git 标签固定为 `v<versionName>`，例如 `v0.1.0`。

## 常用版本方向参考

| 方向 | 示例 | 适用场景 | BiliPai 采用情况 |
| --- | --- | --- | --- |
| `0.x` 语义化版本 | `0.1.0` | 产品重新起步、接口仍会演进 | 当前采用 |
| 稳定语义化版本 | `1.0.0` | 核心体验与兼容承诺稳定 | 后续目标 |
| 延续大版本 | `10.0.0` | 重大重构但不希望重置数字序列 | 本轮不采用 |
| 日期版本 | `2026.8.0` | 高频发布并强调发布时间 | 本轮不采用 |

## 发布一致性

一次正式发布中的下列值必须一致：

- `app/build.gradle.kts` 的 `versionName`；
- Git 标签去掉前缀 `v` 后的版本；
- `CHANGELOG.md` 顶部版本；
- `build-metadata.json` 中的 `versionName`；
- 交付 APK 文件名中的版本。

发布说明的比较基准和固定格式见 [更新日志撰写规范](CHANGELOG_GUIDE.md)。

Release APK 固定命名为 `BiliPai-<versionName>.apk`，例如 `BiliPai-0.1.0.apk`；Dev 验证包命名为 `BiliPai-<versionName>-dev.apk`。AGP 的内部中间文件名不作为发布附件或用户交付名称。
