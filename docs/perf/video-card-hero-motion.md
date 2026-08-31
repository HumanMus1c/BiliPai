# 视频卡片 Hero motion v1

范围仅限卡片 → 视频详情 → 返回；不改变设置枚举、DataStore、普通页面转场或播放 Surface 降级策略。

## 实现与计划适配

- 几何时长在 Nav 宿主的实际 constraints 内计算一次。source/target 为宿主局部 px，除以 density 后计算移动距离与尺寸变化；无有效 bounds 保留原档位时长。窗口/折叠倍率仍由上游基准提供，不重复乘。
- 进场沿用先快后慢的 Continuity；普通返回为几何进场时长 ×0.86，最低 220ms。
- Miuix relativeDepth 是主时钟。背景、文字、媒体直接读取它；Host 不再同时运行固定时长 fallback 并提前标记 IDLE。
- 详情正文展开、加载门槛、媒体预加载和父壳恢复也跟随 Hero 主进度；非 Hero 调用方继续使用原先的兜底时长。
- 自动返回增加单次平滑压缩脉冲，幅度为最终卡片各轴尺寸的 1.25%，不是全屏位移的 1.25%。手势和取消禁用该脉冲；alpha/blur/scrim 不读取它。
- 媒体和来源文字仍在 82%–98% 同窗交接；快照沿用 55%–90% 释放窗口，没有凭离线计算调整窗口。
- BlurEffect 缓存随 snapshot session 保存/释放，进场 1px、返回 4px 量化；API<31、Reduced 或零半径不创建 effect。
- 兼容 Compose sharedBounds 默认仍是 Linear return，保留 seek 契约；新增显式非交互 spec。它与 Miuix 主链路互斥，不同时驱动卡片；既有 key/entry/lifecycle 不做迁移。

当前依赖 Miuix 0.9.4-4f86de92-SNAPSHOT 的本地 sources 有两个约束：

1. NavDisplay 在 relativeDepth≤-1 卸载 outgoing entry。直接给导航 float 添加过冲，会在第一次越界时卸层，无法回落。本实现采用不越界主驱动 + 有界单次空间脉冲。
2. Tween 不承接初始速度，中途打断会回退默认 spring。因此 commit/cancel 使用统一 spec 派生的临界阻尼 spring，承接导航当前值与可用速度；完整 programmatic 开合仍为方向化 tween。spring 真实结束时刻由剩余距离和速度决定，不冒充固定 remainingFraction×duration。视觉层跟随真实结束帧。

上游系统预测返回 cancel 可能传入零速度，不能保证每个 OEM 的取消松手速度连续。本轮不修改/复制导航 runtime，也不引入第二条后台动画掩盖限制。取消从当前位置恢复，但细微顿挫需实机验收。旧路径 settleFromCurrent 支持显式速度且不会与 Nav 并行。

## 无编译离线验证

```bash
python3 -B -m unittest discover -s scripts -p test_video_card_transition_tools.py -v
python3 -B scripts/video_card_transition_curve_probe.py --duration-ms 360 --output /tmp/hero-curves.csv
bash -n scripts/release_card_transition_sample.sh
git diff --check
```

探针从 Kotlin 文件读取曲线和关键 token，按 60/120Hz 输出 progress、velocity、acceleration、landing_scale。释放覆盖 20%/50%/80% 位置和正/负/零速度。最后一帧可能短于刷新间隔，端点加速度留空，不伪造。

这是数学/脚本验证，不等价于 Kotlin 编译、JUnit 执行、GPU 帧率或视觉验收。本轮不运行 Gradle。已追加策略、clock、Nav、timeline 和缓存回归测试，未执行。

BiliPaiNavDisplayHostStructureTest 还包含迁移前 AndroidX NavDisplay 的旧断言；本轮保留旧覆盖并追加当前 Miuix 单时钟断言，不能宣称整类已通过。整理旧架构测试需要单独处理。

## 使用已安装 release/dev 采样

```bash
./scripts/release_card_transition_sample.sh start --device SERIAL \
  --label standard-live-blur-before --report-dir docs/perf/reports
# 手动操作，停在来源页后：
./scripts/release_card_transition_sample.sh stop --device SERIAL
```

默认正式包。dev 请在 start/stop 都设置 PKG=com.android.purebilibili.dev。脚本拒绝 debug/smooth，不构建、不安装、不录屏。窗口中只启用低频 VideoCardMotion 边界日志；stop 后恢复原日志等级，不清设备全局 logcat。

建议短样本分别测打开、按钮返回、预测提交、预测取消。gfxinfo framestats 是有界环形缓冲，长时间八轮操作可能丢失前段帧；整窗口平台计数与最近帧列表的覆盖范围并不相同。

报告从日志获取单调时钟、速度、custom 基准、live surface、blur、预测样式、resolved_duration、source_layout、ownership 和 snapshot/blur/backdrop 计数。不可读取的配置为 unknown。老版本可在 start 加 --config 配置.json；仅刻意采单阶段时，stop 使用 --phase OPENING 等显式归属。

进程重启或样本作废，用以下命令恢复日志等级并归档 session，原始数据保留：

```bash
./scripts/release_card_transition_sample.sh abort --device SERIAL
```

start/stop/abort 必须保持 OUT_DIR 相同；label/report-dir 自动从 session 恢复。

## 离线报告

```bash
python3 -B scripts/video_card_transition_report.py sample-gfxinfo.txt \
  --mem-before sample-mem-before.txt --mem-after sample-mem-after.txt \
  --diagnostics sample-diagnostics.txt --label after \
  --json-out report.json --markdown-out report.md
```

- FrameInterval 优先推断刷新率；老格式按 IntendedVsync 间隔估计并标为 cadence estimate，可用 --refresh-rate 120 覆盖。
- 输出 completion latency p50/p90/p95/p99；优先逐帧 WorkloadTarget 判断预算，不把 latency 当 CPU 工作时长或真实 FPS。
- 默认门槛：>budget≤5%，>2×budget≤1%，p90≤median workload target，PSS 增量≤16MiB。
- exit 0 为通过，exit 2 为门槛失败（仍保存 JSON/Markdown）；缺少有效帧、预算或 PSS 不算通过。
- 阶段归属需要匹配单调时钟且有结束 marker；缺失阶段为 null。missed-vsync/slow UI/bitmap/draw 是整窗口平台计数，不伪造成分阶段数据。
- 接受 gfxinfo/meminfo/diagnostics 文本，不直接解析二进制 Perfetto。深入 CPU/GPU 归因需另采 Perfetto；现有 bili.video_card.* Trace counters 保留。

## 尚待实机验收

60Hz/120Hz 各覆盖 Home 双列/单列、相关推荐横卡、CoverFirst/ImmediatePlayback、live surface/blur 开关、按钮/系统返回、预测提交/取消、进场中途返回、HDR/杜比、横竖屏/平板/折叠态、系统 Reduced。

重点：首帧黑屏、双重叠影、标题晚出现、播放器/封面抢占、压缩脉冲是否明显、取消从当前姿态恢复。离线通过不能将这些项目标为已验收。
