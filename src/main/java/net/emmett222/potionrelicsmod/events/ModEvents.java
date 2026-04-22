package net.emmett222.potionrelicsmod.events;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.emmett222.potionrelicsmod.items.ModItems;
import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.items.relics.AbsorptionRelic;
import net.emmett222.potionrelicsmod.items.relics.BaseRelic;
import net.emmett222.potionrelicsmod.items.relics.InvisibilityRelic;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Events of Potion Relics Mod. Handles all events.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
@Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    /**
     * This is here because of Absorption Relic cheats and glitches.
     * Without this, the hearts stay after losing the relic. This is a glitch, and
     * can be used as a cheat to pass around the relic and everyone keeps the
     * Absorption.
     * 
     * @param event The tick event.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        boolean hasRelic = hasAbsorptionRelic(player);
        float vanillaAbsorption = getVanillaAbsorptionAmount(player);
        float desiredAbsorption = vanillaAbsorption + ModConfigs.absorptionAmount;

        if (hasRelic) {
            if (player.getAbsorptionAmount() < desiredAbsorption
                    && !player.getCooldowns().isOnCooldown(ModItems.ABSORPTIONRELIC.get())) {
                player.setAbsorptionAmount(desiredAbsorption);
                player.getCooldowns().addCooldown(ModItems.ABSORPTIONRELIC.get(), ModConfigs.absorptionCooldown);
            }
            return;
        }

        if (player.getAbsorptionAmount() > vanillaAbsorption) {
            player.setAbsorptionAmount(vanillaAbsorption);
        }
    }

    private static boolean hasAbsorptionRelic(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof AbsorptionRelic
                    && BaseRelic.isEnabled(player.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private static float getVanillaAbsorptionAmount(Player player) {
        MobEffectInstance absorptionEffect = player.getEffect(MobEffects.ABSORPTION);
        if (absorptionEffect == null) {
            return 0.0f;
        }

        return 4.0f * (absorptionEffect.getAmplifier() + 1);
    }

    /**
     * Hides all potion particles while the invisibility relic is actively hiding a
     * player.
     * 
     * @param event The potion color calculation event.
     */
    @SubscribeEvent
    public static void onPotionColorCalculation(PotionColorCalculationEvent event) {
        if (event.getEntity() instanceof Player player && InvisibilityRelic.shouldHidePlayer(player)) {
            event.shouldHideParticles(true);
        }
    }
}
