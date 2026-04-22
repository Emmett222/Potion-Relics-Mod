package net.emmett222.potionrelicsmod.items.relics;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * Base relic class for the relics to extend.
 * 
 * @author Emmett Grebe
 * @version 4-19-2026
 */
public abstract class BaseRelic extends Item {

    MobEffect effect;
    int duration;
    String tooltip;
    int amplifier;
    boolean canUpgrade;
    boolean showSwirls;

    /**
     * Explicit constructor.
     * 
     * @param pProperties The pProperties to be used.
     * @param effect      The effect to be given.
     * @param duration    the duration of the effect. Some effects work incorrectly
     *                    with shorter durations. Must be higher than 70 to work.
     * @param tooltip     The item tooltip to be used.
     * @param amplifier   The level of effect to be used. Effects are 1 less than
     *                    what they should be.
     * @param canUprade   True if the effect can have extra effect in the offhand,
     *                    false otherwise.
     * @param showSwirls  True if the relic shows effect swirls, false otherwise.
     */
    public BaseRelic(Properties pProperties, MobEffect effect, int duration, String tooltip, int amplifier,
            boolean canUpgrade, boolean showSwirls) {
        super(pProperties);
        this.effect = effect;
        this.duration = duration;
        this.tooltip = tooltip;
        this.amplifier = getConfigAmplifier();
        this.canUpgrade = getConfigCanUpgrade();
        this.showSwirls = getConfigShowParticles();
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
        if (pEntity.getType() != EntityType.PLAYER) {
            return;
        }

        if (pEntity instanceof LivingEntity living) {
            if ((pStack == living.getOffhandItem()) && (canUpgrade)) {
                // If in offhand, give an extra 1 to the amplifier.
                if (living.getEffect(effect) != null) {
                    if (living.getEffect(effect).getDuration() > duration - 60)
                        return; // If player already has the effect.
                }

                MobEffectInstance MEI = new MobEffectInstance(effect, duration, amplifier + 1,
                        !showSwirls, showSwirls);
                living.addEffect(MEI);
            } else {
                // Any other slot, just do amplifier.
                if (living.getEffect(effect) != null) {
                    if (living.getEffect(effect).getDuration() > duration - 60)
                        return; // If player already has the effect.
                }

                MobEffectInstance MEI = new MobEffectInstance(effect, duration, amplifier,
                        !showSwirls, showSwirls);
                living.addEffect(MEI);
            }
        }
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
        return true;
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
        Component inventoryComp = Component.translatable("tooltip.potionrelicsmod.inventory",
                effect.getDisplayName().getString(),
                (amplifier + 1))
                .withStyle(ChatFormatting.GRAY);

        pTooltipComponents.add(desComp);
        pTooltipComponents.add(Component.empty());
        pTooltipComponents.add(inventoryComp);

        if (canUpgrade) {
            Component offHandComp = Component.translatable("tooltip.potionrelicsmod.off_hand",
                effect.getDisplayName().getString(),
                (amplifier + 2))
                .withStyle(ChatFormatting.GRAY);
            pTooltipComponents.add(offHandComp);
        }
    }
}
