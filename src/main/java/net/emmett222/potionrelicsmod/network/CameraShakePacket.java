package net.emmett222.potionrelicsmod.network;

import java.util.function.Supplier;

import net.emmett222.potionrelicsmod.events.ClientCameraShakeEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class CameraShakePacket {
    private final float intensity;
    private final int durationTicks;

    public CameraShakePacket(float intensity, int durationTicks) {
        this.intensity = intensity;
        this.durationTicks = durationTicks;
    }

    public static void encode(CameraShakePacket packet, FriendlyByteBuf buf) {
        buf.writeFloat(packet.intensity);
        buf.writeVarInt(packet.durationTicks);
    }

    public static CameraShakePacket decode(FriendlyByteBuf buf) {
        return new CameraShakePacket(buf.readFloat(), buf.readVarInt());
    }

    public static void handle(CameraShakePacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientCameraShakeEvents.startShake(packet.intensity, packet.durationTicks)));
        context.setPacketHandled(true);
    }
}
