package com.ae2channelrouter.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.ae2channelrouter.gui.client.GuiRoutingController;
import com.ae2channelrouter.gui.client.GuiRoutingTerminal;
import com.ae2channelrouter.gui.container.ContainerRoutingController;
import com.ae2channelrouter.gui.container.ContainerRoutingTerminal;
import com.ae2channelrouter.tile.RoutingControllerTile;
import com.ae2channelrouter.tile.RoutingTerminalTile;

import cpw.mods.fml.common.network.IGuiHandler;

/**
 * GUI handler for AE2 Channel Router.
 *
 * Bridges server-side container and client-side GUI creation.
 * Registered with NetworkRegistry during mod initialization.
 */
public class GuiHandler implements IGuiHandler {

    /** GUI ID for Routing Controller */
    public static final int GUI_ROUTING_CONTROLLER = 0;

    /** GUI ID for Routing Terminal */
    public static final int GUI_ROUTING_TERMINAL = 1;

    /**
     * Server-side: Create and return the Container.
     */
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_ROUTING_CONTROLLER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof RoutingControllerTile) {
                return new ContainerRoutingController((RoutingControllerTile) te, player);
            }
        } else if (ID == GUI_ROUTING_TERMINAL) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof RoutingTerminalTile) {
                return new ContainerRoutingTerminal((RoutingTerminalTile) te, player);
            }
        }
        return null;
    }

    /**
     * Client-side: Create and return the GUI.
     */
    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == GUI_ROUTING_CONTROLLER) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof RoutingControllerTile) {
                ContainerRoutingController container = new ContainerRoutingController(
                    (RoutingControllerTile) te,
                    player);
                return new GuiRoutingController(container);
            }
        } else if (ID == GUI_ROUTING_TERMINAL) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof RoutingTerminalTile) {
                ContainerRoutingTerminal container = new ContainerRoutingTerminal(
                    (RoutingTerminalTile) te,
                    player);
                return new GuiRoutingTerminal(container);
            }
        }
        return null;
    }
}
