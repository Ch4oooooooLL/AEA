# Requirements: AE2 Channel Router

**Defined:** 2025-03-16
**Core Value:** 让AE设备能够绕过传统线缆网络，直接从统一频道池获取频道，实现频道资源的灵活分配和高效利用。

## v1 Requirements

### Core Infrastructure

- [x] **CORE-01**: Project setup with Gradle, Forge 10.13.4.1614, Jabel for Java 8 bytecode
- [x] **CORE-02**: Main mod class with FML initialization and AE2 API integration
- [x] **CORE-03**: AENetworkProxy wrapper utility with GridAccessException handling
- [x] **CORE-04**: Base tile entity extending AENetworkInvTile for AE2 integration

### Routing Cable

- [x] **CABL-01**: Routing cable block - connects routing terminals to routing controller
- [x] **CABL-02**: Routing cable tile entity with network communication capability
- [x] **CABL-03**: Cable rendering (distinct from AE2 cables)
- [x] **CABL-04**: Cable connection logic (can connect to routing devices, not AE devices)

### Routing Controller

- [ ] **CTRL-01**: Routing controller block - central hub for channel allocation
- [ ] **CTRL-02**: Auto-detect all AE2 controllers in the network
- [ ] **CTRL-03**: Channel pool management - collect all available channels from detected controllers
- [ ] **CTRL-04**: Channel allocation to routing terminals via wireless communication
- [ ] **CTRL-05**: GUI for displaying channel usage (used/available)

### Routing Terminal

- [ ] **TERM-01**: Routing terminal block - receives channels from controller
- [ ] **TERM-02**: Connect to AE devices via routing cables
- [ ] **TERM-03**: Channel distribution to connected AE devices
- [ ] **TERM-04**: Support unlimited connected devices

### AE2 Integration

- [ ] **AEIN-01**: Detect AE2 network controllers and their available channels
- [ ] **AEIN-02**: Create virtual channel assignment for connected AE devices
- [ ] **AEIN-03**: AE2 network event handling (controller add/remove)
- [ ] **AEIN-04**: Proper GridFlags configuration for channel usage

## v2 Requirements

- **ADV-01**: Auto-upgrade suggestions (cable type optimization)
- **ADV-02**: Channel usage history/analytics
- **ADV-03**: Priority-based channel allocation

## Out of Scope

| Feature | Reason |
|---------|--------|
| Multiple routing controllers | Single controller sufficient, simplifies coordination |
| Per-device channel limits | Channel pool large enough (all controller faces) |
| Detailed connection visualization | Simple GUI sufficient for v1 |
| Alternative pathfinding | Complex, defer to future |

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| CORE-01 | Phase 1 | Complete |
| CORE-02 | Phase 1 | Complete |
| CORE-03 | Phase 1 | Complete |
| CORE-04 | Phase 1 | Complete |
| CABL-01 | Phase 2 | Complete |
| CABL-02 | Phase 2 | Complete |
| CABL-03 | Phase 2 | Complete |
| CABL-04 | Phase 2 | Complete |
| CTRL-01 | Phase 3 | Pending |
| CTRL-02 | Phase 3 | Pending |
| CTRL-03 | Phase 3 | Pending |
| CTRL-04 | Phase 3 | Pending |
| CTRL-05 | Phase 3 | Pending |
| TERM-01 | Phase 4 | Pending |
| TERM-02 | Phase 4 | Pending |
| TERM-03 | Phase 4 | Pending |
| TERM-04 | Phase 4 | Pending |
| AEIN-01 | Phase 5 | Pending |
| AEIN-02 | Phase 5 | Pending |
| AEIN-03 | Phase 5 | Pending |
| AEIN-04 | Phase 5 | Pending |

**Coverage:**
- v1 requirements: 20 total
- Mapped to phases: 20
- Unmapped: 0 ✓

---
*Requirements defined: 2025-03-16*
*Last updated: 2025-03-16 after initial definition*