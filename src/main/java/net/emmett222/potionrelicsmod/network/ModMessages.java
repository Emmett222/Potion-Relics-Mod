package net.emmett222.potionrelicsmod.network;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * Handles mod networking.
 * 
 * @author Emmett Grebe
 * @version 4-22-2026
 */
public class ModMessages {
    private static final String PROTOCOL_VERSION = "1";
    private static int packetId = 0;

    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(PotionRelicsMod.MOD_ID, "messages"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    private static int nextId() {
        return packetId++;
    }

    /**
     * Registers all network packets.
     */
    public static void register() {
        INSTANCE.registerMessage(nextId(),
                ToggleRelicPacket.class,
                ToggleRelicPacket::encode,
                ToggleRelicPacket::decode,
                ToggleRelicPacket::handle);
        INSTANCE.registerMessage(nextId(),
                CameraShakePacket.class,
                CameraShakePacket::encode,
                CameraShakePacket::decode,
                CameraShakePacket::handle);
        INSTANCE.registerMessage(nextId(),
                DragonWardApparitionPacket.class,
                DragonWardApparitionPacket::encode,
                DragonWardApparitionPacket::decode,
                DragonWardApparitionPacket::handle);
    }

    /**
     * Sends a packet to the server.
     * 
     * @param packet The packet to send.
     */
    public static void sendToServer(Object packet) {
        INSTANCE.sendToServer(packet);
    }

    public static void sendToPlayer(ServerPlayer player, Object packet) {
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
