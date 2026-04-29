package net.emmett222.potionrelicsmod.shrines;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.blockentities.RelicShrineBlockEntity;
import net.emmett222.potionrelicsmod.blocks.ModBlocks;
import net.emmett222.potionrelicsmod.blocks.custom.RelicShrineBlock;
import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.emmett222.potionrelicsmod.items.ModItems;
import net.emmett222.potionrelicsmod.items.relics.BaseRelic;
import net.emmett222.potionrelicsmod.network.CameraShakePacket;
import net.emmett222.potionrelicsmod.network.ModMessages;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class RelicShrineManager {
    private static final List<ClaimShockwave> ACTIVE_CLAIM_SHOCKWAVES = new ArrayList<>();
    private static final String PLAYER_DISCOVERIES_TAG = "PotionRelicsShrineDiscoveries";
    private static final int AMBIENT_PARTICLE_INTERVAL = 4;
    private static final int AMBIENT_PARTICLE_COUNT = 3;
    private static final float CLAIM_SHAKE_DURATION_TICKS = 28.0f;
    private static final float CLAIM_SHAKE_INTENSITY = 1.35f;
    private static final int CLAIM_SHOCKWAVE_DURATION_TICKS = 24;
    private static final int CLAIM_SHOCKWAVE_BASE_SAMPLES = 220;
    private static final double CLAIM_SHOCKWAVE_MAX_RADIUS = 50.0d;
    private static final double CLAIM_SHOCKWAVE_SHELL_HALF_THICKNESS = 0.35d;
    private static final int SHRINE_BURST_PARTICLES = 24;
    private static final int SHRINE_DISCOVERY_RADIUS = 24;
    private static final double SHRINE_DISCOVERY_RADIUS_SQR = SHRINE_DISCOVERY_RADIUS * SHRINE_DISCOVERY_RADIUS;
    private static final int SHRINE_RADIUS = 1;
    private static final int SCATTER_MIN_SEPARATION = 48;
    private static final int MAX_FOUNDATION_DEPTH = 6;
    private static final int PILLAR_HEIGHT = 2;
    private static final int SEARCH_EXPANSION = 128;
    private static final int SEARCH_PASSES = 3;
    private static final int SEARCH_GRID_STEP = 16;
    private static final int PENDING_RETRY_BATCH = 4;

    public static boolean placeShrine(ServerLevel level, BlockPos shrinePos, ItemStack relic) {
        RelicShrineTheme theme = RelicShrineTheme.fromRelic(relic);
        if (!ModConfigs.relicShrinesEnabled || relic.isEmpty() || !BaseRelic.isRelic(relic)
                || !canPlaceShrine(level, shrinePos, theme)) {
            return false;
        }

        if (!applyShrineDecorations(level, shrinePos, theme)) {
            return false;
        }

        level.setBlock(shrinePos, ModBlocks.RELIC_SHRINE.get().defaultBlockState().setValue(RelicShrineBlock.THEME, theme),
                Block.UPDATE_ALL);

        if (!(level.getBlockEntity(shrinePos) instanceof RelicShrineBlockEntity shrineBlockEntity)) {
            return false;
        }

        shrineBlockEntity.setRelic(relic.copy());
        ActiveRelicShrineData.get(level).add(shrinePos);
        level.sendBlockUpdated(shrinePos, level.getBlockState(shrinePos), level.getBlockState(shrinePos), Block.UPDATE_ALL);
        return true;
    }

    public static void refreshShrineDecorations(ServerLevel level, BlockPos shrinePos, ItemStack relic) {
        if (relic.isEmpty()) {
            return;
        }

        applyShrineDecorations(level, shrinePos, RelicShrineTheme.fromRelic(relic));
    }

    public static void clearShrine(ServerLevel level, BlockPos shrinePos) {
        BlockState shrineState = level.getBlockState(shrinePos);
        ActiveRelicShrineData.get(level).remove(shrinePos);
        level.removeBlock(shrinePos, false);
        clearShrineDecorations(level, shrinePos, shrineState);
    }

    public static void clearShrineDecorations(ServerLevel level, BlockPos shrinePos, BlockState shrineState) {
        RelicShrineTheme theme = getTheme(shrineState);
        for (int xOffset = -SHRINE_RADIUS; xOffset <= SHRINE_RADIUS; xOffset++) {
            for (int zOffset = -SHRINE_RADIUS; zOffset <= SHRINE_RADIUS; zOffset++) {
                for (int depth = 1; depth <= MAX_FOUNDATION_DEPTH; depth++) {
                    BlockPos foundationPos = shrinePos.offset(xOffset, -depth, zOffset);
                    if (!level.getBlockState(foundationPos).is(theme.foundationBlock())) {
                        break;
                    }

                    level.removeBlock(foundationPos, false);
                }
            }
        }

        clearPillars(level, shrinePos, theme);
    }

    public static boolean spawnShrineForDestroyedRelic(MinecraftServer server, ItemStack relic) {
        if (!ModConfigs.relicShrinesEnabled || !ModConfigs.destroyedRelicsCreateShrines) {
            return false;
        }

        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return false;
        }

        PendingRelicShrineData pendingData = PendingRelicShrineData.get(overworld);
        pendingData.add(relic);
        if (!tryPlacePendingRelic(server, pendingData, relic)) {
            PotionRelicsMod.LOGGER.warn("Queued destroyed relic {} for shrine retry",
                    relic.getDisplayName().getString());
            return false;
        }

        return true;
    }

    public static int calculateAutoScatterSeparation(int minX, int minZ, int maxX, int maxZ, int shrineCount) {
        int width = Math.max(1, maxX - minX + 1);
        int depth = Math.max(1, maxZ - minZ + 1);
        double area = (double) width * depth;
        double cellsPerShrine = area / Math.max(1, shrineCount);
        int maxReasonableSpacing = Math.max(8, Math.min(width, depth) - 2);
        int suggestedSpacing = (int) Math.floor(Math.sqrt(cellsPerShrine) * 0.72d);
        return Mth.clamp(suggestedSpacing, 8, maxReasonableSpacing);
    }

    public static int scatterAllRelicsInArea(ServerLevel level, int minX, int minZ, int maxX, int maxZ,
            int minSeparation) {
        if (!ModConfigs.relicShrinesEnabled) {
            return 0;
        }

        List<BlockPos> placedShrines = new ArrayList<>();
        int placedCount = 0;

        for (ItemStack relic : ModItems.getDefaultRelicStacks()) {
            RelicShrineTheme theme = RelicShrineTheme.fromRelic(relic);
            BlockPos shrinePos = findShrinePosInArea(level, minX, minZ, maxX, maxZ, placedShrines, minSeparation,
                    theme);
            if (shrinePos != null && placeShrine(level, shrinePos, relic)) {
                placedShrines.add(shrinePos);
                placedCount++;
            }
        }

        return placedCount;
    }

    public static boolean canPlaceShrine(ServerLevel level, BlockPos shrinePos, RelicShrineTheme theme) {
        if (!level.isInWorldBounds(shrinePos) || !level.isInWorldBounds(shrinePos.below())) {
            return false;
        }

        for (int xOffset = -SHRINE_RADIUS; xOffset <= SHRINE_RADIUS; xOffset++) {
            for (int zOffset = -SHRINE_RADIUS; zOffset <= SHRINE_RADIUS; zOffset++) {
                BlockPos shrineLayerPos = shrinePos.offset(xOffset, 0, zOffset);

                if (!level.isInWorldBounds(shrineLayerPos)) {
                    return false;
                }

                if (!canReplaceShrineBlock(level.getBlockState(shrineLayerPos), theme)) {
                    return false;
                }

                BlockPos foundationPos = findFoundationPos(level, shrineLayerPos);
                if (foundationPos == null) {
                    return false;
                }

                if (!theme.allowsWaterPlacement()
                        && containsWaterBetween(level, foundationPos.above(), shrineLayerPos.below())) {
                    return false;
                }
            }
        }

        for (BlockPos pillarBasePos : getPillarPositions(shrinePos)) {
            for (int height = 0; height < PILLAR_HEIGHT; height++) {
                BlockPos pillarPos = pillarBasePos.above(height);
                if (!level.isInWorldBounds(pillarPos) || !canReplaceShrineBlock(level.getBlockState(pillarPos), theme)) {
                    return false;
                }
            }
        }

        return true;
    }

    public static BlockPos findRandomShrinePos(ServerLevel level, BlockPos center, int minRadius, int maxRadius,
            List<BlockPos> existingShrines) {
        return findRandomShrinePos(level, center, minRadius, maxRadius, existingShrines, RelicShrineTheme.ABSORPTION,
                ModConfigs.relicShrineSearchAttempts);
    }

    public static BlockPos findRandomShrinePos(ServerLevel level, BlockPos center, int minRadius, int maxRadius,
            List<BlockPos> existingShrines, RelicShrineTheme theme, int attempts) {
        if (maxRadius < minRadius) {
            return null;
        }

        RandomSource random = level.random;
        for (int attempt = 0; attempt < attempts; attempt++) {
            double angle = random.nextDouble() * (Math.PI * 2.0d);
            int distance = minRadius + random.nextInt(maxRadius - minRadius + 1);
            int x = center.getX() + Mth.floor(Math.cos(angle) * distance);
            int z = center.getZ() + Mth.floor(Math.sin(angle) * distance);
            BlockPos shrinePos = suggestShrinePos(level, x, z, theme);

            if (!isFarEnough(shrinePos, existingShrines) || !canPlaceShrine(level, shrinePos, theme)) {
                continue;
            }

            return shrinePos;
        }

        return null;
    }

    public static BlockPos findRandomShrinePosInArea(ServerLevel level, int minX, int minZ, int maxX, int maxZ,
            List<BlockPos> existingShrines, int minSeparation, RelicShrineTheme theme, int attempts) {
        if (maxX < minX || maxZ < minZ) {
            return null;
        }

        RandomSource random = level.random;
        int width = maxX - minX + 1;
        int depth = maxZ - minZ + 1;
        for (int attempt = 0; attempt < attempts; attempt++) {
            int x = minX + random.nextInt(width);
            int z = minZ + random.nextInt(depth);
            BlockPos shrinePos = suggestShrinePos(level, x, z, theme);

            if (!isWithinScatterBounds(shrinePos, minX, minZ, maxX, maxZ)
                    || !isFarEnough(shrinePos, existingShrines, minSeparation)
                    || !canPlaceShrine(level, shrinePos, theme)) {
                continue;
            }

            return shrinePos;
        }

        return null;
    }

    public static int retryPendingShrines(MinecraftServer server) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return 0;
        }

        PendingRelicShrineData pendingData = PendingRelicShrineData.get(overworld);
        if (pendingData.isEmpty()) {
            return 0;
        }

        int placedCount = 0;
        int processed = 0;
        for (ItemStack relic : pendingData.getPendingRelics()) {
            if (processed >= PENDING_RETRY_BATCH) {
                break;
            }

            processed++;
            if (tryPlacePendingRelic(server, pendingData, relic)) {
                placedCount++;
            }
        }

        return placedCount;
    }

    public static void tickClaimShockwaves(ServerLevel level) {
        if (ACTIVE_CLAIM_SHOCKWAVES.isEmpty()) {
            return;
        }

        Iterator<ClaimShockwave> iterator = ACTIVE_CLAIM_SHOCKWAVES.iterator();
        while (iterator.hasNext()) {
            ClaimShockwave shockwave = iterator.next();
            if (shockwave.dimension() != level.dimension()) {
                continue;
            }

            renderClaimShockwave(level, shockwave);
            shockwave.tick();
            if (shockwave.isFinished()) {
                iterator.remove();
            }
        }
    }

    public static void tickNearbyShrines(ServerLevel level) {
        for (BlockPos shrinePos : ActiveRelicShrineData.get(level).getShrinePositions()) {
            if (!level.isLoaded(shrinePos)) {
                continue;
            }

            if (!(level.getBlockEntity(shrinePos) instanceof RelicShrineBlockEntity shrine)
                    || shrine.getRelic().isEmpty()
                    || !level.getBlockState(shrinePos).is(ModBlocks.RELIC_SHRINE.get())) {
                ActiveRelicShrineData.get(level).remove(shrinePos);
                continue;
            }

            handleNearbyPlayers(level, shrinePos, shrine);
        }
    }

    public static BlockPos suggestShrinePos(ServerLevel level, int x, int z) {
        return suggestShrinePos(level, x, z, RelicShrineTheme.ABSORPTION);
    }

    public static BlockPos suggestShrinePos(ServerLevel level, int x, int z, RelicShrineTheme theme) {
        return new BlockPos(x, getSuggestedShrineY(level, x, z, theme) + 1, z);
    }

    public static boolean isProtectedShrineBlock(ServerLevel level, BlockPos blockPos) {
        return findContainingShrine(level, blockPos) != null;
    }

    public static BlockPos findContainingShrine(ServerLevel level, BlockPos blockPos) {
        for (int xOffset = -SHRINE_RADIUS; xOffset <= SHRINE_RADIUS; xOffset++) {
            for (int zOffset = -SHRINE_RADIUS; zOffset <= SHRINE_RADIUS; zOffset++) {
                for (int yOffset = -PILLAR_HEIGHT; yOffset <= MAX_FOUNDATION_DEPTH; yOffset++) {
                    BlockPos shrinePos = blockPos.offset(-xOffset, yOffset, -zOffset);
                    BlockState shrineState = level.getBlockState(shrinePos);
                    if (!shrineState.is(ModBlocks.RELIC_SHRINE.get())) {
                        continue;
                    }

                    if (isPartOfShrine(blockPos, shrinePos)) {
                        return shrinePos;
                    }
                }
            }
        }

        return null;
    }

    private static boolean isFarEnough(BlockPos shrinePos, List<BlockPos> existingShrines) {
        return isFarEnough(shrinePos, existingShrines, SCATTER_MIN_SEPARATION);
    }

    private static boolean isFarEnough(BlockPos shrinePos, List<BlockPos> existingShrines, int minSeparation) {
        for (BlockPos existingShrine : existingShrines) {
            if (existingShrine.distSqr(shrinePos) < (double) minSeparation * minSeparation) {
                return false;
            }
        }
        return true;
    }

    private static boolean canReplaceShrineBlock(BlockState blockState, RelicShrineTheme theme) {
        return (blockState.canBeReplaced() || blockState.is(BlockTags.LEAVES))
                && (blockState.getFluidState().isEmpty()
                        || (theme.allowsWaterPlacement() && blockState.getFluidState().is(FluidTags.WATER)));
    }

    private static boolean isFoundationSolid(BlockState blockState) {
        return !blockState.isAir() && !blockState.is(BlockTags.LEAVES)
                && blockState.getFluidState().isEmpty() && blockState.blocksMotion();
    }

    private static int roundToNearest(int value, int step) {
        if (step <= 1) {
            return value;
        }

        return Math.round((float) value / (float) step) * step;
    }

    private static void handleNearbyPlayers(ServerLevel level, BlockPos shrinePos, RelicShrineBlockEntity shrine) {
        ItemStack relic = shrine.getRelic();
        RelicShrineTheme theme = RelicShrineTheme.fromRelic(relic);
        boolean emitAmbientParticles = level.getGameTime() % AMBIENT_PARTICLE_INTERVAL == 0;
        boolean showNearbyMessage = level.getGameTime() % 20 == 0;
        boolean nearbyPlayerFound = false;

        for (ServerPlayer player : level.players()) {
            if (!isPlayerNearShrine(player, shrinePos)) {
                continue;
            }

            nearbyPlayerFound = true;
            if (showNearbyMessage) {
                player.displayClientMessage(
                        Component.translatable("message.potionrelicsmod.relic_shrine_nearby", relic.getHoverName()),
                        true);
            }

            if (hasPlayerDiscoveredShrine(player, shrine.getShrineId())) {
                continue;
            }

            markPlayerDiscoveredShrine(player, shrine.getShrineId());
            player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 50, 20));
            player.connection.send(new ClientboundSetTitleTextPacket(
                    Component.translatable("title.potionrelicsmod.relic_shrine_found")));
            player.connection.send(new ClientboundSetSubtitleTextPacket(relic.getHoverName()));
            player.displayClientMessage(
                    Component.translatable("message.potionrelicsmod.relic_shrine_discovered", relic.getHoverName()),
                    false);

            level.playSound(null, shrinePos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f,
                    theme.discoveryPitch());
            level.sendParticles(theme.burstParticle(), shrinePos.getX() + 0.5d, shrinePos.getY() + 1.2d,
                    shrinePos.getZ() + 0.5d, SHRINE_BURST_PARTICLES, 0.6d, 0.45d, 0.6d, 0.03d);

            if (!shrine.isDiscovered()) {
                shrine.setDiscovered(true);
                if (ModConfigs.relicShrineDiscoveryBroadcast) {
                    level.getServer().getPlayerList().broadcastSystemMessage(Component.translatable(
                            "message.potionrelicsmod.relic_shrine_discovered_broadcast", player.getDisplayName(),
                            relic.getHoverName()), false);
                }
            }
        }

        if (nearbyPlayerFound && emitAmbientParticles) {
            level.sendParticles(theme.ambientParticle(), shrinePos.getX() + 0.5d, shrinePos.getY() + 1.15d,
                    shrinePos.getZ() + 0.5d, AMBIENT_PARTICLE_COUNT, 0.22d, 0.28d, 0.22d, 0.0025d);
        }
    }

    private static BlockPos findShrinePosWithRetries(ServerLevel level, BlockPos center, int minRadius, int maxRadius,
            List<BlockPos> existingShrines, RelicShrineTheme theme) {
        int currentMinRadius = Math.max(0, minRadius);
        int currentMaxRadius = Math.max(currentMinRadius, maxRadius);

        for (int pass = 0; pass < SEARCH_PASSES; pass++) {
            BlockPos randomPos = findRandomShrinePos(level, center, currentMinRadius, currentMaxRadius,
                    existingShrines, theme, ModConfigs.relicShrineSearchAttempts * (pass + 1));
            if (randomPos != null) {
                return randomPos;
            }

            currentMinRadius = Math.max(0, currentMinRadius - SEARCH_EXPANSION / 2);
            currentMaxRadius += SEARCH_EXPANSION;
        }

        return findGridShrinePos(level, center, currentMinRadius, currentMaxRadius, existingShrines, theme);
    }

    private static BlockPos findShrinePosInArea(ServerLevel level, int minX, int minZ, int maxX, int maxZ,
            List<BlockPos> existingShrines, int minSeparation, RelicShrineTheme theme) {
        BlockPos randomPos = findRandomShrinePosInArea(level, minX, minZ, maxX, maxZ, existingShrines,
                minSeparation, theme, ModConfigs.relicShrineSearchAttempts * SEARCH_PASSES);
        if (randomPos != null) {
            return randomPos;
        }

        return findGridShrinePosInArea(level, minX, minZ, maxX, maxZ, existingShrines, minSeparation, theme);
    }

    private static BlockPos findGridShrinePos(ServerLevel level, BlockPos center, int minRadius, int maxRadius,
            List<BlockPos> existingShrines, RelicShrineTheme theme) {
        int minRadiusSq = minRadius * minRadius;
        int maxRadiusSq = maxRadius * maxRadius;
        BlockPos bestPos = null;
        int bestDistanceSq = Integer.MAX_VALUE;

        for (int x = center.getX() - maxRadius; x <= center.getX() + maxRadius; x += SEARCH_GRID_STEP) {
            for (int z = center.getZ() - maxRadius; z <= center.getZ() + maxRadius; z += SEARCH_GRID_STEP) {
                int deltaX = x - center.getX();
                int deltaZ = z - center.getZ();
                int distanceSq = deltaX * deltaX + deltaZ * deltaZ;
                if (distanceSq < minRadiusSq || distanceSq > maxRadiusSq) {
                    continue;
                }

                BlockPos shrinePos = suggestShrinePos(level, x, z, theme);
                if (!isFarEnough(shrinePos, existingShrines) || !canPlaceShrine(level, shrinePos, theme)) {
                    continue;
                }

                if (distanceSq < bestDistanceSq) {
                    bestDistanceSq = distanceSq;
                    bestPos = shrinePos;
                }
            }
        }

        return bestPos;
    }

    private static BlockPos findGridShrinePosInArea(ServerLevel level, int minX, int minZ, int maxX, int maxZ,
            List<BlockPos> existingShrines, int minSeparation, RelicShrineTheme theme) {
        BlockPos bestPos = null;
        double bestDistanceToOthers = -1.0d;

        for (int x = minX; x <= maxX; x += SEARCH_GRID_STEP) {
            for (int z = minZ; z <= maxZ; z += SEARCH_GRID_STEP) {
                BlockPos shrinePos = suggestShrinePos(level, x, z, theme);
                if (!isWithinScatterBounds(shrinePos, minX, minZ, maxX, maxZ)
                        || !isFarEnough(shrinePos, existingShrines, minSeparation)
                        || !canPlaceShrine(level, shrinePos, theme)) {
                    continue;
                }

                double distanceToOthers = distanceToClosestShrine(shrinePos, existingShrines);
                if (distanceToOthers > bestDistanceToOthers) {
                    bestDistanceToOthers = distanceToOthers;
                    bestPos = shrinePos;
                }
            }
        }

        return bestPos;
    }

    private static int getSuggestedShrineY(ServerLevel level, int centerX, int centerZ, RelicShrineTheme theme) {
        int highestY = Integer.MIN_VALUE;

        for (int xOffset = -SHRINE_RADIUS; xOffset <= SHRINE_RADIUS; xOffset++) {
            for (int zOffset = -SHRINE_RADIUS; zOffset <= SHRINE_RADIUS; zOffset++) {
                int surfaceY = findShrineSurfaceY(level, centerX + xOffset, centerZ + zOffset, theme);
                highestY = Math.max(highestY, surfaceY);
            }
        }

        return highestY;
    }

    private static int findShrineSurfaceY(ServerLevel level, int x, int z, RelicShrineTheme theme) {
        BlockPos surfacePos = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));

        while (surfacePos.getY() > level.getMinBuildHeight()
                && (isTreeBlock(level.getBlockState(surfacePos))
                        || (!theme.allowsWaterPlacement() && level.getFluidState(surfacePos).is(FluidTags.WATER)))) {
            surfacePos = surfacePos.below();
        }

        return surfacePos.getY();
    }

    private static double distanceToClosestShrine(BlockPos shrinePos, List<BlockPos> existingShrines) {
        if (existingShrines.isEmpty()) {
            return Double.MAX_VALUE;
        }

        double closestDistance = Double.MAX_VALUE;
        for (BlockPos existingShrine : existingShrines) {
            closestDistance = Math.min(closestDistance, existingShrine.distSqr(shrinePos));
        }
        return closestDistance;
    }

    private static boolean containsWaterBetween(ServerLevel level, BlockPos fromPos, BlockPos toPos) {
        if (fromPos.getY() > toPos.getY()) {
            return false;
        }

        for (int y = fromPos.getY(); y <= toPos.getY(); y++) {
            if (level.getFluidState(new BlockPos(fromPos.getX(), y, fromPos.getZ())).is(FluidTags.WATER)) {
                return true;
            }
        }

        return false;
    }

    private static BlockPos findFoundationPos(ServerLevel level, BlockPos shrineLayerPos) {
        for (int depth = 1; depth <= MAX_FOUNDATION_DEPTH; depth++) {
            BlockPos foundationPos = shrineLayerPos.below(depth);
            if (!level.isInWorldBounds(foundationPos)) {
                return null;
            }

            if (isFoundationSolid(level.getBlockState(foundationPos))) {
                return foundationPos;
            }
        }

        return null;
    }

    private static List<BlockPos> getPillarPositions(BlockPos shrinePos) {
        return List.of(
                shrinePos.offset(-1, 0, -1),
                shrinePos.offset(-1, 0, 1),
                shrinePos.offset(1, 0, -1),
                shrinePos.offset(1, 0, 1));
    }

    private static boolean isWithinScatterBounds(BlockPos shrinePos, int minX, int minZ, int maxX, int maxZ) {
        return shrinePos.getX() >= minX && shrinePos.getX() <= maxX
                && shrinePos.getZ() >= minZ && shrinePos.getZ() <= maxZ;
    }

    private static boolean isTreeBlock(BlockState blockState) {
        return blockState.is(BlockTags.LEAVES) || blockState.is(BlockTags.LOGS);
    }

    private static void placePillars(ServerLevel level, BlockPos shrinePos, RelicShrineTheme theme) {
        for (BlockPos pillarBasePos : getPillarPositions(shrinePos)) {
            for (int height = 0; height < PILLAR_HEIGHT; height++) {
                level.setBlock(pillarBasePos.above(height), theme.pillarState(), Block.UPDATE_ALL);
            }
        }
    }

    private static void clearPillars(ServerLevel level, BlockPos shrinePos, RelicShrineTheme theme) {
        for (BlockPos pillarBasePos : getPillarPositions(shrinePos)) {
            for (int height = 0; height < PILLAR_HEIGHT; height++) {
                BlockPos pillarPos = pillarBasePos.above(height);
                if (level.getBlockState(pillarPos).is(theme.pillarBlock())) {
                    level.removeBlock(pillarPos, false);
                }
            }
        }
    }

    private static RelicShrineTheme getTheme(BlockState shrineState) {
        if (shrineState.is(ModBlocks.RELIC_SHRINE.get()) && shrineState.hasProperty(RelicShrineBlock.THEME)) {
            return shrineState.getValue(RelicShrineBlock.THEME);
        }

        return RelicShrineTheme.ABSORPTION;
    }

    private static boolean isPartOfShrine(BlockPos blockPos, BlockPos shrinePos) {
        if (blockPos.equals(shrinePos)) {
            return true;
        }

        for (int xOffset = -SHRINE_RADIUS; xOffset <= SHRINE_RADIUS; xOffset++) {
            for (int zOffset = -SHRINE_RADIUS; zOffset <= SHRINE_RADIUS; zOffset++) {
                for (int depth = 1; depth <= MAX_FOUNDATION_DEPTH; depth++) {
                    if (blockPos.equals(shrinePos.offset(xOffset, -depth, zOffset))) {
                        return true;
                    }
                }
            }
        }

        for (BlockPos pillarBasePos : getPillarPositions(shrinePos)) {
            for (int height = 0; height < PILLAR_HEIGHT; height++) {
                if (blockPos.equals(pillarBasePos.above(height))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean applyShrineDecorations(ServerLevel level, BlockPos shrinePos, RelicShrineTheme theme) {
        for (int xOffset = -SHRINE_RADIUS; xOffset <= SHRINE_RADIUS; xOffset++) {
            for (int zOffset = -SHRINE_RADIUS; zOffset <= SHRINE_RADIUS; zOffset++) {
                BlockPos columnPos = shrinePos.offset(xOffset, 0, zOffset);
                BlockPos foundationPos = findFoundationPos(level, columnPos);
                if (foundationPos == null) {
                    return false;
                }

                for (int y = foundationPos.getY() + 1; y < shrinePos.getY(); y++) {
                    BlockPos fillPos = new BlockPos(columnPos.getX(), y, columnPos.getZ());
                    level.setBlock(fillPos, theme.foundationState(), Block.UPDATE_ALL);
                }

                level.setBlock(shrinePos.offset(xOffset, -1, zOffset), theme.foundationState(), Block.UPDATE_ALL);
            }
        }

        placePillars(level, shrinePos, theme);
        return true;
    }

    private static boolean hasPlayerDiscoveredShrine(ServerPlayer player, UUID shrineId) {
        ListTag discoveries = getPlayerDiscoveries(player);
        String shrineIdString = shrineId.toString();

        for (int i = 0; i < discoveries.size(); i++) {
            if (shrineIdString.equals(discoveries.getString(i))) {
                return true;
            }
        }

        return false;
    }

    private static boolean isPlayerNearShrine(ServerPlayer player, BlockPos shrinePos) {
        double deltaX = player.getX() - (shrinePos.getX() + 0.5d);
        double deltaY = player.getY() - (shrinePos.getY() + 0.5d);
        double deltaZ = player.getZ() - (shrinePos.getZ() + 0.5d);
        return deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ <= SHRINE_DISCOVERY_RADIUS_SQR;
    }

    private static ListTag getPlayerDiscoveries(ServerPlayer player) {
        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        return persistedData.getList(PLAYER_DISCOVERIES_TAG, Tag.TAG_STRING);
    }

    private static void markPlayerDiscoveredShrine(ServerPlayer player, UUID shrineId) {
        CompoundTag rootData = player.getPersistentData();
        CompoundTag persistedData = rootData.getCompound(Player.PERSISTED_NBT_TAG);
        ListTag discoveries = persistedData.getList(PLAYER_DISCOVERIES_TAG, Tag.TAG_STRING);
        String shrineIdString = shrineId.toString();

        for (int i = 0; i < discoveries.size(); i++) {
            if (shrineIdString.equals(discoveries.getString(i))) {
                return;
            }
        }

        discoveries.add(StringTag.valueOf(shrineIdString));
        persistedData.put(PLAYER_DISCOVERIES_TAG, discoveries);
        rootData.put(Player.PERSISTED_NBT_TAG, persistedData);
    }

    public static void broadcastClaim(ServerLevel level, BlockPos shrinePos, Player player, ItemStack relic) {
        spawnClaimBurst(level, shrinePos, relic);
        playGlobalClaimThunder(level);
        triggerGlobalScreenShake(level);

        if (!ModConfigs.relicShrineClaimBroadcast) {
            return;
        }

        level.getServer().getPlayerList().broadcastSystemMessage(
                Component.translatable("message.potionrelicsmod.relic_shrine_claimed_broadcast",
                        player.getDisplayName(), relic.getHoverName()),
                false);
    }

    private static void playGlobalClaimThunder(ServerLevel level) {
        for (ServerPlayer serverPlayer : level.players()) {
            serverPlayer.playNotifySound(SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.MASTER, 0.9f, 1.0f);
        }
    }

    private static void spawnClaimBurst(ServerLevel level, BlockPos shrinePos, ItemStack relic) {
        RelicShrineTheme theme = RelicShrineTheme.fromRelic(relic);
        double centerX = shrinePos.getX() + 0.5d;
        double centerY = shrinePos.getY() + 1.0d;
        double centerZ = shrinePos.getZ() + 0.5d;

        level.sendParticles(theme.burstParticle(), centerX, centerY, centerZ, SHRINE_BURST_PARTICLES, 0.6d, 0.45d,
                0.6d, 0.04d);
        level.sendParticles(theme.ambientParticle(), centerX, centerY, centerZ, SHRINE_BURST_PARTICLES / 2, 0.45d,
                0.35d, 0.45d, 0.02d);
        ACTIVE_CLAIM_SHOCKWAVES.add(new ClaimShockwave(level.dimension(), new Vec3(centerX, centerY, centerZ),
                theme.burstParticle(), theme.ambientParticle()));
    }

    private static void triggerGlobalScreenShake(ServerLevel level) {
        CameraShakePacket packet = new CameraShakePacket(CLAIM_SHAKE_INTENSITY, (int) CLAIM_SHAKE_DURATION_TICKS);
        for (ServerPlayer serverPlayer : level.players()) {
            ModMessages.sendToPlayer(serverPlayer, packet);
        }
    }

    private static void renderClaimShockwave(ServerLevel level, ClaimShockwave shockwave) {
        double radiusProgress = (shockwave.age() + 1.0d) / CLAIM_SHOCKWAVE_DURATION_TICKS;
        double radius = CLAIM_SHOCKWAVE_MAX_RADIUS * radiusProgress;
        int samples = CLAIM_SHOCKWAVE_BASE_SAMPLES + (int) Math.round(radius * 18.0d);

        for (int index = 0; index < samples; index++) {
            double progress = (index + 0.5d) / samples;
            double y = 1.0d - (2.0d * progress);
            double horizontalScale = Math.sqrt(1.0d - (y * y));
            double angle = Math.PI * (3.0d - Math.sqrt(5.0d)) * index;
            double sphereX = Math.cos(angle) * horizontalScale;
            double sphereZ = Math.sin(angle) * horizontalScale;
            double innerRadius = Math.max(0.0d, radius - CLAIM_SHOCKWAVE_SHELL_HALF_THICKNESS);
            double outerRadius = radius + CLAIM_SHOCKWAVE_SHELL_HALF_THICKNESS;

            sendShockwaveParticle(level, shockwave.primaryParticle(), shockwave.center(), sphereX, y, sphereZ,
                    outerRadius);
            sendShockwaveParticle(level, shockwave.secondaryParticle(), shockwave.center(), sphereX, y, sphereZ,
                    innerRadius);
        }
    }

    private static void sendShockwaveParticle(ServerLevel level, ParticleOptions particle, Vec3 center, double sphereX,
            double sphereY, double sphereZ, double radius) {
        level.sendParticles(particle, center.x + (sphereX * radius), center.y + (sphereY * radius),
                center.z + (sphereZ * radius), 1, 0.0d, 0.0d, 0.0d, 0.0d);
    }

    private static final class ClaimShockwave {
        private final ResourceKey<Level> dimension;
        private final Vec3 center;
        private final ParticleOptions primaryParticle;
        private final ParticleOptions secondaryParticle;
        private int age;

        private ClaimShockwave(ResourceKey<Level> dimension, Vec3 center, ParticleOptions primaryParticle,
                ParticleOptions secondaryParticle) {
            this.dimension = dimension;
            this.center = center;
            this.primaryParticle = primaryParticle;
            this.secondaryParticle = secondaryParticle;
        }

        private ResourceKey<Level> dimension() {
            return dimension;
        }

        private Vec3 center() {
            return center;
        }

        private ParticleOptions primaryParticle() {
            return primaryParticle;
        }

        private ParticleOptions secondaryParticle() {
            return secondaryParticle;
        }

        private int age() {
            return age;
        }

        private void tick() {
            age++;
        }

        private boolean isFinished() {
            return age >= CLAIM_SHOCKWAVE_DURATION_TICKS;
        }
    }

    private static boolean tryPlacePendingRelic(MinecraftServer server, PendingRelicShrineData pendingData,
            ItemStack relic) {
        ServerLevel overworld = server.overworld();
        if (overworld == null) {
            return false;
        }

        BlockPos shrinePos = findShrinePosWithRetries(overworld, overworld.getSharedSpawnPos(),
                ModConfigs.destroyedRelicShrineMinRadius, ModConfigs.destroyedRelicShrineMaxRadius, List.of(),
                RelicShrineTheme.fromRelic(relic));
        if (shrinePos == null || !placeShrine(overworld, shrinePos, relic)) {
            return false;
        }

        pendingData.removeFirstMatching(relic);
        int hintedX = roundToNearest(shrinePos.getX(), ModConfigs.relicShrineHintStep);
        int hintedZ = roundToNearest(shrinePos.getZ(), ModConfigs.relicShrineHintStep);
        server.getPlayerList().broadcastSystemMessage(
                Component.translatable("message.potionrelicsmod.relic_shrine_hint", relic.getHoverName(), hintedX,
                        hintedZ),
                false);
        return true;
    }
}
