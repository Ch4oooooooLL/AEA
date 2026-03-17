package com.ae2channelrouter.tile;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.api.IRoutingDevice;
import com.ae2channelrouter.network.PacketRoutingChannel;

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
     * This is called from PacketRoutingChannel.Handler on client side.
     */
    public void onChannelAllocated(int allocated) {
        this.allocatedChannels = allocated;
        this.isOnline = true;
        checkSoftLimit();
        markDirty();
        
        AE2ChannelRouter.INSTANCE.getLogger().debug(
            "Terminal {} allocated {} channels",
            terminalId, allocated
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
     * Counts both direct adjacent blocks and devices connected via routing cables.
     */
    public void updateDeviceConnections() {
        if (worldObj == null || worldObj.isRemote) return;
        
        int previousCount = connectedDeviceCount;
        connectedDeviceCount = 0;
        
        // Check all 6 directions
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int x = xCoord + dir.offsetX;
            int y = yCoord + dir.offsetY;
            int z = zCoord + dir.offsetZ;
            
            // Check for AE2 tile entities (not routing devices)
            TileEntity te = worldObj.getTileEntity(x, y, z);
            if (isAEDevice(te)) {
                connectedDeviceCount++;
            }
        }
        
        // If device count changed significantly, request new channels
        if (Math.abs(connectedDeviceCount - previousCount) >= 2) {
            requestDefaultChannels();
        }
        
        markDirty();
    }

    /**
     * Check if a tile entity is an AE device (but not a routing device).
     */
    private boolean isAEDevice(TileEntity te) {
        if (te == null) return false;
        
        // Check if it's an AE2 network tile (has grid proxy)
        // But exclude our routing devices
        if (te instanceof IRoutingDevice) {
            return false; // Don't count routing cables/controllers/terminals
        }
        
        // Check for AE2 tile entities by class name or interface
        // This is a simplified check - in production, use AE2 API
        String className = te.getClass().getName();
        return className.contains("appeng") && !className.contains("BlockCable");
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
