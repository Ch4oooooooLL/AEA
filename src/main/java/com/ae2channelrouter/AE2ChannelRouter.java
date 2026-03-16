package com.ae2channelrouter;

import org.apache.logging.log4j.Logger;

import com.ae2channelrouter.block.ModBlocks;
import com.ae2channelrouter.client.ClientInit;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * AE2 Channel Router - Main Mod Class
 *
 * Provides channel routing capabilities for Applied Energistics 2 networks,
 * allowing devices to access channels from a unified pool.
 */
@Mod(
    modid = AE2ChannelRouter.MOD_ID,
    acceptedMinecraftVersions = "[1.7.10]",
    name = AE2ChannelRouter.MOD_NAME,
    version = BuildTags.VERSION,
    dependencies = "required-after:Forge@[10.13.4.1614,);" + "required-after:appliedenergistics2;"
        + "required-after:gtnhlib@[0.6.11,)")
public class AE2ChannelRouter {

    public static final String MOD_ID = "ae2channelrouter";
    public static final String MOD_NAME = "AE2 Channel Router";

    @Mod.Instance(MOD_ID)
    public static AE2ChannelRouter INSTANCE;

    private Logger logger;

    /**
     * Get the mod logger.
     *
     * @return the Logger instance
     */
    public Logger getLogger() {
        return logger;
    }

    /**
     * Pre-initialization handler.
     * GTNH convention: ALL initialization happens in preInit.
     * This includes blocks, items, tile entities, and network registration.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // Initialize logger
        this.logger = event.getModLog();
        logger.info("Initializing AE2 Channel Router");

        // Register blocks and tile entities
        ModBlocks.registerBlocks();
        ModBlocks.registerTileEntities();

        logger.info("AE2 Channel Router preInit complete");
    }

    /**
     * Initialization handler.
     * Called during FMLInitializationEvent.
     */
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        // Initialization that requires other mods to be ready
        logger.info("AE2 Channel Router init phase");

        // Initialize client-side components
        if (event.getSide().isClient()) {
            ClientInit.init(event);
        }
    }

    /**
     * Post-initialization handler.
     * Called during FMLPostInitializationEvent.
     */
    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        // Post-initialization cleanup
        logger.info("AE2 Channel Router postInit phase");
    }
}
