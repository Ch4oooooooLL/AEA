package com.ae2channelrouter.block;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.ae2channelrouter.AE2ChannelRouter;
import com.ae2channelrouter.tile.RoutingCableTile;

import appeng.block.AEBaseBlock;

/**
 * Block for routing cables.
 * 
 * This block extends AEBaseBlock from AE2 and provides the physical
 * representation of routing cables in the world. Cables connect routing
 * terminals to the routing controller.
 */
public class RoutingCableBlock extends AEBaseBlock {

    /**
     * Constructor sets up block properties.
     */
    public RoutingCableBlock() {
        super(Material.glass);
        
        // Block properties
        setHardness(0.0F);
        setResistance(0.0F);
        setStepSound(soundTypeGlass);
        setBlockName("routing_cable");
        setBlockTextureName(AE2ChannelRouter.MOD_ID + ":routing_cable");
        
        // Non-opaque for proper rendering
        setLightOpacity(0);
        
        // Creative tab
        setCreativeTab(AE2ChannelRouter.INSTANCE.getCreativeTab());
    }
    
    /**
     * Check if block is opaque.
     * Cables are non-opaque for proper rendering.
     */
    @Override
    public boolean isOpaqueCube() {
        return false;
    }
    
    /**
     * Check if block renders as a normal block.
     * Cables use custom rendering.
     */
    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }
    
    /**
     * Get the collision bounding box.
     * Thin cable bounds: 6/16 to 10/16 (similar to AE2 cables).
     */
    @Override
    public void setBlockBoundsBasedOnState(World world, int x, int y, int z) {
        TileEntity te = world.getTileEntity(x, y, z);
        
        if (te instanceof RoutingCableTile) {
            RoutingCableTile cable = (RoutingCableTile) te;
            EnumSet<ForgeDirection> connections = cable.getConnections();
            
            // Default center bounds
            float minX = 0.375F; // 6/16
            float minY = 0.375F;
            float minZ = 0.375F;
            float maxX = 0.625F; // 10/16
            float maxY = 0.625F;
            float maxZ = 0.625F;
            
            // Expand bounds based on connections
            if (connections.contains(ForgeDirection.WEST)) {
                minX = 0.0F;
            }
            if (connections.contains(ForgeDirection.EAST)) {
                maxX = 1.0F;
            }
            if (connections.contains(ForgeDirection.DOWN)) {
                minY = 0.0F;
            }
            if (connections.contains(ForgeDirection.UP)) {
                maxY = 1.0F;
            }
            if (connections.contains(ForgeDirection.NORTH)) {
                minZ = 0.0F;
            }
            if (connections.contains(ForgeDirection.SOUTH)) {
                maxZ = 1.0F;
            }
            
            setBlockBounds(minX, minY, minZ, maxX, maxY, maxZ);
        } else {
            // Default center bounds
            setBlockBounds(0.375F, 0.375F, 0.375F, 0.625F, 0.625F, 0.625F);
        }
    }
    
    /**
     * Get light value emitted by this block.
     * Cables don't emit light.
     */
    @Override
    public int getLightValue(World world, int x, int y, int z) {
        return 0;
    }
    
    /**
     * Create the tile entity for this block.
     */
    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new RoutingCableTile();
    }
    
    /**
     * Check if the block has a tile entity.
     */
    @Override
    public boolean hasTileEntity(int metadata) {
        return true;
    }
}
