package net.emmett222.potionrelicsmod.items.relics;

import java.util.List;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Invisibility Relic. Gives the player constant Invisibility if in the
 * inventory.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public class InvisibilityRelic extends BaseRelic {
    private static final int MAIN_HAND_DURATION = 40;

    /**
     * Explicit constructor.
     * Sets the effect to Invisibility 1 and does not allow offhand upgrading.
     * 
     * @param pProperties The pProperties to be used.
     */
    public InvisibilityRelic(Properties pProperties) {
        super(pProperties,
                MobEffects.INVISIBILITY,
                MAIN_HAND_DURATION,
                "tooltip.potionrelicsmod.invisibilityrelic");
    }

    /**
     * Returns whether the relic is actively granting invisibility.
     * 
     * @param living The living entity to check.
     * @return True if the relic conditions are met and invisibility is active.
     */
    public static boolean isRelicActive(LivingEntity living) {
        return hasActiveInvisibilityRelic(living) && living.hasEffect(MobEffects.INVISIBILITY);
    }

    /**
     * Returns whether the relic should hide the player model and held item.
     * 
     * @param living The living entity to check.
     * @return True if render hiding is enabled and the relic is active.
     */
    public static boolean shouldHidePlayer(LivingEntity living) {
        return ModConfigs.invisibilityHidePlayerRender && isRelicActive(living);
    }

    private static boolean isInvisibilityRelic(ItemStack stack) {
        return stack.getItem() instanceof InvisibilityRelic;
    }

    private static boolean hasActiveInvisibilityRelic(LivingEntity living) {
        if (ModConfigs.invisibilityRequiresMainHand) {
            return isInvisibilityRelic(living.getMainHandItem())
                    && BaseRelic.isEnabled(living.getMainHandItem());
        }

        if (!(living instanceof Player player)) {
            return false;
        }

        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (isInvisibilityRelic(stack) && BaseRelic.isEnabled(stack)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Called each tick as long as the relic is in the inventory.
     * Only grants invisibility while the relic is in the main hand.
     * 
     * @param pStack      The ItemStack to be used.
     * @param pLevel      The pLevel to be used.
     * @param pEntity     The pEntity to be used.
     * @param pSlotId     The location in inventory to be used. Not used in the
     *                    override.
     * @param pIsSelected If the item is selected or not. Not used in the override.
     */
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pLevel.isClientSide || pEntity.getType() != EntityType.PLAYER) {
            return;
        }

        if (pEntity instanceof LivingEntity living) {
            if (!BaseRelic.isEnabled(pStack)) {
                return;
            }

            if (ModConfigs.invisibilityRequiresMainHand && pStack != living.getMainHandItem()) {
                return;
            }

            MobEffectInstance currentEffect = living.getEffect(MobEffects.INVISIBILITY);
            if (currentEffect != null
                    && currentEffect.getDuration() > getRefreshThresholdTicks()
                    && !currentEffect.isVisible()) {
                return;
            }

            MobEffectInstance effect = new MobEffectInstance(MobEffects.INVISIBILITY, MAIN_HAND_DURATION,
                    getConfigAmplifier(), true, false);
            living.addEffect(effect);
        }
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
     * Sets the tooltip for the main-hand-only invisibility relic.
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
        pTooltipComponents.add(Component.translatable(
                ModConfigs.invisibilityRequiresMainHand ? "tooltip.potionrelicsmod.main_hand"
                        : "tooltip.potionrelicsmod.inventory")
                .withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable(effect.getDisplayName().getString() + " " + (getConfigAmplifier() + 1))
                .withStyle(ChatFormatting.DARK_GREEN));
        if (ModConfigs.invisibilityHidePlayerRender) {
            pTooltipComponents.add(
                    Component.translatable("tooltip.potionrelicsmod.invisibilityrelic_hidden").withStyle(ChatFormatting.DARK_GRAY));
        }
        addToggleTooltip(pStack, pTooltipComponents);
    }
}
