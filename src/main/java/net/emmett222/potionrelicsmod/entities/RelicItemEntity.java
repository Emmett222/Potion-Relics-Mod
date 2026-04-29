package net.emmett222.potionrelicsmod.entities;

import net.emmett222.potionrelicsmod.shrines.RelicShrineManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class RelicItemEntity extends ItemEntity {
    private boolean playerPickupInProgress;
    private boolean shrineHandled;

    public RelicItemEntity(ItemEntity itemEntity) {
        super(itemEntity.level(), itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), itemEntity.getItem().copy());
        setDeltaMovement(itemEntity.getDeltaMovement());
        setPickUpDelay(10);
        setNoGravity(itemEntity.isNoGravity());
        setInvulnerable(itemEntity.isInvulnerable());
    }

    @Override
    public void playerTouch(Player pPlayer) {
        playerPickupInProgress = true;
        super.playerTouch(pPlayer);

        if (isAlive()) {
            playerPickupInProgress = false;
        }
    }

    @Override
    public void remove(Entity.RemovalReason pReason) {
        if (!level().isClientSide && !shrineHandled && !playerPickupInProgress && shouldSpawnShrine(pReason)) {
            shrineHandled = true;
            ItemStack relic = getItem().copy();

            if (level() instanceof ServerLevel serverLevel) {
                RelicShrineManager.spawnShrineForDestroyedRelic(serverLevel.getServer(), relic);
            }
        }

        super.remove(pReason);
    }

    private boolean shouldSpawnShrine(Entity.RemovalReason reason) {
        return reason == Entity.RemovalReason.DISCARDED || reason == Entity.RemovalReason.KILLED;
    }
}
