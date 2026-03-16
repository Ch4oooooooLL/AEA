package com.ae2channelrouter.network;

import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.pathing.IPathingGrid;
import appeng.me.GridAccessException;
import appeng.me.helpers.AENetworkProxy;

/**
 * Wrapper utility for AENetworkProxy that provides safe grid access.
 *
 * Per user decision: GridAccessException is caught and null/defaults are returned
 * to simplify calling code. Callers should check for null before using returned values.
 */
public class NetworkProxy {

    private final AENetworkProxy proxy;

    public NetworkProxy(AENetworkProxy proxy) {
        this.proxy = proxy;
    }

    /**
     * Check if the proxy is connected to a valid grid.
     *
     * @return true if proxy is active and ready
     */
    public boolean isConnected() {
        return proxy.isActive() && proxy.isReady();
    }

    /**
     * Safely get the IGrid. Returns null if grid is not accessible.
     *
     * Per user decision: GridAccessException returns null instead of propagating.
     *
     * @return IGrid instance or null if not accessible
     */
    public IGrid getGridSafe() {
        try {
            return proxy.getGrid();
        } catch (GridAccessException e) {
            return null; // Per user decision: return null on exception
        }
    }

    /**
     * Safely get the IPathingGrid. Returns null if grid is not accessible.
     *
     * @return IPathingGrid instance or null if not accessible
     */
    public IPathingGrid getPathSafe() {
        try {
            return proxy.getPath();
        } catch (GridAccessException e) {
            return null; // Per user decision: return null on exception
        }
    }

    /**
     * Safely get the IEnergyGrid. Returns null if grid is not accessible.
     *
     * @return IEnergyGrid instance or null if not accessible
     */
    public IEnergyGrid getEnergySafe() {
        try {
            return proxy.getEnergy();
        } catch (GridAccessException e) {
            return null; // Per user decision: return null on exception
        }
    }

    /**
     * Check if the network has power.
     * AENetworkProxy handles exceptions internally for this method.
     *
     * @return true if network is powered
     */
    public boolean isPowered() {
        return proxy.isPowered();
    }

    /**
     * Check if the proxy is ready for grid access.
     *
     * @return true if proxy is ready
     */
    public boolean isReady() {
        return proxy.isReady();
    }

    /**
     * Check if the proxy is active (has a valid grid node).
     *
     * @return true if proxy is active
     */
    public boolean isActive() {
        return proxy.isActive();
    }

    /**
     * Get the underlying AENetworkProxy.
     * Use with caution - prefer the safe wrapper methods.
     *
     * @return the wrapped AENetworkProxy
     */
    public AENetworkProxy getUnderlyingProxy() {
        return proxy;
    }
}
