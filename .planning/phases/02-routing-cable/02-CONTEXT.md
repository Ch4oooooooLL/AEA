# Phase 2: Routing Cable - Context

**Gathered:** 2026-03-16
**Status:** Ready for planning

<domain>
## Phase Boundary

物理路由网络层 — 创建独立于 AE2 网格的路由线缆系统，连接路由终端和路由控制器。线缆仅传递路由消息，不参与 AE2 频道分配。

</domain>

<decisions>
## Implementation Decisions

### 线缆架构
- 架构类型：简单方块，继承 AEBaseRouterTile
- Tile Entity：继承 AEBaseRouterTile，复用 AE2 网格集成逻辑
- 数据存储：无状态转发，线缆不存储路由信息
- 渲染方式：独立细线缆模型，类似 AE2 风格但自定义实现（不使用 AE2 PartCable 系统）

### 视觉设计
- 区分方式：独特颜色，与 AE2 灰色/黑色线缆形成对比
- 颜色：橙色
- 连接外观：标准连接，无特殊粒子效果
- 材质风格：玻璃风格，类似 AE2 玻璃线缆的亮面外观

### 网络通信
- 路由处理：纯转发模式，所有路由逻辑在控制器和终端处理
- 消息机制：Forge 消息系统（SimpleNetworkWrapper）
- 消息类型：Phase 2 仅定义基础消息（连接建立、连接断开）
- 转发方式：广播式转发，消息发送给所有相邻线缆

### 连接规则
- 连接检测：接口检测（IRoutingDevice 接口），只有实现此接口的方块才能连接
- 面连接：六面连接，线缆可以连接到任何相邻的 IRoutingDevice
- 状态更新：自动检测，相邻方块变化时自动重新计算连接

### Claude's Discretion
- 具体线缆模型尺寸和形状
- 消息协议的具体字段设计
- IRoutingDevice 接口的具体方法签名

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### AE2 源码参考
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/` — AE2 rv3-beta-695-GTNH 源码
- `appeng/parts/networking/PartCable.java` — 线缆部件实现参考
- `appeng/block/networking/BlockCableBus.java` — 线缆方块容器参考
- `appeng/parts/CableBusContainer.java` — 线缆容器逻辑参考

### 项目文档
- `.planning/research/ARCHITECTURE.md` — AE2 集成模式，AENetworkProxy 使用方式，TileEntity 继承模式
- `.planning/research/SUMMARY.md` — 技术栈和风险总结

### Phase 1 上下文
- `.planning/phases/01-core-infrastructure/01-CONTEXT.md` — 构建配置、包结构、基础 Tile 实现决策

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `AEBaseRouterTile.java` — 基础 Tile Entity，已实现 AE2 网格集成、GridFlags 配置、生命周期管理
- `NetworkProxy.java` — 安全的网格访问包装器，处理 GridAccessException
- `ModBlocks.java` — 方块注册框架，已有routing_cable 占位符
- `AE2ChannelRouter.java` — 主类，preInit 中注册方块和 Tile

### Established Patterns
- 包结构：`com.ae2channelrouter.block`（方块）、`com.ae2channelrouter.tile`（TileEntity）
- AE2 集成：继承 AENetworkInvTile，使用 AENetworkProxy
- GridAccessException 处理：返回 null 或默认值
- preInit 集中初始化：遵循 GTNH 约定

### Integration Points
- 方块注册：在 ModBlocks.registerBlocks() 添加
- Tile Entity 注册：在 ModBlocks.registerTileEntities() 添加
- 渲染注册：需要在客户端代理中注册 TESR 或 ISmartBlock
- 接口定义：IRoutingDevice 接口需要定义，用于连接检测

</code_context>

<specifics>
## Specific Ideas

- 路由线缆视觉上类似 AE2 玻璃线缆，但颜色为橙色，便于识别
- 线缆不存储路由状态，简化实现，所有智能逻辑在控制器和终端
- 使用接口检测连接，扩展性好，未来可以添加更多路由设备

</specifics>

<deferred>
## Deferred Ideas

无 — 讨论保持在阶段范围内

</deferred>

---

*Phase: 02-routing-cable*
*Context gathered: 2026-03-16*