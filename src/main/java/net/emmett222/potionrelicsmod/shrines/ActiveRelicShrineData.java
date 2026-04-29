package net.emmett222.potionrelicsmod.shrines;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public class ActiveRelicShrineData extends SavedData {
    private static final String DATA_NAME = "potionrelicsmod_active_relic_shrines";
    private static final String POSITIONS_TAG = "ShrinePositions";

    private final Set<Long> shrinePositions = new LinkedHashSet<>();

    public static ActiveRelicShrineData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ActiveRelicShrineData::load, ActiveRelicShrineData::new,
                DATA_NAME);
    }

    public static ActiveRelicShrineData load(CompoundTag tag) {
        ActiveRelicShrineData data = new ActiveRelicShrineData();
        for (long position : tag.getLongArray(POSITIONS_TAG)) {
            data.shrinePositions.add(position);
        }
        return data;
    }

    public void add(BlockPos shrinePos) {
        if (shrinePositions.add(shrinePos.asLong())) {
            setDirty();
        }
    }

    public void remove(BlockPos shrinePos) {
        if (shrinePositions.remove(shrinePos.asLong())) {
            setDirty();
        }
    }

    public List<BlockPos> getShrinePositions() {
        return shrinePositions.stream()
                .map(BlockPos::of)
                .toList();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putLongArray(POSITIONS_TAG, shrinePositions.stream()
                .mapToLong(Long::longValue)
                .toArray());
        return tag;
    }
}
