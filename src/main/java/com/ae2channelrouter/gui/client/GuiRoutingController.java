package com.ae2channelrouter.gui.client;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.util.StatCollector;

import com.ae2channelrouter.gui.container.ContainerRoutingController;
import com.ae2channelrouter.tile.RoutingControllerTile;

/**
 * Client-side GUI for the Routing Controller.
 *
 * Displays real-time channel statistics including:
 * - Total available channels
 * - Used/allocated channels
 * - Available channels
 * - Number of detected AE2 controllers
 * - Number of connected routing terminals
 */
public class GuiRoutingController extends GuiContainer {

    private final ContainerRoutingController container;
    private final RoutingControllerTile tile;

    // GUI dimensions
    private static final int GUI_WIDTH = 176;
    private static final int GUI_HEIGHT = 166;

    /**
     * Constructor.
     *
     * @param container The container holding tile and player references
     */
    public GuiRoutingController(ContainerRoutingController container) {
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
        // In a full implementation, this would draw a custom texture
        this.drawDefaultBackground();

        // Draw a simple panel background
        int x = (this.width - this.xSize) / 2;
        int y = (this.height - this.ySize) / 2;

        // Draw a gray background panel
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
        String title = StatCollector.translateToLocal("gui.ae2channelrouter.routing_controller.title");
        if (title.equals("gui.ae2channelrouter.routing_controller.title")) {
            // Fallback if translation not found
            title = "Routing Controller";
        }

        int titleWidth = this.fontRendererObj.getStringWidth(title);
        this.fontRendererObj.drawString(title, (xSize - titleWidth) / 2, 8, 0x404040);

        // Get statistics from tile
        int totalChannels = tile.getTotalChannels();
        int allocatedChannels = tile.getAllocatedChannels();
        int availableChannels = tile.getAvailableChannels();
        int controllerCount = tile.getDetectedControllerCount();
        int terminalCount = tile.getConnectedTerminalCount();

        // Draw statistics
        int yPos = 30;
        int leftMargin = 10;
        int lineHeight = 12;

        // Channel statistics section
        this.fontRendererObj.drawString("Channel Statistics:", leftMargin, yPos, 0x404040);
        yPos += lineHeight + 2;

        this.fontRendererObj.drawString("Total: " + totalChannels, leftMargin, yPos, 0x404040);
        yPos += lineHeight;

        this.fontRendererObj.drawString(
            "Used: " + allocatedChannels,
            leftMargin,
            yPos,
            getChannelColor(allocatedChannels, totalChannels));
        yPos += lineHeight;

        this.fontRendererObj.drawString("Available: " + availableChannels, leftMargin, yPos, 0x00AA00); // Green
        yPos += lineHeight + 4;

        // Draw usage bar
        if (totalChannels > 0) {
            int barWidth = xSize - 20;
            int filledWidth = (int) ((allocatedChannels / (float) totalChannels) * barWidth);

            // Background bar
            drawRect(leftMargin, yPos, leftMargin + barWidth, yPos + 8, 0xFF555555);
            // Filled portion
            drawRect(
                leftMargin,
                yPos,
                leftMargin + filledWidth,
                yPos + 8,
                getUsageBarColor(allocatedChannels, totalChannels));

            yPos += 14;
        }

        // Device statistics section
        yPos += 4;
        this.fontRendererObj.drawString("Network Status:", leftMargin, yPos, 0x404040);
        yPos += lineHeight + 2;

        this.fontRendererObj.drawString("Controllers: " + controllerCount, leftMargin, yPos, 0x404040);
        yPos += lineHeight;

        this.fontRendererObj.drawString("Terminals: " + terminalCount, leftMargin, yPos, 0x404040);
        yPos += lineHeight;

        // Network status indicator
        yPos += 6;
        boolean isConnected = tile.hasGridAccess();
        String statusText = isConnected ? "Connected" : "Disconnected";
        int statusColor = isConnected ? 0x00AA00 : 0xAA0000; // Green or Red

        this.fontRendererObj.drawString("Status: " + statusText, leftMargin, yPos, statusColor);

        // Draw routing channel ID if set
        int routingChannelId = tile.getRoutingChannelId();
        if (routingChannelId != 0) {
            yPos += lineHeight;
            this.fontRendererObj.drawString("Channel ID: " + routingChannelId, leftMargin, yPos, 0x404040);
        }
    }

    /**
     * Get color for channel usage text based on utilization.
     */
    private int getChannelColor(int used, int total) {
        if (total == 0) {
            return 0x404040; // Gray (no controllers)
        }

        float ratio = used / (float) total;
        if (ratio < 0.5f) {
            return 0x00AA00; // Green (low usage)
        } else if (ratio < 0.8f) {
            return 0xAA8800; // Yellow/Orange (medium usage)
        } else {
            return 0xAA0000; // Red (high usage)
        }
    }

    /**
     * Get color for usage bar based on utilization.
     */
    private int getUsageBarColor(int used, int total) {
        if (total == 0) {
            return 0xFF555555; // Gray
        }

        float ratio = used / (float) total;
        if (ratio < 0.5f) {
            return 0xFF00AA00; // Green
        } else if (ratio < 0.8f) {
            return 0xFFAA8800; // Yellow/Orange
        } else {
            return 0xFFAA0000; // Red
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
