package net.emmett222.potionrelicsmod.blockentities;

import java.util.UUID;

import net.emmett222.potionrelicsmod.blocks.custom.RelicShrineBlock;
import net.emmett222.potionrelicsmod.shrines.ActiveRelicShrineData;
import net.emmett222.potionrelicsmod.shrines.RelicShrineManager;
import net.emmett222.potionrelicsmod.shrines.RelicShrineTheme;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class RelicShrineBlockEntity extends BlockEntity {
    private static final String DISCOVERED_TAG = "Discovered";
    private static final String RELIC_TAG = "Relic";
    private static final String SHRINE_ID_TAG = "ShrineId";

    private boolean discovered;
    private ItemStack relic = ItemStack.EMPTY;
    private UUID shrineId = UUID.randomUUID();

    public RelicShrineBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.RELIC_SHRINE.get(), pPos, pBlockState);
    }

    public ItemStack getRelic() {
        return relic;
    }

    public UUID getShrineId() {
        if (shrineId == null) {
            shrineId = UUID.randomUUID();
        }

        return shrineId;
    }

    public boolean isDiscovered() {
        return discovered;
    }

    public void setDiscovered(boolean discovered) {
        this.discovered = discovered;
        setChanged();
    }

    public void setRelic(ItemStack stack) {
        relic = stack.copy();
        discovered = false;
        shrineId = UUID.randomUUID();
        syncTheme();
        setChanged();
    }

    public ItemStack removeRelic() {
        ItemStack removed = relic.copy();
        relic = ItemStack.EMPTY;
        setChanged();
        return removed;
    }

    @Override
    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);

        pTag.putBoolean(DISCOVERED_TAG, discovered);
        pTag.putUUID(SHRINE_ID_TAG, getShrineId());
        if (!relic.isEmpty()) {
            pTag.put(RELIC_TAG, relic.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag pTag) {
        super.load(pTag);
        discovered = pTag.getBoolean(DISCOVERED_TAG);
        relic = pTag.contains(RELIC_TAG) ? ItemStack.of(pTag.getCompound(RELIC_TAG)) : ItemStack.EMPTY;
        shrineId = pTag.hasUUID(SHRINE_ID_TAG) ? pTag.getUUID(SHRINE_ID_TAG) : UUID.randomUUID();
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void refreshClientState() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        setChanged();
        serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        syncTheme();

        if (level instanceof ServerLevel serverLevel && !relic.isEmpty()) {
            ActiveRelicShrineData.get(serverLevel).add(getBlockPos());
        }
    }

    private void syncTheme() {
        if (level == null || level.isClientSide || relic.isEmpty() || !getBlockState().hasProperty(RelicShrineBlock.THEME)) {
            return;
        }

        RelicShrineTheme theme = RelicShrineTheme.fromRelic(relic);
        if (getBlockState().getValue(RelicShrineBlock.THEME) != theme) {
            level.setBlock(getBlockPos(), getBlockState().setValue(RelicShrineBlock.THEME, theme), 3);
        }

        ActiveRelicShrineData.get((ServerLevel) level).add(getBlockPos());
        RelicShrineManager.refreshShrineDecorations((ServerLevel) level, getBlockPos(), relic);
    }
}
