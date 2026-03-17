# Phase 5: AE2 Integration - Context

**Gathered:** 2026-03-17
**Status:** Ready for planning

<domain>
## Phase Boundary

无缝 AE2 网络集成，让路由系统与 AE2 原生频道系统正确协作。包括虚拟频道注入、控制器事件处理、设备频道验证和 GridFlags 配置。

**本阶段交付：**
- AE2 控制器增删事件正确检测和响应
- 连接的 AE 设备显示有效的虚拟频道分配
- 网络事件触发正确的频道重新计算
- GridFlags 配置正确，频道使用跟踪准确

**需求覆盖：** AEIN-01, AEIN-02, AEIN-03, AEIN-04

</domain>

<decisions>
## Implementation Decisions

### 虚拟频道注入策略

- **方案倾向**：高性能方案
  - 优先考虑性能，减少对设备数量的敏感度
  - 支持灵活拓扑：1个终端连100个设备 或 100个终端各连1个设备，性能表现相似
  
- **用户体验**：玩家零操作
  - 即插即用，频道自动分配
  - 玩家放置设备后不需要额外配置

- **技术方向**：深入 AE2 机制
  - 需要与 AE2 的频道计算系统深度集成
  - 可能需要修改 PathGridCache 或注册自定义 GridCache

### 控制器事件处理粒度

- **新控制器加入**：仅增加可用池
  - 新增 AE2 控制器的频道进入可用池
  - 已分配给终端的频道保持不变
  - 只有新请求才能使用新增频道
  - 保持现有分配稳定

- **控制器移除**：立即强制回收
  - 控制器被移除时立即从终端回收频道
  - 确保总使用量不超过新容量
  - 快速响应，可能中断设备但保证一致性

- **传播时机**：终端按需拉取
  - 终端定期查询控制器状态
  - 控制器被动响应
  - 终端控制节奏，减少控制器负担
  - 与阶段 3-4 的拉取模式一致

- **多控制器处理**：汇总所有控制器
  - 路由控制器检测整个 AE 网络中所有 AE2 控制器
  - 所有控制器的所有面频道汇总到频道池
  - 符合项目范围定义

### 设备频道验证方式

- **设备识别**：实现 AE2 接口检测
  - 检查 Tile Entity 是否实现 AE2 相关接口
  - 主要检查 IGridHost、IGridNode 接口
  - 标准 API 方式，有明确支持

- **需要频道判断**：检查 GridFlags.REQUIRE_CHANNEL
  - 使用 AE2 原生标志判断设备是否需要频道
  - 与 AEBaseRouterTile 当前使用的机制一致

- **验证时机**：事件驱动验证
  - 订阅 AE2 网络事件
  - 频道变化时收到通知
  - 零轮询开销
  - 依赖 gridChanged() 回调机制

- **验证失败处理**：在 GUI 中显示设备状态
  - 明确显示哪些设备获得了频道，哪些没有
  - 直观显示频道分配状态
  - 需要额外 GUI 支持

### GridFlags 配置策略

- **路由控制器**：需要频道（REQUIRE_CHANNEL）
  - 占用一个 AE2 频道参与网络
  - 像其他 AE2 设备一样需要频道才能工作
  - 保持与传统 AE2 设备的一致性

- **路由终端**：频道中性
  - 不消耗 AE2 频道
  - 纯频道转发器角色
  - 从路由控制器的频道池分配

- **路由线缆**：频道中性
  - 不消耗 AE2 频道
  - 仅传递路由消息
  - 与终端保持一致

- **连接的 AE 设备**：设备自己管理频道标志
  - 设备按 AE2 原生机制自己申请频道
  - 路由系统只是转发频道
  - 保持设备原生行为

### Claude's Discretion

- 虚拟频道注入的具体实现机制（PathGridCache 修改 vs 自定义 GridCache）
- 事件回调的具体注册和触发细节
- GUI 中设备状态显示的具体布局和样式
- 终端查询控制器的具体频率（tick 间隔）

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### AE2 源码参考
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/me/cache/PathGridCache.java` — AE2 频道计算核心类，理解频道分配机制
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/me/GridNode.java` — GridNode 实现，GridFlags 定义，频道需求判断
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/api/networking/IGridHost.java` — AE2 网络主机接口，用于设备识别
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/api/networking/IGridNode.java` — AE2 网络节点接口
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/api/networking/GridFlags.java` — GridFlags 枚举定义

### 阶段上下文
- `.planning/phases/01-core-infrastructure/01-CONTEXT.md` — 构建配置、AE2 集成模式
- `.planning/phases/02-routing-cable/02-CONTEXT.md` — IRoutingDevice 接口、连接检测机制
- `.planning/phases/03-routing-controller/03-CONTEXT.md` — 控制器检测策略、频道池管理、通信协议
- `.planning/phases/04-routing-terminal/04-CONTEXT.md` — 终端架构、频道分发机制、设备连接策略

### 项目文档
- `.planning/REQUIREMENTS.md` — AEIN-01 到 AEIN-04 需求定义
- `.planning/PROJECT.md` — 项目核心价值和约束

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `AEBaseRouterTile.java` — 基础 Tile，已集成 AE2 网络、GridFlags 配置
  - `gridChanged()` 回调用于网络拓扑变化检测
  - `hasGridAccess()` 检查网格连接状态
  - 目前只设置 `GridFlags.REQUIRE_CHANNEL`
  
- `RoutingControllerTile.java` — 控制器已实现频道池管理
  - `detectControllers()` 检测所有 AE2 控制器
  - `allocateChannels()` / `releaseChannels()` 频道分配 API
  - `gridChanged()` 触发重新检测
  
- `RoutingTerminalTile.java` — 终端已实现频道申请和分发
  - `canProvideChannels()` 占位方法（Phase 4）
  - `isAEDevice()` 设备检测（字符串匹配，需要改进）
  - `updateDeviceConnections()` 设备连接扫描
  
- `IRoutingDevice.java` — 路由设备接口
  - `DeviceType` 枚举定义 CONTROLLER、TERMINAL、CABLE

### Established Patterns
- **AE2 集成**：继承 `AENetworkInvTile`，使用 `AENetworkProxy`
- **事件驱动**：`gridChanged()` 回调处理网络变化
- **频道池管理**：`Set<TileController>` 跟踪控制器，实时计算总频道
- **拉取模式**：终端主动从控制器请求频道
- **GridFlags**：使用 `REQUIRE_CHANNEL` 标志

### Integration Points
- **GridFlags 扩展**：需要为不同设备类型配置不同的标志
- **频道注入**：需要扩展 `canProvideChannels()` 实现真正注入
- **设备检测改进**：`isAEDevice()` 需要从字符串匹配改为接口检测
- **GUI 扩展**：需要在终端 GUI 中显示设备频道状态
- **事件订阅**：可能需要订阅更多 AE2 网络事件

</code_context>

<specifics>
## Specific Ideas

- **虚拟频道注入核心思路**：
  - 让路由终端成为 AE2 频道的"提供者"而非"消费者"
  - 连接的 AE 设备仍然按原生机制申请频道
  - 路由系统透明地将频道池中的频道分配给设备

- **控制器事件响应**：
  - 扩容自由（新控制器只增加可用池）
  - 缩容严格（控制器移除立即回收）
  - 保持系统稳定性和一致性

- **GridFlags 配置模式**：
  - 只有路由控制器需要 AE2 频道（1 个）
  - 路由终端和线缆都是频道中性（0 个）
  - 路由系统本身的开销就是 1 个 AE2 频道

- **设备验证改进**：
  - 从字符串匹配升级到接口检测
  - 使用 AE2 原生的事件系统
  - 在 GUI 中直观显示状态

</specifics>

<deferred>
## Deferred Ideas

**阶段 6+ 功能（超出本阶段范围）：**
- 频道使用历史统计和分析（v2）
- 设备级别的频道优先级系统（v2）
- 更详细的设备类型识别和分类（v2）
- 频道分配的智能调度和优化（v2）
- 多路由控制器协调（明确不在范围内）
- 终端级别的频道硬限制（v2，当前用软限制警告）

</deferred>

---

*Phase: 05-ae2-integration*
*Context gathered: 2026-03-17*