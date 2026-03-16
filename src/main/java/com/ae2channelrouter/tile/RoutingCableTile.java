package com.ae2channelrouter.tile;

import java.util.EnumSet;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

import com.ae2channelrouter.api.IRoutingDevice;

import appeng.api.util.AECableType;
import appeng.api.util.DimensionalCoord;
import appeng.tile.TileEvent;
import appeng.tile.events.TileEventType;
import appeng.tile.inventory.InvOperation;

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
     * Tick counter for periodic connection updates.
     */
    private int tickCount = 0;

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
     * Get the cable connection type for AE2 integration.
     * Routing cables use SMART cable type for visual connection.
     */
    @Override
    public AECableType getCableConnectionType(ForgeDirection dir) {
        return AECableType.SMART;
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

    /**
     * Get internal inventory - cables don't have inventory.
     * Required by AEBaseInvTile.
     */
    @Override
    public IInventory getInternalInventory() {
        return null;
    }

    /**
     * Handle inventory changes - cables don't have inventory.
     * Required by AEBaseInvTile.
     */
    @Override
    public void onChangeInventory(IInventory inv, int slot, InvOperation mc, ItemStack removed, ItemStack added) {
        // Cables don't have inventory - no action needed
    }

    /**
     * Get accessible slots by side - cables don't have inventory.
     * Required by AEBaseInvTile.
     */
    @Override
    public int[] getAccessibleSlotsBySide(ForgeDirection whichSide) {
        return new int[0];
    }

    /**
     * Get the dimensional coordinate of this tile.
     * Required by IGridProxyable.
     */
    @Override
    public DimensionalCoord getLocation() {
        return new DimensionalCoord(this);
    }

    /**
     * Update connections to neighboring IRoutingDevice implementations.
     * Checks all 6 sides and updates the connections EnumSet accordingly.
     */
    public void updateConnections() {
        connections.clear();
        if (worldObj == null) return;

        int x = xCoord;
        int y = yCoord;
        int z = zCoord;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity te = worldObj.getTileEntity(x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ);

            if (te instanceof IRoutingDevice) {
                IRoutingDevice device = (IRoutingDevice) te;
                if (device.canConnectFrom(dir.getOpposite())) {
                    connections.add(dir);
                }
            }
        }
        markDirty();
    }

    /**
     * Called each tick to handle periodic updates.
     * Performs connection checks every 16 ticks on the server side.
     * Uses @TileEvent annotation for AE2's tick system.
     */
    @TileEvent(TileEventType.TICK)
    public void onTick() {
        tickCount++;

        // Periodic connection check (every 16 ticks to reduce load)
        if (!worldObj.isRemote && tickCount % 16 == 0) {
            updateConnections();
        }
    }

    /**
     * Called when the tile entity is ready and the world is available.
     * Performs initial connection detection.
     */
    @Override
    public void onReady() {
        super.onReady();
        updateConnections();
    }
}
