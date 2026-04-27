package net.emmett222.potionrelicsmod.events;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.items.relics.InvisibilityRelic;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side rendering hooks for relic visuals.
 * 
 * @author Emmett Grebe
 * @version 4-22-2026
 */
@Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientRenderEvents {

    /**
     * Hides the first-person hand/item render while the invisibility relic is active.
     * 
     * @param event The render hand event.
     */
    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (Minecraft.getInstance().player != null && InvisibilityRelic.shouldHidePlayer(Minecraft.getInstance().player)) {
            event.setCanceled(true);
        }
    }

    /**
     * Hides the first-person arm render while the invisibility relic is active.
     * 
     * @param event The render arm event.
     */
    @SubscribeEvent
    public static void onRenderArm(RenderArmEvent event) {
        if (InvisibilityRelic.shouldHidePlayer(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    /**
     * Hides the full player model in third-person while the invisibility relic is
     * active.
     * 
     * @param event The render player event.
     */
    @SubscribeEvent
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (InvisibilityRelic.shouldHidePlayer(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}
