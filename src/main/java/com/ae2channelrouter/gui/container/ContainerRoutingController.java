package com.ae2channelrouter.gui.container;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;

import com.ae2channelrouter.tile.RoutingControllerTile;

/**
 * Container for the Routing Controller GUI.
 *
 * Handles server-client synchronization of controller statistics including
 * total channels, allocated channels, controller count, and terminal count.
 */
public class ContainerRoutingController extends Container {

    private final RoutingControllerTile tile;
    private final EntityPlayer player;

    // Cached values for change detection
    private int lastTotalChannels = -1;
    private int lastAllocatedChannels = -1;
    private int lastControllerCount = -1;
    private int lastTerminalCount = -1;

    /**
     * Constructor.
     *
     * @param tile   The routing controller tile entity
     * @param player The player viewing the GUI
     */
    public ContainerRoutingController(RoutingControllerTile tile, EntityPlayer player) {
        this.tile = tile;
        this.player = player;

        // No inventory slots for controller - it's a status display GUI
        // If we wanted to add upgrade slots, they would go here
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        // Check if tile is still valid and player is close enough
        return tile.getWorldObj()
            .getTileEntity(tile.xCoord, tile.yCoord, tile.zCoord) == tile
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
        int totalChannels = tile.getTotalChannels();
        int allocatedChannels = tile.getAllocatedChannels();
        int controllerCount = tile.getDetectedControllerCount();
        int terminalCount = tile.getConnectedTerminalCount();

        // Sync to all crafters (listeners) if changed
        @SuppressWarnings("unchecked")
        List<ICrafting> crafters = this.crafters;

        for (ICrafting crafter : crafters) {
            // Send updates only when values change
            if (lastTotalChannels != totalChannels) {
                crafter.sendProgressBarUpdate(this, 0, totalChannels);
            }
            if (lastAllocatedChannels != allocatedChannels) {
                crafter.sendProgressBarUpdate(this, 1, allocatedChannels);
            }
            if (lastControllerCount != controllerCount) {
                crafter.sendProgressBarUpdate(this, 2, controllerCount);
            }
            if (lastTerminalCount != terminalCount) {
                crafter.sendProgressBarUpdate(this, 3, terminalCount);
            }
        }

        // Update cached values
        lastTotalChannels = totalChannels;
        lastAllocatedChannels = allocatedChannels;
        lastControllerCount = controllerCount;
        lastTerminalCount = terminalCount;
    }

    /**
     * Called on client side when progress bar update is received.
     */
    @Override
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case 0:
                // Note: These are client-side values, would need separate sync mechanism
                // For now, we just cache them in the container
                break;
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
        }
    }

    /**
     * Get the tile entity.
     */
    public RoutingControllerTile getTile() {
        return tile;
    }

    /**
     * Get total channels (server-side only).
     */
    public int getTotalChannels() {
        return tile.getTotalChannels();
    }

    /**
     * Get allocated channels (server-side only).
     */
    public int getAllocatedChannels() {
        return tile.getAllocatedChannels();
    }

    /**
     * Get available channels (server-side only).
     */
    public int getAvailableChannels() {
        return tile.getAvailableChannels();
    }

    /**
     * Get controller count (server-side only).
     */
    public int getControllerCount() {
        return tile.getDetectedControllerCount();
    }

    /**
     * Get terminal count (server-side only).
     */
    public int getTerminalCount() {
        return tile.getConnectedTerminalCount();
    }
}
