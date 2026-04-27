package net.emmett222.potionrelicsmod.items;

import java.util.List;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.items.relics.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS,
            PotionRelicsMod.MOD_ID);

    private static Item.Properties relicProperties() {
        return new Item.Properties().stacksTo(1);
    }

    public static final RegistryObject<Item> ABSORPTIONRELIC = ITEMS.register("absorptionrelic",
            () -> new AbsorptionRelic(relicProperties()));

    public static final RegistryObject<Item> DOLPHINSGRACERELIC = ITEMS.register("dolphinsgracerelic",
            () -> new DolphinsGraceRelic(relicProperties()));

    public static final RegistryObject<Item> DRAGONAPPARITIONDEBUG = ITEMS.register("dragonapparitiondebug",
            () -> new DragonApparitionDebugItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> DRAGONRELIC = ITEMS.register("dragonrelic",
            () -> new DragonRelic(relicProperties()));

    public static final RegistryObject<Item> FIRERESISTANCERELIC = ITEMS.register("fireresistancerelic",
            () -> new FireResistanceRelic(relicProperties()));

    public static final RegistryObject<Item> HASTERELIC = ITEMS.register("hasterelic",
            () -> new HasteRelic(relicProperties()));

    public static final RegistryObject<Item> HEROOFTHEVILLAGERELIC = ITEMS.register("heroofthevillagerelic",
            () -> new HeroOfTheVillageRelic(relicProperties()));

    public static final RegistryObject<Item> INVISIBILITYRELIC = ITEMS.register("invisibilityrelic",
            () -> new InvisibilityRelic(relicProperties()));

    public static final RegistryObject<Item> JUMPBOOSTRELIC = ITEMS.register("jumpboostrelic",
            () -> new JumpBoostRelic(relicProperties()));

    public static final RegistryObject<Item> NIGHTVISIONRELIC = ITEMS.register("nightvisionrelic",
            () -> new NightVisionRelic(relicProperties()));

    public static final RegistryObject<Item> REGENERATIONRELIC = ITEMS.register("regenerationrelic",
            () -> new RegenerationRelic(relicProperties()));

    public static final RegistryObject<Item> RESISTANCERELIC = ITEMS.register("resistancerelic",
            () -> new ResistanceRelic(relicProperties()));

    public static final RegistryObject<Item> SATURATIONRELIC = ITEMS.register("saturationrelic",
            () -> new SaturationRelic(relicProperties()));

    public static final RegistryObject<Item> SLOWFALLINGRELIC = ITEMS.register("slowfallingrelic",
            () -> new SlowFallingRelic(relicProperties()));

    public static final RegistryObject<Item> STRENGTHRELIC = ITEMS.register("strengthrelic",
            () -> new StrengthRelic(relicProperties()));

    public static final RegistryObject<Item> SWIFTNESSRELIC = ITEMS.register("swiftnessrelic",
            () -> new SwiftnessRelic(relicProperties()));

    public static final RegistryObject<Item> WATERBREATHINGRELIC = ITEMS.register("waterbreathingrelic",
            () -> new WaterBreathingRelic(relicProperties()));

            

    public static final RegistryObject<Item> TABICON = ITEMS.register("tabicon",
            () -> new Item(new Item.Properties().stacksTo(1)));

    public static List<RegistryObject<Item>> getRelics() {
        return List.of(
                ABSORPTIONRELIC,
                DOLPHINSGRACERELIC,
                DRAGONRELIC,
                FIRERESISTANCERELIC,
                HASTERELIC,
                HEROOFTHEVILLAGERELIC,
                INVISIBILITYRELIC,
                JUMPBOOSTRELIC,
                NIGHTVISIONRELIC,
                REGENERATIONRELIC,
                RESISTANCERELIC,
                SATURATIONRELIC,
                SLOWFALLINGRELIC,
                STRENGTHRELIC,
                SWIFTNESSRELIC,
                WATERBREATHINGRELIC);
    }

    public static List<ItemStack> getDefaultRelicStacks() {
        return getRelics().stream()
                .map(relic -> new ItemStack(relic.get()))
                .toList();
    }

    public static List<ResourceLocation> getRelicIds() {
        return getRelics().stream()
                .map(RegistryObject::getId)
                .toList();
    }

    public static int getRelicCount() {
        return getRelics().size();
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
