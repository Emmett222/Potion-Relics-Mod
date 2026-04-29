package net.emmett222.potionrelicsmod.blocks.custom;

import net.emmett222.potionrelicsmod.blockentities.RelicShrineBlockEntity;
import net.emmett222.potionrelicsmod.shrines.RelicShrineManager;
import net.emmett222.potionrelicsmod.shrines.RelicShrineTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class RelicShrineBlock extends BaseEntityBlock implements EntityBlock {
    public static final EnumProperty<RelicShrineTheme> THEME = EnumProperty.create("theme", RelicShrineTheme.class);

    public RelicShrineBlock(Properties pProperties) {
        super(pProperties);
        registerDefaultState(stateDefinition.any().setValue(THEME, RelicShrineTheme.ABSORPTION));
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer,
            InteractionHand pHand, BlockHitResult pHit) {
        if (pLevel.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        return tryClaimShrine((ServerLevel) pLevel, pPos, pPlayer) ? InteractionResult.CONSUME : InteractionResult.FAIL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()
                && pLevel.getBlockEntity(pPos) instanceof RelicShrineBlockEntity shrine) {
            ItemStack relic = shrine.removeRelic();
            if (!relic.isEmpty()) {
                Containers.dropItemStack(pLevel, pPos.getX() + 0.5d, pPos.getY() + 0.5d, pPos.getZ() + 0.5d, relic);
            }

            if (!pLevel.isClientSide) {
                RelicShrineManager.clearShrineDecorations((ServerLevel) pLevel, pPos, pState);
            }
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public float getShadeBrightness(BlockState pState, BlockGetter pLevel, BlockPos pPos) {
        return 1.0f;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new RelicShrineBlockEntity(pPos, pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(THEME);
    }

    public static boolean tryClaimShrine(ServerLevel level, BlockPos shrinePos, Player player) {
        if (!(level.getBlockEntity(shrinePos) instanceof RelicShrineBlockEntity shrine)) {
            return false;
        }

        ItemStack relic = shrine.getRelic();
        if (relic.isEmpty()) {
            RelicShrineManager.clearShrine(level, shrinePos);
            return true;
        }

        if (!player.getInventory().add(relic.copy())) {
            player.displayClientMessage(Component.translatable("message.potionrelicsmod.relic_shrine_inventory_full"),
                    true);
            return false;
        }

        ItemStack claimedRelic = shrine.removeRelic();
        RelicShrineManager.clearShrine(level, shrinePos);
        RelicShrineManager.broadcastClaim(level, shrinePos, player, claimedRelic);
        player.displayClientMessage(
                Component.translatable("message.potionrelicsmod.relic_shrine_claimed", claimedRelic.getHoverName()),
                true);
        return true;
    }
}
