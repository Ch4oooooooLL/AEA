# Research Summary: AE2 Channel Optimization Mod

**Project:** AE2 Channel Optimization Mod for Minecraft 1.7.10 (GTNH 2.8.4)
**Synthesized:** 2026-03-16
**Confidence:** MEDIUM-HIGH

---

## Executive Summary

This is a Minecraft 1.7.10 Forge mod that optimizes Applied Energistics 2 channel allocation. AE2 networks are limited by channels (8 per regular cable, 32 per dense cable), and the default greedy BFS algorithm does not produce optimal distributions. The mod aims to provide visibility and optimization for channel usage.

The recommended approach is to build a GTNH-targeted mod using Forge 10.13.4.1614 with Gradle 8.13 and the GTNH Convention Plugin. Use Jabel to enable modern Java syntax while targeting Java 8 bytecode. Start with table-stakes features (channel display, bottleneck detection) before attempting complex algorithm changes.

The primary risks are tick budget exhaustion (50ms server tick limit) and GridCache lifecycle bugs. These should be addressed early with profiling infrastructure and careful lifecycle management.

---

## Key Findings

### From STACK.md (Technology Stack)

| Technology | Version | Rationale |
|------------|---------|-----------|
| Minecraft | 1.7.10 | Target version for GTNH modpack |
| Forge | 10.13.4.1614 | Latest stable for 1.7.10 |
| Gradle | 8.13 | Required by GTNH plugin |
| GTNH Convention Plugin | 1.0.42+ | Build conventions, code style, publishing |
| Jabel | Latest | Modern Java syntax, Java 8 bytecode |
| MCP | Version 12 | Deobfuscation for 1.7.10 |
| Java Target | 8 | 1.7.10 runtime requires Java 8 |

### From FEATURES.md (Feature Landscape)

**Table Stakes (MVP):**
1. Channel Usage Display - Users must see channel usage
2. Network Channel Count - Total channels used/available
3. Bottleneck Detection - Identify overburdened cables
4. Channel Calculation/Preview - Plan networks before building

**Differentiators (v2+):**
- Alternative Pathfinding Algorithm (high complexity)
- Auto-Upgrade Suggestions
- Multi-Controller Optimization
- Channel Rebalancing

**Anti-Features to Avoid:**
- Unlimited channels (breaks game balance)
- Per-device channel override
- Real-time auto-routing

### From ARCHITECTURE.md (Integration Patterns)

Core integration through three patterns:

1. **AENetworkProxy** - Bridge class for all grid connections
2. **Network Tile** - Full blocks extending `AENetworkInvTile`
3. **Network Parts** - Cable-side devices extending `AEBasePart`
4. **Grid Caches** - Network-wide services via `IGridCache`

Key hook points:
- `PathingCalculation.compute()` - Replace channel allocation
- `GridNode.getMaxChannels()` - Modify cable limits
- GridFlags manipulation - Change device requirements

### From PITFALLS.md (Domain Risks)

**Critical (Must Avoid):**
1. Tick Budget Exhaustion - 50ms limit per tick; profile early
2. GridCache Lifecycle - Always call super in addNode/removeNode
3. Event System Deadlocks - Use re-entrancy guards
4. Network Storage Split/Join - Test topology changes
5. Bytecode Incompatibility - Use Jabel, target Java 8

**Moderate:**
6. Thread Safety - Use ConcurrentHashMap
7. World/Chunk Loading Order - Defer to onReady()
8. API Version Compatibility - Test against GTNH 2.8.4

---

## Implications for Roadmap

### Recommended Phase Structure

**Phase 1: Build Infrastructure & Basic Integration**
- Setup Gradle project with GTNH Convention Plugin
- Configure Jabel for modern Java with Java 8 target
- Create base AENetworkProxy wrapper utility
- Build minimal TileEntity extending AENetworkInvTile
- Establish connection to AE2 network
- *Pitfall to avoid: Bytecode incompatibility*

**Phase 2: Core Visibility Features**
- Channel Usage Display (visualize per-segment usage)
- Network Channel Count (total used/available)
- Bottleneck Detection (identify constrained cables)
- *Feature dependencies: Display -> Count -> Bottleneck*
- *Pitfall to avoid: Tick budget exhaustion (profile channel scanning)*

**Phase 3: Network Analysis**
- Channel Calculation/Preview (pre-build planning)
- Auto-Upgrade Suggestions (cable type recommendations)
- Dense Cable Placement Guide
- *Pitfall to avoid: World/chunk loading order bugs*

**Phase 4: Grid Infrastructure (Post-MVP)**
- Custom IGridCache for network-wide state
- Implement lifecycle callbacks properly
- Test add/remove network cycles extensively
- *Pitfall to avoid: GridCache lifecycle bugs, storage split/join*

**Phase 5: Advanced Optimization**
- Alternative pathfinding algorithm
- Multi-controller optimization
- Channel rebalancing
- *Pitfall to avoid: Event system deadlocks, crafting rebuild starvation*
- *Flags: Requires deep research on PathingCalculation internals*

### Research Flags

| Phase | Research Needed | Standard Pattern |
|-------|-----------------|------------------|
| Phase 1 | None | Standard Forge/GTNH setup |
| Phase 2 | None | AE2 part rendering understood |
| Phase 3 | None | Channel algorithm understood from source |
| Phase 4 | **HIGH** - Custom GridCache in GTNH | Limited documentation |
| Phase 5 | **HIGH** - Alternative pathfinding | No existing implementations found |

---

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | HIGH | Based on active GTNH project (NotEnoughEnergistics) |
| Features | MEDIUM-HIGH | Source code analysis of AE2, no existing competition |
| Architecture | HIGH | Direct AE2 API patterns, clear component boundaries |
| Pitfalls | MEDIUM | Code analysis, limited web verification (version age) |

### Gaps to Address

1. **No existing competition** - Cannot benchmark against similar mods; feature set is original
2. **GTNH-specific API** - Must test against actual GTNH 2.8.4 environment; dev may differ
3. **Alternative pathfinding** - Algorithm requires prototype testing; feasibility uncertain
4. **Performance impact** - Cannot predict tick cost without benchmarking

---

## Sources

- **STACK.md**: NotEnoughEnergistics-1.7.14 project (GTNH mod), Gradle/Forge docs
- **FEATURES.md**: AE2 source code analysis (PathingCalculation.java, GridNode.java, PartCable.java)
- **ARCHITECTURE.md**: AE2 API (appeng.api.networking.*), NotEnoughEnergistics integration
- **PITFALLS.md**: AE2 source (Grid.java, TickHandler.java), GTNH community knowledge