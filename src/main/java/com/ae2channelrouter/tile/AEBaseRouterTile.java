package com.ae2channelrouter.tile;

import net.minecraft.item.ItemStack;

import appeng.api.networking.GridFlags;
import appeng.me.helpers.AENetworkProxy;
import appeng.tile.grid.AENetworkInvTile;

/**
 * Base router tile entity that extends AENetworkInvTile.
 *
 * This class provides the foundation for all routing devices in the AE2 Channel Router mod.
 * It automatically handles AE2 network integration including:
 * - Grid proxy lifecycle management
 * - Network connectivity status
 * - Channel requirements
 *
 * All routing blocks (cables, controller, terminal) should extend this class
 * or use its patterns for AE2 integration.
 */
public abstract class AEBaseRouterTile extends AENetworkInvTile {

    /**
     * Constructor configures the AENetworkProxy.
     * Per GTNH/AE2 conventions, proxy is created by parent AENetworkInvTile.
     * Grid flags are configured by subclasses via configureGridFlags().
     */
    public AEBaseRouterTile() {
        super();
        // Set idle power usage (AE/t) - base cost for being connected
        this.getProxy()
            .setIdlePowerUsage(1.0);
    }

    /**
     * Called when the tile becomes ready (world loaded, validated).
     * Initializes the grid proxy for network connectivity and configures
     * grid flags via subclass implementation.
     */
    @Override
    public void onReady() {
        super.onReady();
        // Configure grid flags - subclass determines channel requirements
        configureGridFlags();
        // Initialize the grid proxy - connects to AE2 network
        this.getProxy()
            .onReady();
    }

    /**
     * Configure GridFlags for this tile.
     * Called during onReady() to set appropriate flags.
     * Subclasses must implement to set their specific flags.
     */
    protected abstract void configureGridFlags();

    /**
     * Called when the tile is invalidated (chunk unloaded, block broken).
     * Cleans up the grid proxy to prevent network leaks.
     */
    @Override
    public void invalidate() {
        super.invalidate();
        // Clean up grid proxy - removes from AE2 network
        this.getProxy()
            .invalidate();
    }

    /**
     * Called when the chunk containing this tile is unloaded.
     * Notifies the grid proxy of chunk unload.
     */
    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        // Notify proxy of chunk unload
        this.getProxy()
            .onChunkUnload();
    }

    /**
     * Called when the grid state changes (controller added/removed, etc).
     * Subclasses should override to react to network changes.
     */
    @Override
    public void gridChanged() {
        super.gridChanged();
        // Grid state changed - network topology updated
        // Subclasses can override to react to changes
        onGridConnectionStateChanged(
            this.getProxy()
                .isActive());
    }

    /**
     * Check if this tile has a valid connection to the AE2 network.
     *
     * @return true if connected to a valid grid with power
     */
    public boolean hasGridAccess() {
        return this.getProxy()
            .isReady()
            && this.getProxy()
                .isActive();
    }

    /**
     * Get the current power state of the network.
     *
     * @return true if the network has power
     */
    public boolean isNetworkPowered() {
        return this.getProxy()
            .isPowered();
    }

    /**
     * Get the AENetworkProxy for direct access if needed.
     * Prefer using hasGridAccess() and wrapper methods instead.
     *
     * @return the AENetworkProxy instance
     */
    public AENetworkProxy getNetworkProxy() {
        return this.getProxy();
    }

    /**
     * Hook for subclasses to react to grid connection state changes.
     * Called from gridChanged() when connection state changes.
     *
     * @param connected true if now connected, false if disconnected
     */
    protected abstract void onGridConnectionStateChanged(boolean connected);

    /**
     * Get the ItemStack representation of this tile for network display.
     *
     * @return ItemStack representing this tile
     */
    protected abstract ItemStack getItemStackFromTile();
}
