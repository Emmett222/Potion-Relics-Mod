package net.emmett222.potionrelicsmod.events;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.commands.RelicShrineCommands;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModCommandEvents {
    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        RelicShrineCommands.register(event.getDispatcher());
    }
}
