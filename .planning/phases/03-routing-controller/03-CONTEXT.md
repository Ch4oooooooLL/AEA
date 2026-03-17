# Phase 3: Routing Controller - Context

**Gathered:** 2026-03-17
**Status:** Ready for planning

<domain>
## Phase Boundary

路由控制器是 AE2 频道路由系统的中央枢纽。自动检测 AE 网络中的所有控制器，接管所有可用频道（每控制器 6 面 × 32 频道），并通过无线"路由频道"向路由终端分配频道。

**本阶段交付：**
- RoutingControllerBlock 和 RoutingControllerTile 实现
- AE2 控制器自动检测和频道池管理
- 实时 GUI 显示网络统计信息
- 与路由终端的无线通信协议基础

</domain>

<decisions>
## Implementation Decisions

### 控制器检测策略
- **检测机制：** 事件驱动（Event-driven）
  - 依赖 AE2 的 `gridChanged()` 回调和网络事件
  - 避免定期扫描，减少性能开销
- **触发时机：** 方块放置时 + 网络拓扑变化时
  - 控制器方块放置执行初始检测
  - 网络变化（控制器增删）触发更新
- **移除处理：** 立即重新计算
  - 控制器被移除时立即重新扫描
  - 实时反映真实可用频道数
- **多控制器处理：** 汇总所有频道
  - 收集所有 AE2 控制器的所有面
  - 每个控制器提供 192 频道（6 面 × 32 频道）
  - 总频道数 = 控制器数量 × 192

### 频道池管理
- **跟踪方式：** 按控制器跟踪 + 实时计算
  - 分别记录每个检测到的控制器
  - 实时计算总频道数
  - 便于 GUI 显示详细统计
- **分配策略：** 先到先得（First-come-first-served）
  - 按终端连接顺序分配
  - 后续请求失败（频道不足时）
  - 简单公平，易于调试
- **更新策略：** 立即更新
  - 控制器或终端变化时立即重新计算
  - 保证数据一致性
  - 适合实时 GUI 显示

### GUI 信息显示
- **信息级别：** 中等详情
  - 显示：总频道数、已用频道数
  - 显示：控制器数量、终端数量
  - 平衡信息量和界面复杂度
- **更新频率：** 实时更新
  - 频道变化时立即更新 GUI
  - 与立即更新策略保持一致
- **布局风格：** 数字统计型
  - 类似 AE2 网络工具的样式
  - 数字统计 + 进度条
  - 与 AE2 风格一致，玩家熟悉

### 终端通信协议
- **通信模式：** 拉取模式（Pull Mode）
  - 终端主动请求频道分配
  - 终端控制请求节奏
- **发现机制：** 自动发现（相同路由频道）
  - 终端自动连接到相同"路由频道"的控制器
  - 无视距离限制（无线通信）
  - 通过路由频道 ID 匹配
- **断开处理：** 立即回收频道
  - 终端断开时立即回收其分配的频道
  - 资源高效，立即释放给其他设备

### Claude's Discretion
- 具体 GUI 布局细节（间距、颜色、字体大小）
- 消息协议的具体字段设计
- 路由频道 ID 的生成和管理机制
- 频道分配的内部数据结构优化

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### AE2 源码参考
- `D:/Desktop/LYC/code/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/tile/networking/TileController.java` — AE2 控制器实现，邻接检测逻辑
- `D:/Desktop/LYC/code/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/me/cache/PathGridCache.java` — 频道计算和网络管理
- `D:/Desktop/LYC/code/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/me/pathfinding/PathingCalculation.java` — BFS/DFS 频道分配算法
- `D:/Desktop/LYC/code/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/me/GridNode.java` — GridNode 实现，CHANNEL_COUNT 数组定义（32 频道每面）

### 项目上下文
- `.planning/phases/01-core-infrastructure/01-CONTEXT.md` — 构建配置、AE2 集成模式
- `.planning/phases/02-routing-cable/02-CONTEXT.md` — IRoutingDevice 接口、路由线缆实现

### 技术规格
- **频道计算**：每个控制器面 = 32 频道（`CHANNEL_COUNT[2]`）
- **控制器结构**：多方块结构，必须在 7×7×7 范围内
- **总频道公式**：总频道 = 控制器数量 × 6 面 × 32 频道 = 192 × 控制器数量

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `AEBaseRouterTile.java` — 基础 Tile，已集成 AE2 网格、GridFlags、生命周期管理
  - `gridChanged()` 回调用于网络拓扑变化检测
  - `hasGridAccess()` 检查网格连接状态
- `IRoutingDevice.java` — 路由设备接口
  - `DeviceType.CONTROLLER` 枚举值已定义
  - `canConnectFrom()` 用于连接验证
- `RoutingCableTile.java` — 连接检测模式
  - `EnumSet<ForgeDirection>` 用于六面连接跟踪
  - `updateConnections()` 方法可复用
- `NetworkProxy.java` — 网格访问包装器

### Established Patterns
- **AE2 集成**：继承 `AENetworkInvTile`，使用 `AENetworkProxy`
- **包结构**：`com.ae2channelrouter.block`（方块）、`com.ae2channelrouter.tile`（TileEntity）
- **注册模式**：在 `ModBlocks` 中注册方块和 Tile
- **接口检测**：使用 `IRoutingDevice` 接口识别路由设备
- **preInit 初始化**：遵循 GTNH 约定，在 preInit 中完成注册

### Integration Points
- **方块注册**：在 `ModBlocks.registerBlocks()` 添加路由控制器方块
- **Tile 注册**：在 `ModBlocks.registerTileEntities()` 注册 Tile
- **AE2 网格接入**：继承 `AEBaseRouterTile` 自动获得网格接入能力
- **GUI 注册**：需要在 `ClientInit` 或网络代理中注册 GUI 容器
- **网络通信**：Forge SimpleNetworkWrapper（已在前期阶段设置）

</code_context>

<specifics>
## Specific Ideas

- **频道计算依据 AE2 源码**：
  - `CHANNEL_COUNT = {0, 8, 32, Integer.MAX_VALUE}`
  - 控制器使用索引 2，即 32 频道每面
  - 控制器形成多方块结构时，每个控制器仍贡献完整的 6 面
  
- **GUI 风格参考 AE2 网络工具**：
  - 简洁的数字显示
  - 频道使用进度条
  - 实时更新的统计数据

- **无线通信模型**：
  - 无视物理距离限制
  - 通过"路由频道"ID 匹配
  - 终端和控制器必须在相同路由频道

</specifics>

<deferred>
## Deferred Ideas

**阶段 4+ 功能（超出本阶段范围）：**
- 路由终端具体实现（阶段 4）
- 频道分配优先级系统（v2）
- 详细的连接可视化（v2）
- 多路由控制器协调（明确不在范围内）
- 终端级别频道限制（明确不在范围内）

</deferred>

---

*Phase: 03-routing-controller*
*Context gathered: 2026-03-17*
