package com.ae2channelrouter.tile;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

import com.ae2channelrouter.api.IRoutingDevice;

/**
 * Tile entity for routing cables.
 * 
 * This tile extends AEBaseRouterTile and implements IRoutingDevice to provide
 * the core functionality for routing cable network connections. The cable acts
 * as a simple forwarding device that connects routing terminals to the controller.
 */
public class RoutingCableTile extends AEBaseRouterTile implements IRoutingDevice {

    /**
     * Tracks which sides of the cable have active connections.
     * Uses ForgeDirection for 6-sided connection tracking.
     */
    private EnumSet<ForgeDirection> connections;
    
    /**
     * Constructor initializes the tile with empty connections.
     */
    public RoutingCableTile() {
        super();
        this.connections = EnumSet.noneOf(ForgeDirection.class);
    }
    
    /**
     * Get the device type - cables are CABLE type.
     */
    @Override
    public DeviceType getDeviceType() {
        return DeviceType.CABLE;
    }
    
    /**
     * Check if a specific side has an active connection.
     * 
     * @param side The side to check
     * @return true if connected on that side
     */
    public boolean isConnected(ForgeDirection side) {
        return connections.contains(side);
    }
    
    /**
     * Get all currently connected sides.
     * 
     * @return EnumSet of connected directions
     */
    public EnumSet<ForgeDirection> getConnections() {
        return EnumSet.copyOf(connections);
    }
    
    /**
     * Add a connection to a specific side.
     * 
     * @param side The side to connect
     */
    public void addConnection(ForgeDirection side) {
        connections.add(side);
        markDirty();
    }
    
    /**
     * Remove a connection from a specific side.
     * 
     * @param side The side to disconnect
     */
    public void removeConnection(ForgeDirection side) {
        connections.remove(side);
        markDirty();
    }
    
    /**
     * Clear all connections.
     */
    public void clearConnections() {
        connections.clear();
        markDirty();
    }
    
    /**
     * Called when grid connection state changes.
     * Cables update their visual state based on connectivity.
     */
    @Override
    protected void onGridConnectionStateChanged(boolean connected) {
        // Cable doesn't need special handling for grid state changes
        // Visual updates will be handled by block rendering
        markDirty();
    }
    
    /**
     * Get the ItemStack representation for this tile.
     * Used for network display and WAILA integration.
     */
    @Override
    protected ItemStack getItemStackFromTile() {
        // Return null for now - will be populated when block item is created
        return null;
    }
}
