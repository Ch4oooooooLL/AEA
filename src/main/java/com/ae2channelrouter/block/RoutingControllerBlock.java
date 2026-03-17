package com.ae2channelrouter.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.gui.GuiHandler;
import com.ae2channelrouter.tile.RoutingControllerTile;

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
     * Check if block renders as a normal block.
     * Controller is a full block.
     */
    @Override
    public boolean renderAsNormalBlock() {
        return true;
    }

    /**
     * Check if the block is opaque.
     */
    @Override
    public boolean isOpaqueCube() {
        return true;
    }

    /**
     * Create the tile entity for this block.
     */
    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new RoutingControllerTile();
    }

    /**
     * Check if the block has a tile entity.
     */
    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
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
