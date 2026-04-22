package net.emmett222.potionrelicsmod.events;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;

/**
 * Handles putting the config information into a cache for quick use.
 */
@Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModConfigEvents {
    @SubscribeEvent
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        ModConfigs.cacheConfigs();
    }
}
