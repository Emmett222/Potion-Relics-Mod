package net.emmett222.potionrelicsmod.shrines;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;

public class PendingRelicShrineData extends SavedData {
    private static final String DATA_NAME = "potionrelicsmod_pending_relic_shrines";
    private static final String RELICS_TAG = "PendingRelics";

    private final List<ItemStack> pendingRelics = new ArrayList<>();

    public static PendingRelicShrineData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(PendingRelicShrineData::load, PendingRelicShrineData::new,
                DATA_NAME);
    }

    public static PendingRelicShrineData load(CompoundTag tag) {
        PendingRelicShrineData data = new PendingRelicShrineData();
        ListTag relicsTag = tag.getList(RELICS_TAG, Tag.TAG_COMPOUND);

        for (int i = 0; i < relicsTag.size(); i++) {
            ItemStack relic = ItemStack.of(relicsTag.getCompound(i));
            if (!relic.isEmpty()) {
                data.pendingRelics.add(relic);
            }
        }

        return data;
    }

    public void add(ItemStack relic) {
        pendingRelics.add(relic.copy());
        setDirty();
    }

    public boolean removeFirstMatching(ItemStack relic) {
        for (int i = 0; i < pendingRelics.size(); i++) {
            if (ItemStack.isSameItemSameTags(pendingRelics.get(i), relic)) {
                pendingRelics.remove(i);
                setDirty();
                return true;
            }
        }

        return false;
    }

    public List<ItemStack> getPendingRelics() {
        return pendingRelics.stream()
                .map(ItemStack::copy)
                .toList();
    }

    public boolean isEmpty() {
        return pendingRelics.isEmpty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag relicsTag = new ListTag();
        for (ItemStack relic : pendingRelics) {
            relicsTag.add(relic.save(new CompoundTag()));
        }

        tag.put(RELICS_TAG, relicsTag);
        return tag;
    }
}
