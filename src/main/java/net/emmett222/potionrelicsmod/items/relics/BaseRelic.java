package net.emmett222.potionrelicsmod.items.relics;

import java.util.List;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Base relic class for the relics to extend.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
public abstract class BaseRelic extends Item {
    private static final String ENABLED_TAG = "Enabled";

    MobEffect effect;
    int duration;
    String tooltip;

    /**
     * Explicit constructor.
     * 
     * @param pProperties The pProperties to be used.
     * @param effect      The effect to be given.
     * @param duration    The duration of the managed effect window in ticks.
     * @param tooltip     The item tooltip to be used.
     */
    public BaseRelic(Properties pProperties, MobEffect effect, int duration, String tooltip) {
        super(pProperties);
        this.effect = effect;
        this.duration = duration;
        this.tooltip = tooltip;
    }

    /**
     * Returns the level of the effect.
     * 
     * @return The level of the effect.
     */
    protected abstract int getConfigAmplifier();

    /**
     * Returns if the Relic can be upgraded in the offhand.
     * 
     * @return True if it can be upgraded, false otherwise.
     */
    protected abstract boolean getConfigCanUpgrade();

    /**
     * Returns if the Relic shows particles.
     * 
     * @return True if it does show particles, false otherwise.
     */
    protected abstract boolean getConfigShowParticles();

    /**
     * Returns if the relic stack is enabled.
     * 
     * @param stack The stack to be checked.
     * @return True if enabled, false otherwise.
     */
    public static boolean isEnabled(ItemStack stack) {
        if (!ModConfigs.relicTogglingEnabled) {
            return true;
        }

        CompoundTag tag = stack.getTag();
        return tag == null || !tag.contains(ENABLED_TAG) || tag.getBoolean(ENABLED_TAG);
    }

    /**
     * Returns if the stack is a relic.
     * 
     * @param stack The stack to check.
     * @return True if it is a relic, false otherwise.
     */
    public static boolean isRelic(ItemStack stack) {
        return stack.getItem() instanceof BaseRelic;
    }

    /**
     * Sets if a relic is enabled.
     * 
     * @param stack   The stack to update.
     * @param enabled The enabled state to set.
     */
    public static void setEnabled(ItemStack stack, boolean enabled) {
        if (enabled) {
            CompoundTag tag = stack.getTag();
            if (tag != null) {
                tag.remove(ENABLED_TAG);
                if (tag.isEmpty()) {
                    stack.setTag(null);
                }
            }
            return;
        }

        stack.getOrCreateTag().putBoolean(ENABLED_TAG, false);
    }

    /**
     * Toggles a relic and notifies the player.
     * 
     * @param stack  The stack to toggle.
     * @param player The player toggling the stack.
     * @return The new enabled state.
     */
    public static boolean toggleEnabled(ItemStack stack, Player player) {
        boolean enabled = !isEnabled(stack);
        setEnabled(stack, enabled);

        if (player != null) {
            player.displayClientMessage(Component.translatable(
                    enabled ? "message.potionrelicsmod.relic_enabled" : "message.potionrelicsmod.relic_disabled",
                    stack.getHoverName()), true);
        }

        return enabled;
    }

    /**
     * Returns how low the active effect can get before the relic refreshes it.
     * 
     * @return The refresh threshold in ticks.
     */
    protected int getRefreshThresholdTicks() {
        return Math.max(10, duration / 2);
    }

    /**
     * Called each tick as long as the relic is in the inventory.
     * Gives the effect, if the item holder is a player of course.
     * 
     * @param pStack      The ItemStack to be used. Not used in the override.
     * @param pLevel      The pLevel to be used. Not used in the override.
     * @param pEntity     The pEntity to be used.
     * @param pSlotId     The location in inventory to be used. Not used in the
     *                    override.
     * @param pIsSelected If the item is selected or not. Not used in the override.
     */
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        // If the entity is not a player, do nothing to it.
        if (pLevel.isClientSide || !isEnabled(pStack) || pEntity.getType() != EntityType.PLAYER) {
            return;
        }

        if (pEntity instanceof LivingEntity living) {
            int amplifier = getConfigAmplifier();
            if ((pStack == living.getOffhandItem()) && getConfigCanUpgrade()) {
                amplifier++;
            }

            MobEffectInstance currentEffect = living.getEffect(effect);
            if (currentEffect != null
                    && currentEffect.getAmplifier() >= amplifier
                    && currentEffect.getDuration() > getRefreshThresholdTicks()) {
                return;
            }

            MobEffectInstance MEI = new MobEffectInstance(effect, duration, amplifier,
                    !getConfigShowParticles(), getConfigShowParticles());
            living.addEffect(MEI);
        }
    }

    /**
     * Toggles the relic while held.
     * 
     * @param pLevel The level.
     * @param pPlayer The player.
     * @param pUsedHand The used hand.
     * @return The interaction result.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (!ModConfigs.relicTogglingEnabled || !pPlayer.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!pLevel.isClientSide) {
            toggleEnabled(stack, pPlayer);
            pPlayer.getInventory().setChanged();
        }

        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide);
    }

    /**
     * Adds gold and bold to the relic name.
     * 
     * @param pStack Used to getName from super.
     */
    @Override
    public Component getName(ItemStack pStack) {
        MutableComponent newName = (MutableComponent) super.getName(pStack);
        newName.withStyle(ChatFormatting.GOLD);
        newName.withStyle(ChatFormatting.BOLD);
        return newName;
    }

    /**
     * Makes relic glow as if it is enchanted.
     * 
     * @param pStack Unused.
     */
    @Override
    public boolean isFoil(ItemStack pStack) {
        return !ModConfigs.relicTogglingEnabled || isEnabled(pStack);
    }

    /**
     * Adds toggle information to a relic tooltip.
     * 
     * @param pStack             The relic stack.
     * @param pTooltipComponents The tooltip components to update.
     */
    protected void addToggleTooltip(ItemStack pStack, List<Component> pTooltipComponents) {
        if (!ModConfigs.relicTogglingEnabled) {
            return;
        }

        Component statusComp = Component
                .translatable(isEnabled(pStack) ? "tooltip.potionrelicsmod.enabled" : "tooltip.potionrelicsmod.disabled")
                .withStyle(isEnabled(pStack) ? ChatFormatting.GREEN : ChatFormatting.RED);
        Component heldToggleComp = Component.translatable("tooltip.potionrelicsmod.toggle_held")
                .withStyle(ChatFormatting.DARK_GRAY);
        Component inventoryToggleComp = Component.translatable("tooltip.potionrelicsmod.toggle_inventory")
                .withStyle(ChatFormatting.DARK_GRAY);

        pTooltipComponents.add(Component.empty());
        pTooltipComponents.add(statusComp);
        pTooltipComponents.add(heldToggleComp);
        pTooltipComponents.add(inventoryToggleComp);
    }

    /**
     * Sets the tooltip and makes it italic.
     * 
     * @param pStack             The pStack to be used.
     * @param pLevel             the pLevel to be used.
     * @param pTooltipComponents The pTooltipComponents to be used. Will be updated
     *                           by this function.
     */
    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
            TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        Component desComp = Component.translatable(tooltip)
                .withStyle(ChatFormatting.ITALIC);
        Component inventoryComp = Component.translatable("tooltip.potionrelicsmod.inventory")
                .withStyle(ChatFormatting.GRAY);
        Component effectInvComp = Component
                .translatable(effect.getDisplayName().getString() + " " + (getConfigAmplifier() + 1))
                .withStyle(ChatFormatting.DARK_GREEN);

        pTooltipComponents.add(desComp);
        pTooltipComponents.add(Component.empty());
        pTooltipComponents.add(inventoryComp);
        pTooltipComponents.add(effectInvComp);

        if (getConfigCanUpgrade()) {
            Component offHandComp = Component.translatable("tooltip.potionrelicsmod.off_hand")
                    .withStyle(ChatFormatting.GRAY);
            Component effectOffHandComp = Component
                    .translatable(effect.getDisplayName().getString() + " " + (getConfigAmplifier() + 2))
                    .withStyle(ChatFormatting.DARK_GREEN);
            pTooltipComponents.add(offHandComp);
            pTooltipComponents.add(effectOffHandComp);
        }

        addToggleTooltip(pStack, pTooltipComponents);
    }
}
