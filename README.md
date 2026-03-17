# AE2 Channel Router

> 一个用于 GTNH 2.8.4 (Minecraft 1.7.10) 环境下 AE2 模组的频道优化模组。

## 项目简介

AE2 Channel Router 是一个 Minecraft 1.7.10 Forge 模组，通过独立的路由系统接管 AE 网络中的所有控制器频道，将频道分配给需要使用频道的 AE 设备。

### 核心价值

让 AE 设备能够绕过传统线缆网络，直接从统一频道池获取频道，实现频道资源的灵活分配和高效利用。

### 解决的问题

- **突破频道限制**：传统 AE 线缆每面最多 32 个频道，本模组通过路由系统突破此限制
- **优化性能**：减少频道跳数，降低网络复杂度
- **简化布线**：路由终端与控制器无线通信，无视距离限制

---

## 功能特性

### 已实现 (v1.0)

| 功能模块 | 描述 | 状态 |
|---------|------|------|
| **路由线缆** | 独立于 AE 线缆的物理网络，连接路由终端和控制器 | ✅ 完成 |
| **路由控制器** | 自动接管 AE 网络中所有控制器的所有面频道 | ✅ 完成 |
| **路由终端** | 从路由控制器获取频道，分配给连接的 AE 设备 | ✅ 完成 |
| **GUI 界面** | 显示已用/可用频道等基本信息 | ✅ 完成 |
| **无线通信** | 路由终端与控制器之间无视距离的频道分配 | ✅ 完成 |
| **AE2 集成** | 虚拟频道注入，设备自动检测与事件处理 | ✅ 完成 |

### 计划功能 (v2.0)

- 自动升级建议（线缆类型优化）
- 频道使用历史/分析
- 基于优先级的频道分配

---

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Minecraft | 1.7.10 | 目标游戏版本 |
| Forge | 10.13.4.1614 | Mod 加载器和 API |
| Gradle | 8.13 | 构建工具 |
| GTNH Convention Plugin | 1.0.42+ | GTNH 构建约定 |
| Jabel | Latest | 现代 Java 语法，Java 8 字节码 |
| MCP | Version 12 | 反混淆映射 |

### 依赖模组

- **Applied Energistics 2** rv3-beta-695-GTNH
- 目标整合包：**GTNH (GregTech New Horizons) 2.8.4**

---

## 项目结构

```
src/main/java/com/ae2channelrouter/
├── AE2ChannelRouter.java          # 主类
├── api/
│   └── IRoutingDevice.java        # 路由设备接口
├── block/
│   ├── ModBlocks.java             # 方块注册
│   ├── RoutingCableBlock.java     # 路由线缆
│   ├── RoutingControllerBlock.java # 路由控制器
│   └── RoutingTerminalBlock.java  # 路由终端
├── client/
│   ├── ClientInit.java            # 客户端初始化
│   └── render/
│       └── RoutingCableRender.java # 线缆渲染
├── gui/
│   ├── GuiHandler.java            # GUI 处理器
│   ├── client/
│   │   ├── GuiRoutingController.java # 控制器 GUI
│   │   └── GuiRoutingTerminal.java   # 终端 GUI
│   └── container/
│       ├── ContainerRoutingController.java
│       └── ContainerRoutingTerminal.java
├── me/
│   ├── GridCacheRegistration.java # 网格缓存注册
│   └── RoutingChannelCache.java   # 路由频道缓存
├── network/
│   ├── NetworkProxy.java          # 网络代理
│   └── PacketRoutingChannel.java  # 频道数据包
└── tile/
    ├── AEBaseRouterTile.java      # 基础网络方块实体
    ├── RoutingCableTile.java      # 线缆方块实体
    ├── RoutingControllerTile.java # 控制器方块实体
    └── RoutingTerminalTile.java   # 终端方块实体
```

**项目统计**

- Java 源文件：21 个
- 代码行数：约 3,549 行

---

## 快速开始

### 前置要求

- Java 8 (JDK 1.8)
- Gradle 8.13+

### 构建步骤

```bash
# 克隆项目
git clone <repository-url>
cd AE2ChannelRouter

# 设置工作空间
./gradlew setupDecompWorkspace

# 生成 IDE 项目（IntelliJ IDEA）
./gradlew idea

# 构建 JAR
./gradlew build
```

### 运行开发客户端

```bash
./gradlew runClient
```

---

## 工作原理

### AE2 原有频道机制

- 每个控制器有 6 个面，每面最多 32 个频道（普通线缆）
- 频道分配通过 BFS/DFS 算法从控制器向外传播
- 设备需要通过 AE 线缆物理连接才能获取频道

### 本模组方案

1. **路由线缆** - 独立于 AE 线缆，创建独立的路由网络
2. **路由控制器** - 自动接管所有控制器的所有面频道
3. **路由终端** - 与路由控制器通过"路由频道"无线通信
4. **AE 设备连接** - 通过路由线缆连接到路由终端获取频道

### 技术亮点

- **GridFlags 配置**：控制器使用 REQUIRE_CHANNEL，终端/线缆保持中性
- **虚拟频道注入**：通过自定义 IGridCache 实现虚拟频道分配
- **设备检测**：使用 AE2 API（IGridHost, IGridNode）进行设备验证
- **事件处理**：控制器添加/移除时自动重新计算频道池

---

## 开发路线图

| 阶段 | 目标 | 状态 | 完成时间 |
|------|------|------|----------|
| Phase 1 | Core Infrastructure - 项目设置与 AE2 集成基础 | ✅ VERIFIED | 2026-03-16 |
| Phase 2 | Routing Cable - 物理路由网络层 | ✅ VERIFIED | 2026-03-17 |
| Phase 3 | Routing Controller - 频道池和分配中心 | ✅ COMPLETE | 2026-03-17 |
| Phase 4 | Routing Terminal - 向 AE 设备分发频道 | ✅ COMPLETE | 2026-03-17 |
| Phase 5 | AE2 Integration - 网络事件处理和虚拟频道 | ✅ COMPLETE | 2026-03-17 |

**总体进度：** 20/20 需求已实现 (100%)

---

## 注意事项

### 当前限制

- 一个网络只需要一个路由控制器
- 暂不限制每个设备的频道数
- 暂时不需要查看哪些设备连接到了哪个终端

### 风险规避

- **Tick 预算耗尽** - 50ms 服务器 tick 限制，已进行性能分析
- **GridCache 生命周期** - 在 addNode/removeNode 中始终调用 super
- **字节码兼容性** - 使用 Jabel 目标 Java 8 字节码

---

## 贡献指南

1. Fork 本项目
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

---

## 许可证

本项目采用 [LICENSE] 许可证（待添加）

---

## 致谢

- [Applied Energistics 2](https://github.com/AppliedEnergistics/Applied-Energistics-2) - 核心 API 和网络系统
- [GTNH Team](https://github.com/GTNewHorizons) - GTNH 整合包和构建工具链
- [NotEnoughEnergistics](https://github.com/GTNewHorizons/NotEnoughEnergistics) - 参考项目结构

---

*最后更新: 2026-03-17 | v1.0 里程碑已完成*
