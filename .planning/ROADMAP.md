# ROADMAP: AE2 Channel Router

**Project:** AE2 Channel Router
**Created:** 2026-03-16
**Granularity:** Standard

## Overview

This roadmap maps 20 v1 requirements into 5 phases. Each phase delivers a coherent, verifiable capability.

## Phases

- [x] **Phase 1: Core Infrastructure** - Project setup and AE2 integration foundation **[VERIFIED]** ✅
- [x] **Phase 2: Routing Cable** - Physical routing network layer **[VERIFIED]** ✅
- [x] **Phase 3: Routing Controller** - Channel pooling and allocation hub **[COMPLETE]** ✓
- [ ] **Phase 4: Routing Terminal** - Channel distribution to AE devices
- [ ] **Phase 5: AE2 Integration** - Network event handling and virtual channels

## Phase Details

### Phase 1: Core Infrastructure

**Goal:** Project compiles and establishes base AE2 integration foundation

**Depends on:** Nothing (first phase)

**Requirements:** CORE-01, CORE-02, CORE-03, CORE-04

**Success Criteria** (what must be TRUE):
1. Gradle build completes without errors, producing valid Forge mod JAR
2. Mod loads in Minecraft with FML initialization logging visible
3. AENetworkProxy wrapper successfully connects to AE2 grid
4. Base tile entity extends AENetworkInvTile and responds to grid events

**Plans:** 3 plans

- **01-01**: ~~Gradle Build Setup~~ — Create GTNH-compliant Gradle configuration with AE2 local source dependency **[COMPLETE]**
- **01-02**: ~~Mod Foundation~~ — Main mod class with @Mod annotation and NetworkProxy wrapper utility **[COMPLETE]**
- **01-03**: ~~Base Tile Entity~~ — AEBaseRouterTile extending AENetworkInvTile with lifecycle management **[COMPLETE]**

**Wave Structure:**
- Wave 1: 01-01 (Gradle setup - no dependencies)
- Wave 2: 01-02 (Mod foundation - depends on build config)
- Wave 3: 01-03 (Base tile - depends on mod foundation)

---

### Phase 2: Routing Cable

**Goal:** Physical routing network layer connects terminals to controller

**Depends on:** Phase 1

**Requirements:** CABL-01, CABL-02, CABL-03, CABL-04

**Success Criteria** (what must be TRUE):
1. Routing cable block places in world and renders distinctly from AE cables
2. Routing cable tile entity can send/receive network messages
3. Cable visually appears different from standard AE2 cables in-game
4. Cable connects only to routing devices (terminals, controller), not standard AE devices

**Plans:** 3 plans

- **02-01**: ~~Routing Cable Foundation~~ — IRoutingDevice interface, RoutingCableBlock, RoutingCableTile **[COMPLETE]**
- **02-02**: ~~Cable Rendering~~ — Custom orange glass style rendering **[COMPLETE]**
- **02-03**: ~~Connection Logic~~ — 6-sided IRoutingDevice detection **[COMPLETE]**

**Wave Structure:**
- Wave 1: 02-01 (Cable foundation - depends on Phase 1)
- Wave 2: 02-02 (Cable rendering - depends on foundation)
- Wave 3: 02-03 (Connection logic - depends on foundation)

---

### Phase 3: Routing Controller

**Goal:** Central hub automatically collects and allocates channels from AE2 network

**Depends on:** Phase 2

**Requirements:** CTRL-01, CTRL-02, CTRL-03, CTRL-04, CTRL-05

**Success Criteria** (what must be TRUE):
1. Routing controller block places and forms in world
2. Controller auto-detects all AE2 controllers in the connected network
3. Channel pool displays total available channels (sum of all controller faces)
4. GUI displays used/available channel count and basic network info
5. Controller can allocate channels to routing terminals via wireless communication

**Plans:** 3 plans

- **03-01**: ~~Routing Controller Foundation~~ — Block, tile entity, basic structure **[COMPLETE]**
- **03-02**: ~~Controller Detection~~ — AE2 controller auto-detection, channel pool calculation **[COMPLETE]**
- **03-03**: ~~GUI and Communication~~ — Network GUI, wireless terminal protocol **[COMPLETE]**

**Wave Structure:**
- Wave 1: 03-01 (Foundation - depends on Phase 2)
- Wave 2: 03-02 (Core logic - depends on 03-01)
- Wave 3: 03-03 (GUI/communication - depends on 03-02)

---

### Phase 4: Routing Terminal

**Goal:** Distributes channels from controller to connected AE devices

**Depends on:** Phase 3

**Requirements:** TERM-01, TERM-02, TERM-03, TERM-04

**Success Criteria** (what must be TRUE):
1. Routing terminal block places and connects to routing cables
2. Terminal receives channel allocation from controller
3. AE devices connected to terminal via routing cables receive channels
4. Terminal supports unlimited connected AE devices (no per-terminal channel limit)

**Plans:** 3 plans

- **04-01**: ~~Routing Terminal Foundation~~ — Block, tile, basic structure and NBT serialization **[COMPLETE]**
- **04-02**: Terminal-Controller Communication — Wireless protocol, channel allocation **[PENDING]**
- **04-03**: Terminal GUI — Status display, device connection handling **[PENDING]**

**Wave Structure:**
- Wave 1: 04-01 (Foundation - depends on Phase 3)
- Wave 2: 04-02 (Communication - depends on 04-01)
- Wave 3: 04-03 (GUI - depends on 04-02)

---

### Phase 5: AE2 Integration

**Goal:** Seamless AE2 network integration with proper event handling

**Depends on:** Phase 4

**Requirements:** AEIN-01, AEIN-02, AEIN-03, AEIN-04

**Success Criteria** (what must be TRUE):
1. Mod correctly detects when AE2 controllers are added/removed from network
2. Connected AE devices appear to have valid virtual channel assignments
3. Network events (controller connect/disconnect) trigger proper recalculation
4. GridFlags configured correctly for channel usage tracking

**Plans:** TBD

---

## Progress Table

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. Core Infrastructure | 3/3 | **Complete** | 2026-03-16 |
| 2. Routing Cable | 3/3 | **Complete** | 2026-03-17 |
| 3. Routing Controller | 3/3 | **Complete** | 2026-03-17 |
| 4. Routing Terminal | 1/3 | In Progress | 2026-03-17 |
| 5. AE2 Integration | 0/1 | Not started | - |

---

## Coverage Map

| Phase | Requirement Count | Requirements |
|-------|-------------------|--------------|
| 1 | 4 | CORE-01, CORE-02, CORE-03, CORE-04 |
| 2 | 4 | CABL-01, CABL-02, CABL-03, CABL-04 |
| 3 | 5 | CTRL-01, CTRL-02, CTRL-03, CTRL-04, CTRL-05 |
| 4 | 4 | TERM-01, TERM-02, TERM-03, TERM-04 |
| 5 | 4 | AEIN-01, AEIN-02, AEIN-03, AEIN-04 |

**Total:** 20/20 requirements mapped
