package net.emmett222.potionrelicsmod.items;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.items.relics.*;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
        DeferredRegister.create(ForgeRegistries.ITEMS, PotionRelicsMod.MOD_ID);

    public static final RegistryObject<Item> NIGHTVISIONRELIC = ITEMS.register("nightvisionrelic", 
        () -> new NightVisionRelic(new Item.Properties()));

    public static final RegistryObject<Item> STRENGTHRELIC = ITEMS.register("strengthrelic", 
        () -> new StrengthRelic(new Item.Properties()));

    public static final RegistryObject<Item> DOLPHINSGRACERELIC = ITEMS.register("dolphinsgracerelic", 
        () -> new DolphinsGraceRelic(new Item.Properties()));
    
    public static final RegistryObject<Item> INVISIBILITYRELIC = ITEMS.register("invisibilityrelic", 
        () -> new InvisibilityRelic(new Item.Properties()));
    
    public static final RegistryObject<Item> ABSORPTIONRELIC = ITEMS.register("absorptionrelic", 
        () -> new AbsorptionRelic(new Item.Properties()));

    public static final RegistryObject<Item> REGENERATIONRELIC = ITEMS.register("regenerationrelic", 
        () -> new RegenerationRelic(new Item.Properties()));

    public static final RegistryObject<Item> TABICON = ITEMS.register("tabicon", 
        () -> new Item(new Item.Properties().stacksTo(1)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
