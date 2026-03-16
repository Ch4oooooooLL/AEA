package com.ae2channelrouter.api;

import net.minecraftforge.common.util.ForgeDirection;

/**
 * Interface for routing devices that can connect to routing cables.
 * 
 * Implement this interface on TileEntities that should be discoverable
 * and connectable by routing cables.
 */
public interface IRoutingDevice {

    /**
     * Get the type of routing device.
     * 
     * @return Device type identifier
     */
    DeviceType getDeviceType();

    /**
     * Check if this device can accept a connection from a specific direction.
     * 
     * @param from The facing direction of the incoming connection
     * @return true if connection is allowed
     */
    default boolean canConnectFrom(ForgeDirection from) {
        return true;
    }

    /**
     * Types of routing devices.
     */
    enum DeviceType {
        CONTROLLER,
        TERMINAL,
        CABLE
    }
}
