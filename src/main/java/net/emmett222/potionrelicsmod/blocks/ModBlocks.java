package net.emmett222.potionrelicsmod.blocks;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.blocks.custom.RelicShrineBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS,
            PotionRelicsMod.MOD_ID);

    public static final RegistryObject<Block> RELIC_SHRINE = BLOCKS.register("relic_shrine",
            () -> new RelicShrineBlock(BlockBehaviour.Properties.copy(Blocks.CRYING_OBSIDIAN)
                    .strength(4.0f, 6.0f)
                    .lightLevel(state -> 7)
                    .sound(SoundType.AMETHYST)
                    .noOcclusion()));

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
