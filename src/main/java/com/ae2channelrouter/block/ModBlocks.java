package com.ae2channelrouter.block;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.tile.AEBaseRouterTile;
import com.ae2channelrouter.tile.RoutingCableTile;

import cpw.mods.fml.common.registry.GameRegistry;

/**
 * Block and tile entity registry for AE2 Channel Router.
 *
 * Handles registration of all blocks and their associated tile entities.
 * Per GTNH convention, all registration happens in preInit.
 */
public class ModBlocks {

    // Block IDs - will be replaced with actual Block instances in future phases
    public static final String BLOCK_ROUTING_CABLE = "routing_cable";
    public static final String BLOCK_ROUTING_CONTROLLER = "routing_controller";
    public static final String BLOCK_ROUTING_TERMINAL = "routing_terminal";

    // Tile entity IDs
    public static final String TILE_ROUTING_CABLE = "routing_cable_tile";
    public static final String TILE_ROUTING_CONTROLLER = "routing_controller_tile";
    public static final String TILE_ROUTING_TERMINAL = "routing_terminal_tile";

    // Block instances
    public static AEBaseBlock routingCable;

    /**
     * Register all blocks for this mod.
     * Called during FMLPreInitializationEvent.
     */
    public static void registerBlocks() {
        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Registering AE2 Channel Router blocks");

        // Phase 2: Routing Cable
        routingCable = new RoutingCableBlock();
        GameRegistry.registerBlock(routingCable, BLOCK_ROUTING_CABLE);
        AE2ChannelRouter.INSTANCE.getLogger().info("Registered routing cable block");

        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Block registration complete");
    }

    /**
     * Register all tile entities for this mod.
     * Called during FMLPreInitializationEvent after blocks are registered.
     *
     * Note: Currently registers base tile class for testing.
     * Specific tiles will be registered in their respective phases.
     */
    public static void registerTileEntities() {
        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Registering AE2 Channel Router tile entities");

        // Phase 2: Routing Cable Tile
        GameRegistry.registerTileEntity(RoutingCableTile.class, AE2ChannelRouter.MOD_ID + ":" + TILE_ROUTING_CABLE);
        AE2ChannelRouter.INSTANCE.getLogger().info("Registered routing cable tile entity");

        // Register base router tile - used for testing AE2 integration
        // This will be replaced with specific tile implementations in later phases
        GameRegistry
            .registerTileEntity(AEBaseRouterTile.class, AE2ChannelRouter.MOD_ID + ":" + TILE_ROUTING_CONTROLLER);

        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Tile entity registration complete");
    }
}
