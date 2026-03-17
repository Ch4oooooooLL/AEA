package com.ae2channelrouter.gui.container;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;

import com.ae2channelrouter.tile.RoutingTerminalTile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Container for the Routing Terminal GUI.
 *
 * Handles server-client synchronization of terminal statistics including
 * allocated channels, connected device count, online status, and soft limit warnings.
 */
public class ContainerRoutingTerminal extends Container {

    private final RoutingTerminalTile tile;

    // Client-side cached values (synced from server)
    private int allocatedChannels = 0;
    private int connectedDeviceCount = 0;
    private boolean isOnline = false;
    private boolean softLimitWarning = false;

    // Server-side cached values for change detection
    private int lastAllocatedChannels = -1;
    private int lastDeviceCount = -1;
    private boolean lastOnline = false;
    private boolean lastWarning = false;

    /**
     * Constructor.
     *
     * @param tile   The routing terminal tile entity
     * @param player The player viewing the GUI
     */
    public ContainerRoutingTerminal(RoutingTerminalTile tile, EntityPlayer player) {
        this.tile = tile;

        // No inventory slots for terminal - it's a status display GUI
        // If we wanted to add upgrade slots, they would go here
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        // Check if tile is still valid and player is close enough
        return tile != null 
            && tile.getWorldObj().getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == tile
            && player.getDistanceSq(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5) <= 64.0;
    }

    /**
     * Called each tick to detect changes and sync to client.
     */
    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (tile == null) {
            return;
        }

        // Get current values from tile
        int allocated = tile.getAllocatedChannels();
        int devices = tile.getConnectedDeviceCount();
        boolean online = tile.isOnline();
        boolean warning = tile.isSoftLimitWarning();

        // Check if any values changed
        if (allocated != lastAllocatedChannels || 
            devices != lastDeviceCount ||
            online != lastOnline ||
            warning != lastWarning) {
            
            // Sync to all crafters (listeners)
            @SuppressWarnings("unchecked")
            List<ICrafting> crafters = this.crafters;

            for (ICrafting crafter : crafters) {
                crafter.sendProgressBarUpdate(this, 0, allocated);
                crafter.sendProgressBarUpdate(this, 1, devices);
                crafter.sendProgressBarUpdate(this, 2, online ? 1 : 0);
                crafter.sendProgressBarUpdate(this, 3, warning ? 1 : 0);
            }

            // Update cached values
            lastAllocatedChannels = allocated;
            lastDeviceCount = devices;
            lastOnline = online;
            lastWarning = warning;
        }
    }

    /**
     * Called on client side when progress bar update is received.
     */
    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case 0:
                allocatedChannels = value;
                break;
            case 1:
                connectedDeviceCount = value;
                break;
            case 2:
                isOnline = value == 1;
                break;
            case 3:
                softLimitWarning = value == 1;
                break;
        }
    }

    // ==================== Getters for GUI ====================

    /**
     * Get the tile entity.
     */
    public RoutingTerminalTile getTile() {
        return tile;
    }

    /**
     * Get allocated channels (client-side synced value).
     */
    public int getAllocatedChannels() {
        return allocatedChannels;
    }

    /**
     * Get connected device count (client-side synced value).
     */
    public int getConnectedDeviceCount() {
        return connectedDeviceCount;
    }

    /**
     * Check if terminal is online (client-side synced value).
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Check if soft limit warning is active (client-side synced value).
     */
    public boolean isSoftLimitWarning() {
        return softLimitWarning;
    }
}
