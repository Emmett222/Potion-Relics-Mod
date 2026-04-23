package net.emmett222.potionrelicsmod.network;

import java.util.function.Supplier;

import net.emmett222.potionrelicsmod.items.relics.BaseRelic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Toggles a relic from an inventory screen.
 * 
 * @author Emmett Grebe
 * @version 4-22-2026
 */
public class ToggleRelicPacket {
    private final int slotId;

    public ToggleRelicPacket(int slotId) {
        this.slotId = slotId;
    }

    public static void encode(ToggleRelicPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.slotId);
    }

    public static ToggleRelicPacket decode(FriendlyByteBuf buf) {
        return new ToggleRelicPacket(buf.readVarInt());
    }

    public static void handle(ToggleRelicPacket packet, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || !player.containerMenu.isValidSlotIndex(packet.slotId)) {
                return;
            }

            Slot slot = player.containerMenu.getSlot(packet.slotId);
            if (!(slot.container instanceof Inventory) || slot.container != player.getInventory()) {
                return;
            }

            ItemStack stack = slot.getItem();
            if (!BaseRelic.isRelic(stack)) {
                return;
            }

            BaseRelic.toggleEnabled(stack, player);
            slot.setChanged();
            player.getInventory().setChanged();
            player.containerMenu.broadcastChanges();
        });
        context.setPacketHandled(true);
    }
}
