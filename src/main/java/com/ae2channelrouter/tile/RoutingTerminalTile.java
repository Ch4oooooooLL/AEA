package com.ae2channelrouter.tile;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.api.IRoutingDevice;
import com.ae2channelrouter.network.PacketRoutingChannel;

import appeng.api.networking.GridFlags;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.util.DimensionalCoord;

/**
 * Tile entity for the Routing Terminal block.
 *
 * This is the distribution node that receives channels from the controller
 * and provides them to connected AE devices. It extends AEBaseRouterTile
 * for AE2 network integration and implements IRoutingDevice with DeviceType.TERMINAL.
 *
 * Features:
 * - Unique UUID for identification
 * - Channel management (allocated, requested)
 * - Device connection tracking
 * - Wireless communication with controller via routing channel ID
 * - Soft limit warnings for high channel usage
 */
public class RoutingTerminalTile extends AEBaseRouterTile implements IRoutingDevice {

    // Constants
    private static final int DEFAULT_CHANNEL_REQUEST = 8;
    private static final int SOFT_LIMIT_THRESHOLD = 16; // Warn if single device > this

    // Unique terminal identifier
    private UUID terminalId;

    // Channel management
    private int routingChannelId = 0;
    private int allocatedChannels = 0;
    private int requestedChannels = 0;
    private int connectedDeviceCount = 0;
    private boolean isOnline = false;
    private boolean softLimitWarning = false;

    // Connection tracking
    private EnumSet<ForgeDirection> cableConnections = EnumSet.noneOf(ForgeDirection.class);

    /**
     * Default constructor.
     * Generates a unique UUID for this terminal.
     */
    public RoutingTerminalTile() {
        super();
        this.terminalId = UUID.randomUUID();
    }

    @Override
    protected void configureGridFlags() {
        // Terminal is channel-neutral - doesn't consume AE2 channels directly
        // Channels come from controller's pool, not from AE2 network
        this.getProxy().setFlags(); // No flags = neutral
    }

    @Override
    public DeviceType getDeviceType() {
        return DeviceType.TERMINAL;
    }

    @Override
    protected void onGridConnectionStateChanged(boolean connected) {
        if (!connected) {
            // Lost grid connection, release channels
            releaseChannels();
        } else {
            // Gained connection, request channels
            requestDefaultChannels();
        }
    }

    @Override
    protected ItemStack getItemStackFromTile() {
        // Return null for now - can be enhanced to return the block's item stack
        return null;
    }

    @Override
    public void invalidate() {
        releaseChannels();
        super.invalidate();
    }

    /**
     * Check if this device can accept a connection from a specific direction.
     * Terminals allow connections from all sides for AE devices.
     *
     * @param from The facing direction of the incoming connection
     * @return true if connection is allowed
     */
    @Override
    public boolean canConnectFrom(ForgeDirection from) {
        return true;
    }

    /**
     * Update connections to routing cables.
     * Called periodically to refresh connection state.
     */
    public void updateConnections() {
        // Scan all 6 sides for routing cables
        // This will be expanded in future phases
        // For now, mark that we've checked
        markDirty();
    }

    // ==================== Channel Management ====================

    /**
     * Request channels from the controller.
     * Called on initialization or when device count changes significantly.
     */
    public void requestChannels(int requestedCount) {
        if (worldObj == null || worldObj.isRemote) return; // Server-side only
        
        this.requestedChannels = requestedCount;
        
        // Create and send packet to controller
        PacketRoutingChannel packet = new PacketRoutingChannel(
            terminalId,
            routingChannelId,
            PacketRoutingChannel.Action.REQUEST,
            requestedCount
        );
        
        // Send to server (controller will handle)
        AE2ChannelRouter.network.sendToServer(packet);
        
        AE2ChannelRouter.INSTANCE.getLogger().debug(
            "Terminal {} requesting {} channels on routing channel {}",
            terminalId, requestedCount, routingChannelId
        );
    }

    /**
     * Request default channel allocation.
     */
    public void requestDefaultChannels() {
        // Calculate based on connected devices: 8 channels per device minimum
        int requested = Math.max(DEFAULT_CHANNEL_REQUEST, connectedDeviceCount * 8);
        requestChannels(requested);
    }

    /**
     * Called when controller responds with allocated channels.
     * Handles both normal allocations and forced reclamations.
     */
    public void onChannelAllocated(int allocated) {
        int previousAllocation = this.allocatedChannels;
        this.allocatedChannels = allocated;
        this.isOnline = true;
        
        // Check if this was a reduction
        if (allocated < previousAllocation) {
            AE2ChannelRouter.INSTANCE.getLogger().warn(
                "Terminal {} channels reduced: {} -> {} (capacity shortage)",
                terminalId, previousAllocation, allocated
            );
            // Could trigger UI warning here
        }
        
        checkSoftLimit();
        markDirty();
        
        AE2ChannelRouter.INSTANCE.getLogger().debug(
            "Terminal {} now has {} channels (was: {})",
            terminalId, allocated, previousAllocation
        );
    }

    /**
     * Release all allocated channels back to controller.
     * Called when terminal is destroyed or loses grid connection.
     */
    public void releaseChannels() {
        if (worldObj == null || worldObj.isRemote) return; // Server-side only
        
        if (allocatedChannels > 0) {
            PacketRoutingChannel packet = new PacketRoutingChannel(
                terminalId,
                routingChannelId,
                PacketRoutingChannel.Action.RELEASE,
                0
            );
            
            AE2ChannelRouter.network.sendToServer(packet);
            
            AE2ChannelRouter.INSTANCE.getLogger().debug(
                "Terminal {} releasing {} channels",
                terminalId, allocatedChannels
            );
            
            allocatedChannels = 0;
            isOnline = false;
            markDirty();
        }
    }

    // ==================== Device Connection Tracking ====================

    @Override
    public void updateEntity() {
        super.updateEntity();
        
        if (worldObj == null || worldObj.isRemote) return;
        
        // Scan for devices every 20 ticks (1 second)
        if (worldObj.getTotalWorldTime() % 20 == 0) {
            updateDeviceConnections();
        }
        
        // Request channels on first update if not allocated
        if (allocatedChannels == 0 && worldObj.getTotalWorldTime() % 100 == 0) {
            requestDefaultChannels();
        }
    }

    /**
     * Scan all 6 sides for connected AE devices.
     * Updates connectedDeviceCount and tracks which devices were detected.
     */
    public void updateDeviceConnections() {
        if (worldObj == null || worldObj.isRemote) return;
        
        int previousCount = connectedDeviceCount;
        connectedDeviceCount = 0;
        
        // Track which directions have valid AE devices (for debugging/display)
        Map<ForgeDirection, String> detectedDevices = new HashMap<>();
        
        // Check all 6 directions
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int x = xCoord + dir.offsetX;
            int y = yCoord + dir.offsetY;
            int z = zCoord + dir.offsetZ;
            
            TileEntity te = worldObj.getTileEntity(x, y, z);
            if (isAEDevice(te)) {
                connectedDeviceCount++;
                detectedDevices.put(dir, te.getClass().getSimpleName());
                
                AE2ChannelRouter.INSTANCE.getLogger().debug(
                    "Terminal {} detected AE device at {}: {}",
                    terminalId, dir.name(), te.getClass().getSimpleName()
                );
            }
        }
        
        // Log summary if device count changed
        if (connectedDeviceCount != previousCount) {
            AE2ChannelRouter.INSTANCE.getLogger().info(
                "Terminal {} device count changed: {} -> {} (detected: {})",
                terminalId, previousCount, connectedDeviceCount, detectedDevices.values()
            );
            
            // Request channel adjustment if changed significantly
            if (Math.abs(connectedDeviceCount - previousCount) >= 2) {
                requestDefaultChannels();
            }
        }
        
        markDirty();
    }
    
    /**
     * Get information about connected devices for GUI display.
     * Returns map of direction -> device class name.
     * 
     * @return Map of connected devices
     */
    public Map<ForgeDirection, String> getConnectedDeviceInfo() {
        Map<ForgeDirection, String> devices = new HashMap<>();
        
        if (worldObj == null || worldObj.isRemote) {
            return devices;
        }
        
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int x = xCoord + dir.offsetX;
            int y = yCoord + dir.offsetY;
            int z = zCoord + dir.offsetZ;
            
            TileEntity te = worldObj.getTileEntity(x, y, z);
            if (isAEDevice(te)) {
                devices.put(dir, te.getClass().getSimpleName());
            }
        }
        
        return devices;
    }

    /**
     * Check if a tile entity is an AE device that requires channels.
     * Uses proper AE2 API instead of string matching.
     * 
     * @param te The tile entity to check
     * @return true if this is an AE device that needs channels
     */
    private boolean isAEDevice(TileEntity te) {
        // Null check and exclude routing devices
        if (te == null) {
            return false;
        }
        
        // Exclude our routing devices
        if (te instanceof IRoutingDevice) {
            return false;
        }
        
        // Check if it's an AE2 grid host (implements IGridHost)
        if (!(te instanceof IGridHost)) {
            return false;
        }
        
        try {
            // Get the grid node to check its flags
            IGridNode node = ((IGridHost) te).getGridNode(ForgeDirection.UNKNOWN);
            
            if (node == null) {
                // No grid node, can't be an active AE device
                return false;
            }
            
            // Check if this device requires a channel
            // This is the key check - only count devices that actually need channels
            return node.hasFlag(GridFlags.REQUIRE_CHANNEL);
            
        } catch (Exception e) {
            // If we can't get the node or check flags, it's not a valid AE device
            return false;
        }
    }

    // ==================== Soft Limit Warning ====================

    /**
     * Check if any single device is using more channels than the soft limit.
     * Returns true if warning should be shown.
     */
    public boolean isSoftLimitWarning() {
        return softLimitWarning;
    }

    /**
     * Calculate average channels per device and check against soft limit.
     * In v2, this could track per-device usage. For v1, we use average.
     */
    public void checkSoftLimit() {
        if (connectedDeviceCount == 0) {
            softLimitWarning = false;
            return;
        }
        
        // Average channels per device
        int avgChannelsPerDevice = allocatedChannels / connectedDeviceCount;
        softLimitWarning = avgChannelsPerDevice > SOFT_LIMIT_THRESHOLD;
        
        if (softLimitWarning) {
            AE2ChannelRouter.INSTANCE.getLogger().warn(
                "Terminal {} has high channel usage: {} channels for {} devices (avg: {})",
                terminalId, allocatedChannels, connectedDeviceCount, avgChannelsPerDevice
            );
        }
    }

    // ==================== Getters and Setters ====================

    public UUID getTerminalId() {
        return terminalId;
    }

    public int getRoutingChannelId() {
        return routingChannelId;
    }

    public void setRoutingChannelId(int routingChannelId) {
        this.routingChannelId = routingChannelId;
        markDirty();
    }

    public int getAllocatedChannels() {
        return allocatedChannels;
    }

    public void setAllocatedChannels(int allocatedChannels) {
        this.allocatedChannels = allocatedChannels;
        markDirty();
    }

    public int getRequestedChannels() {
        return requestedChannels;
    }

    public void setRequestedChannels(int requestedChannels) {
        this.requestedChannels = requestedChannels;
        markDirty();
    }

    public int getConnectedDeviceCount() {
        return connectedDeviceCount;
    }

    public void setConnectedDeviceCount(int connectedDeviceCount) {
        this.connectedDeviceCount = connectedDeviceCount;
        markDirty();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
        markDirty();
    }

    public EnumSet<ForgeDirection> getCableConnections() {
        return cableConnections;
    }

    public void setCableConnections(EnumSet<ForgeDirection> connections) {
        this.cableConnections = connections != null ? connections : EnumSet.noneOf(ForgeDirection.class);
        markDirty();
    }

    // ==================== Channel Distribution ====================

    /**
     * Check if this terminal can provide channels to AE devices.
     * Returns true if allocated channels > 0 and online.
     * 
     * Note: Full AE2 channel integration will be in Phase 5.
     * For Phase 4, the terminal provides the infrastructure and tracks
     * that it has channel capacity.
     *
     * @return true if terminal can provide channels
     */
    public boolean canProvideChannels() {
        return isOnline && allocatedChannels > 0;
    }

    /**
     * Get available channel count for distribution to connected devices.
     * 
     * Note: Full AE2 channel integration will be in Phase 5.
     * For now, returns allocated channels if terminal is online.
     *
     * @return number of channels available for distribution
     */
    public int getAvailableChannelsForDevices() {
        return canProvideChannels() ? allocatedChannels : 0;
    }

    // ==================== NBT Serialization ====================

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("terminalId", terminalId.toString());
        nbt.setInteger("routingChannelId", routingChannelId);
        nbt.setInteger("allocatedChannels", allocatedChannels);
        nbt.setInteger("requestedChannels", requestedChannels);
        nbt.setInteger("connectedDeviceCount", connectedDeviceCount);
        nbt.setBoolean("isOnline", isOnline);
        nbt.setBoolean("softLimitWarning", softLimitWarning);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("terminalId")) {
            try {
                this.terminalId = UUID.fromString(nbt.getString("terminalId"));
            } catch (IllegalArgumentException e) {
                // Invalid UUID, generate a new one
                this.terminalId = UUID.randomUUID();
            }
        }
        routingChannelId = nbt.getInteger("routingChannelId");
        allocatedChannels = nbt.getInteger("allocatedChannels");
        requestedChannels = nbt.getInteger("requestedChannels");
        connectedDeviceCount = nbt.getInteger("connectedDeviceCount");
        isOnline = nbt.getBoolean("isOnline");
        softLimitWarning = nbt.getBoolean("softLimitWarning");
    }

    // ==================== Abstract Method Implementations ====================

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }
}
