# Phase 3 Research: Routing Controller

**Phase:** 03-routing-controller  
**Researched:** 2026-03-17  
**Confidence:** HIGH

## Executive Summary

Phase 3 implements the central routing controller that auto-detects AE2 controllers, manages a pooled channel allocation system, and provides a GUI for monitoring. The controller communicates wirelessly with routing terminals via a "routing channel" abstraction.

## Technical Findings

### AE2 Controller Detection

From AE2 source analysis (`TileController.java`, `PathGridCache.java`):

1. **Controller Structure**: Multi-block structure requiring 7×7×7 space
2. **Channel Capacity**: Each controller face provides 32 channels (verified via `CHANNEL_COUNT[2]` in GridNode)
3. **Detection Methods**:
   - Via `IGrid.getMachines(TileController.class)` - returns all controllers in network
   - Each controller node has `GridFlags.MULTIBLOCK` flag
   - 6 faces per controller = 192 channels per controller

### Channel Calculation

```
Total Channels = Controller Count × 6 faces × 32 channels per face
               = Controller Count × 192
```

### Implementation Approach

**Controller Detection Strategy (Event-Driven):**
- Use `gridChanged()` callback when network topology changes
- Scan for controllers using `grid.getMachines(TileController.class)`
- Cache detected controllers and recalculate channel pool
- Avoid periodic scanning - AE2 handles controller state changes

**Channel Pool Management:**
- Maintain `Set<TileController>` of detected controllers
- Calculate total: `controllers.size() * 192`
- Track allocated: Map<terminal_id, channel_count>
- Available = Total - Allocated.sum()

**GUI Implementation (Forge 1.7.10):**
- Container/GuiContainer pattern
- Sync data via Container.detectAndSendChanges()
- Update GUI on server tick
- Draw statistics using Minecraft font renderer
- Progress bar for channel usage visualization

**Wireless Communication:**
- "Routing Channel" = integer ID (configurable)
- Terminals "tune" to controller by matching ID
- Server-side tracking: Map<routing_channel, controller_reference>
- Protocol messages:
  - CHANNEL_REQUEST (terminal -> controller)
  - CHANNEL_ALLOCATE (controller -> terminal)
  - CHANNEL_RELEASE (terminal -> controller)

## Risk Assessment

| Risk | Probability | Impact | Mitigation |
|------|-------------|--------|------------|
| Controller detection race conditions | Low | Medium | Use AE2's event system, not polling |
| GUI desync | Medium | Low | Standard container sync patterns |
| Channel count overflow | Low | Low | Use long for total channels if needed |
| Network edge cases | Medium | Medium | Test with multiple controller configurations |

## Recommended Implementation Order

1. **Wave 1**: Block and Tile foundation
   - RoutingControllerBlock (full block, not cable-like)
   - RoutingControllerTile extends AEBaseRouterTile
   - Basic registration

2. **Wave 2**: Core controller logic
   - Controller detection via grid.getMachines()
   - Channel pool calculation (controllers × 192)
   - Terminal tracking data structures

3. **Wave 3**: GUI and communication
   - Container and GUI implementation
   - Network packet handling for terminal communication
   - Channel allocation/deallocation logic

## Code Patterns

### Controller Detection
```java
// In gridChanged() callback
IGrid grid = getProxy().getGrid();
Iterator<TileController> controllers = grid.getMachines(TileController.class).iterator();
while (controllers.hasNext()) {
    TileController controller = controllers.next();
    // Track this controller
}
```

### Channel Calculation
```java
int controllerCount = detectedControllers.size();
int totalChannels = controllerCount * 192; // 6 faces × 32 channels
int availableChannels = totalChannels - allocatedChannels;
```

### GUI Container Sync
```java
@Override
detectAndSendChanges() {
    super.detectAndSendChanges();
    // Send channel stats to client
    for (Object crafter : crafters) {
        // Send packet with updated data
    }
}
```

## References

- `.planning/phases/03-routing-controller/03-CONTEXT.md` - Implementation decisions
- `ARCHITECTURE.md` - AE2 integration patterns
- AE2 Source: `TileController.java`, `PathGridCache.java`, `GridNode.java`
- Existing code: `AEBaseRouterTile.java`, `IRoutingDevice.java`

## Deferred for Future Phases

- **Phase 4**: Routing terminal implementation, actual channel distribution
- **Phase 5**: AE2 network event handling refinements, virtual channels
- **v2**: Priority-based allocation, analytics/history

---

*Research complete. Ready for planning.*
