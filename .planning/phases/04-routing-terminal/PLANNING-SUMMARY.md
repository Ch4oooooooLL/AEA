# Phase 4: Routing Terminal - Planning Summary

**Phase:** 04-routing-terminal  
**Status:** Planning Complete  
**Created:** 2026-03-17  
**Plans:** 3

---

## Overview

Phase 4 implements the Routing Terminal - the distribution node that receives channels from the controller and provides them to connected AE devices. The terminal is the key component that enables AE devices to bypass traditional cable networks and receive channels directly from the unified channel pool.

## Success Criteria

Per ROADMAP.md, Phase 4 delivers:

1. ✅ Routing terminal block places and connects to routing cables
2. ✅ Terminal receives channel allocation from controller
3. ✅ AE devices connected to terminal via routing cables receive channels
4. ✅ Terminal supports unlimited connected AE devices (no per-terminal channel limit)

## Requirements Covered

| Requirement | Description | Plan |
|-------------|-------------|------|
| TERM-01 | Terminal block places and connects to routing cables | 04-01 |
| TERM-02 | Terminal receives channel allocation from controller | 04-02 |
| TERM-03 | AE devices receive channels from terminal | 04-03 |
| TERM-04 | Unlimited device support with soft limit warnings | 04-02, 04-03 |

## Plans

### Plan 04-01: Routing Terminal Foundation (Wave 1)

**Files Created/Modified:**
- `src/main/java/com/ae2channelrouter/tile/RoutingTerminalTile.java`
- `src/main/java/com/ae2channelrouter/block/RoutingTerminalBlock.java`
- `src/main/java/com/ae2channelrouter/block/ModBlocks.java`

**Key Deliverables:**
- RoutingTerminalTile extending AEBaseRouterTile
- DeviceType.TERMINAL implementation
- UUID generation for terminal identification
- NBT serialization for persistence
- Full block registration

**Dependencies:** Phase 3 (03-03) complete

---

### Plan 04-02: Terminal Communication & Channel Distribution (Wave 2)

**Files Created/Modified:**
- `src/main/java/com/ae2channelrouter/tile/RoutingTerminalTile.java` (enhanced)
- `src/main/java/com/ae2channelrouter/network/PacketRoutingChannel.java` (enhanced)

**Key Deliverables:**
- Wireless channel request/release protocol
- Terminal UUID-based identification
- Device connection tracking (count only)
- Soft limit warning system (16 channels/device threshold)
- Automatic channel re-request on device count changes
- Graceful offline handling

**Key Design Decisions:**
- Silent registration: Terminal sends REQUEST on initialization
- On-demand refresh: Only communicate when needed
- Push mode: Terminal actively manages channel distribution
- Soft limits: Warnings only, no hard caps

**Dependencies:** 04-01

---

### Plan 04-03: GUI and Device Connection (Wave 3)

**Files Created/Modified:**
- `src/main/java/com/ae2channelrouter/gui/container/ContainerRoutingTerminal.java`
- `src/main/java/com/ae2channelrouter/gui/client/GuiRoutingTerminal.java`
- `src/main/java/com/ae2channelrouter/gui/GuiHandler.java` (enhanced)
- `src/main/java/com/ae2channelrouter/block/RoutingTerminalBlock.java` (enhanced)

**Key Deliverables:**
- Terminal GUI with AE2 network tool styling
- Real-time status display (online/offline, channels, devices)
- Soft limit warning indicator
- Right-click GUI opening
- Server-client data synchronization
- Channel distribution infrastructure (Phase 5 will add full AE2 integration)

**GUI Layout:**
```
+------------------+
|  Routing Terminal|
+------------------+
| Status: Online   |  (green text)
+------------------+
| Allocated: 32    |
| Devices: 4       |
+------------------+
| ! High Usage     |  (warning, orange)
+------------------+
```

**Dependencies:** 04-02

---

## Wave Structure

```
Wave 1 (04-01): Foundation
├── Task 1: Create RoutingTerminalTile with UUID
├── Task 2: Create RoutingTerminalBlock
├── Task 3: Register in ModBlocks
└── Task 4: Add NBT serialization

Wave 2 (04-02): Communication
├── Task 1: Add channel request logic
├── Task 2: Add channel release logic
├── Task 3: Update packet handling
├── Task 4: Add device connection tracking
└── Task 5: Add soft limit warning system

Wave 3 (04-03): GUI & Connection
├── Task 1: Create ContainerRoutingTerminal
├── Task 2: Create GuiRoutingTerminal
├── Task 3: Update GuiHandler
├── Task 4: Update block for GUI opening
├── Task 5: Verify all getters exist
└── Task 6: Add channel distribution infrastructure
```

## Integration Points

### With Phase 3 (Controller)
- Uses existing PacketRoutingChannel for wireless communication
- Matches controller via routingChannelId
- Controller's allocateChannels() API used

### With Phase 2 (Routing Cable)
- Implements IRoutingDevice interface
- Terminal connects to routing cables
- Uses same connection tracking pattern (EnumSet<ForgeDirection>)

### With Phase 1 (Infrastructure)
- Extends AEBaseRouterTile
- Uses AENetworkProxy for AE2 integration
- Follows established block/tile registration patterns

### With Phase 5 (AE2 Integration)
- Terminal provides channel distribution infrastructure
- Full AE2 channel assignment in Phase 5
- Current phase sets up the framework

## Testing Strategy

### Unit Testing
1. Terminal UUID generation unique per instance
2. NBT serialization/deserialization round-trip
3. Channel request packet creation
4. Device counting logic

### Integration Testing
1. Terminal connects to routing cables
2. Terminal requests channels from controller
3. Controller allocates channels to terminal
4. GUI displays correct statistics
5. Warning triggers at >16 channels/device average

### Manual Testing
1. Place terminal in world
2. Connect to controller via same routing channel ID
3. Connect AE devices to terminal
4. Verify channels display in GUI
5. Test soft limit warning

## Deferred to Phase 5

- Full AE2 channel API integration
- Actual channel assignment to AE devices
- Per-device channel tracking (currently uses average)
- Advanced device type detection
- Channel usage statistics and history

## Risk Mitigation

| Risk | Mitigation |
|------|------------|
| UUID serialization issues | Use UUID.toString() and fromString() with try-catch |
| Packet handling complexity | Use existing PacketRoutingChannel pattern from Phase 3 |
| Device detection accuracy | Simple AE class name check (sufficient for v1) |
| GUI sync issues | Use standard Forge Container pattern with updateProgressBar |
| Memory leaks | Use invalidate() to release channels on destroy |

## Estimates

| Plan | Tasks | Est. Time | Complexity |
|------|-------|-----------|------------|
| 04-01 | 4 | 45 min | Low |
| 04-02 | 5 | 60 min | Medium |
| 04-03 | 6 | 60 min | Medium |
| **Total** | **15** | **~3 hours** | **Medium** |

## Next Steps

1. **Execute Wave 1:** Implement 04-01-PLAN.md
2. **Execute Wave 2:** Implement 04-02-PLAN.md  
3. **Execute Wave 3:** Implement 04-03-PLAN.md
4. **Verify Phase 4:** Run verification checklist
5. **Update STATE.md:** Mark Phase 4 complete
6. **Plan Phase 5:** Begin AE2 Integration planning

## Files Reference

### Plans
- `.planning/phases/04-routing-terminal/04-01-PLAN.md`
- `.planning/phases/04-routing-terminal/04-02-PLAN.md`
- `.planning/phases/04-routing-terminal/04-03-PLAN.md`

### Context
- `.planning/phases/04-routing-terminal/04-CONTEXT.md`

### Upstream Dependencies
- `.planning/phases/03-routing-controller/03-03-PLAN.md`
- `src/main/java/com/ae2channelrouter/tile/RoutingControllerTile.java`
- `src/main/java/com/ae2channelrouter/network/PacketRoutingChannel.java`

---

*Phase 4 planning complete. Ready for execution.*
