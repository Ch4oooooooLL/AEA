package com.ae2channelrouter;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

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
    dependencies = "required-after:Forge@[10.13.4.1614,);" +
                   "required-after:appliedenergistics2;" +
                   "required-after:gtnhlib@[0.6.11,)"
)
public class AE2ChannelRouter {

    public static final String MOD_ID = "ae2channelrouter";
    public static final String MOD_NAME = "AE2 Channel Router";

    @Mod.Instance(MOD_ID)
    public static AE2ChannelRouter INSTANCE;

    /**
     * Pre-initialization handler.
     * GTNH convention: ALL initialization happens in preInit.
     * This includes blocks, items, tile entities, and network registration.
     */
    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // All initialization goes here per GTNH convention
        // Blocks, items, tile entities registered in preInit only
    }
}
