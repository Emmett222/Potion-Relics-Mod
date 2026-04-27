package net.emmett222.potionrelicsmod.items.relics;

import java.util.ArrayList;
import java.util.List;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.emmett222.potionrelicsmod.items.ModItems;
import net.emmett222.potionrelicsmod.network.DragonWardApparitionPacket;
import net.emmett222.potionrelicsmod.events.ClientDragonRelicEffects;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;

/**
 * Dragon relic with one active combat ability and two passive survival
 * abilities.
 */
public class DragonRelic extends BaseRelic {
    private static final String DRAGON_DATA_TAG = "DragonRelic";
    private static final String ROAR_READY_AT_TAG = "RoarReadyAt";
    private static final String RECALL_READY_AT_TAG = "RecallReadyAt";
    private static final String WARD_READY_AT_TAG = "WardReadyAt";
    private static final String LAST_SAFE_X_TAG = "LastSafeX";
    private static final String LAST_SAFE_Y_TAG = "LastSafeY";
    private static final String LAST_SAFE_Z_TAG = "LastSafeZ";
    private static final String LAST_SAFE_DIMENSION_TAG = "LastSafeDimension";
    private static final String WARD_ACTIVE_UNTIL_TAG = "WardActiveUntil";
    private static final String WARD_CENTER_X_TAG = "WardCenterX";
    private static final String WARD_CENTER_Y_TAG = "WardCenterY";
    private static final String WARD_CENTER_Z_TAG = "WardCenterZ";
    private static final String WARD_DIMENSION_TAG = "WardDimension";

    private static final double WARD_RADIUS = 4.25d;
    private static final int WARD_LATITUDE_BANDS = 10;
    private static final int WARD_LONGITUDE_POINTS = 32;
    private static final int WARD_MERIDIAN_STRANDS = 8;
    private static final int WARD_MERIDIAN_POINTS = 16;
    private static final double WARD_EJECTION_MARGIN = 0.65d;
    private static final double WARD_EJECTION_SPEED = 1.35d;
    private static final double WARD_EJECTION_VERTICAL_SEARCH = 3.0d;
    private static final double WARD_EJECTION_VERTICAL_STEP = 0.5d;

    public DragonRelic(Properties pProperties) {
        super(pProperties, MobEffects.DAMAGE_RESISTANCE, 40, "tooltip.potionrelicsmod.dragonrelic");
    }

    @Override
    protected int getConfigAmplifier() {
        return 0;
    }

    @Override
    protected boolean getConfigCanUpgrade() {
        return false;
    }

    @Override
    protected boolean getConfigShowParticles() {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        InteractionResultHolder<ItemStack> toggleResult = super.use(pLevel, pPlayer, pUsedHand);
        if (toggleResult.getResult() != InteractionResult.PASS) {
            return toggleResult;
        }

        ItemStack stack = pPlayer.getItemInHand(pUsedHand);
        if (pUsedHand != InteractionHand.MAIN_HAND || stack != pPlayer.getMainHandItem() || !isEnabled(stack)) {
            return InteractionResultHolder.pass(stack);
        }

        if (pPlayer.isSpectator()) {
            return InteractionResultHolder.pass(stack);
        }

        if (pLevel.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(pPlayer instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.pass(stack);
        }

        long remainingTicks = getRemainingCooldownTicks(serverPlayer, ROAR_READY_AT_TAG);
        if (remainingTicks > 0L) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "message.potionrelicsmod.dragon_roar_cooldown",
                    formatSeconds(remainingTicks)), true);
            return InteractionResultHolder.fail(stack);
        }

        activateRoar(serverPlayer);
        if (serverPlayer.isCreative()) {
            resetCooldowns(serverPlayer);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }

    public static boolean hasEnabledRelic(Player player) {
        for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) {
            ItemStack stack = player.getInventory().getItem(slot);
            if (stack.getItem() instanceof DragonRelic && BaseRelic.isEnabled(stack)) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasEnabledMainHandRelic(Player player) {
        return player.getMainHandItem().getItem() instanceof DragonRelic
                && BaseRelic.isEnabled(player.getMainHandItem());
    }

    public static void tickPlayer(ServerPlayer player) {
        clearExpiredWard(player);

        if (player.isCreative() && hasEnabledMainHandRelic(player)) {
            resetCooldowns(player);
        }

        if (!hasEnabledRelic(player) || player.isCreative() || player.isSpectator()) {
            clearWard(player);
            return;
        }

        syncVisualCooldown(player);

        if (!isWardActive(player) && shouldUpdateSafePosition(player)) {
            saveLastSafePosition(player);
        }

        if (isWardActive(player)) {
            tickWard(player);
        }
    }

    public static boolean tryVoidRecall(ServerPlayer player) {
        if (!hasEnabledRelic(player) || !isCooldownReady(player, RECALL_READY_AT_TAG) || !hasSavedSafePosition(player)) {
            return false;
        }

        CompoundTag data = getDragonData(player);
        String savedDimension = data.getString(LAST_SAFE_DIMENSION_TAG);
        String currentDimension = player.serverLevel().dimension().location().toString();
        if (!currentDimension.equals(savedDimension)) {
            return false;
        }

        double x = data.getDouble(LAST_SAFE_X_TAG);
        double y = data.getDouble(LAST_SAFE_Y_TAG);
        double z = data.getDouble(LAST_SAFE_Z_TAG);

        player.connection.teleport(x, y, z, player.getYRot(), player.getXRot());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0f;
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, ModConfigs.dragonRecallWeaknessDurationTicks, 0,
                false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,
                ModConfigs.dragonRecallSlownessDurationTicks, 1, false, true, true));

        setCooldown(player, RECALL_READY_AT_TAG, ModConfigs.dragonRecallCooldownTicks);
        applyVisualCooldown(player);

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.PORTAL, x, y + 1.0d, z, 48, 0.45d, 0.65d, 0.45d, 0.18d);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, x, y + 1.0d, z, 26, 0.35d, 0.45d, 0.35d, 0.03d);
        level.playSound(null, x, y, z, SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 1.2f, 0.8f);
        player.displayClientMessage(Component.translatable("message.potionrelicsmod.dragon_void_recall"), true);
        return true;
    }

    public static boolean tryActivateWard(ServerPlayer player) {
        if (!hasEnabledRelic(player) || hasTotemInHands(player)
                || !isCooldownReady(player, ROAR_READY_AT_TAG)
                || !isCooldownReady(player, RECALL_READY_AT_TAG)
                || !isCooldownReady(player, WARD_READY_AT_TAG)) {
            return false;
        }

        CompoundTag data = getDragonData(player);
        data.putLong(WARD_ACTIVE_UNTIL_TAG, getGameTime(player) + ModConfigs.dragonWardDurationTicks);
        data.putDouble(WARD_CENTER_X_TAG, player.getX());
        data.putDouble(WARD_CENTER_Y_TAG, player.getY());
        data.putDouble(WARD_CENTER_Z_TAG, player.getZ());
        data.putString(WARD_DIMENSION_TAG, player.serverLevel().dimension().location().toString());
        saveDragonData(player, data);

        player.setHealth((float) Mth.clamp(ModConfigs.dragonWardHealAmount, 1.0d, player.getMaxHealth()));
        player.clearFire();
        player.removeEffect(MobEffects.POISON);
        player.removeEffect(MobEffects.WITHER);
        player.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        player.removeEffect(MobEffects.WEAKNESS);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0, false, true, true));
        player.getFoodData().setFoodLevel(20);
        player.getFoodData().setSaturation(20.0f);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0f;

        setCooldown(player, ROAR_READY_AT_TAG, ModConfigs.dragonRoarCooldownTicks);
        setCooldown(player, RECALL_READY_AT_TAG, ModConfigs.dragonRecallCooldownTicks);
        setCooldown(player, WARD_READY_AT_TAG, ModConfigs.dragonWardCooldownTicks);
        applyVisualCooldown(player);

        ServerLevel level = player.serverLevel();
        level.sendParticles(ParticleTypes.DRAGON_BREATH, player.getX(), player.getY() + 1.1d, player.getZ(), 80,
                0.55d, 0.95d, 0.55d, 0.04d);
        level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY() + 1.1d, player.getZ(), 24,
                0.35d, 0.75d, 0.35d, 0.03d);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.1d, player.getZ(), 36,
                0.45d, 0.65d, 0.45d, 0.02d);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.35f,
                0.9f);
        for (ServerPlayer serverPlayer : level.players()) {
            net.emmett222.potionrelicsmod.network.ModMessages.sendToPlayer(serverPlayer,
                    DragonWardApparitionPacket.forEntity(player, ModConfigs.dragonWardDurationTicks));
        }
        player.displayClientMessage(Component.translatable("message.potionrelicsmod.dragon_ward"), true);
        return true;
    }

    public static boolean isWardActive(Player player) {
        CompoundTag data = getDragonData(player);
        if (!data.contains(WARD_ACTIVE_UNTIL_TAG) || !data.contains(WARD_DIMENSION_TAG)) {
            return false;
        }

        return data.getLong(WARD_ACTIVE_UNTIL_TAG) > getGameTime(player)
                && player.level().dimension().location().toString().equals(data.getString(WARD_DIMENSION_TAG));
    }

    public static void clearWard(Player player) {
        CompoundTag data = getDragonData(player);
        data.remove(WARD_ACTIVE_UNTIL_TAG);
        data.remove(WARD_CENTER_X_TAG);
        data.remove(WARD_CENTER_Y_TAG);
        data.remove(WARD_CENTER_Z_TAG);
        data.remove(WARD_DIMENSION_TAG);
        saveDragonData(player, data);
    }

    private static void activateRoar(ServerPlayer player) {
        setCooldown(player, ROAR_READY_AT_TAG, ModConfigs.dragonRoarCooldownTicks);
        applyVisualCooldown(player);

        double radius = ModConfigs.dragonRoarRadius;
        double knockback = ModConfigs.dragonRoarKnockback;
        Vec3 center = player.position().add(0.0d, player.getBbHeight() * 0.45d, 0.0d);
        Vec3 forward = player.getLookAngle().multiply(1.0d, 0.0d, 1.0d);
        if (forward.lengthSqr() < 1.0E-6d) {
            forward = new Vec3(0.0d, 0.0d, 1.0d);
        }
        forward = forward.normalize();
        Vec3 right = new Vec3(-forward.z, 0.0d, forward.x);
        List<Entity> hitTargets = new ArrayList<>();

        List<Entity> targets = player.serverLevel().getEntities(player,
                new AABB(center.x - radius, center.y - radius, center.z - radius,
                        center.x + radius, center.y + radius, center.z + radius),
                target -> target != player && target.isPushable() && !target.isSpectator());

        for (Entity target : targets) {
            Vec3 delta = target.position().add(0.0d, target.getBbHeight() * 0.35d, 0.0d).subtract(center);
            double distance = delta.length();
            if (distance <= 0.05d || distance > radius) {
                continue;
            }

            Vec3 horizontalDelta = delta.multiply(1.0d, 0.0d, 1.0d);
            if (horizontalDelta.lengthSqr() <= 1.0E-6d) {
                horizontalDelta = forward;
            }

            Vec3 horizontalDirection = horizontalDelta.normalize();
            double frontDot = Mth.clamp(horizontalDirection.dot(forward), -1.0d, 1.0d);
            double distanceFactor = 1.0d - Mth.clamp(distance / radius, 0.0d, 1.0d);
            double forwardBonus = Math.max(0.0d, frontDot);
            double horizontalStrength = knockback * (1.45d + (distanceFactor * 0.95d) + (forwardBonus * 0.9d));
            double verticalStrength = 0.42d + (distanceFactor * 0.28d) + (forwardBonus * 0.16d);
            Vec3 push = horizontalDirection.scale(horizontalStrength)
                    .add(forward.scale(knockback * (0.18d + (forwardBonus * 0.16d))))
                    .add(0.0d, verticalStrength, 0.0d);
            target.push(push.x, push.y, push.z);
            target.hurtMarked = true;
            target.fallDistance = 0.0f;
            hitTargets.add(target);
        }

        ServerLevel level = player.serverLevel();
        spawnRoarParticles(level, player, center, forward, right, radius, hitTargets);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 1.0f,
                1.15f);
        level.playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_SHOOT, SoundSource.PLAYERS, 0.9f,
                0.82f);
    }

    private static void spawnRoarParticles(ServerLevel level, ServerPlayer player, Vec3 center, Vec3 forward,
            Vec3 right, double radius, List<Entity> hitTargets) {
        Vec3 burstCenter = center.add(0.0d, 0.35d, 0.0d);
        level.sendParticles(ParticleTypes.DRAGON_BREATH, burstCenter.x, burstCenter.y, burstCenter.z, 90,
                0.35d, 0.45d, 0.35d, 0.045d);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, burstCenter.x, burstCenter.y, burstCenter.z, 70,
                0.25d, 0.3d, 0.25d, 0.04d);
        level.sendParticles(ParticleTypes.END_ROD, burstCenter.x, burstCenter.y, burstCenter.z, 18,
                0.15d, 0.22d, 0.15d, 0.02d);

        for (int wave = 0; wave < 4; wave++) {
            double progress = (wave + 1.0d) / 4.0d;
            double distance = radius * (0.28d + (progress * 0.82d));
            int samples = 26 + (wave * 10);
            double arcHalfAngle = 0.72d + (progress * 0.34d);
            spawnRoarWaveArc(level, burstCenter.add(0.0d, wave * 0.08d, 0.0d), forward, right, distance,
                    arcHalfAngle, samples);
        }

        for (int lane = -2; lane <= 2; lane++) {
            double sideOffset = lane * 0.58d;
            Vec3 laneStart = burstCenter.add(right.scale(sideOffset));
            Vec3 laneEnd = laneStart.add(forward.scale(radius * 1.12d))
                    .add(0.0d, 0.12d + (Math.abs(lane) * 0.05d), 0.0d);
            spawnRoarTrail(level, laneStart, laneEnd, 18 + (int) Math.round(radius * 7.0d), lane == 0);
        }

        for (Entity target : hitTargets) {
            Vec3 targetCenter = target.position().add(0.0d, target.getBbHeight() * 0.45d, 0.0d);
            spawnRoarTrail(level, burstCenter, targetCenter, 12 + (int) Math.round(burstCenter.distanceTo(targetCenter) * 4.5d),
                    true);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, targetCenter.x, targetCenter.y, targetCenter.z, 18,
                    0.24d, 0.32d, 0.24d, 0.03d);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, targetCenter.x, targetCenter.y, targetCenter.z, 14,
                    0.22d, 0.28d, 0.22d, 0.02d);
            level.sendParticles(ParticleTypes.END_ROD, targetCenter.x, targetCenter.y, targetCenter.z, 8,
                    0.14d, 0.18d, 0.14d, 0.01d);
        }
    }

    private static void spawnRoarWaveArc(ServerLevel level, Vec3 origin, Vec3 forward, Vec3 right, double distance,
            double arcHalfAngle, int samples) {
        for (int sample = 0; sample <= samples; sample++) {
            double t = sample / (double) samples;
            double angle = Mth.lerp(t, -arcHalfAngle, arcHalfAngle);
            Vec3 direction = forward.scale(Math.cos(angle)).add(right.scale(Math.sin(angle))).normalize();
            Vec3 point = origin.add(direction.scale(distance))
                    .add(0.0d, Math.sin((t * Math.PI) + (distance * 0.18d)) * 0.12d, 0.0d);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, point.x, point.y, point.z, 1,
                    0.0d, 0.0d, 0.0d, 0.0d);
            if (sample % 2 == 0) {
                level.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z, 1,
                        0.0d, 0.0d, 0.0d, 0.0d);
            }
            if (sample % 4 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1,
                        0.0d, 0.0d, 0.0d, 0.0d);
            }
        }
    }

    private static void spawnRoarTrail(ServerLevel level, Vec3 start, Vec3 end, int samples, boolean emphasize) {
        Vec3 delta = end.subtract(start);
        for (int sample = 0; sample <= samples; sample++) {
            double t = sample / (double) samples;
            Vec3 point = start.add(delta.scale(t));
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, point.x, point.y, point.z, 1,
                    0.0d, 0.0d, 0.0d, 0.0d);
            if (sample % 2 == 0) {
                level.sendParticles(ParticleTypes.DRAGON_BREATH, point.x, point.y, point.z, 1,
                        0.0d, 0.0d, 0.0d, 0.0d);
            }
            if (emphasize && sample % 3 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, point.x, point.y, point.z, 1,
                        0.0d, 0.0d, 0.0d, 0.0d);
            }
        }
    }

    private static void tickWard(ServerPlayer player) {
        if (!isWardActive(player)) {
            clearWard(player);
            return;
        }

        CompoundTag data = getDragonData(player);
        double centerX = data.getDouble(WARD_CENTER_X_TAG);
        double centerY = data.getDouble(WARD_CENTER_Y_TAG);
        double centerZ = data.getDouble(WARD_CENTER_Z_TAG);

        player.stopUsingItem();
        player.setSprinting(false);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0f;
        player.teleportTo(centerX, centerY, centerZ);
        ejectWardIntruders(player, centerX, centerY, centerZ);

        if (player.serverLevel().getGameTime() % 2L == 0L) {
            spawnWardParticles(player.serverLevel(), centerX, centerY, centerZ);
        }
    }

    private static void ejectWardIntruders(ServerPlayer owner, double centerX, double centerY, double centerZ) {
        ServerLevel level = owner.serverLevel();
        double shellCenterY = centerY + 1.05d;
        Vec3 shellCenter = new Vec3(centerX, shellCenterY, centerZ);
        AABB searchBox = new AABB(centerX - WARD_RADIUS - 2.0d, shellCenterY - WARD_RADIUS - 2.0d,
                centerZ - WARD_RADIUS - 2.0d, centerX + WARD_RADIUS + 2.0d, shellCenterY + WARD_RADIUS + 2.0d,
                centerZ + WARD_RADIUS + 2.0d);

        List<Entity> intruders = level.getEntities(owner, searchBox,
                entity -> entity != owner && !entity.isRemoved() && !entity.isSpectator());

        for (Entity intruder : intruders) {
            AABB bounds = intruder.getBoundingBox();
            double closestX = Mth.clamp(shellCenter.x, bounds.minX, bounds.maxX);
            double closestY = Mth.clamp(shellCenter.y, bounds.minY, bounds.maxY);
            double closestZ = Mth.clamp(shellCenter.z, bounds.minZ, bounds.maxZ);
            Vec3 closestPoint = new Vec3(closestX, closestY, closestZ);
            Vec3 offset = closestPoint.subtract(shellCenter);
            double distanceSq = offset.lengthSqr();
            if (distanceSq >= WARD_RADIUS * WARD_RADIUS) {
                continue;
            }

            Vec3 horizontalCenter = new Vec3(centerX, intruder.getY(), centerZ);
            Vec3 direction = intruder.position().subtract(horizontalCenter).multiply(1.0d, 0.0d, 1.0d);
            if (direction.lengthSqr() < 1.0E-6d) {
                direction = owner.getLookAngle().multiply(1.0d, 0.0d, 1.0d);
            }
            if (direction.lengthSqr() < 1.0E-6d) {
                direction = new Vec3(1.0d, 0.0d, 0.0d);
            }

            direction = direction.normalize();
            double targetRadius = WARD_RADIUS + Math.max(intruder.getBbWidth(), intruder.getBbHeight()) + WARD_EJECTION_MARGIN;
            Vec3 targetPos = horizontalCenter.add(direction.scale(targetRadius));
            targetPos = findSafeEjectionPosition(level, intruder, targetPos, centerY);
            Vec3 launch = direction.scale(WARD_EJECTION_SPEED).add(0.0d, 0.22d, 0.0d);

            if (intruder instanceof ServerPlayer targetPlayer) {
                targetPlayer.connection.teleport(targetPos.x, targetPos.y, targetPos.z, targetPlayer.getYRot(),
                        targetPlayer.getXRot());
            } else {
                intruder.teleportTo(targetPos.x, targetPos.y, targetPos.z);
            }

            intruder.setDeltaMovement(launch);
            intruder.hurtMarked = true;
            intruder.fallDistance = 0.0f;

            level.sendParticles(ParticleTypes.PORTAL, targetPos.x, targetPos.y + 0.5d, targetPos.z, 8, 0.18d, 0.28d,
                    0.18d, 0.02d);
            level.sendParticles(ParticleTypes.DRAGON_BREATH, targetPos.x, targetPos.y + 0.5d, targetPos.z, 6, 0.15d,
                    0.2d, 0.15d, 0.01d);
        }
    }

    private static Vec3 findSafeEjectionPosition(ServerLevel level, Entity intruder, Vec3 desiredPos, double centerY) {
        Vec3 basePos = new Vec3(desiredPos.x, Math.max(desiredPos.y, centerY + 0.1d), desiredPos.z);
        AABB currentBounds = intruder.getBoundingBox();

        for (double offsetY = 0.0d; offsetY <= WARD_EJECTION_VERTICAL_SEARCH; offsetY += WARD_EJECTION_VERTICAL_STEP) {
            Vec3 candidate = basePos.add(0.0d, offsetY, 0.0d);
            Vec3 delta = candidate.subtract(intruder.position());
            if (level.noCollision(intruder, currentBounds.move(delta))) {
                return candidate;
            }
        }

        return basePos;
    }

    private static void spawnWardParticles(ServerLevel level, double centerX, double centerY, double centerZ) {
        double shellCenterY = centerY + 1.05d;
        double time = level.getGameTime() * 0.08d;

        for (int band = 0; band < WARD_LATITUDE_BANDS; band++) {
            double latitudeProgress = band / (double) (WARD_LATITUDE_BANDS - 1);
            double latitude = (-Math.PI / 2.0d) + (latitudeProgress * Math.PI);
            double ringRadius = Math.cos(latitude) * WARD_RADIUS;
            double y = shellCenterY + Math.sin(latitude) * WARD_RADIUS;
            int points = WARD_LONGITUDE_POINTS + (int) Math.round(Math.abs(ringRadius) * 4.0d);
            double angularOffset = time * (band % 2 == 0 ? 1.0d : -1.0d);

            for (int point = 0; point < points; point++) {
                double angle = (Math.PI * 2.0d * point) / points + angularOffset;
                double x = centerX + Math.cos(angle) * ringRadius;
                double z = centerZ + Math.sin(angle) * ringRadius;
                level.sendParticles(ParticleTypes.DRAGON_BREATH, x, y, z, 1, 0.0d, 0.0d, 0.0d, 0.0d);
                if (point % 3 == 0) {
                    level.sendParticles(ParticleTypes.PORTAL, x, y, z, 1, 0.0d, 0.0d, 0.0d, 0.0d);
                }
                if (band == WARD_LATITUDE_BANDS / 2 && point % 6 == 0) {
                    level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0d, 0.0d, 0.0d, 0.0d);
                }
            }
        }

        for (int meridian = 0; meridian < WARD_MERIDIAN_STRANDS; meridian++) {
            double meridianAngle = (Math.PI * 2.0d * meridian) / WARD_MERIDIAN_STRANDS + (time * 0.75d);
            for (int step = 1; step < WARD_MERIDIAN_POINTS; step++) {
                double latitude = (-Math.PI / 2.0d) + (Math.PI * step / WARD_MERIDIAN_POINTS);
                double radiusAtLatitude = Math.cos(latitude) * WARD_RADIUS;
                double x = centerX + Math.cos(meridianAngle) * radiusAtLatitude;
                double y = shellCenterY + Math.sin(latitude) * WARD_RADIUS;
                double z = centerZ + Math.sin(meridianAngle) * radiusAtLatitude;
                level.sendParticles(ParticleTypes.END_ROD, x, y, z, 1, 0.0d, 0.0d, 0.0d, 0.0d);
            }
        }

        double equatorAngleOffset = time * 1.15d;
        for (int point = 0; point < 32; point++) {
            double angle = (Math.PI * 2.0d * point) / 32.0d + equatorAngleOffset;
            double x = centerX + Math.cos(angle) * WARD_RADIUS;
            double z = centerZ + Math.sin(angle) * WARD_RADIUS;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, shellCenterY, z, 1, 0.0d, 0.0d, 0.0d, 0.0d);
            if (point % 4 == 0) {
                level.sendParticles(ParticleTypes.PORTAL, x, shellCenterY, z, 1, 0.0d, 0.0d, 0.0d, 0.0d);
            }
        }
    }

    private static boolean shouldUpdateSafePosition(ServerPlayer player) {
        if (!player.onGround() || player.isPassenger() || player.isFallFlying()) {
            return false;
        }

        BlockPos belowPos = BlockPos.containing(player.getX(), player.getY() - 0.2d, player.getZ());
        return player.level().getBlockState(belowPos).blocksMotion() && player.level().getFluidState(belowPos).isEmpty();
    }

    private static void saveLastSafePosition(ServerPlayer player) {
        CompoundTag data = getDragonData(player);
        data.putDouble(LAST_SAFE_X_TAG, player.getX());
        data.putDouble(LAST_SAFE_Y_TAG, player.getY());
        data.putDouble(LAST_SAFE_Z_TAG, player.getZ());
        data.putString(LAST_SAFE_DIMENSION_TAG, player.serverLevel().dimension().location().toString());
        saveDragonData(player, data);
    }

    private static boolean hasSavedSafePosition(Player player) {
        CompoundTag data = getDragonData(player);
        return data.contains(LAST_SAFE_X_TAG) && data.contains(LAST_SAFE_Y_TAG) && data.contains(LAST_SAFE_Z_TAG)
                && data.contains(LAST_SAFE_DIMENSION_TAG);
    }

    private static boolean hasTotemInHands(Player player) {
        return player.getMainHandItem().is(Items.TOTEM_OF_UNDYING) || player.getOffhandItem().is(Items.TOTEM_OF_UNDYING);
    }

    private static void clearExpiredWard(Player player) {
        if (!isWardActive(player)) {
            clearWard(player);
        }
    }

    private static boolean isCooldownReady(Player player, String key) {
        return getRemainingCooldownTicks(player, key) <= 0L;
    }

    private static long getRemainingCooldownTicks(Player player, String key) {
        CompoundTag data = getDragonData(player);
        long readyAt = data.getLong(key);
        return Math.max(0L, readyAt - getGameTime(player));
    }

    public static long getLongestRemainingCooldownTicks(Player player) {
        return Math.max(getRemainingCooldownTicks(player, ROAR_READY_AT_TAG),
                Math.max(getRemainingCooldownTicks(player, RECALL_READY_AT_TAG),
                        getRemainingCooldownTicks(player, WARD_READY_AT_TAG)));
    }

    private static void setCooldown(Player player, String key, int durationTicks) {
        CompoundTag data = getDragonData(player);
        data.putLong(key, getGameTime(player) + Math.max(0, durationTicks));
        saveDragonData(player, data);
    }

    private static void syncVisualCooldown(ServerPlayer player) {
        long remaining = getLongestRemainingCooldownTicks(player);
        if (remaining > 0L && !player.getCooldowns().isOnCooldown(ModItems.DRAGONRELIC.get())) {
            player.getCooldowns().addCooldown(ModItems.DRAGONRELIC.get(), (int) Math.min(Integer.MAX_VALUE, remaining));
        }
    }

    private static void applyVisualCooldown(ServerPlayer player) {
        long remaining = getLongestRemainingCooldownTicks(player);
        if (remaining > 0L) {
            player.getCooldowns().addCooldown(ModItems.DRAGONRELIC.get(), (int) Math.min(Integer.MAX_VALUE, remaining));
        }
    }

    private static void resetCooldowns(ServerPlayer player) {
        CompoundTag data = getDragonData(player);
        data.remove(ROAR_READY_AT_TAG);
        data.remove(RECALL_READY_AT_TAG);
        data.remove(WARD_READY_AT_TAG);
        saveDragonData(player, data);
        player.getCooldowns().removeCooldown(ModItems.DRAGONRELIC.get());
    }

    private static long getGameTime(Player player) {
        return player.level().getGameTime();
    }

    private static CompoundTag getDragonData(Player player) {
        CompoundTag persistedData = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        return persistedData.getCompound(DRAGON_DATA_TAG);
    }

    private static void saveDragonData(Player player, CompoundTag dragonData) {
        CompoundTag rootData = player.getPersistentData();
        CompoundTag persistedData = rootData.getCompound(Player.PERSISTED_NBT_TAG);
        persistedData.put(DRAGON_DATA_TAG, dragonData);
        rootData.put(Player.PERSISTED_NBT_TAG, persistedData);
    }

    private static String formatSeconds(long ticks) {
        return String.format("%.1f", ticks / 20.0d);
    }

    @Override
    public boolean isBarVisible(ItemStack pStack) {
        return super.isBarVisible(pStack)
                || Boolean.TRUE.equals(DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
                        () -> ClientDragonRelicEffects::isDragonRelicOnCooldown));
    }

    @Override
    public int getBarWidth(ItemStack pStack) {
        Integer width = DistExecutor.unsafeCallWhenOn(Dist.CLIENT,
                () -> ClientDragonRelicEffects::getDragonRelicCooldownBarWidth);
        return width == null ? super.getBarWidth(pStack) : width;
    }

    @Override
    public int getBarColor(ItemStack pStack) {
        return 0x8F42FF;
    }

    @Override
    public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> pTooltipComponents,
            TooltipFlag pIsAdvanced) {
        pTooltipComponents.add(Component.translatable(tooltip).withStyle(ChatFormatting.ITALIC));
        pTooltipComponents.add(Component.empty());
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.main_hand").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.dragonrelic_roar")
                .withStyle(ChatFormatting.DARK_PURPLE));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.inventory").withStyle(ChatFormatting.GRAY));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.dragonrelic_recall")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.dragonrelic_ward")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        pTooltipComponents.add(Component.translatable("tooltip.potionrelicsmod.dragonrelic_ward_rule")
                .withStyle(ChatFormatting.DARK_GRAY));
        addToggleTooltip(pStack, pTooltipComponents);
    }
}
