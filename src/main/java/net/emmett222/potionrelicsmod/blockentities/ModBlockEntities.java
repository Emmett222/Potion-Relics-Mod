package net.emmett222.potionrelicsmod.blockentities;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.blocks.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister
            .create(ForgeRegistries.BLOCK_ENTITY_TYPES, PotionRelicsMod.MOD_ID);

    public static final RegistryObject<BlockEntityType<RelicShrineBlockEntity>> RELIC_SHRINE = BLOCK_ENTITIES
            .register("relic_shrine",
                    () -> BlockEntityType.Builder.of(RelicShrineBlockEntity::new, ModBlocks.RELIC_SHRINE.get())
                            .build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
