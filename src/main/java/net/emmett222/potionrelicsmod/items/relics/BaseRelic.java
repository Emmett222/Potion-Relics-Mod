package net.emmett222.potionrelicsmod.items.relics;

import java.util.List;

import org.antlr.v4.Tool;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
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
    String tooltip;

    /**
     * Explicit constructor.
     * 
     * @param pProperties The pProperties to be used.
     * @param effect The effect to be given.
     * @param tooltip The item tooltip to be used.
     */
    public BaseRelic(Properties pProperties, MobEffect effect, String tooltip) {
        super(pProperties);
        this.effect = effect;
        this.tooltip = tooltip;
    }

    /**
     * Called each tick as long as the relic is in the inventory.
     * Gives the effect, if the item holder is a player of course.
     * 
     * @param pStack The ItemStack to be used. Not used in the override.
     * @param pLevel The pLevel to be used. Not used in the override.
     * @param pEntity The pEntity to be used.
     * @param pSlotId The location in inventory to be used. Not used in the override.
     * @param pIsSelected If the item is selected or not. Not used in the override.
     */
    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        // If the entity is not a player, do nothing to it.
        if (pEntity.getType() != EntityType.PLAYER) {
            return;
        }

        if (pEntity instanceof LivingEntity living) {
            MobEffectInstance MEI = new MobEffectInstance(effect, 300, 0, true, false);
            living.addEffect(MEI);
        }
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
            TooltipFlag pIsAdvanced) {
        super.appendHoverText(pStack, pLevel, pTooltipComponents, pIsAdvanced);

        Component desComponent = Component.translatable(tooltip)
            .withStyle(ChatFormatting.ITALIC);
        pTooltipComponents.add(desComponent);
    }
}
