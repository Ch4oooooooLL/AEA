# Phase 5 Research: AE2 Integration

**Phase:** 05-ae2-integration  
**Researched:** 2026-03-17  
**Confidence:** MEDIUM (based on AE2 API documentation and existing implementation)

## Executive Summary

Phase 5 integrates the routing system with AE2's native channel system to provide virtual channel assignments to connected AE devices. This involves GridFlags configuration, controller event handling, device detection improvements, and the actual channel injection mechanism.

## Requirements Mapping

| Requirement | Description | Implementation Focus |
|-------------|-------------|---------------------|
| AEIN-01 | Detect AE2 controllers and their channels | Controller detection via grid.getMachines() |
| AEIN-02 | Virtual channel assignment for AE devices | Channel injection mechanism, GridFlags |
| AEIN-03 | Network event handling (controller add/remove) | gridChanged() callback, event propagation |
| AEIN-04 | Proper GridFlags configuration | Different flags per device type |

## Technical Findings

### 1. GridFlags Configuration Strategy

Based on context decisions and existing code analysis:

**Current State (AEBaseRouterTile):**
```java
this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
```
All routing devices currently use REQUIRE_CHANNEL.

**Required Changes:**

| Device Type | GridFlags | Rationale |
|-------------|-----------|-----------|
| RoutingController | REQUIRE_CHANNEL | Needs AE2 network participation |
| RoutingTerminal | None (neutral) | Pure channel forwarder, no direct AE2 channel usage |
| RoutingCable | None (neutral) | Physical connection only, no channel consumption |

**Implementation Approach:**
- Override `getProxy()` configuration in each tile class
- OR use `setFlags()` in constructors with appropriate flags
- Terminal and Cable should NOT set REQUIRE_CHANNEL

### 2. Controller Event Handling

**Current Implementation (RoutingControllerTile):**
```java
@Override
public void gridChanged() {
    super.gridChanged();
    detectControllers();
}
```

**Event Handling Strategy:**
- **New Controller Added**: Contribute channels to available pool only
  - New allocations can use the extra capacity
  - Existing allocations remain unchanged
  - Non-breaking change to running system
  
- **Controller Removed**: Immediately reclaim channels
  - Force terminal channel reduction if over capacity
  - Prioritize based on terminal order or usage
  - Must guarantee total allocated ≤ total available

**Propagation Pattern:**
```
Controller detects change → Updates pool → Terminals query on next tick → Adjust allocations
```

### 3. Device Detection Improvements

**Current Implementation (RoutingTerminalTile):**
```java
private boolean isAEDevice(TileEntity te) {
    if (te instanceof IRoutingDevice) return false;
    String className = te.getClass().getName();
    return className.contains("appeng") && !className.contains("BlockCable");
}
```

**Problems:**
- String matching is fragile
- Doesn't properly detect AE2 interfaces
- May miss devices or include non-devices

**Improved Approach:**
```java
private boolean isAEDevice(TileEntity te) {
    if (te == null || te instanceof IRoutingDevice) return false;
    
    // Check for IGridHost interface
    if (te instanceof appeng.api.networking.IGridHost) {
        try {
            IGridNode node = ((IGridHost) te).getGridNode(ForgeDirection.UNKNOWN);
            if (node != null) {
                // Check if device requires channel
                return node.hasFlag(GridFlags.REQUIRE_CHANNEL);
            }
        } catch (Exception e) {
            // Not a valid grid node
        }
    }
    return false;
}
```

**Benefits:**
- Uses AE2 API directly
- Detects devices that actually need channels
- More robust against AE2 updates

### 4. Virtual Channel Injection Mechanism

**Challenge:** AE2's channel system is built around physical cables and controllers. The routing system needs to make AE devices "think" they have channels without actually being connected to AE2 controllers via cables.

**Proposed Architecture:**

```
AE Device → Routing Terminal → [Virtual Channel Assignment] → Device operates normally
```

**Approach 1: Custom GridCache (Preferred)**
- Register a custom GridCache with AE2's grid system
- Intercept channel requests from devices connected to routing terminals
- Return "channel available" for devices in routing system
- Requires AE2's IGridCache interface

**Approach 2: GridNode Manipulation**
- Create proxy GridNodes for connected devices
- Set their channel state directly
- More invasive, risk of breaking AE2 invariants

**Approach 3: PathGridCache Integration (Limited)**
- Extend or modify PathGridCache behavior
- Very invasive, likely incompatible with AE2 updates

**Recommended: Approach 1 with Fallback**
- Implement IGridCache registration if API supports it
- Document limitation: Some AE2 versions may not support custom caches
- Provide graceful degradation

### 5. Grid Event Subscription

**Available Events via AENetworkProxy:**

| Event | Trigger | Handler |
|-------|---------|---------|
| gridChanged() | Network topology change | RoutingControllerTile.detectControllers() |
| onReady() | Tile ready, grid connected | Initialize controller detection |
| invalidate() | Tile destroyed | Release all allocations |

**Additional Events to Consider:**
- Channel usage changes (may need custom monitoring)
- Power state changes (already handled by isPowered())

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| AE2 API changes | Medium | High | Use stable API surface, avoid internals |
| Channel injection failure | Medium | High | Graceful degradation, clear error messages |
| Performance with many devices | Low | Medium | Efficient device detection, caching |
| GridFlags conflicts | Low | Medium | Test with various AE2 device types |
| Network synchronization issues | Medium | Medium | Server-authoritative, client prediction |

## Integration Points

### Existing Code Modifications Required

**AEBaseRouterTile.java:**
- Refactor: Move GridFlags configuration to subclasses
- Keep base proxy management

**RoutingControllerTile.java:**
- No major changes - detection already implemented
- Ensure proper GridFlags (already has REQUIRE_CHANNEL)
- Verify gridChanged() triggers recalculation

**RoutingTerminalTile.java:**
- Remove REQUIRE_CHANNEL from flags
- Improve isAEDevice() with interface detection
- Implement virtual channel provision mechanism
- Update canProvideChannels() to actually verify AE2 channel availability

**IRoutingDevice.java:**
- No changes required

**PacketRoutingChannel.java:**
- No changes required for this phase

## Recommended Implementation Order

1. **Wave 1: GridFlags Configuration**
   - Modify AEBaseRouterTile to allow subclass flag configuration
   - Set appropriate flags in RoutingControllerTile (REQUIRE_CHANNEL)
   - Remove REQUIRE_CHANNEL from RoutingTerminalTile and RoutingCableTile

2. **Wave 2: Device Detection Improvement**
   - Update RoutingTerminalTile.isAEDevice() to use IGridHost interface
   - Implement proper GridFlags.REQUIRE_CHANNEL checking
   - Test with various AE2 devices

3. **Wave 3: Controller Event Handling Enhancement**
   - Implement controller removal channel reclamation
   - Add terminal notification on allocation changes
   - Ensure consistency between pool and allocations

4. **Wave 4: Virtual Channel Injection (Research Phase)**
   - Investigate IGridCache API for custom cache registration
   - Prototype channel injection mechanism
   - Test with sample AE devices
   - Document limitations and fallback behavior

## Open Questions

1. **IGridCache Registration**: Does AE2 rv3-beta support custom GridCache registration?
2. **Channel State Query**: Can we query a device's current channel usage from AE2 API?
3. **Device Removal Events**: How to detect when an AE device is removed from terminal?
4. **Multi-Controller Coordination**: How do multiple routing controllers interact? (Out of scope per REQUIREMENTS.md)

## References

- `AEBaseRouterTile.java` - Base tile with AE2 network integration
- `RoutingControllerTile.java` - Controller detection implementation
- `RoutingTerminalTile.java` - Device detection and channel distribution
- `.planning/REQUIREMENTS.md` - AEIN-01 to AEIN-04 requirements
- `.planning/phases/05-ae2-integration/05-CONTEXT.md` - Implementation decisions

---

*Research completed: 2026-03-17*  
*Next step: Create implementation plans based on this research*
