# Phase 1: Core Infrastructure - Context

**Gathered:** 2026-03-16
**Status:** Ready for planning

<domain>
## Phase Boundary

项目编译通过并建立 AE2 集成基础。包括 Gradle 项目配置、主 mod 类、AENetworkProxy 封装、基础 Tile Entity。

</domain>

<decisions>
## Implementation Decisions

### 构建配置
- Gradle 版本：8.x
- Java 版本：Java 21（GTNH 支持直接使用 Java 21 编译）
- AE2 依赖：本地源码依赖，指向 `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH`
- 使用 GTNH Convention Plugin (`com.gtnewhorizons.gtnhconvention`)

### Mod 入口
- 主类结构：单类入口模式
- FML 初始化：全部在 preInit 中完成（遵循 GTNH 惯例）
- 包结构：按类型分组（block, tile, item, network, gui）

### AE2 集成
- AENetworkProxy 封装：单例模式，便于全局访问
- GridAccessException 处理：返回 null 或默认值，简化调用
- 基础 Tile Entity：继承 AENetworkInvTile，实现网格回调

### Claude's Discretion
- 具体 Forge 版本号选择
- 依赖配置细节（implementation vs compileOnly）
- 混淆配置

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### AE2 源码参考
- `D:/CODE/Sources/Applied-Energistics-2-Unofficial-rv3-beta-695-GTNH/` — AE2 rv3-beta-695-GTNH 源码，包含 API 和实现
- 参考其 build.gradle 了解 GTNH 构建配置

### GTNH 开发文档
- GTNH 使用 `com.gtnewhorizons.gtnhconvention` Gradle 插件
- Java 21 可直接编译，插件处理字节码转换

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- AE2 源码中的 API 类：AENetworkProxy, GridAccessException, AENetworkInvTile
- GTNH 模组构建模板参考：NotEnoughEnergistics 项目结构

### Integration Points
- 主类通过 @Mod 注解注册
- FML 生命周期：preInit → init → postInit
- Tile Entity 通过 Forge 注册

</code_context>

<specifics>
## Specific Ideas

- 使用 GTNH 标准的项目结构，与 NotEnoughEnergistics 保持一致
- AE2 依赖通过本地源码集成，便于调试和 API 访问

</specifics>

<deferred>
## Deferred Ideas

无 — 讨论保持在阶段范围内

</deferred>

---

*Phase: 01-core-infrastructure*
*Context gathered: 2026-03-16*