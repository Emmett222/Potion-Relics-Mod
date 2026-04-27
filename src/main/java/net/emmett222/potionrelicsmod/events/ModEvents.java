package net.emmett222.potionrelicsmod.events;

import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.emmett222.potionrelicsmod.items.ModItems;
import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.blocks.custom.RelicShrineBlock;
import net.emmett222.potionrelicsmod.items.relics.AbsorptionRelic;
import net.emmett222.potionrelicsmod.items.relics.BaseRelic;
import net.emmett222.potionrelicsmod.items.relics.DragonRelic;
import net.emmett222.potionrelicsmod.items.relics.InvisibilityRelic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.PotionColorCalculationEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.emmett222.potionrelicsmod.shrines.RelicShrineManager;

/**
 * Events of Potion Relics Mod. Handles all events.
 * 
 * @author Emmett Grebe
 * @version 4-21-2026
 */
@Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEvents {

    /**
     * This is here because of Absorption Relic cheats and glitches.
     * Without this, the hearts stay after losing the relic. This is a glitch, and
     * can be used as a cheat to pass around the relic and everyone keeps the
     * Absorption.
     * 
     * @param event The tick event.
     */
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        Player player = event.player;
        if (player instanceof ServerPlayer serverPlayer) {
            DragonRelic.tickPlayer(serverPlayer);
        }

        boolean hasRelic = hasAbsorptionRelic(player);
        float vanillaAbsorption = getVanillaAbsorptionAmount(player);
        float desiredAbsorption = vanillaAbsorption + ModConfigs.absorptionAmount;

        if (hasRelic) {
            if (player.getAbsorptionAmount() < desiredAbsorption
                    && !player.getCooldowns().isOnCooldown(ModItems.ABSORPTIONRELIC.get())) {
                player.setAbsorptionAmount(desiredAbsorption);
                player.getCooldowns().addCooldown(ModItems.ABSORPTIONRELIC.get(), ModConfigs.absorptionCooldown);
            }
            return;
        }

        if (player.getAbsorptionAmount() > vanillaAbsorption) {
            player.setAbsorptionAmount(vanillaAbsorption);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity() instanceof Player player && DragonRelic.isWardActive(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        if (DragonRelic.isWardActive(player)) {
            event.setCanceled(true);
            return;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || player.isCreative() || player.isSpectator()) {
            return;
        }

        float effectiveHealth = player.getHealth() + player.getAbsorptionAmount();
        if (event.getAmount() < effectiveHealth) {
            return;
        }

        if (event.getSource().is(DamageTypes.FELL_OUT_OF_WORLD)) {
            if (DragonRelic.tryVoidRecall(serverPlayer)) {
                event.setCanceled(true);
            }
            return;
        }

        if (DragonRelic.tryActivateWard(serverPlayer)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!ModConfigs.dragonRelicDropsFromDragons || !(event.getEntity() instanceof EnderDragon dragon)
                || dragon.level().isClientSide) {
            return;
        }

        event.getDrops().add(new ItemEntity(dragon.level(), dragon.getX(), dragon.getY() + 1.0d, dragon.getZ(),
                new ItemStack(ModItems.DRAGONRELIC.get())));
    }

    private static boolean hasAbsorptionRelic(Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() instanceof AbsorptionRelic
                    && BaseRelic.isEnabled(player.getInventory().getItem(i))) {
                return true;
            }
        }
        return false;
    }

    private static float getVanillaAbsorptionAmount(Player player) {
        MobEffectInstance absorptionEffect = player.getEffect(MobEffects.ABSORPTION);
        if (absorptionEffect == null) {
            return 0.0f;
        }

        return 4.0f * (absorptionEffect.getAmplifier() + 1);
    }

    /**
     * Hides all potion particles while the invisibility relic is actively hiding a
     * player.
     * 
     * @param event The potion color calculation event.
     */
    @SubscribeEvent
    public static void onPotionColorCalculation(PotionColorCalculationEvent event) {
        if (event.getEntity() instanceof Player player && InvisibilityRelic.isRelicActive(player)) {
            event.shouldHideParticles(true);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.level instanceof ServerLevel serverLevel)) {
            return;
        }

        RelicShrineManager.tickClaimShockwaves(serverLevel);

        if (serverLevel.getGameTime() % 4 == 0) {
            RelicShrineManager.tickNearbyShrines(serverLevel);
        }

        if (serverLevel.dimension() == Level.OVERWORLD && serverLevel.getGameTime() % 100 == 0) {
            RelicShrineManager.retryPendingShrines(serverLevel.getServer());
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (DragonRelic.isWardActive(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (DragonRelic.isWardActive(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (DragonRelic.isWardActive(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (DragonRelic.isWardActive(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (DragonRelic.isWardActive(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (DragonRelic.isWardActive(event.getEntity())) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player && DragonRelic.isWardActive(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (DragonRelic.isWardActive(event.getPlayer())) {
            event.setCanceled(true);
            return;
        }

        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }

        BlockPos shrinePos = RelicShrineManager.findContainingShrine(serverLevel, event.getPos());
        if (shrinePos == null) {
            return;
        }

        event.setCanceled(true);
        if (!RelicShrineBlock.tryClaimShrine(serverLevel, shrinePos, event.getPlayer())) {
            serverLevel.sendBlockUpdated(event.getPos(), serverLevel.getBlockState(event.getPos()),
                    serverLevel.getBlockState(event.getPos()), Block.UPDATE_ALL);
            serverLevel.sendBlockUpdated(shrinePos, serverLevel.getBlockState(shrinePos),
                    serverLevel.getBlockState(shrinePos), Block.UPDATE_ALL);
        }
    }
}
