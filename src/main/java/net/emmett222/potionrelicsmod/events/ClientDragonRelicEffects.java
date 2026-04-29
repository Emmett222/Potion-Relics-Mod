package net.emmett222.potionrelicsmod.events;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.items.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientDragonRelicEffects {
    private static final List<WardApparitionState> ACTIVE_WARD_APPARITIONS = new ArrayList<>();

    public static void startWardApparition(boolean followEntity, int entityId, Vec3 anchor, float yawDegrees,
            int durationTicks) {
        ACTIVE_WARD_APPARITIONS.add(new WardApparitionState(Math.max(1, durationTicks), followEntity, entityId,
                anchor, yawDegrees));
    }

    public static boolean isDragonRelicOnCooldown() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && minecraft.player.getCooldowns().isOnCooldown(ModItems.DRAGONRELIC.get());
    }

    public static int getDragonRelicCooldownBarWidth() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return 0;
        }

        float percent = minecraft.player.getCooldowns().getCooldownPercent(ModItems.DRAGONRELIC.get(), 0.0f);
        return Math.max(0, Math.min(13, Math.round(13.0f * (1.0f - percent))));
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.isPaused() || minecraft.level == null) {
            return;
        }

        Iterator<WardApparitionState> iterator = ACTIVE_WARD_APPARITIONS.iterator();
        while (iterator.hasNext()) {
            WardApparitionState state = iterator.next();
            state.tick(minecraft.level);
            if (state.remainingTicks <= 0) {
                iterator.remove();
            }
        }
    }

    private static class WardApparitionState {
        private static final double COLUMN_HEIGHT = 7.0d;
        private static final double COLUMN_RADIUS = 2.2d;
        private static final double ORBIT_RING_RADIUS = 4.5d;
        private static final double ORBIT_RING_RADIUS_INNER = 2.8d;
        private static final double SHOCKWAVE_SHELL_HALF_THICKNESS = 0.26d;
        private static final int SHOCKWAVE_DURATION_TICKS = 16;
        private static final int SHOCKWAVE_BASE_SAMPLES = 120;
        private static final int[] SHOCKWAVE_START_TICKS = new int[] { 2, 11, 20 };
        private static final double[] SHOCKWAVE_MAX_RADII = new double[] { 12.0d, 22.0d, 32.0d };

        private final int durationTicks;
        private final boolean followEntity;
        private final int entityId;
        private final Vec3 staticAnchor;
        private int remainingTicks;
        private boolean initialized;
        private float yawDegrees;

        private WardApparitionState(int durationTicks, boolean followEntity, int entityId, Vec3 staticAnchor,
                float yawDegrees) {
            this.durationTicks = durationTicks;
            this.remainingTicks = durationTicks;
            this.followEntity = followEntity;
            this.entityId = entityId;
            this.staticAnchor = staticAnchor;
            this.yawDegrees = yawDegrees;
        }

        private void tick(ClientLevel level) {
            Vec3 anchor = staticAnchor;

            if (followEntity) {
                Entity entity = level.getEntity(entityId);
                if (entity == null || entity.isRemoved()) {
                    remainingTicks = 0;
                    return;
                }

                anchor = entity.position();
                if (!initialized) {
                    yawDegrees = entity.getYRot();
                }
            }

            initialized = true;
            int ageTicks = durationTicks - remainingTicks;
            remainingTicks--;

            spawnWardEvent(level, anchor, yawDegrees, ageTicks, durationTicks);
        }

        private void spawnWardEvent(ClientLevel level, Vec3 anchor, float yawDegrees, int ageTicks, int durationTicks) {
            float fade = 1.0f - (ageTicks / (float) durationTicks);
            Vec3 center = anchor.add(0.0d, 3.8d + (Math.sin(ageTicks * 0.11d) * 0.18d), 0.0d);
            Vec3 shockwaveCenter = anchor.add(0.0d, 1.15d, 0.0d);
            Vec3 forward = Vec3.directionFromRotation(0.0f, yawDegrees).normalize();
            Vec3 right = new Vec3(-forward.z, 0.0d, forward.x);

            spawnVerticalColumn(level, anchor, center, ageTicks, fade);
            spawnOrbitRings(level, center, right, forward, ageTicks, fade);
            spawnHelix(level, anchor, center, ageTicks, 1.0d);
            spawnHelix(level, anchor, center, ageTicks, -1.0d);
            spawnTopCrown(level, center, ageTicks);
            spawnShardFan(level, center, right, forward, ageTicks);
            spawnShockwaves(level, shockwaveCenter, ageTicks);
        }

        private void spawnVerticalColumn(ClientLevel level, Vec3 anchor, Vec3 center, int ageTicks, float fade) {
            for (int index = 0; index <= 24; index++) {
                double t = index / 24.0d;
                double swirl = ageTicks * 0.16d + (t * Math.PI * 3.0d);
                double radius = COLUMN_RADIUS * (0.35d + (0.65d * t));
                double x = center.x + (Math.cos(swirl) * radius);
                double y = anchor.y + 0.4d + (t * COLUMN_HEIGHT);
                double z = center.z + (Math.sin(swirl) * radius);

                level.addParticle(ParticleTypes.PORTAL, x, y, z, 0.0d, 0.0d, 0.0d);
                level.addParticle(ParticleTypes.REVERSE_PORTAL, x, y, z, 0.0d, 0.0d, 0.0d);

                if (index % 2 == 0) {
                    level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0d, 0.0d, 0.0d);
                }
                if (index % 3 == 0) {
                    level.addParticle(ParticleTypes.DRAGON_BREATH, x, y + (fade * 0.08d), z, 0.0d, 0.0d, 0.0d);
                }
            }
        }

        private void spawnOrbitRings(ClientLevel level, Vec3 center, Vec3 right, Vec3 forward, int ageTicks,
                float fade) {
            spawnOrbitRing(level, center.add(0.0d, 1.3d, 0.0d), right, forward,
                    ORBIT_RING_RADIUS + (Math.sin(ageTicks * 0.1d) * 0.2d), ageTicks * 0.09d, 36,
                    ParticleTypes.PORTAL, ParticleTypes.REVERSE_PORTAL);
            spawnOrbitRing(level, center.add(0.0d, -0.15d, 0.0d), right, forward,
                    ORBIT_RING_RADIUS_INNER + (Math.cos(ageTicks * 0.12d) * 0.18d), -ageTicks * 0.12d, 28,
                    ParticleTypes.DRAGON_BREATH, ParticleTypes.PORTAL);

            for (int i = 0; i < 10; i++) {
                double angle = (Math.PI * 2.0d * i / 10.0d) + (ageTicks * 0.08d);
                Vec3 point = center.add(right.scale(Math.cos(angle) * 1.65d))
                        .add(forward.scale(Math.sin(angle) * 1.65d));
                level.addParticle(ParticleTypes.END_ROD, point.x, point.y + (fade * 0.6d), point.z, 0.0d, 0.0d,
                        0.0d);
            }
        }

        private void spawnOrbitRing(ClientLevel level, Vec3 center, Vec3 right, Vec3 forward, double radius,
                double angleOffset, int samples, ParticleOptions primary, ParticleOptions secondary) {
            for (int sample = 0; sample < samples; sample++) {
                double angle = angleOffset + (Math.PI * 2.0d * sample / samples);
                Vec3 point = center.add(right.scale(Math.cos(angle) * radius))
                        .add(forward.scale(Math.sin(angle) * radius));
                level.addParticle(primary, point.x, point.y, point.z, 0.0d, 0.0d, 0.0d);

                if (sample % 2 == 0) {
                    level.addParticle(secondary, point.x, point.y, point.z, 0.0d, 0.0d, 0.0d);
                }
            }
        }

        private void spawnHelix(ClientLevel level, Vec3 anchor, Vec3 center, int ageTicks, double direction) {
            for (int index = 0; index <= 22; index++) {
                double t = index / 22.0d;
                double angle = (ageTicks * 0.24d * direction) + (t * Math.PI * 4.0d * direction);
                double radius = 0.9d + (t * 2.6d);
                double x = center.x + (Math.cos(angle) * radius);
                double y = anchor.y + 0.6d + (t * (COLUMN_HEIGHT + 1.2d));
                double z = center.z + (Math.sin(angle) * radius);

                level.addParticle(direction > 0.0d ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL, x, y, z,
                        0.0d, 0.0d, 0.0d);
                if (index % 2 == 0) {
                    level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0d, 0.0d, 0.0d);
                }
            }
        }

        private void spawnTopCrown(ClientLevel level, Vec3 center, int ageTicks) {
            Vec3 crownCenter = center.add(0.0d, 2.35d, 0.0d);
            for (int index = 0; index < 18; index++) {
                double angle = (Math.PI * 2.0d * index / 18.0d) + (ageTicks * 0.14d);
                double radius = 1.1d + (Math.sin(ageTicks * 0.18d + index) * 0.18d);
                double x = crownCenter.x + (Math.cos(angle) * radius);
                double y = crownCenter.y + (Math.sin(ageTicks * 0.12d + index) * 0.18d);
                double z = crownCenter.z + (Math.sin(angle) * radius);
                level.addParticle(ParticleTypes.END_ROD, x, y, z, 0.0d, 0.0d, 0.0d);
                if (index % 3 == 0) {
                    level.addParticle(ParticleTypes.PORTAL, x, y, z, 0.0d, 0.0d, 0.0d);
                }
            }
        }

        private void spawnShardFan(ClientLevel level, Vec3 center, Vec3 right, Vec3 forward, int ageTicks) {
            for (int shard = -3; shard <= 3; shard++) {
                double side = shard / 3.0d;
                Vec3 start = center.add(0.0d, 0.5d, 0.0d).add(right.scale(side * 0.45d));
                Vec3 end = center.add(right.scale(side * 3.4d))
                        .add(forward.scale(2.3d + (Math.cos(ageTicks * 0.1d + shard) * 0.45d)))
                        .add(0.0d, 1.2d + (Math.sin(ageTicks * 0.16d + shard) * 0.3d), 0.0d);
                spawnLine(level, start, end, 8, shard % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL);
            }
        }

        private void spawnShockwaves(ClientLevel level, Vec3 center, int ageTicks) {
            for (int index = 0; index < SHOCKWAVE_START_TICKS.length; index++) {
                int startTick = SHOCKWAVE_START_TICKS[index];
                int localAge = ageTicks - startTick;
                if (localAge >= 0 && localAge < SHOCKWAVE_DURATION_TICKS) {
                    renderShockwaveShell(level, center, localAge, SHOCKWAVE_MAX_RADII[index],
                            index % 2 == 0 ? ParticleTypes.REVERSE_PORTAL : ParticleTypes.PORTAL,
                            ParticleTypes.DRAGON_BREATH);
                }
            }
        }

        private void renderShockwaveShell(ClientLevel level, Vec3 center, int localAge, double maxRadius,
                ParticleOptions primary, ParticleOptions secondary) {
            double radiusProgress = (localAge + 1.0d) / SHOCKWAVE_DURATION_TICKS;
            double radius = maxRadius * radiusProgress;
            int samples = SHOCKWAVE_BASE_SAMPLES + (int) Math.round(radius * 10.0d);

            for (int sample = 0; sample < samples; sample++) {
                double phi = Math.acos(1.0d - (2.0d * (sample + 0.5d) / samples));
                double theta = Math.PI * (1.0d + Math.sqrt(5.0d)) * sample;
                double horizontalScale = Math.sin(phi);
                double x = Math.cos(theta) * horizontalScale;
                double y = Math.cos(phi);
                double z = Math.sin(theta) * horizontalScale;

                sendShockwaveParticle(level, primary, center, x, y, z,
                        Math.max(0.0d, radius - SHOCKWAVE_SHELL_HALF_THICKNESS));
                sendShockwaveParticle(level, secondary, center, x, y, z,
                        radius + SHOCKWAVE_SHELL_HALF_THICKNESS);
            }
        }

        private void sendShockwaveParticle(ClientLevel level, ParticleOptions particle, Vec3 center, double sphereX,
                double sphereY, double sphereZ, double radius) {
            level.addParticle(particle, center.x + (sphereX * radius), center.y + (sphereY * radius),
                    center.z + (sphereZ * radius), 0.0d, 0.0d, 0.0d);
        }

        private void spawnLine(ClientLevel level, Vec3 start, Vec3 end, int points, ParticleOptions particle) {
            for (int index = 0; index <= points; index++) {
                double t = index / (double) points;
                Vec3 point = lerp(start, end, t);
                level.addParticle(particle, point.x, point.y, point.z, 0.0d, 0.0d, 0.0d);
            }
        }

        private Vec3 lerp(Vec3 start, Vec3 end, double t) {
            return new Vec3(Mth.lerp(t, start.x, end.x), Mth.lerp(t, start.y, end.y), Mth.lerp(t, start.z, end.z));
        }
    }
}
