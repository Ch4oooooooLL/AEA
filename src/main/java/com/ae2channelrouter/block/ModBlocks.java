package com.ae2channelrouter.block;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.tile.AEBaseRouterTile;
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

    /**
     * Register all blocks for this mod.
     * Called during FMLPreInitializationEvent.
     */
    public static void registerBlocks() {
        AE2ChannelRouter.INSTANCE.getLogger().info("Registering AE2 Channel Router blocks");

        // Block registration will be implemented in subsequent phases
        // Phase 2: Routing Cable blocks
        // Phase 3: Routing Controller block
        // Phase 4: Routing Terminal blocks

        AE2ChannelRouter.INSTANCE.getLogger().info("Block registration complete");
    }

    /**
     * Register all tile entities for this mod.
     * Called during FMLPreInitializationEvent after blocks are registered.
     *
     * Note: Currently registers base tile class for testing.
     * Specific tiles will be registered in their respective phases.
     */
    public static void registerTileEntities() {
        AE2ChannelRouter.INSTANCE.getLogger().info("Registering AE2 Channel Router tile entities");

        // Register base router tile - used for testing AE2 integration
        // This will be replaced with specific tile implementations in later phases
        GameRegistry.registerTileEntity(
            AEBaseRouterTile.class,
            AE2ChannelRouter.MOD_ID + ":" + TILE_ROUTING_CONTROLLER
        );

        AE2ChannelRouter.INSTANCE.getLogger().info("Tile entity registration complete");
    }
}
