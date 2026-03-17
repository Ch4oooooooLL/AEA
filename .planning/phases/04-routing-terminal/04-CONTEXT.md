# Phase 4: Routing Terminal - Context

**Gathered:** 2026-03-17
**Status:** Ready for planning

<domain>
## Phase Boundary

路由终端是 AE2 频道路由系统的分发节点。从控制器接收频道分配，并通过路由线缆将频道分发给连接的 AE 设备。本阶段交付一个完整功能的路由终端，支持无限制数量的连接设备。

**本阶段交付：**
- RoutingTerminalBlock 和 RoutingTerminalTile 实现
- 终端与控制器之间的无线通信机制
- 频道分发逻辑（将控制器分配的频道传递给 AE 设备）
- 基础 GUI 显示终端状态信息

</domain>

<decisions>
## Implementation Decisions

### 终端架构
- **控制器发现机制：** 自动发现 - 相同路由频道 ID
  - 终端自动连接到与其路由频道 ID 匹配的控制器
  - 继承 Phase 3 的"路由频道"无线通信模型
  - 无视物理距离限制
- **数据缓存策略：** 终端本地缓存频道分配信息
  - 减少频繁的网络通信
  - 提高响应速度
  - 在适当时机与控制器同步
- **GUI 需求：** 基础状态显示
  - 显示已分配频道数
  - 显示连接状态（与控制器通信状态）
  - 显示连接设备数量
  - 类似 AE2 网络工具的简洁风格

### 频道分发机制
- **分发模式：** 主动推送模式
  - 终端主动为连接的 AE 设备分配频道
  - 终端管理频道的分配和回收
  - 控制器只需响应终端的分配请求
- **设备跟踪：** 仅计数，不维护详细列表
  - 跟踪连接设备数量
  - 最小化内存和计算开销
  - 满足高性能要求
- **断开处理：** 立即回收
  - 检测到设备断开时立即回收其频道
  - 频道立即返回控制器池，供其他设备使用
  - 资源高效利用

### 通信协议细节
- **初始连接：** 静默注册
  - 终端启动时直接向控制器发送注册请求
  - 无发现过程，最少网络开销
  - 最高性能方案
- **刷新策略：** 按需刷新
  - 仅在需要频道或状态变化时与控制器通信
  - 避免不必要的轮询
  - 最大化效率
- **失败处理：** 标记离线
  - 通信失败后标记控制器为离线状态
  - 等待下次成功通信或重新发现
  - 避免无限重试造成网络负担

### 设备连接策略
- **连接方式：** 混合模式
  - 支持通过路由线缆连接 AE 设备
  - 支持 AE 设备直接放置在终端旁边
  - 两种连接方式同等对待
- **设备限制：** 软限制（警告模式）
  - 不强制限制单个设备的频道使用
  - 超过合理阈值时显示警告
  - 允许玩家自由配置（符合 TERM-04 要求）
- **设备检测：** 不检测设备类型
  - 所有连接设备同等对待
  - 统一分配频道
  - 简化实现，提高性能

### Claude's Discretion
- 具体 GUI 布局细节（间距、颜色、字体大小）
- 终端缓存同步的具体触发时机
- 软限制阈值的具体数值
- 混合连接模式的检测优先级
- 静默注册消息的具体字段设计

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase 3 上下文（控制器实现）
- `.planning/phases/03-routing-controller/03-CONTEXT.md` — 控制器检测策略、频道池管理、通信协议基础
- `src/main/java/com/ae2channelrouter/tile/RoutingControllerTile.java` — 控制器频道分配 API

### Phase 2 上下文（路由线缆）
- `.planning/phases/02-routing-cable/02-CONTEXT.md` — IRoutingDevice 接口、连接检测机制
- `src/main/java/com/ae2channelrouter/tile/RoutingCableTile.java` — 六面连接跟踪实现

### Phase 1 上下文（基础架构）
- `.planning/phases/01-core-infrastructure/01-CONTEXT.md` — 构建配置、AE2 集成模式
- `src/main/java/com/ae2channelrouter/tile/AEBaseRouterTile.java` — 基础 Tile 实现

### API 接口
- `src/main/java/com/ae2channelrouter/api/IRoutingDevice.java` — 路由设备接口，包含 TERMINAL 类型

### AE2 源码参考
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/tile/networking/TileController.java` — AE2 控制器实现
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/src/main/java/appeng/api/networking/` — AE2 网络 API

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `RoutingControllerTile.java` — 控制器已实现频道分配 API
  - `allocateChannels(UUID terminalId, int requestedChannels)` — 请求频道分配
  - `releaseChannels(UUID terminalId)` — 释放频道
  - `getAllocatedChannels(UUID terminalId)` — 查询已分配频道
- `IRoutingDevice.java` — 接口已定义 TERMINAL 类型
  - `DeviceType.TERMINAL` 枚举值可用
  - `canConnectFrom()` 用于连接验证
- `RoutingCableTile.java` — 连接检测模式可复用
  - `EnumSet<ForgeDirection>` 用于六面连接跟踪
  - `updateConnections()` 方法可复用
- `AEBaseRouterTile.java` — 基础 Tile 继承
  - `gridChanged()` 回调用于网络变化检测
  - `hasGridAccess()` 检查网格连接状态

### Established Patterns
- **AE2 集成：** 继承 `AEBaseRouterTile`，使用 `AENetworkProxy`
- **设备类型：** 通过 `IRoutingDevice.getDeviceType()` 识别设备类型
- **连接检测：** 使用 `EnumSet<ForgeDirection>` 跟踪六面连接
- **包结构：** `com.ae2channelrouter.block`（方块）、`com.ae2channelrouter.tile`（TileEntity）
- **注册模式：** 在 `ModBlocks` 中注册方块和 Tile
- **preInit 初始化：** 遵循 GTNH 约定

### Integration Points
- **方块注册：** 需要在 `ModBlocks.registerBlocks()` 添加路由终端方块
- **Tile 注册：** 需要在 `ModBlocks.registerTileEntities()` 注册 Tile
- **GUI 注册：** 需要在 `GuiHandler` 中注册终端 GUI
- **网络通信：** Forge SimpleNetworkWrapper（已在前期阶段设置）
- **AE2 网络接入：** 继承 `AEBaseRouterTile` 自动获得网格接入能力
- **控制器通信：** 通过路由频道 ID 匹配实现无线通信

</code_context>

<specifics>
## Specific Ideas

- 终端 GUI 应类似 AE2 网络工具的简洁风格，显示关键统计数字
- 路由终端与控制器之间的通信是无线的，通过路由频道 ID 匹配
- 终端作为透明分发节点，对 AE 设备而言频道分配是透明的
- 软限制阈值建议：单个设备使用超过 16 频道时显示警告
- 混合连接模式优先检测直接相邻，然后检测线缆连接

</specifics>

<deferred>
## Deferred Ideas

**阶段 5+ 功能（超出本阶段范围）：**
- 详细的设备类型识别和分类（v2）
- 每个设备的频道使用历史统计（v2）
- 强制性的设备频道硬限制（v2）
- 终端级别的频道优先级系统（v2）
- 多控制器故障转移（明确不在范围内）

</deferred>

---

*Phase: 04-routing-terminal*
*Context gathered: 2026-03-17*
