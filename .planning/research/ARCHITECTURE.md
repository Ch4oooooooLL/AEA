# Architecture Patterns: AE2 Integration

**Domain:** Minecraft Forge Mod - Applied Energistics 2 Integration
**Researched:** 2026-03-16
**Confidence:** HIGH

## Executive Summary

AE2 integration follows a layered architecture with clear separation between the grid system, storage system, and part/tile entities. The core integration point is through `AENetworkProxy`, which bridges any TileEntity or Part to the ME Network. Channel allocation is controlled via `GridFlags` - specifically `REQUIRE_CHANNEL` for devices that consume channels.

Integration approaches:
1. **Parts** (`AEBasePart`) - For items placed on cable sides
2. **Network Tiles** (`AENetworkInvTile`) - For full block TileEntities that join the network
3. **Grid Caches** - For network-wide services and state

---

## Recommended Architecture

### Component Boundaries

```
+------------------------+
|   Your Mod's Code      |
+------------------------+
           |
           v
+------------------------+
|  AE2 API Interface     |  <- AEApi.instance() entry point
+------------------------+
           |
     +-----+-----+
     |           |
     v           v
+------+   +----------+
|Parts |   | Tile API |
+------+   +----------+
     |           |
     v           v
+------------------------+
|   AENetworkProxy      |  <- Core bridge class
+------------------------+
           |
           v
+------------------------+
|    IGrid / IGridNode  |  <- Network abstraction
+------------------------+
           |
           v
+------------------------+
|  Grid Caches           |  <- IStorageGrid, IEnergyGrid,
|  (Network Services)   |    ICraftingGrid, ITickManager
+------------------------+
```

### Key Components

| Component | Responsibility | API Surface |
|-----------|---------------|-------------|
| **AENetworkProxy** | Manages grid connection, node lifecycle, NBT | `getNode()`, `getGrid()`, `isActive()` |
| **IGridNode** | Represents a device on the network | `updateState()`, `destroy()`, `getGrid()` |
| **IGridBlock** | Defines device properties (power, flags, location) | `getIdlePowerUsage()`, `getFlags()` |
| **IGridHost** | Implemented by your TileEntity/Part | `getGridNode()`, `getCableConnectionType()` |
| **IGridCache** | Network-wide services | `onUpdateTick()`, `addNode()`, `removeNode()` |
| **IGridTickable** | Per-tick processing for network devices | `tickingRequest()`, `getTickingRequest()` |

---

## Integration Patterns

### Pattern 1: Network Tile (Full Block)

For blocks that function as AE2 devices (like interfaces, storage buses):

```java
public class MyTileEntity extends AENetworkInvTile implements IGridTickable {

    public MyTileEntity() {
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setIdlePowerUsage(1.0);
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.getProxy().getNode();
    }

    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, false, true);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        // Per-tick logic
        return TickRateModulation.SLEEP;
    }
}
```

**Component communication:**
- TileEntity -> AENetworkProxy -> IGridNode -> IGrid -> Caches
- Events flow back via @MENetworkEventSubscribe annotations

### Pattern 2: Network Part (Cable-Side Device)

For items that attach to the side of cables/blocks:

```java
public class MyPart extends AEBasePart implements IGridTickable {

    public MyPart(ItemStack is) {
        super(is);
        this.getProxy().setFlags(GridFlags.REQUIRE_CHANNEL);
        this.getProxy().setIdlePowerUsage(1.0);
    }

    @Override
    public IGridNode getGridNode(ForgeDirection dir) {
        return this.proxy.getNode();
    }

    // AEBasePart already implements IGridHost via AENetworkProxy
}
```

**Component communication:**
- Part -> AEBasePart (contains AENetworkProxy) -> IGridNode -> IGrid

### Pattern 3: Custom Grid Cache (Network Service)

For mods that need network-wide state tracking:

```java
public class MyGridCache implements IGridCache {

    public MyGridCache(IGrid grid) {
        this.grid = grid;
    }

    @Override
    public void onUpdateTick() {
        // Network-wide tick logic
    }

    @Override
    public void addNode(IGridNode node, IGridHost machine) {
        // Node joined the network
    }

    @Override
    public void removeNode(IGridNode node, IGridHost machine) {
        // Node left the network
    }

    // ... other IGridCache methods
}
```

**Registration:** Must be registered via `AEApi.instance().registries().caches().register()`

---

## Data Flow

### Initialization Flow

```
1. TileEntity.onReady() / Part.addToWorld()
2. -> AENetworkProxy.getNode() [lazy creation]
3. -> AEApi.instance().createGridNode(IGridBlock)
4. -> IGridNode.updateState()
5. -> Grid assigns channel, connects to neighbors
6. -> IGridBlock.setNetworkStatus() callback
```

### Storage Operations Flow

```
1. getProxy().getStorage().getItemInventory()
2. -> returns IMEMonitor<IAEItemStack>
3. -> .injectItems() or .extractItems() for storage ops
4. -> Powered operations via IEnergyGrid
```

### Event Flow

```
Network Event (channel change, power, etc.)
  -> IGrid.postEvent(MENetworkEvent)
  -> @MENetworkEventSubscribe annotated methods on all nodes
  -> gridChanged() callback on IGridBlock
```

---

## Channel System Details

### GridFlags

| Flag | Purpose | Channel Usage |
|------|---------|---------------|
| `REQUIRE_CHANNEL` | Devices that need dedicated channels | Consumes 1 channel |
| `COMPRESSED_CHANNEL` | P2P tunnels | Compressed P2P |
| `CANNOT_CARRY` | Blocks that break channel continuity | No channel |
| `DENSE_CAPACITY` | Tier 2 cables, quantum bridges | 32 channels |
| `PREFERRED` | Toggle paths through this node | Routing preference |
| `MULTIBLOCK` | Part of a multiblock structure | Multiblock-aware |

### Channel Allocation

- Each network has max 32 channels per cable segment
- Devices with `REQUIRE_CHANNEL` consume 1 channel
- Cables propagate channels based on their capacity flags
- Use `IGridNode.meetsChannelRequirements()` to check if device has channel

---

## Suggested Build Order

Based on component dependencies:

### Phase 1: Core Infrastructure
1. **AENetworkProxy wrapper** - Create a utility class wrapping grid access
   - Handles `GridAccessException`
   - Provides typed access to caches (storage, energy, crafting, tick)
   - Lifecycle management (onReady, invalidate, onChunkUnload)

2. **Base TileEntity/Part class** - Extend AE2 base classes
   - Extend `AENetworkInvTile` for full blocks
   - Extend `AEBasePart` for cable-side parts

### Phase 2: Grid Integration
3. **Grid connection** - Connect to network with flags
   - Set `REQUIRE_CHANNEL` for device functionality
   - Set idle power usage
   - Configure cable connection type

4. **Tickable behavior** - Implement `IGridTickable`
   - `getTickingRequest()` defines rate
   - `tickingRequest()` is the tick callback

### Phase 3: Storage Integration
5. **Storage access** - Read/write to ME Network
   - Get storage inventory via proxy
   - Use `Actionable.SIMULATE` for checks
   - Use `Actionable.MODULATE` for actual operations

### Phase 4: Advanced Features
6. **Custom GridCache** - Network-wide services
7. **Crafting integration** - ICraftingProvider
8. **Security integration** - MachineSource for actions

---

## Real-World Example: NotEnoughEnergistics

The NEE mod's `TilePatternInterface` demonstrates a complete integration:

- Extends `AENetworkInvTile`
- Implements `IGridTickable` for tick processing
- Implements `ICraftingProvider` for pattern management
- Uses `getProxy().getStorage()` for inventory operations
- Uses `getProxy().getCrafting()` for CPU management
- Handles network events via `@MENetworkEventSubscribe`
- Sets `GridFlags.REQUIRE_CHANNEL` for channel consumption

---

## Anti-Patterns to Avoid

### 1. Direct Grid Access Without Proxy
**Bad:**
```java
IGrid grid = someNode.getGrid(); // May be null
```
**Good:**
```java
try {
    IGrid grid = this.getProxy().getGrid();
} catch (GridAccessException e) {
    // Handle - device not connected
}
```

### 2. Forgetting Channel Requirements
**Bad:** Device works even when network has no free channels
**Good:** Set `GridFlags.REQUIRE_CHANNEL` to properly fail when no channels available

### 3. Calling Grid Methods at Wrong Time
**Bad:** Accessing grid in `addNode()` cache callback
**Good:** Defer grid operations to `onUpdateTick()` - cache callbacks have limited state

### 4. Not Handling Chunk Unload
**Bad:** Node remains referenced after chunk unload
**Good:** Call `node.destroy()` in `onChunkUnload()` / `invalidate()`

---

## Sources

- AE2 API: `appeng.api.networking.*` (IGridBlock, IGridNode, IGrid, IGridHost)
- AE2 Core: `appeng.me.helpers.AENetworkProxy`
- AE2 Parts: `appeng.parts.AEBasePart`
- AE2 Tiles: `appeng.tile.grid.AENetworkInvTile`
- Integration Example: NotEnoughEnergistics `TilePatternInterface`