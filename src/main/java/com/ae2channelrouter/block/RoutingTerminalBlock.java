package com.ae2channelrouter.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.gui.GuiHandler;

import appeng.block.AEBaseBlock;
import appeng.core.CreativeTab;

/**
 * Block for the Routing Terminal.
 *
 * This is a full block (not cable-like) that serves as the distribution node
 * for channels. It receives channel allocation from the controller and
 * provides channels to connected AE devices. Players can right-click to open the GUI.
 */
public class RoutingTerminalBlock extends AEBaseBlock {

    /**
     * Constructor sets up block properties.
     */
    public RoutingTerminalBlock() {
        super(Material.iron);

        // Block properties - sturdy like a crafting table or furnace
        setHardness(3.0F);
        setResistance(10.0F);
        setStepSound(soundTypeMetal);
        setBlockName("routing_terminal");
        setBlockTextureName(AE2ChannelRouter.MOD_ID + ":routing_terminal");

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
     * Opens the terminal GUI.
     */
    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(AE2ChannelRouter.INSTANCE, GuiHandler.GUI_ROUTING_TERMINAL, world, x, y, z);
        }
        return true;
    }
}
