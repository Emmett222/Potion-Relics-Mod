package net.emmett222.potionrelicsmod.items;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, PotionRelicsMod.MOD_ID);

    public static final RegistryObject<Item> NIGHTVISIONRELIC = ITEMS.register("nightvisionrelic", 
        () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
