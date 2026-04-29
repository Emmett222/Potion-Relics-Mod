package net.emmett222.potionrelicsmod.items.relics;

import java.util.List;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Invisibility Relic. Cloaks the player while enabled in inventory, then rewards
 * actions that break the cloak.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class InvisibilityRelic extends BaseRelic {
    private static final int CLOAK_EFFECT_DURATION = 40;
    private static final String INVISIBILITY_DATA_TAG = "InvisibilityRelic";
    private static final String CLOAKED_TAG = "Cloaked";
    private static final String PVP_DISABLED_UNTIL_TAG = "PvpDisabledUntil";
    private static final String REVEALED_UNTIL_TAG = "RevealedUntil";

    /**
     * Explicit constructor.
     * Sets the effect to Invisibility 1 and does not allow offhand upgrading.
     *
     * @param pProperties The pProperties to be used.
     */
    public InvisibilityRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.INVISIBILITY,
                CLOAK_EFFECT_DURATION,
                "tooltip.potionrelicsmod.invisibilityrelic");
    }

    /**
     * Returns whether the relic is actively cloaking the player.
     *
     * @param living The living entity to check.
     * @return True if the relic cloak effect is active.
     */
    public static boolean isRelicActive(LivingEntity living) {
        return hasRelicInvisibilityEffect(living);
    }

    public static void markHitByPlayer(Player player) {
        setPvpDisabledUntil(player, player.level().getGameTime() + ModConfigs.invisibilityPvpLockoutTicks);
        stopCloak(player);
    }

    public static boolean isDisabledByRecentPlayerHit(LivingEntity living) {
        if (!(living instanceof Player player)) {
            return false;
        }

        return getPvpDisabledUntil(player) > player.level().getGameTime();
    }

    /**
     * Handles the first attack made from cloak.
     *
     * @param player The attacking player.
     * @param target The attacked entity.
     * @return True if a shadow strike was triggered.
     */
    public static boolean tryShadowStrike(Player player, Entity target) {
        if (!isRelicActive(player)) {
            return false;
        }

        breakCloak(player, true);
        if (target instanceof LivingEntity living && living != player) {
            living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS,
                    ModConfigs.invisibilityStrikeDebuffTicks, 0, true, false));
            living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                    ModConfigs.invisibilityStrikeDebuffTicks, 0, true, false));
        }

        return true;
    }

    /**
     * Reveals a cloaked player after an item, block, or entity interaction.
     *
     * @param player The player taking the action.
     */
    public static void revealFromAction(Player player) {
        if (isRelicActive(player)) {
            breakCloak(player, true);
        }
    }

    private static boolean isInvisibilityRelic(ItemStack stack) {
        return stack.getItem() instanceof InvisibilityRelic;
    }

    private static boolean hasActiveInvisibilityRelic(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isInvisibilityRelic(stack) && BaseRelic.isEnabled(stack)) {
                return true;
            }
        }

        return false;
    }

    private static boolean hasRelicInvisibilityEffect(LivingEntity living) {
        MobEffectInstance currentEffect = living.getEffect(MobEffects.INVISIBILITY);
        return currentEffect != null
                && currentEffect.isAmbient()
                && !currentEffect.isVisible()
                && currentEffect.getAmplifier() == ModConfigs.invisibilityLevel;
    }

    /**
     * Ticks the cloak state for a player.
     * 
     * @param player The player to update.
     */
    public static void tickPlayer(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        if (!hasActiveInvisibilityRelic(player) || isDisabledByRecentPlayerHit(player) || isRevealed(player)) {
            stopCloak(player);
            return;
        }

        if (player.isSprinting() || player.isUsingItem() || player.isFallFlying() || player.isPassenger()) {
            if (isCloaked(player)) {
                breakCloak(player, true);
            } else {
                stopCloak(player);
            }
            return;
        }

        MobEffectInstance currentEffect = player.getEffect(MobEffects.INVISIBILITY);
        if (currentEffect == null
                || currentEffect.getAmplifier() < ModConfigs.invisibilityLevel
                || currentEffect.getDuration() <= CLOAK_EFFECT_DURATION / 2
                || currentEffect.isVisible()) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, CLOAK_EFFECT_DURATION,
                    ModConfigs.invisibilityLevel, true, false));
        }

        setCloaked(player, true);
    }

    /**
     * Cloak is managed from the player tick so it can be cleared when the relic is
     * moved or dropped.
     */
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
    }

    private static boolean isRevealed(Player player) {
        return getInvisibilityData(player).getLong(REVEALED_UNTIL_TAG) > player.level().getGameTime();
    }

    private static void breakCloak(Player player, boolean grantShadowStep) {
        setRevealedUntil(player, player.level().getGameTime() + ModConfigs.invisibilityRevealTicks);
        if (grantShadowStep && ModConfigs.invisibilityShadowStepTicks > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,
                    ModConfigs.invisibilityShadowStepTicks,
                    ModConfigs.invisibilityShadowStepLevel, true, false));
        }

        stopCloak(player);
    }

    private static void stopCloak(Player player) {
        setCloaked(player, false);
        clearRelicInvisibility(player);
    }

    private static void clearRelicInvisibility(LivingEntity living) {
        MobEffectInstance currentEffect = living.getEffect(MobEffects.INVISIBILITY);
        if (currentEffect == null) {
            return;
        }

        if (currentEffect.isAmbient()
                && !currentEffect.isVisible()
                && currentEffect.getAmplifier() == ModConfigs.invisibilityLevel
                && currentEffect.getDuration() <= CLOAK_EFFECT_DURATION) {
            living.removeEffect(MobEffects.INVISIBILITY);
        }
    }

    private static long getPvpDisabledUntil(Player player) {
        return getInvisibilityData(player).getLong(PVP_DISABLED_UNTIL_TAG);
    }

    private static void setPvpDisabledUntil(Player player, long gameTime) {
        CompoundTag invisibilityData = getInvisibilityData(player);
        invisibilityData.putLong(PVP_DISABLED_UNTIL_TAG, gameTime);
        saveInvisibilityData(player, invisibilityData);
    }

    private static void setRevealedUntil(Player player, long gameTime) {
        CompoundTag invisibilityData = getInvisibilityData(player);
        invisibilityData.putLong(REVEALED_UNTIL_TAG, gameTime);
        saveInvisibilityData(player, invisibilityData);
    }

    private static boolean isCloaked(Player player) {
        return getInvisibilityData(player).getBoolean(CLOAKED_TAG);
    }

    private static void setCloaked(Player player, boolean cloaked) {
        CompoundTag invisibilityData = getInvisibilityData(player);
        invisibilityData.putBoolean(CLOAKED_TAG, cloaked);
        saveInvisibilityData(player, invisibilityData);
    }

    private static CompoundTag getInvisibilityData(Player player) {
        CompoundTag persistedData = player.getPersistentData()
                .getCompound(Player.PERSISTED_NBT_TAG);
        return persistedData.getCompound(INVISIBILITY_DATA_TAG);
    }

    private static void saveInvisibilityData(Player player, CompoundTag invisibilityData) {
        CompoundTag rootData = player.getPersistentData();
        CompoundTag persistedData = rootData.getCompound(Player.PERSISTED_NBT_TAG);
        persistedData.put(INVISIBILITY_DATA_TAG, invisibilityData);
        rootData.put(Player.PERSISTED_NBT_TAG, persistedData);
    }

    /**
     * Returns the Invisibility level from the configs.
     * 
     * @return The Invisibility level denoted in config.
     */
    @Override
    protected int getConfigAmplifier() {
        return ModConfigs.invisibilityLevel;
    }

    /**
     * Returns if the Invisibility Relic can upgrade in offhand from the configs.
     * 
     * @return True if it can upgrade, false otherwise.
     */
    @Override
    protected boolean getConfigCanUpgrade() {
        return false;
    }

    /**
     * Returns if the Invisibility Relic shows particles around the player.
     * 
     * @return True if it shows particles, false otherwise.
     */
    @Override
    protected boolean getConfigShowParticles() {
        return false;
    }

    /**
     * Sets the tooltip for the inventory-based invisibility relic.
     * 
     * @param pStack             The pStack to be used.
     * @param pLevel             The pLevel to be used.
     * @param pTooltipComponents The tooltip components to be updated.
     * @param pIsAdvanced        Whether advanced tooltips are shown.
     */
    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
            TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        pTooltipComponents.clear();
        pTooltipComponents.add(Component.translatable(tooltip).withStyle(ChatFormatting.ITALIC));
        pTooltipComponents.add(Component.empty());
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.inventory")
                .withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.invisibilityrelic_cloak")
                .withStyle(ChatFormatting.DARK_GREEN));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.invisibilityrelic_shadow_step")
                .withStyle(ChatFormatting.DARK_GREEN));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.invisibilityrelic_shadow_strike")
                .withStyle(ChatFormatting.DARK_GREEN));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.invisibilityrelic_lockout")
                .withStyle(ChatFormatting.DARK_GRAY));
        addToggleTooltip(pStack, pTooltipComponents);
    }
}
