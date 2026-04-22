package net.emmett222.potionrelicsmod.items;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
        DeferredRegister.create(Registries.CREATIVE_MODE_TAB, PotionRelicsMod.MOD_ID);

    public static final RegistryObject<CreativeModeTab> POTION_RELICS_TAB = CREATIVE_MODE_TABS.register("potion_relics_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.TABICON.get()))
        .title(Component.translatable("creativetab.potionrelicstab"))
        .displayItems((pParameters, pOutput) -> {
            pOutput.accept(ModItems.ABSORPTIONRELIC.get());
            pOutput.accept(ModItems.DOLPHINSGRACERELIC.get());
            pOutput.accept(ModItems.FIRERESISTANCERELIC.get());
            pOutput.accept(ModItems.HASTERELIC.get());
            pOutput.accept(ModItems.HEROOFTHEVILLAGERELIC.get());
            pOutput.accept(ModItems.INVISIBILITYRELIC.get());
            pOutput.accept(ModItems.NIGHTVISIONRELIC.get());
            pOutput.accept(ModItems.REGENERATIONRELIC.get());
            pOutput.accept(ModItems.RESISTANCERELIC.get());
            pOutput.accept(ModItems.SATURATIONRELIC.get());
            pOutput.accept(ModItems.STRENGTHRELIC.get());
            pOutput.accept(ModItems.SWIFTNESSRELIC.get());
            pOutput.accept(ModItems.WATERBREATHINGRELIC.get());
        })
        .build()
    );

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
