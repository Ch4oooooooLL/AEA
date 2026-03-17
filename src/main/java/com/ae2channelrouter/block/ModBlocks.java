package com.ae2channelrouter.block;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.tile.AEBaseRouterTile;
import com.ae2channelrouter.tile.RoutingCableTile;
import com.ae2channelrouter.tile.RoutingControllerTile;
import com.ae2channelrouter.tile.RoutingTerminalTile;

import appeng.block.AEBaseBlock;
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
    public static AEBaseBlock routingController;
    public static AEBaseBlock routingTerminal;

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
        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Registered routing cable block");

        // Phase 3: Routing Controller
        routingController = new RoutingControllerBlock();
        GameRegistry.registerBlock(routingController, BLOCK_ROUTING_CONTROLLER);
        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Registered routing controller block");

        // Phase 4: Routing Terminal
        routingTerminal = new RoutingTerminalBlock();
        GameRegistry.registerBlock(routingTerminal, BLOCK_ROUTING_TERMINAL);
        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Registered routing terminal block");

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
        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Registered routing cable tile entity");

        // Phase 3: Routing Controller Tile
        GameRegistry.registerTileEntity(RoutingControllerTile.class, AE2ChannelRouter.MOD_ID + ":" + TILE_ROUTING_CONTROLLER);
        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Registered routing controller tile entity");

        // Phase 4: Routing Terminal Tile
        GameRegistry.registerTileEntity(RoutingTerminalTile.class, AE2ChannelRouter.MOD_ID + ":" + TILE_ROUTING_TERMINAL);
        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Registered routing terminal tile entity");

        AE2ChannelRouter.INSTANCE.getLogger()
            .info("Tile entity registration complete");
    }
}
