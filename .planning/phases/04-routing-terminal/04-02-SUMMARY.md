# Plan 04-02 Summary: Terminal-Controller Communication

**Status:** ✅ COMPLETE  
**Phase:** 04-routing-terminal  
**Completed:** 2026-03-17

## What Was Built

Implemented terminal-controller wireless communication and channel distribution logic. The terminal can now request/receive channels from the controller and manages distribution to connected AE devices.

## Key Components

### 1. RoutingTerminalTile Enhancements
- **Location:** `src/main/java/com/ae2channelrouter/tile/RoutingTerminalTile.java`
- **Previous Lines:** 193
- **Current Lines:** 391
- **Added:** 198 lines

**New Features:**
- **Channel Request System:**
  - `requestChannels(int)` - Sends REQUEST packet to controller
  - `requestDefaultChannels()` - Calculates based on device count (8 per device)
  - Uses `DEFAULT_CHANNEL_REQUEST = 8` constant
  
- **Channel Release System:**
  - `releaseChannels()` - Sends RELEASE packet, clears allocation
  - Override `invalidate()` to release on destroy
  - `onGridConnectionStateChanged()` handles connect/disconnect
  
- **Device Connection Tracking:**
  - `updateEntity()` - Runs every tick, scans every 20 ticks (1 second)
  - `updateDeviceConnections()` - Scans 6 sides for AE devices
  - `isAEDevice()` - Filters out routing devices, detects AE2 tiles
  - Re-requests channels when device count changes by >= 2
  
- **Soft Limit Warning System:**
  - `SOFT_LIMIT_THRESHOLD = 16` channels per device
  - `checkSoftLimit()` - Calculates average channels per device
  - `isSoftLimitWarning()` - Returns warning state
  - Logs warning when threshold exceeded
  - Persisted in NBT

### 2. PacketRoutingChannel Enhancements
- **Location:** `src/main/java/com/ae2channelrouter/network/PacketRoutingChannel.java`
- **Previous Lines:** 214
- **Current Lines:** 231
- **Added:** 17 lines

**New Features:**
- **Client-Side Response Handling:**
  - Enhanced `handleClient()` to find terminal by UUID
  - Uses `Minecraft.getMinecraft().theWorld` for client world access
  - Calls `terminal.onChannelAllocated()` with allocated count
  - Searches loaded tile entities (v1 approach)
  
- **Dynamic Channel Requests:**
  - `handleChannelRequest()` now uses `message.getChannelCount()`
  - Previously hardcoded to 8, now respects terminal's request
  - Passes requested count to `controller.allocateChannels()`

## Implementation Details

### Channel Request Flow
```
1. Terminal placed / Grid connected → requestDefaultChannels()
2. Calculate: max(8, connectedDeviceCount * 8)
3. Send PacketRoutingChannel(REQUEST, requestedCount)
4. Server finds controller by routingChannelId
5. Controller allocates channels (up to requested, limited by availability)
6. Send PacketRoutingChannel(RESPONSE, allocatedCount)
7. Client receives RESPONSE → terminal.onChannelAllocated(allocated)
8. Terminal updates allocatedChannels, isOnline = true
```

### Channel Release Flow
```
1. Terminal destroyed → invalidate() → releaseChannels()
2. OR Grid disconnected → onGridConnectionStateChanged(false)
3. Send PacketRoutingChannel(RELEASE, 0)
4. Server finds controller, calls releaseChannels(terminalId)
5. Controller removes terminal from allocation map
6. Terminal clears allocatedChannels = 0, isOnline = false
```

### Device Detection Algorithm
```java
for each of 6 directions:
    get adjacent tile entity
    if isAEDevice(tile):
        connectedDeviceCount++

isAEDevice(te):
    - null check
    - NOT an IRoutingDevice (exclude cables/controllers/terminals)
    - Class name contains "appeng" (AE2 tile)
    - NOT a BlockCable (exclude AE2 cables)
```

### Soft Limit Calculation
```java
if connectedDeviceCount > 0:
    avgChannelsPerDevice = allocatedChannels / connectedDeviceCount
    softLimitWarning = avgChannelsPerDevice > SOFT_LIMIT_THRESHOLD (16)
else:
    softLimitWarning = false
```

## Deviations from Plan

**None.** All tasks completed as specified.

Minor implementation notes:
- Device detection uses simple class name checking ("appeng" substring) for v1
- Client-side terminal search iterates all loaded tiles (inefficient but functional for v1)
- Request throttling uses world time modulo checks rather than explicit tick counters

## Verification

- ✅ Terminal requests channels on initialization
- ✅ Terminal receives and stores allocated channel count
- ✅ Terminal releases channels when destroyed/offline
- ✅ Device counting tracks connected AE devices
- ✅ Soft limit warning triggers at >16 channels per device average
- ✅ No hard limit on connected devices
- ✅ All methods have server-side only guards
- ✅ NBT serialization updated for softLimitWarning

## Key Links

| From | To | Via |
|------|-----|-----|
| RoutingTerminalTile | PacketRoutingChannel | requestChannels() sends REQUEST |
| RoutingTerminalTile | PacketRoutingChannel | releaseChannels() sends RELEASE |
| PacketRoutingChannel | RoutingControllerTile | handleChannelRequest() finds controller |
| PacketRoutingChannel | RoutingTerminalTile | handleClient() → onChannelAllocated() |
| RoutingControllerTile | PacketRoutingChannel | allocateChannels() returns allocated count |

## Enables for Next Wave

This communication layer enables Wave 3 (Terminal GUI) by providing:
- Real-time `allocatedChannels` count for GUI display
- `isOnline` status for connection indicator
- `connectedDeviceCount` for device monitoring
- `softLimitWarning` for warning display
- `isAEDevice()` logic for connected device display

## Commits

1. `61b305d` - feat(04-02): Add terminal-controller communication to RoutingTerminalTile
2. `1990dd0` - feat(04-02): Update PacketRoutingChannel for terminal communication
