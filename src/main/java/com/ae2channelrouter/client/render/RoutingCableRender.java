/*
 * This file is part of AE2 Channel Router.
 * Copyright (c) 2026, AE2 Channel Router Team, All rights reserved.
 *
 * AE2 Channel Router is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */

package com.ae2channelrouter.client.render;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.util.ForgeDirection;

import com.ae2channelrouter.block.RoutingCableBlock;
import com.ae2channelrouter.tile.RoutingCableTile;

import cpw.mods.fml.client.registry.ISimpleBlockRenderingHandler;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Custom renderer for routing cables.
 *
 * Renders orange glass-style cables that are visually distinct from standard AE2 cables.
 * Supports 6-sided connections and renders cable extensions based on connection state.
 */
@SideOnly(Side.CLIENT)
public class RoutingCableRender implements ISimpleBlockRenderingHandler {

    public static final RoutingCableRender INSTANCE = new RoutingCableRender();
    private static final int RENDER_ID = RenderingRegistry.getNextAvailableRenderId();

    // Orange color - RGB (255, 165, 0)
    private static final int ORANGE_COLOR = 0xFFA500;

    // Cable dimensions in 1/16ths
    private static final float CABLE_MIN = 6.0F / 16.0F;
    private static final float CABLE_MAX = 10.0F / 16.0F;

    private RoutingCableRender() {}

    /**
     * Get the render ID for this renderer.
     */
    public static int getRenderId() {
        return RENDER_ID;
    }

    @Override
    public int getRenderId() {
        return RENDER_ID;
    }

    @Override
    public boolean shouldRender3DInInventory(int modelId) {
        return true;
    }

    @Override
    public void renderInventoryBlock(Block block, int metadata, int modelID, RenderBlocks renderer) {
        // Render a simple center cable in inventory
        renderer.setRenderBounds(CABLE_MIN, CABLE_MIN, CABLE_MIN, CABLE_MAX, CABLE_MAX, CABLE_MAX);

        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        tess.setColorOpaque_I(ORANGE_COLOR);

        // Render all faces with orange color
        IIcon icon = block.getIcon(0, metadata);
        if (icon == null) {
            icon = ((net.minecraft.client.renderer.texture.TextureMap)
                            net.minecraft.client.Minecraft.getMinecraft().getTextureManager()
                                    .getTexture(net.minecraft.client.renderer.texture.TextureMap.locationBlocksTexture))
                    .getAtlasSprite("minecraft:blocks/glass");
        }

        renderer.renderBlockAsItem(block, metadata, 1.0F);

        tess.draw();
    }

    @Override
    public boolean renderWorldBlock(IBlockAccess world, int x, int y, int z, Block block, int modelId,
            RenderBlocks renderer) {
        if (!(block instanceof RoutingCableBlock)) {
            return false;
        }

        RoutingCableBlock cableBlock = (RoutingCableBlock) block;
        EnumSet<ForgeDirection> connections = EnumSet.noneOf(ForgeDirection.class);

        // Get connections from tile entity
        TileEntity te = world.getTileEntity(x, y, z);
        if (te instanceof RoutingCableTile) {
            connections = ((RoutingCableTile) te).getConnections();
        }

        // Set color for orange glass appearance
        int color = ORANGE_COLOR;

        // Render central cable core
        renderer.setRenderBounds(CABLE_MIN, CABLE_MIN, CABLE_MIN, CABLE_MAX, CABLE_MAX, CABLE_MAX);
        renderer.renderStandardBlockWithColorMultiplier(block, x, y, z, 
            ((color >> 16) & 0xFF) / 255.0F, 
            ((color >> 8) & 0xFF) / 255.0F, 
            (color & 0xFF) / 255.0F);

        // Render connections for each direction
        for (ForgeDirection dir : connections) {
            renderConnection(renderer, cableBlock, x, y, z, dir, color);
        }

        return true;
    }

    /**
     * Render a cable connection in the specified direction.
     */
    private void renderConnection(RenderBlocks renderer, Block block, int x, int y, int z, ForgeDirection dir,
            int color) {
        float minX = CABLE_MIN;
        float minY = CABLE_MIN;
        float minZ = CABLE_MIN;
        float maxX = CABLE_MAX;
        float maxY = CABLE_MAX;
        float maxZ = CABLE_MAX;

        switch (dir) {
            case DOWN -> {
                minY = 0.0F;
                maxY = CABLE_MIN;
            }
            case UP -> {
                minY = CABLE_MAX;
                maxY = 1.0F;
            }
            case NORTH -> {
                minZ = 0.0F;
                maxZ = CABLE_MIN;
            }
            case SOUTH -> {
                minZ = CABLE_MAX;
                maxZ = 1.0F;
            }
            case WEST -> {
                minX = 0.0F;
                maxX = CABLE_MIN;
            }
            case EAST -> {
                minX = CABLE_MAX;
                maxX = 1.0F;
            }
            default -> {
                return;
            }
        }

        renderer.setRenderBounds(minX, minY, minZ, maxX, maxY, maxZ);
        renderer.renderStandardBlockWithColorMultiplier(block, x, y, z,
            ((color >> 16) & 0xFF) / 255.0F,
            ((color >> 8) & 0xFF) / 255.0F,
            (color & 0xFF) / 255.0F);
    }

    /**
     * Render the item for inventory/display.
     */
    public void renderItem(ItemStack item, IItemRenderer.ItemRenderType type, Object[] data) {
        RenderBlocks renderer = (RenderBlocks) data[0];
        Block block = Block.getBlockFromItem(item.getItem());

        if (!(block instanceof RoutingCableBlock)) {
            return;
        }

        // Set bounds for center cable
        renderer.setRenderBounds(CABLE_MIN, CABLE_MIN, CABLE_MIN, CABLE_MAX, CABLE_MAX, CABLE_MAX);

        // Use standard block rendering
        Tessellator tess = Tessellator.instance;
        tess.startDrawingQuads();
        renderer.renderBlockAsItem(block, item.getItemDamage(), 1.0F);
        tess.draw();
    }
}
