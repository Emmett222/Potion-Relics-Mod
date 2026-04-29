package net.emmett222.potionrelicsmod.events;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientCameraShakeEvents {
    private static int shakeDurationTicks;
    private static int shakeTicksRemaining;
    private static float shakeIntensity;

    public static void startShake(float intensity, int durationTicks) {
        shakeIntensity = Math.max(shakeIntensity, intensity);
        shakeDurationTicks = Math.max(shakeDurationTicks, durationTicks);
        shakeTicksRemaining = Math.max(shakeTicksRemaining, durationTicks);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().isPaused() || shakeTicksRemaining <= 0) {
            return;
        }

        shakeTicksRemaining--;
        if (shakeTicksRemaining <= 0) {
            shakeTicksRemaining = 0;
            shakeDurationTicks = 0;
            shakeIntensity = 0.0f;
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        if (shakeTicksRemaining <= 0 || shakeDurationTicks <= 0 || Minecraft.getInstance().player == null) {
            return;
        }

        float elapsed = (shakeDurationTicks - shakeTicksRemaining) + (float) event.getPartialTick();
        float lifeProgress = elapsed / (float) shakeDurationTicks;
        float fade = 1.0f - lifeProgress;
        float amplitude = shakeIntensity * fade;

        event.setYaw(event.getYaw() + (float) Math.sin(elapsed * 1.15f) * amplitude * 1.8f);
        event.setPitch(event.getPitch() + (float) Math.cos(elapsed * 1.5f) * amplitude * 1.35f);
        event.setRoll(event.getRoll() + (float) Math.sin(elapsed * 2.2f) * amplitude * 1.1f);
    }
}
