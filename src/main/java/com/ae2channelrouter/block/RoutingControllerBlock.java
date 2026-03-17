package com.ae2channelrouter.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.gui.GuiHandler;

import appeng.block.AEBaseBlock;
import appeng.core.CreativeTab;

/**
 * Block for the Routing Controller.
 *
 * This is a full block (not cable-like) that serves as the central hub
 * for channel allocation. Players can right-click to open the GUI.
 */
public class RoutingControllerBlock extends AEBaseBlock {

    /**
     * Constructor sets up block properties.
     */
    public RoutingControllerBlock() {
        super(Material.iron);

        // Block properties - sturdy like a crafting table or furnace
        setHardness(3.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("routing_controller");
        setBlockTextureName(AE2ChannelRouter.MOD_ID + ":routing_controller");

        // Full block rendering
        this.isOpaque = true;
        this.isFullSize = true;
        setLightOpacity(255);

        // Creative tab - use AE2's creative tab
        setCreativeTab(CreativeTab.instance);

        // Harvest tool: pickaxe
        setHarvestLevel("pickaxe", 0);
    }

    /**
     * Called when the block is right-clicked.
     * Opens the controller GUI.
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(AE2ChannelRouter.INSTANCE, GuiHandler.GUI_ROUTING_CONTROLLER, world, x, y, z);
        }
        return true;
    }
}
