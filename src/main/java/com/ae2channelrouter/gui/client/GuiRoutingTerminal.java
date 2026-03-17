package com.ae2channelrouter.gui.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.StatCollector;

import com.ae2channelrouter.gui.container.ContainerRoutingTerminal;
import com.ae2channelrouter.tile.RoutingTerminalTile;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Client-side GUI for the Routing Terminal.
 *
 * Displays real-time terminal statistics including:
 * - Connection status (online/offline)
 * - Allocated channels from controller
 * - Connected AE device count
 * - Soft limit warning indicator
 */
@SideOnly(Side.CLIENT)
public class GuiRoutingTerminal extends GuiContainer {

    private final ContainerRoutingTerminal container;
    private final RoutingTerminalTile tile;

    // GUI dimensions
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 140;

    /**
     * Constructor.
     *
     * @param container The container holding tile and player references
     */
    public GuiRoutingTerminal(ContainerRoutingTerminal container) {
        super(container);
        this.container = container;
        this.tile = container.getTile();

        // Set GUI dimensions
        this.xSize = GUI_WIDTH;
        this.ySize = GUI_HEIGHT;
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        // Draw background - using default dark background
        this.drawDefaultBackground();

        // Draw a simple panel background
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw a gray background panel (AE2 style)
        drawRect(x, y, x + xSize, y + ySize, 0xFFC6C6C6);

        // Draw a darker border
        drawRect(x, y, x + xSize, y + 1, 0xFF373737); // Top
        drawRect(x, y, x + 1, y + ySize, 0xFF373737); // Left
        drawRect(x + xSize - 1, y, x + xSize, y + ySize, 0xFFFFFFFF); // Right
        drawRect(x, y + ySize - 1, x + xSize, y + ySize, 0xFFFFFFFF); // Bottom
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        // Draw title centered
        String title = StatCollector.translateToLocal("gui.ae2channelrouter.routing_terminal.title");
        if (title.equals("gui.ae2channelrouter.routing_terminal.title")) {
            // Fallback if translation not found
            title = "Routing Terminal";
        }

        int titleWidth = this.fontRendererObj.getStringWidth(title);
        this.fontRendererObj.drawString(title, (xSize - titleWidth) / 2, 8, 0x404040);

        // Get statistics from container (synced from server)
        int allocatedChannels = container.getAllocatedChannels();
        int deviceCount = container.getConnectedDeviceCount();
        boolean online = container.isOnline();
        boolean warning = container.isSoftLimitWarning();

        // Draw statistics
        int yPos = 30;
        int leftMargin = 10;
        int lineHeight = 12;

        // Status line
        String statusText = online ? "Status: Online" : "Status: Offline";
        int statusColor = online ? 0x00AA00 : 0xAA0000; // Green or Red
        this.fontRendererObj.drawString(statusText, leftMargin, yPos, statusColor);
        yPos += lineHeight + 4;

        // Channel statistics
        this.fontRendererObj.drawString("Channel Statistics:", leftMargin, yPos, 0x404040);
        yPos += lineHeight + 2;

        this.fontRendererObj.drawString("Allocated: " + allocatedChannels, leftMargin, yPos, 0x404040);
        yPos += lineHeight;

        this.fontRendererObj.drawString("Connected Devices: " + deviceCount, leftMargin, yPos, 0x404040);
        yPos += lineHeight + 4;

        // Warning indicator
        if (warning) {
            String warningText = "! High Channel Usage";
            this.fontRendererObj.drawString(warningText, leftMargin, yPos, 0xFFAA00); // Orange/Yellow
            yPos += lineHeight;

            String warningSubtext = "  (Avg > 16 ch/device)";
            this.fontRendererObj.drawString(warningSubtext, leftMargin, yPos, 0xAAAAAA); // Gray
        }

        // Draw routing channel ID if set
        int routingChannelId = tile.getRoutingChannelId();
        if (routingChannelId != 0) {
            yPos = ySize - 20;
            this.fontRendererObj.drawString("Channel ID: " + routingChannelId, leftMargin, yPos, 0x666666);
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        // Initialize any buttons or controls here
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        // Update any animated elements here
    }
}
