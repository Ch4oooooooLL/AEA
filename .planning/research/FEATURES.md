# Feature Landscape: AE2 Channel Optimization Mod

**Domain:** Minecraft mod optimization (Applied Energistics 2)
**Researched:** 2026-03-16
**Confidence:** MEDIUM-HIGH (based on source code analysis)

## Executive Summary

An AE2 channel optimization mod addresses the fundamental constraint in AE2 networks: channels are a limited resource that determines how many devices can connect to an ME network. The base AE2 algorithm uses a two-pass BFS/DFS approach that allocates channels greedily from controllers, which may not produce optimal channel distributions.

Based on analysis of the AE2 source code (`PathingCalculation.java`, `GridNode.java`, `PartCable.java`), the channel system has these key constraints:
- **Regular cables**: 8 channels max
- **Dense cables**: 32 channels max
- **Channel allocation**: Priority-based BFS from controller
- **Multiblocks**: Can share channels within a single multiblock

## Feature Categories

### Table Stakes (Must Have)

Features that users expect or the mod is useless. Missing these = users won't use the mod.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **Channel Usage Display** | Users cannot see how many channels each cable segment uses | Low | Display via GUI or in-world rendering (like smart cables but for all cable types) |
| **Network Channel Count** | Show total channels in use vs total available | Low | Display on controller or in network GUI |
| **Channel Calculation/Preview** | Users want to plan networks before building | Medium | Calculate channel usage for a proposed network layout before placing blocks |
| **Bottleneck Detection** | Identify which cable segments are limiting channel capacity | Medium | Highlight overburdened cable segments |

### Differentiators (Competitive Advantage)

Features that set the mod apart. Not expected, but valued by power users.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **Alternative Pathfinding Algorithm** | Better channel distribution than default greedy algorithm | High | Replace BFS with algorithm that optimizes for balanced channel usage |
| **Auto-Upgrade Suggestions** | Recommend when to upgrade cable types | Medium | Analyze network and suggest glass->covered->smart->dense upgrades |
| **Multi-Controller Optimization** | Optimize channel usage across multiple ME controllers | High | For large bases with multiple subnetworks |
| **Channel Rebalancing** | Dynamically reallocate channels without full rebuild | Very High | Modify running network channel assignment |
| **Channel Limit Modification** | Increase/decrease base channel limits | Low | Config option to change 8->X for regular, 32->Y for dense |
| **Priority Tuning** | Allow users to specify which devices get channels first | Medium | Override default PREFERRED flag behavior |
| **Dense Cable Placement Guide** | Help users place dense cables where they provide most value | Medium | Show optimal dense cable placement positions |

### Anti-Features

Things to deliberately NOT build. These either conflict with AE2's design or cause problems.

| Anti-Feature | Why Avoid | What to Do Instead |
|--------------|-----------|-------------------|
| **Unlimited Channels** | Breaks game balance, trivializes late-game infrastructure | Provide optimized allocation, not unlimited |
| **Per-device Channel Override** | Undermines the channel constraint system | Provide visibility and suggestions instead |
| **Real-time Auto-routing** | Too complex, performance risk | Pre-calculation mode only |
| **Remove Channel Requirement Entirely** | Defeats the purpose of the mod | Keep channels as core mechanic |

## Feature Dependencies

```
Channel Usage Display
    │
    ├── Network Channel Count
    │
    └── Bottleneck Detection
             │
             └── Auto-Upgrade Suggestions
                       │
                       └── Dense Cable Placement Guide

Alternative Pathfinding Algorithm ──► Channel Rebalancing
        │                                    │
        └── Multi-Controller Optimization ──┘

Channel Calculation/Preview (standalone, core feature)
```

## MVP Recommendation

Prioritize these in order:

1. **Channel Usage Display** - Table stakes, immediate value
2. **Bottleneck Detection** - Shows users where problems are
3. **Network Channel Count** - Basic visibility
4. **Channel Calculation/Preview** - Planning tool for users

Defer:
- Alternative pathfinding (complex, risky)
- Channel rebalancing (very complex, edge case)
- Multi-controller optimization (niche use case)

## Technical Implementation Notes

### AE2 Channel Allocation (From Source Analysis)

The core algorithm in `PathingCalculation.java`:

1. **BFS Pass**: Starts from controllers, uses priority queues (dense > cable > device)
2. **Channel Check**: For each device requiring a channel, checks bottleneck nodes
3. **DFS Pass**: Propagates channel counts upward to controllers
4. **Optimization**: Uses `highestSimilarAncestor` to avoid checking every node

### Key Hook Points for Optimization

| Hook Point | Purpose | Difficulty |
|------------|---------|------------|
| `PathingCalculation.compute()` | Replace/reorder channel allocation | High |
| `GridNode.getMaxChannels()` | Modify per-cable channel limits | Low |
| `GridFlags` manipulation | Change device channel requirements | Medium |
| Pre-connection validation | Block invalid network topologies | Low |

### GridFlags Relevant to Channels

- `REQUIRE_CHANNEL` - Device needs a channel
- `DENSE_CAPACITY` - Can carry 32 channels (vs 8)
- `PREFERRED` - Gets priority in queue (cables use this)
- `MULTIBLOCK` - Can share channel with other multiblock parts
- `CANNOT_CARRY` - Cannot carry any channels
- `CANNOT_CARRY_COMPRESSED` - Cannot carry compressed channels

## Sources

- **Applied Energistics 2 Source Code** (analyzed):
  - `PathingCalculation.java` - Channel allocation algorithm (BFS/DFS two-pass)
  - `GridNode.java` - Channel capacity (CHANNEL_COUNT = {0, 8, 32, Integer.MAX_VALUE})
  - `PartCable.java` - Cable implementation, channel visualization per side
- **AE2 Wiki** - Channel system documentation
- **GTNH Modpack** - Known for heavy AE2 usage, channel optimization needs

## Confidence Assessment

| Area | Confidence | Reason |
|------|------------|--------|
| Table Stakes | HIGH | Based on source code analysis and common UX needs |
| Differentiators | MEDIUM | Technical feasibility assessed from code structure |
| Anti-Features | HIGH | Clear from AE2 game design principles |
| Dependencies | MEDIUM | Derived from code structure, not tested |

## Gaps to Address

- Could not verify what existing mods in this space offer (search returned no results)
- Channel rebalancing algorithm details need prototype testing
- Performance impact of alternative pathfinding unknown without benchmarking