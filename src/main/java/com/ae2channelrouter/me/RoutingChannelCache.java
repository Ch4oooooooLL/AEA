package com.ae2channelrouter.me;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;

/**
 * Custom AE2 GridCache that provides virtual channel assignments
 * for devices connected to routing terminals.
 * 
 * This cache intercepts channel queries from devices in the routing system
 * and returns channel availability based on the controller's pool.
 */
public class RoutingChannelCache implements IGridCache {
    
    private IGrid grid;
    
    // Track which devices are managed by which terminal
    // Map: Device GridNode -> Terminal UUID
    private Map<IGridNode, UUID> managedDevices = new HashMap<>();
    
    // Track terminal allocations
    // Map: Terminal UUID -> Available channel count for that terminal
    private Map<UUID, Integer> terminalChannelPools = new HashMap<>();
    
    @Override
    public void onJoin(IGrid grid) {
        this.grid = grid;
    }
    
    @Override
    public void onSplit(IGrid grid) {
        // Handle grid split - managed devices might need reassignment
        this.grid = null;
    }
    
    @Override
    public void populateGridStorage(Object storage) {
        // Not needed for our use case
    }
    
    @Override
    public void onUpdateTick() {
        // Periodic validation of managed devices
        validateManagedDevices();
    }
    
    /**
     * Register a device as being managed by a routing terminal.
     * 
     * @param deviceNode The device's GridNode
     * @param terminalId The managing terminal's UUID
     * @param availableChannels Channels available from this terminal
     */
    public void registerDevice(IGridNode deviceNode, UUID terminalId, int availableChannels) {
        managedDevices.put(deviceNode, terminalId);
        terminalChannelPools.put(terminalId, availableChannels);
    }
    
    /**
     * Unregister a device from routing management.
     */
    public void unregisterDevice(IGridNode deviceNode) {
        UUID terminalId = managedDevices.remove(deviceNode);
        // Clean up terminal pool if no more devices
        if (terminalId != null && !managedDevices.containsValue(terminalId)) {
            terminalChannelPools.remove(terminalId);
        }
    }
    
    /**
     * Update available channels for a terminal.
     */
    public void updateTerminalChannels(UUID terminalId, int availableChannels) {
        terminalChannelPools.put(terminalId, availableChannels);
    }
    
    /**
     * Check if a device has virtual channel availability.
     * Called by AE2 when checking channel requirements.
     * 
     * @param deviceNode The device to check
     * @return true if device has available channels via routing system
     */
    public boolean hasVirtualChannel(IGridNode deviceNode) {
        UUID terminalId = managedDevices.get(deviceNode);
        if (terminalId == null) {
            return false; // Not a managed device
        }
        
        Integer available = terminalChannelPools.get(terminalId);
        return available != null && available > 0;
    }
    
    /**
     * Validate all managed devices are still valid.
     */
    private void validateManagedDevices() {
        Iterator<Map.Entry<IGridNode, UUID>> iterator = managedDevices.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<IGridNode, UUID> entry = iterator.next();
            IGridNode node = entry.getKey();
            // Check if node is still valid
            if (node.getGrid() == null) {
                iterator.remove(); // Remove invalid node
            }
        }
    }
}
