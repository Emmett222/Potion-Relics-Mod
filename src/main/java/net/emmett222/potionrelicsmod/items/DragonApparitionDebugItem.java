package net.emmett222.potionrelicsmod.items;

import net.emmett222.potionrelicsmod.network.DragonWardApparitionPacket;
import net.emmett222.potionrelicsmod.network.ModMessages;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class DragonApparitionDebugItem extends Item {
    private static final int DEFAULT_DURATION_TICKS = 160;

    public DragonApparitionDebugItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        Vec3 anchor = Vec3.atCenterOf(context.getClickedPos()).add(0.0d, 0.5d, 0.0d);
        ModMessages.sendToPlayer(serverPlayer,
                DragonWardApparitionPacket.forPosition(anchor, player.getYRot(), DEFAULT_DURATION_TICKS));
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity interactionTarget,
            InteractionHand usedHand) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.sidedSuccess(player.level().isClientSide);
        }

        ModMessages.sendToPlayer(serverPlayer,
                DragonWardApparitionPacket.forEntity(interactionTarget, DEFAULT_DURATION_TICKS));
        return InteractionResult.CONSUME;
    }
}
