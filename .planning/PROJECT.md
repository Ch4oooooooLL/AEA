# AE2 Channel Router

## What This Is

一个用于GTNH 2.8.4 (Minecraft 1.7.10) 环境下AE2模组的频道优化模组。通过独立的路由系统接管AE网络中的所有控制器频道，将频道分配给需要使用频道的AE设备，突破传统AE线缆每面32频道的限制，并减少跳数提升性能。

## Core Value

让AE设备能够绕过传统线缆网络，直接从统一频道池获取频道，实现频道资源的灵活分配和高效利用。

## Requirements

### Active

- [ ] 创建路由线缆 — 独立于AE线缆，仅传递本模组信息，连接路由终端和路由控制器
- [ ] 创建路由控制器 — 自动接管AE网络中所有控制器的所有面频道，作为频道分配中心
- [ ] 创建路由终端 — 从路由控制器获取频道，通过路由线缆连接需要频道的AE设备
- [ ] 实现路由控制器与路由终端之间的无线通信机制
- [ ] 实现路由终端与AE设备之间的频道分配
- [ ] 添加GUI显示基本信息（已用/可用频道等）

### Out of Scope

- 多路由控制器协调 — 一个网络只需要一个路由控制器
- 设备级别频道限制 — 暂不限制每个设备的频道数
- 具体连接详情查看 — 暂时不需要查看哪些设备连接到了哪个终端

## Context

- **目标游戏版本**: Minecraft 1.7.10
- **整合包**: GTNH (GregTech New Horizons) 2.8.4
- **依赖模组**: Applied Energistics 2 (AE2) rv3-beta-695-GTNH
- **源码位置**: D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH

### 技术背景

AE2原有频道机制：
- 每个控制器有6个面，每面最多32个频道（普通线缆）
- 频道分配通过BFS/DFS算法从控制器向外传播
- 设备需要通过AE线缆物理连接才能获取频道

本模组方案：
- 路由线缆独立于AE线缆，创建独立的路由网络
- 路由控制器自动接管所有控制器的所有面频道
- 路由终端与路由控制器通过"路由频道"无线通信
- AE设备通过路由线缆连接到路由终端获取频道

## Constraints

- **Minecraft版本**: 1.7.10
- **API依赖**: AE2 API (rv3-beta-695-GTNH版本)
- **编译环境**: Gradle (基于现有NotEnoughEnergistics项目结构)
- **兼容性**: 需要与GTNH 2.8.4整合包中的AE2完全兼容

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 路由线缆独立于AE线缆 | 绕过AE原有频道分配算法，减少开销 | — Pending |
| 路由终端与控制器无线通信 | 無視距离限制，简化布线 | — Pending |
| 路由终端连接设备数量无限制 | 频道池足够大，无需限制 | — Pending |
| 单一路由控制器 | 简化协调逻辑，频道池已足够 | — Pending |

---
*Last updated: 2025-03-16 after initialization*