package com.ae2channelrouter.tile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.ae2channelrouter.api.IRoutingDevice;

import appeng.api.networking.IGrid;
import appeng.api.networking.GridAccessException;
import appeng.api.util.DimensionalCoord;
import appeng.tile.networking.TileController;

/**
 * Tile entity for the Routing Controller block.
 *
 * This is the central hub for channel allocation in the AE2 Channel Router mod.
 * It extends AEBaseRouterTile for AE2 network integration and implements
 * IRoutingDevice with DeviceType.CONTROLLER.
 *
 * Features:
 * - Auto-detects AE2 controllers in the connected network
 * - Manages channel pool allocation (controllers × 192 channels)
 * - Tracks terminal allocations
 * - Provides real-time channel statistics
 */
public class RoutingControllerTile extends AEBaseRouterTile implements IRoutingDevice {

    // Constants for channel calculation
    private static final int CHANNELS_PER_FACE = 32;
    private static final int FACES_PER_CONTROLLER = 6;
    private static final int CHANNELS_PER_CONTROLLER = CHANNELS_PER_FACE * FACES_PER_CONTROLLER; // 192

    // Data structures for channel management
    private Set<TileController> detectedControllers = new HashSet<>();
    private Map<UUID, Integer> terminalAllocations = new HashMap<>();
    private int routingChannelId = 0;
    private int totalChannels = 0;
    private int allocatedChannels = 0;

    /**
     * Default constructor.
     */
    public RoutingControllerTile() {
        super();
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.CONTROLLER;
    }

    @Override
    protected void onGridConnectionStateChanged(boolean connected) {
        if (connected) {
            detectControllers();
        } else {
            detectedControllers.clear();
            updateChannelStats();
        }
    }

    @Override
    protected ItemStack getItemStackFromTile() {
        // Return null for now - can be enhanced to return the block's item stack
        return null;
    }

    @Override
    public void gridChanged() {
        super.gridChanged();
        detectControllers();
    }

    /**
     * Scan the network for all AE2 controllers.
     * Called when grid changes or periodically.
     */
    private void detectControllers() {
        if (!hasGridAccess()) {
            detectedControllers.clear();
            updateChannelStats();
            return;
        }

        try {
            IGrid grid = getProxy().getGrid();
            detectedControllers.clear();

            // Get all controller machines in the grid
            Iterator<TileController> controllers = grid.getMachines(TileController.class).iterator();
            while (controllers.hasNext()) {
                TileController controller = controllers.next();
                detectedControllers.add(controller);
            }

            updateChannelStats();
            markDirty();

        } catch (GridAccessException e) {
            detectedControllers.clear();
            updateChannelStats();
        }
    }

    /**
     * Update channel statistics based on detected controllers.
     */
    private void updateChannelStats() {
        totalChannels = detectedControllers.size() * CHANNELS_PER_CONTROLLER;
        allocatedChannels = calculateTotalAllocated();
    }

    /**
     * Calculate total channels allocated to terminals.
     */
    private int calculateTotalAllocated() {
        int total = 0;
        for (int allocated : terminalAllocations.values()) {
            total += allocated;
        }
        return total;
    }

    // ==================== Public API ====================

    /**
     * Request channel allocation from a terminal.
     *
     * @param terminalId Unique terminal identifier
     * @param requestedChannels Number of channels requested
     * @return Number of channels actually allocated (may be less than requested)
     */
    public int allocateChannels(UUID terminalId, int requestedChannels) {
        int available = getAvailableChannels();
        int toAllocate = Math.min(requestedChannels, available);

        if (toAllocate > 0) {
            terminalAllocations.put(terminalId, toAllocate);
            updateChannelStats();
            markDirty();
        }

        return toAllocate;
    }

    /**
     * Release channels allocated to a terminal.
     *
     * @param terminalId Terminal to release channels from
     */
    public void releaseChannels(UUID terminalId) {
        if (terminalAllocations.containsKey(terminalId)) {
            terminalAllocations.remove(terminalId);
            updateChannelStats();
            markDirty();
        }
    }

    /**
     * Get channels allocated to a specific terminal.
     *
     * @param terminalId Terminal identifier
     * @return Number of channels allocated (0 if none)
     */
    public int getAllocatedChannels(UUID terminalId) {
        return terminalAllocations.getOrDefault(terminalId, 0);
    }

    /**
     * Check if a terminal has allocation.
     *
     * @param terminalId Terminal identifier
     * @return true if terminal has channels allocated
     */
    public boolean hasAllocation(UUID terminalId) {
        return terminalAllocations.containsKey(terminalId);
    }

    // ==================== Getters for Statistics ====================

    public int getTotalChannels() {
        return totalChannels;
    }

    public int getAllocatedChannels() {
        return allocatedChannels;
    }

    public int getAvailableChannels() {
        return totalChannels - allocatedChannels;
    }

    public int getDetectedControllerCount() {
        return detectedControllers.size();
    }

    public int getConnectedTerminalCount() {
        return terminalAllocations.size();
    }

    public int getRoutingChannelId() {
        return routingChannelId;
    }

    public void setRoutingChannelId(int routingChannelId) {
        this.routingChannelId = routingChannelId;
        markDirty();
    }

    // ==================== NBT Serialization ====================

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("routingChannelId", routingChannelId);
        nbt.setInteger("totalChannels", totalChannels);
        nbt.setInteger("allocatedChannels", allocatedChannels);

        // Save terminal allocations
        NBTTagCompound allocationsTag = new NBTTagCompound();
        for (Map.Entry<UUID, Integer> entry : terminalAllocations.entrySet()) {
            allocationsTag.setInteger(entry.getKey().toString(), entry.getValue());
        }
        nbt.setTag("terminalAllocations", allocationsTag);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        routingChannelId = nbt.getInteger("routingChannelId");
        totalChannels = nbt.getInteger("totalChannels");
        allocatedChannels = nbt.getInteger("allocatedChannels");

        // Load terminal allocations
        terminalAllocations.clear();
        if (nbt.hasKey("terminalAllocations")) {
            NBTTagCompound allocationsTag = nbt.getCompoundTag("terminalAllocations");
            for (Object keyObj : allocationsTag.func_150296_c()) {
                String key = (String) keyObj;
                try {
                    UUID terminalId = UUID.fromString(key);
                    int allocated = allocationsTag.getInteger(key);
                    terminalAllocations.put(terminalId, allocated);
                } catch (IllegalArgumentException e) {
                    // Invalid UUID, skip
                }
            }
        }
    }

    // ==================== Abstract Method Implementations ====================

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }
}
