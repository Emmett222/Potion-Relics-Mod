package net.emmett222.potionrelicsmod.events;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.items.relics.BaseRelic;
import net.emmett222.potionrelicsmod.network.ModMessages;
import net.emmett222.potionrelicsmod.network.ToggleRelicPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side relic toggle inputs.
 * 
 * @author Emmett Grebe
 * @version 4-22-2026
 */
public class ClientToggleEvents {
    public static final KeyMapping TOGGLE_RELIC = new KeyMapping(
            "key.potionrelicsmod.toggle_relic",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "key.categories.potionrelicsmod");

    /**
     * Registers key mappings.
     */
    @Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class KeyRegistration {
        @SubscribeEvent
        public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
            event.register(TOGGLE_RELIC);
        }
    }

    /**
     * Handles the hovered relic hotkey in container screens.
     */
    @Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ScreenInput {
        @SubscribeEvent
        public static void onScreenKeyPressed(ScreenEvent.KeyPressed.Pre event) {
            if (!(event.getScreen() instanceof AbstractContainerScreen<?> screen)) {
                return;
            }

            InputConstants.Key key = InputConstants.getKey(event.getKeyCode(), event.getScanCode());
            if (!TOGGLE_RELIC.isActiveAndMatches(key)) {
                return;
            }

            Slot hoveredSlot = screen.getSlotUnderMouse();
            if (hoveredSlot == null || !hoveredSlot.hasItem() || !BaseRelic.isRelic(hoveredSlot.getItem())) {
                return;
            }

            int slotId = screen.getMenu().slots.indexOf(hoveredSlot);
            if (slotId < 0) {
                return;
            }

            ModMessages.sendToServer(new ToggleRelicPacket(slotId));
            event.setCanceled(true);
        }
    }
}
