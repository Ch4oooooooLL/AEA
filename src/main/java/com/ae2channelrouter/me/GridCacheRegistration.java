package com.ae2channelrouter.me;

import com.ae2channelrouter.AE2ChannelRouter;

import appeng.api.AEApi;

/**
 * Handles registration of the RoutingChannelCache with AE2.
 */
public class GridCacheRegistration {
    
    private static boolean registered = false;
    
    /**
     * Register the RoutingChannelCache with AE2's grid system.
     * Must be called during mod initialization (FMLPreInitializationEvent).
     * 
     * @return true if registration successful, false if not supported
     */
    public static boolean registerCache() {
        if (registered) {
            return true;
        }
        
        try {
            // Try to get the grid cache registry
            Object registry = AEApi.instance().registries().getClass()
                .getMethod("gridCache")
                .invoke(AEApi.instance().registries());
            
            if (registry != null) {
                // Try to register the cache
                registry.getClass()
                    .getMethod("registerGridCache", Class.class)
                    .invoke(registry, RoutingChannelCache.class);
                
                registered = true;
                AE2ChannelRouter.INSTANCE.getLogger().info(
                    "Successfully registered RoutingChannelCache with AE2"
                );
                return true;
            }
        } catch (NoSuchMethodException e) {
            AE2ChannelRouter.INSTANCE.getLogger().warn(
                "GridCache API not available in this AE2 version: {}",
                e.getMessage()
            );
        } catch (Exception e) {
            AE2ChannelRouter.INSTANCE.getLogger().warn(
                "Could not register RoutingChannelCache: {}",
                e.getMessage()
            );
        }
        
        AE2ChannelRouter.INSTANCE.getLogger().info(
            "RoutingChannelCache registration skipped - using fallback mode"
        );
        return false;
    }
    
    /**
     * Check if custom GridCache registration is supported.
     */
    public static boolean isSupported() {
        try {
            AEApi.instance().registries().getClass().getMethod("gridCache");
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
