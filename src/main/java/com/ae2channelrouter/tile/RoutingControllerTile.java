package com.ae2channelrouter.tile;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.api.IRoutingDevice;
import com.ae2channelrouter.network.PacketRoutingChannel;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.util.DimensionalCoord;
import appeng.me.GridAccessException;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.InvOperation;
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

    // Status tracking
    private boolean wasOverCapacity = false;
    private int lastControllerCount = 0;

    /**
     * Default constructor.
     */
    public RoutingControllerTile() {
        super();
    }

    @Override
    protected void configureGridFlags() {
        // Controller requires channel to participate in AE2 network
        this.getProxy()
            .setFlags(GridFlags.REQUIRE_CHANNEL);
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
     * Handles controller addition and removal events.
     */
    private void detectControllers() {
        if (!hasGridAccess()) {
            if (!detectedControllers.isEmpty()) {
                // We lost grid access, clear all controllers
                handleAllControllersRemoved();
            }
            return;
        }

        try {
            IGrid grid = getProxy().getGrid();

            // Build new set of detected controllers
            Set<TileController> newControllers = new HashSet<>();
            for (appeng.api.networking.IGridNode node : grid.getMachines(TileController.class)) {
                if (node.getMachine() instanceof TileController) {
                    newControllers.add((TileController) node.getMachine());
                }
            }

            // Compare with previous set to detect changes
            Set<TileController> added = new HashSet<>(newControllers);
            added.removeAll(detectedControllers);

            Set<TileController> removed = new HashSet<>(detectedControllers);
            removed.removeAll(newControllers);

            // Update the detected set
            detectedControllers = newControllers;

            // Handle additions
            if (!added.isEmpty()) {
                handleControllersAdded(added);
            }

            // Handle removals
            if (!removed.isEmpty()) {
                handleControllersRemoved(removed);
            }

            updateChannelStats();
            markDirty();

        } catch (GridAccessException e) {
            handleAllControllersRemoved();
        }
    }

    /**
     * Handle new controllers being added to the network.
     * Adds their channels to the available pool.
     */
    private void handleControllersAdded(Set<TileController> added) {
        int addedChannels = added.size() * CHANNELS_PER_CONTROLLER;

        AE2ChannelRouter.INSTANCE.getLogger()
            .info(
                "Controller {} detected {} new AE2 controller(s), adding {} channels to pool",
                routingChannelId,
                added.size(),
                addedChannels);

        // Channels are automatically available - no need to notify terminals
        // They can use the extra capacity on next request
    }

    /**
     * Handle controllers being removed from the network.
     * Must reclaim channels if we're now over capacity.
     */
    private void handleControllersRemoved(Set<TileController> removed) {
        int removedChannels = removed.size() * CHANNELS_PER_CONTROLLER;

        AE2ChannelRouter.INSTANCE.getLogger()
            .warn(
                "Controller {} lost {} AE2 controller(s), removing {} channels from pool",
                routingChannelId,
                removed.size(),
                removedChannels);

        // Check if we're now over capacity
        if (allocatedChannels > totalChannels) {
            int overage = allocatedChannels - totalChannels;
            AE2ChannelRouter.INSTANCE.getLogger()
                .error("Controller {} is over capacity by {} channels! Reclaiming...", routingChannelId, overage);
            reclaimChannels(overage);
        }
    }

    /**
     * Handle complete loss of grid access.
     */
    private void handleAllControllersRemoved() {
        if (!detectedControllers.isEmpty()) {
            AE2ChannelRouter.INSTANCE.getLogger()
                .error(
                    "Controller {} lost all grid access! Reclaiming all {} channels",
                    routingChannelId,
                    allocatedChannels);

            detectedControllers.clear();
            updateChannelStats();

            // Force reclamation of all channels
            if (allocatedChannels > 0) {
                reclaimChannels(allocatedChannels);
            }

            markDirty();
        }
    }

    /**
     * Reclaim channels from terminals when over capacity.
     * Uses a simple strategy: reclaim proportionally from all terminals.
     * 
     * @param amount Number of channels to reclaim
     */
    private void reclaimChannels(int amount) {
        if (amount <= 0 || terminalAllocations.isEmpty()) {
            return;
        }

        AE2ChannelRouter.INSTANCE.getLogger()
            .warn("Reclaiming {} channels from {} terminals", amount, terminalAllocations.size());

        // Calculate reclamation per terminal (proportional)
        int totalAllocated = calculateTotalAllocated();
        Map<UUID, Integer> reclaimed = new HashMap<>();

        for (Map.Entry<UUID, Integer> entry : terminalAllocations.entrySet()) {
            UUID terminalId = entry.getKey();
            int allocated = entry.getValue();

            // Reclaim proportionally (round down, handle remainder later)
            int toReclaim = (allocated * amount) / totalAllocated;

            if (toReclaim > 0 && toReclaim < allocated) {
                int newAllocation = allocated - toReclaim;
                terminalAllocations.put(terminalId, newAllocation);
                reclaimed.put(terminalId, toReclaim);

                AE2ChannelRouter.INSTANCE.getLogger()
                    .info(
                        "Reclaimed {} channels from terminal {} (was: {}, now: {})",
                        toReclaim,
                        terminalId,
                        allocated,
                        newAllocation);
            }
        }

        // Handle any rounding remainder
        int actuallyReclaimed = 0;
        for (int reclaimedAmount : reclaimed.values()) {
            actuallyReclaimed += reclaimedAmount;
        }
        int remainder = amount - actuallyReclaimed;

        if (remainder > 0 && !terminalAllocations.isEmpty()) {
            // Take from the largest allocation
            UUID largestTerminal = null;
            int maxAllocation = 0;
            for (Map.Entry<UUID, Integer> entry : terminalAllocations.entrySet()) {
                if (entry.getValue() > maxAllocation) {
                    maxAllocation = entry.getValue();
                    largestTerminal = entry.getKey();
                }
            }

            if (largestTerminal != null) {
                int current = terminalAllocations.get(largestTerminal);
                int toReclaim = Math.min(remainder, current - 1); // Leave at least 1
                if (toReclaim > 0) {
                    terminalAllocations.put(largestTerminal, current - toReclaim);
                    Integer currentReclaimed = reclaimed.get(largestTerminal);
                    reclaimed.put(largestTerminal, currentReclaimed != null ? currentReclaimed + toReclaim : toReclaim);
                }
            }
        }

        // Notify all affected terminals
        for (UUID terminalId : reclaimed.keySet()) {
            notifyTerminalAllocationChanged(terminalId, terminalAllocations.get(terminalId));
        }

        updateChannelStats();
        markDirty();
    }

    /**
     * Notify a terminal that its channel allocation has changed.
     * Sends a packet to update the terminal's state.
     * 
     * @param terminalId    The terminal to notify
     * @param newAllocation The new channel allocation
     */
    private void notifyTerminalAllocationChanged(UUID terminalId, int newAllocation) {
        PacketRoutingChannel packet = new PacketRoutingChannel(
            terminalId,
            routingChannelId,
            PacketRoutingChannel.Action.RESPONSE,
            newAllocation);

        // Send to all players (simplest approach)
        AE2ChannelRouter.network.sendToAll(packet);

        AE2ChannelRouter.INSTANCE.getLogger()
            .debug("Notified terminal {} of new allocation: {} channels", terminalId, newAllocation);
    }

    /**
     * Update channel statistics based on detected controllers.
     */
    private void updateChannelStats() {
        int previousTotal = totalChannels;
        totalChannels = detectedControllers.size() * CHANNELS_PER_CONTROLLER;
        allocatedChannels = calculateTotalAllocated();

        // Track if we were over capacity
        wasOverCapacity = (allocatedChannels > previousTotal);
        lastControllerCount = detectedControllers.size();
    }

    /**
     * Check if controller is currently over capacity.
     * 
     * @return true if allocated > total
     */
    public boolean isOverCapacity() {
        return allocatedChannels > totalChannels;
    }

    /**
     * Get the current capacity overage.
     * 
     * @return Number of channels over capacity (0 if not over)
     */
    public int getOverage() {
        return Math.max(0, allocatedChannels - totalChannels);
    }

    /**
     * Check if controller just recovered from over-capacity state.
     * Useful for GUI notifications.
     * 
     * @return true if was over capacity but now is not
     */
    public boolean justRecoveredFromOverCapacity() {
        boolean wasOver = wasOverCapacity;
        wasOverCapacity = isOverCapacity();
        return wasOver && !wasOverCapacity;
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
     * @param terminalId        Unique terminal identifier
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

    @TileEvent(TileEventType.WORLD_NBT_WRITE)
    public void writeToNBT_RoutingControllerTile(NBTTagCompound nbt) {
        nbt.setInteger("routingChannelId", routingChannelId);
        nbt.setInteger("totalChannels", totalChannels);
        nbt.setInteger("allocatedChannels", allocatedChannels);

        // Save terminal allocations
        NBTTagCompound allocationsTag = new NBTTagCompound();
        for (Map.Entry<UUID, Integer> entry : terminalAllocations.entrySet()) {
            allocationsTag.setInteger(
                entry.getKey()
                    .toString(),
                entry.getValue());
        }
        nbt.setTag("terminalAllocations", allocationsTag);
    }

    @TileEvent(TileEventType.WORLD_NBT_READ)
    public void readFromNBT_RoutingControllerTile(NBTTagCompound nbt) {
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

    @Override
    public int[] getAccessibleSlotsBySide(net.minecraftforge.common.util.ForgeDirection side) {
        // Controller has no inventory slots accessible from sides
        return new int[0];
    }

    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        // Controller does not have an internal inventory that needs change tracking
    }
}
