package net.emmett222.potionrelicsmod.items.relics;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Saturation Relic. Gives the player constant Saturation if in the inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class SaturationRelic extends BaseRelic {
    private static final int DEFAULT_FOOD_REFILL_INTERVAL = 40;

    /**
     * Explicit constructor.
     * Sets the effect to Saturation 1 and allows offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public SaturationRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.SATURATION,
                DEFAULT_FOOD_REFILL_INTERVAL,
                "tooltip.potionrelicsmod.saturationrelic");
    }

    /**
     * Called each tick as long as the relic is in the inventory.
     * Restores hunger directly instead of applying vanilla Saturation so the relic
     * no longer creates saturated-regeneration abuse.
     * 
     * @param pStack      The ItemStack to be used.
     * @param pLevel      The level.
     * @param pEntity     The entity carrying the relic.
     * @param pSlotId     The slot id. Not used in the override.
     * @param pIsSelected If the stack is selected. Not used in the override.
     */
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pLevel.isClientSide || pEntity.getType() != EntityType.PLAYER) {
            return;
        }

        if (pEntity instanceof Player player) {
            int foodLevel = player.getFoodData().getFoodLevel();
            if (foodLevel >= 20 || player.getCooldowns().isOnCooldown(this)) {
                return;
            }

            int foodRestore = getConfigAmplifier() + 1;
            if (pStack == player.getOffhandItem() && getConfigCanUpgrade()) {
                foodRestore++;
            }

            player.getFoodData().setFoodLevel(Math.min(20, foodLevel + foodRestore));
            player.getCooldowns().addCooldown(this, ModConfigs.saturationRefillInterval);
        }
    }

    /**
     * Returns the Saturation level from the configs.
     * 
     * @return The Saturation level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.saturationLevel;
    }

    /**
     * Returns if the Saturation Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return ModConfigs.saturationUpgrade;
    }

    /**
     * Returns if the Saturation Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return ModConfigs.saturationParticles;
    }
}
