package net.emmett222.potionrelicsmod.network;

import java.util.function.Supplier;

import net.emmett222.potionrelicsmod.events.ClientDragonRelicEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class DragonWardApparitionPacket {
    private final boolean followEntity;
    private final int entityId;
    private final double anchorX;
    private final double anchorY;
    private final double anchorZ;
    private final float yawDegrees;
    private final int durationTicks;

    public DragonWardApparitionPacket(boolean followEntity, int entityId, double anchorX, double anchorY,
            double anchorZ, float yawDegrees, int durationTicks) {
        this.followEntity = followEntity;
        this.entityId = entityId;
        this.anchorX = anchorX;
        this.anchorY = anchorY;
        this.anchorZ = anchorZ;
        this.yawDegrees = yawDegrees;
        this.durationTicks = durationTicks;
    }

    public static DragonWardApparitionPacket forEntity(Entity entity, int durationTicks) {
        Vec3 position = entity.position();
        return new DragonWardApparitionPacket(true, entity.getId(), position.x, position.y, position.z,
                entity.getYRot(), durationTicks);
    }

    public static DragonWardApparitionPacket forPosition(Vec3 position, float yawDegrees, int durationTicks) {
        return new DragonWardApparitionPacket(false, -1, position.x, position.y, position.z, yawDegrees, durationTicks);
    }

    public static void encode(DragonWardApparitionPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.followEntity);
        buf.writeVarInt(packet.entityId);
        buf.writeDouble(packet.anchorX);
        buf.writeDouble(packet.anchorY);
        buf.writeDouble(packet.anchorZ);
        buf.writeFloat(packet.yawDegrees);
        buf.writeVarInt(packet.durationTicks);
    }

    public static DragonWardApparitionPacket decode(FriendlyByteBuf buf) {
        return new DragonWardApparitionPacket(buf.readBoolean(), buf.readVarInt(), buf.readDouble(),
                buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readVarInt());
    }

    public static void handle(DragonWardApparitionPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientDragonRelicEffects.startWardApparition(packet.followEntity, packet.entityId,
                        new Vec3(packet.anchorX, packet.anchorY, packet.anchorZ), packet.yawDegrees,
                        packet.durationTicks)));
        context.setPacketHandled(true);
    }
}
