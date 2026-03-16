/*
 * This file is part of AE2 Channel Router.
 * Copyright (c) 2026, AE2 Channel Router Team, All rights reserved.
 *
 * AE2 Channel Router is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.ae2channelrouter.client;

import com.ae2channelrouter.client.render.RoutingCableRender;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-side initialization handler.
 *
 * Handles all client-only registration including renderers.
 */
@SideOnly(Side.CLIENT)
public class ClientInit {

    /**
     * Initialize client-side components.
     * Called during FMLInitializationEvent on client side only.
     */
    public static void init(FMLInitializationEvent event) {
        // Register custom block renderer for routing cables
        RenderingRegistry.registerBlockHandler(RoutingCableRender.INSTANCE);
    }

    /**
     * Get the render ID for routing cables.
     */
    public static int getRoutingCableRenderId() {
        return RoutingCableRender.getRenderId();
    }
}
