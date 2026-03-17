package com.ae2channelrouter.tile;

import java.util.EnumSet;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import com.ae2channelrouter.api.IRoutingDevice;

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
 */
public class RoutingTerminalTile extends AEBaseRouterTile implements IRoutingDevice {

    // Unique terminal identifier
    private UUID terminalId;

    // Channel management
    private int routingChannelId = 0;
    private int allocatedChannels = 0;
    private int requestedChannels = 0;
    private int connectedDeviceCount = 0;
    private boolean isOnline = false;

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
        if (connected) {
            setOnline(true);
        } else {
            setOnline(false);
            allocatedChannels = 0;
        }
        markDirty();
    }

    @Override
    protected ItemStack getItemStackFromTile() {
        // Return null for now - can be enhanced to return the block's item stack
        return null;
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
    }

    // ==================== Abstract Method Implementations ====================

    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }
}
