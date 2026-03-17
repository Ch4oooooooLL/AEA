package com.ae2channelrouter.network;

import java.util.UUID;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.ae2channelrouter.tile.RoutingControllerTile;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;

/**
 * Network packet for terminal-controller communication.
 *
 * Handles channel allocation requests and releases between routing terminals
 * and the routing controller. Supports three actions:
 * - REQUEST: Terminal requests channels from controller
 * - RELEASE: Terminal releases allocated channels
 * - RESPONSE: Controller responds with allocated channel count
 */
public class PacketRoutingChannel implements IMessage {

    private UUID terminalId;
    private int routingChannelId;
    private byte action;
    private int channelCount;

    /**
     * Default constructor required for Forge networking.
     */
    public PacketRoutingChannel() {
    }

    /**
     * Constructor for creating packets.
     *
     * @param terminalId      Unique terminal identifier
     * @param routingChannelId Routing channel ID for wireless matching
     * @param action          Action type (REQUEST, RELEASE, RESPONSE)
     * @param channelCount    Channel count (for RESPONSE)
     */
    public PacketRoutingChannel(UUID terminalId, int routingChannelId, Action action, int channelCount) {
        this.terminalId = terminalId;
        this.routingChannelId = routingChannelId;
        this.action = action.getId();
        this.channelCount = channelCount;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        // Read terminalId (UUID = 2 longs)
        long mostSig = buf.readLong();
        long leastSig = buf.readLong();
        this.terminalId = new UUID(mostSig, leastSig);

        this.routingChannelId = buf.readInt();
        this.action = buf.readByte();
        this.channelCount = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // Write terminalId (UUID = 2 longs)
        buf.writeLong(terminalId.getMostSignificantBits());
        buf.writeLong(terminalId.getLeastSignificantBits());

        buf.writeInt(routingChannelId);
        buf.writeByte(action);
        buf.writeInt(channelCount);
    }

    // ==================== Getters ====================

    public UUID getTerminalId() {
        return terminalId;
    }

    public int getRoutingChannelId() {
        return routingChannelId;
    }

    public Action getAction() {
        return Action.fromId(action);
    }

    public int getChannelCount() {
        return channelCount;
    }

    // ==================== Action Enum ====================

    public enum Action {
        REQUEST((byte) 0),
        RELEASE((byte) 1),
        RESPONSE((byte) 2);

        private final byte id;

        Action(byte id) {
            this.id = id;
        }

        public byte getId() {
            return id;
        }

        public static Action fromId(byte id) {
            for (Action action : values()) {
                if (action.id == id) {
                    return action;
                }
            }
            return REQUEST; // Default
        }
    }

    // ==================== Handler ====================

    public static class Handler implements IMessageHandler<PacketRoutingChannel, IMessage> {

        @Override
        public IMessage onMessage(PacketRoutingChannel message, MessageContext ctx) {
            if (ctx.side.isServer()) {
                return handleServer(message, ctx);
            } else {
                return handleClient(message, ctx);
            }
        }

        private IMessage handleServer(PacketRoutingChannel message, MessageContext ctx) {
            Action action = message.getAction();

            switch (action) {
                case REQUEST:
                    return handleChannelRequest(message, ctx);
                case RELEASE:
                    handleChannelRelease(message, ctx);
                    return null;
                default:
                    return null;
            }
        }

        private IMessage handleClient(PacketRoutingChannel message, MessageContext ctx) {
            // Client-side handling for RESPONSE
            if (message.getAction() == Action.RESPONSE) {
                // Update local client state with allocated channels
                // This will be used by the terminal tile/GUI
            }
            return null;
        }

        private IMessage handleChannelRequest(PacketRoutingChannel message, MessageContext ctx) {
            // Find the controller by routing channel ID
            // For now, we'll search in the player's dimension
            // In a full implementation, this might use a registry

            int routingChannelId = message.getRoutingChannelId();
            UUID terminalId = message.getTerminalId();

            // Get the world from the player
            World world = ctx.getServerHandler().playerEntity.worldObj;

            // Search for a controller with matching routing channel ID
            // This is a simplified approach - in production, use a proper registry
            RoutingControllerTile controller = findControllerByChannelId(world, routingChannelId);

            int allocated = 0;
            if (controller != null) {
                // Request channels from the controller
                // Default request: 8 channels (configurable)
                allocated = controller.allocateChannels(terminalId, 8);
            }

            // Send response back to client
            return new PacketRoutingChannel(terminalId, routingChannelId, Action.RESPONSE, allocated);
        }

        private void handleChannelRelease(PacketRoutingChannel message, MessageContext ctx) {
            int routingChannelId = message.getRoutingChannelId();
            UUID terminalId = message.getTerminalId();

            World world = ctx.getServerHandler().playerEntity.worldObj;
            RoutingControllerTile controller = findControllerByChannelId(world, routingChannelId);

            if (controller != null) {
                controller.releaseChannels(terminalId);
            }
        }

        /**
         * Find a controller by its routing channel ID.
         * Simplified implementation - searches loaded chunks.
         */
        private RoutingControllerTile findControllerByChannelId(World world, int routingChannelId) {
            // This is a placeholder implementation
            // In a full implementation, maintain a registry of controllers
            // For now, we'll iterate through loaded tile entities

            for (Object tileObj : world.loadedTileEntityList) {
                if (tileObj instanceof RoutingControllerTile) {
                    RoutingControllerTile controller = (RoutingControllerTile) tileObj;
                    if (controller.getRoutingChannelId() == routingChannelId) {
                        return controller;
                    }
                }
            }
            return null;
        }
    }
}
