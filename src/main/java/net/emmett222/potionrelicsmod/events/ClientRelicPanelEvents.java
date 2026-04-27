package net.emmett222.potionrelicsmod.events;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import net.emmett222.potionrelicsmod.PotionRelicsMod;
import net.emmett222.potionrelicsmod.configs.ModConfigs;
import net.emmett222.potionrelicsmod.items.relics.BaseRelic;
import net.emmett222.potionrelicsmod.network.ModMessages;
import net.emmett222.potionrelicsmod.network.ToggleRelicPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

/**
 * Inventory-side relic toggle panel.
 * 
 * @author Emmett Grebe
 * @version 4-22-2026
 */
@Mod.EventBusSubscriber(modid = PotionRelicsMod.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ClientRelicPanelEvents {
    private static final int INVENTORY_WIDTH = 176;
    private static final int INVENTORY_HEIGHT = 166;
    private static final int PANEL_GAP = 6;
    private static final int PANEL_PADDING = 4;
    private static final int SIDE_PANEL_WIDTH = 124;
    private static final int TAB_WIDTH = 58;
    private static final int TAB_HEIGHT = 14;
    private static final int HEADER_HEIGHT = 18;
    private static final int ROW_HEIGHT = 20;
    private static final int FOOTER_HEIGHT = 14;
    private static final int MAX_VISIBLE_ROWS = 6;

    private static int scrollOffset = 0;
    private static boolean panelOpen = false;
    private static Screen trackedScreen = null;

    /**
     * Renders the relic panel next to the player inventory.
     * 
     * @param event The render event.
     */
    @SubscribeEvent
    public static void onScreenRender(ScreenEvent.Render.Post event) {
        if (!ModConfigs.relicTogglingEnabled
                || !ModConfigs.inventoryRelicPanelEnabled
                || !(event.getScreen() instanceof InventoryScreen screen)) {
            return;
        }

        syncScreenState(screen);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        List<RelicEntry> entries = getRelicEntries(screen, minecraft.player.getInventory());
        PanelLayout layout = getPanelLayout(screen, entries.size());
        TabLayout tabLayout = getTabLayout(screen, layout);
        scrollOffset = Mth.clamp(scrollOffset, 0, getMaxScroll(entries.size(), layout.visibleRows));

        renderTab(event.getGuiGraphics(), minecraft.font, tabLayout);

        if (panelOpen) {
            renderPanel(event.getGuiGraphics(), minecraft, layout, entries, event.getMouseX(), event.getMouseY());
        }
    }

    /**
     * Toggles a relic when the player clicks one of the panel rows.
     * 
     * @param event The mouse press event.
     */
    @SubscribeEvent
    public static void onMouseButtonPressed(ScreenEvent.MouseButtonPressed.Pre event) {
        if (!ModConfigs.relicTogglingEnabled
                || !ModConfigs.inventoryRelicPanelEnabled
                || event.getButton() != 0
                || !(event.getScreen() instanceof InventoryScreen screen)) {
            return;
        }

        syncScreenState(screen);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        List<RelicEntry> entries = getRelicEntries(screen, minecraft.player.getInventory());
        PanelLayout layout = getPanelLayout(screen, entries.size());
        TabLayout tabLayout = getTabLayout(screen, layout);
        scrollOffset = Mth.clamp(scrollOffset, 0, getMaxScroll(entries.size(), layout.visibleRows));

        if (tabLayout.contains(event.getMouseX(), event.getMouseY())) {
            panelOpen = !panelOpen;
            event.setCanceled(true);
            return;
        }

        if (!panelOpen) {
            return;
        }

        if (!layout.contains(event.getMouseX(), event.getMouseY())) {
            return;
        }

        event.setCanceled(true);

        int hoveredRow = getHoveredRow(layout, entries.size(), event.getMouseX(), event.getMouseY());
        if (hoveredRow < 0) {
            return;
        }

        RelicEntry entry = entries.get(scrollOffset + hoveredRow);
        ModMessages.sendToServer(new ToggleRelicPacket(entry.slotId));
    }

    /**
     * Scrolls the relic panel when needed.
     * 
     * @param event The mouse scroll event.
     */
    @SubscribeEvent
    public static void onMouseScrolled(ScreenEvent.MouseScrolled.Pre event) {
        if (!ModConfigs.relicTogglingEnabled
                || !ModConfigs.inventoryRelicPanelEnabled
                || !(event.getScreen() instanceof InventoryScreen screen)) {
            return;
        }

        syncScreenState(screen);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null) {
            return;
        }

        if (!panelOpen) {
            return;
        }

        List<RelicEntry> entries = getRelicEntries(screen, minecraft.player.getInventory());
        PanelLayout layout = getPanelLayout(screen, entries.size());
        int maxScroll = getMaxScroll(entries.size(), layout.visibleRows);
        if (maxScroll <= 0) {
            return;
        }

        if (!layout.contains(event.getMouseX(), event.getMouseY())) {
            return;
        }

        if (event.getScrollDelta() > 0.0D) {
            scrollOffset = Mth.clamp(scrollOffset - 1, 0, maxScroll);
        } else if (event.getScrollDelta() < 0.0D) {
            scrollOffset = Mth.clamp(scrollOffset + 1, 0, maxScroll);
        }

        event.setCanceled(true);
    }

    private static void renderTab(GuiGraphics guiGraphics, Font font, TabLayout tabLayout) {
        int backgroundColor = panelOpen ? 0xD02A2A2A : 0xD0181818;
        int borderColor = panelOpen ? 0xFF9C9C9C : 0xFF6F6F6F;
        Component label = Component.translatable(
                panelOpen ? "screen.potionrelicsmod.relic_tab_open" : "screen.potionrelicsmod.relic_tab_closed");

        guiGraphics.fill(tabLayout.x, tabLayout.y, tabLayout.x + tabLayout.width, tabLayout.y + tabLayout.height,
                backgroundColor);
        drawBorder(guiGraphics, tabLayout.x, tabLayout.y, tabLayout.width, tabLayout.height, borderColor);
        guiGraphics.drawString(font,
                label,
                tabLayout.x + (tabLayout.width - font.width(label)) / 2,
                tabLayout.y + 3,
                0xFFFFFF,
                false);
    }

    private static void renderPanel(GuiGraphics guiGraphics, Minecraft minecraft, PanelLayout layout,
            List<RelicEntry> entries, int mouseX, int mouseY) {
        Font font = minecraft.font;
        int panelRight = layout.x + layout.width;
        int panelBottom = layout.y + layout.height;
        int maxScroll = getMaxScroll(entries.size(), layout.visibleRows);

        guiGraphics.fill(layout.x, layout.y, panelRight, panelBottom, 0xD0101010);
        guiGraphics.fill(layout.x, layout.y, panelRight, layout.y + HEADER_HEIGHT, 0xD02A2A2A);
        drawBorder(guiGraphics, layout.x, layout.y, layout.width, layout.height, 0xFF6F6F6F);

        guiGraphics.drawString(font,
                Component.translatable("screen.potionrelicsmod.relic_panel"),
                layout.x + PANEL_PADDING,
                layout.y + 5,
                0xFFFFFF,
                false);

        if (entries.isEmpty()) {
            guiGraphics.drawString(font,
                    Component.translatable("screen.potionrelicsmod.relic_panel_empty"),
                    layout.x + PANEL_PADDING,
                    layout.contentY + 6,
                    0xA0A0A0,
                    false);
            return;
        }

        for (int row = 0; row < layout.visibleRows; row++) {
            int entryIndex = scrollOffset + row;
            if (entryIndex >= entries.size()) {
                break;
            }

            RelicEntry entry = entries.get(entryIndex);
            int rowY = layout.contentY + row * ROW_HEIGHT;
            boolean hovered = isWithin(mouseX, mouseY, layout.x + PANEL_PADDING, rowY,
                    layout.width - (PANEL_PADDING * 2) - 4, ROW_HEIGHT - 2);

            guiGraphics.fill(layout.x + PANEL_PADDING, rowY, panelRight - PANEL_PADDING - 4, rowY + ROW_HEIGHT - 2,
                    hovered ? 0xA0353535 : 0x90303030);

            guiGraphics.renderItem(entry.stack, layout.x + 6, rowY + 1);
            guiGraphics.renderItemDecorations(font, entry.stack, layout.x + 6, rowY + 1);

            boolean enabled = BaseRelic.isEnabled(entry.stack);
            int buttonWidth = 26;
            int buttonX = panelRight - PANEL_PADDING - buttonWidth - 6;
            int buttonY = rowY + 4;
            String buttonText = Component.translatable(enabled ? "options.on" : "options.off").getString();
            String displayName = trimToWidth(font, entry.stack.getHoverName().getString(),
                    buttonX - (layout.x + 26) - 4);

            guiGraphics.drawString(font, displayName, layout.x + 26, rowY + 6, 0xFFFFFF, false);
            guiGraphics.fill(buttonX, buttonY, buttonX + buttonWidth, buttonY + 12, enabled ? 0xC02F7A33 : 0xC08B3232);
            drawBorder(guiGraphics, buttonX, buttonY, buttonWidth, 12, enabled ? 0xFF7FD886 : 0xFFE07B7B);
            guiGraphics.drawString(font, buttonText,
                    buttonX + (buttonWidth - font.width(buttonText)) / 2,
                    buttonY + 2,
                    0xFFFFFF,
                    false);
        }

        if (maxScroll > 0) {
            renderScrollBar(guiGraphics, layout, entries.size());
            guiGraphics.drawString(font,
                    Component.translatable("screen.potionrelicsmod.relic_panel_scroll"),
                    layout.x + PANEL_PADDING,
                    panelBottom - 10,
                    0x8F8F8F,
                    false);
        } else {
            guiGraphics.drawString(font,
                    Component.translatable("screen.potionrelicsmod.relic_panel_click"),
                    layout.x + PANEL_PADDING,
                    panelBottom - 10,
                    0x8F8F8F,
                    false);
        }

        int hoveredRow = getHoveredRow(layout, entries.size(), mouseX, mouseY);
        if (hoveredRow >= 0) {
            RelicEntry hoveredEntry = entries.get(scrollOffset + hoveredRow);
            guiGraphics.renderTooltip(font,
                    Screen.getTooltipFromItem(minecraft, hoveredEntry.stack),
                    Optional.empty(),
                    mouseX,
                    mouseY);
        }
    }

    private static void renderScrollBar(GuiGraphics guiGraphics, PanelLayout layout, int entryCount) {
        int maxScroll = getMaxScroll(entryCount, layout.visibleRows);
        int trackX = layout.x + layout.width - 4;
        int trackY = layout.contentY;
        int trackHeight = (layout.visibleRows * ROW_HEIGHT) - 2;
        int thumbHeight = Math.max(12, trackHeight * layout.visibleRows / entryCount);
        int thumbTravel = trackHeight - thumbHeight;
        int thumbY = thumbTravel <= 0 ? trackY : trackY + (scrollOffset * thumbTravel / maxScroll);

        guiGraphics.fill(trackX, trackY, trackX + 2, trackY + trackHeight, 0xB02A2A2A);
        guiGraphics.fill(trackX, thumbY, trackX + 2, thumbY + thumbHeight, 0xFFE0E0E0);
    }

    private static List<RelicEntry> getRelicEntries(InventoryScreen screen, Inventory playerInventory) {
        List<RelicEntry> entries = new ArrayList<>();

        for (int slotId = 0; slotId < screen.getMenu().slots.size(); slotId++) {
            Slot slot = screen.getMenu().getSlot(slotId);
            if (!(slot.container instanceof Inventory) || slot.container != playerInventory || !slot.hasItem()) {
                continue;
            }

            ItemStack stack = slot.getItem();
            if (!BaseRelic.isRelic(stack)) {
                continue;
            }

            entries.add(new RelicEntry(slotId, stack));
        }

        return entries;
    }

    private static TabLayout getTabLayout(InventoryScreen screen, PanelLayout layout) {
        int leftPos = ObfuscationReflectionHelper.getPrivateValue(AbstractContainerScreen.class, screen, "f_97735_");
        int topPos = ObfuscationReflectionHelper.getPrivateValue(AbstractContainerScreen.class, screen, "f_97736_");

        return switch (layout.placement) {
            case BELOW -> new TabLayout(leftPos + INVENTORY_WIDTH - TAB_WIDTH - PANEL_PADDING,
                    topPos + INVENTORY_HEIGHT + 1, TAB_WIDTH, TAB_HEIGHT);
            case ABOVE -> new TabLayout(leftPos + INVENTORY_WIDTH - TAB_WIDTH - PANEL_PADDING,
                    topPos - TAB_HEIGHT - 1, TAB_WIDTH, TAB_HEIGHT);
            case RIGHT -> new TabLayout(leftPos + INVENTORY_WIDTH + 1, topPos + PANEL_PADDING, TAB_WIDTH, TAB_HEIGHT);
            case LEFT -> new TabLayout(leftPos - TAB_WIDTH - 1, topPos + PANEL_PADDING, TAB_WIDTH, TAB_HEIGHT);
        };
    }

    private static PanelLayout getPanelLayout(InventoryScreen screen, int entryCount) {
        int leftPos = ObfuscationReflectionHelper.getPrivateValue(AbstractContainerScreen.class, screen, "f_97735_");
        int topPos = ObfuscationReflectionHelper.getPrivateValue(AbstractContainerScreen.class, screen, "f_97736_");
        int minHeight = getPanelHeight(1);

        int belowY = topPos + INVENTORY_HEIGHT + PANEL_GAP;
        int belowAvailableHeight = screen.height - belowY - PANEL_PADDING;
        if (belowAvailableHeight >= minHeight) {
            int visibleRows = getVisibleRows(entryCount, belowAvailableHeight);
            return new PanelLayout(leftPos, belowY, INVENTORY_WIDTH, getPanelHeight(visibleRows), visibleRows,
                    PanelPlacement.BELOW);
        }

        int aboveAvailableHeight = topPos - PANEL_GAP - PANEL_PADDING;
        if (aboveAvailableHeight >= minHeight) {
            int visibleRows = getVisibleRows(entryCount, aboveAvailableHeight);
            int height = getPanelHeight(visibleRows);
            return new PanelLayout(leftPos, topPos - PANEL_GAP - height, INVENTORY_WIDTH, height, visibleRows,
                    PanelPlacement.ABOVE);
        }

        int visibleRows = Math.max(1, Math.min(MAX_VISIBLE_ROWS, entryCount));
        int height = getPanelHeight(visibleRows);
        int x = leftPos + INVENTORY_WIDTH + PANEL_GAP;
        PanelPlacement placement = PanelPlacement.RIGHT;
        if (x + SIDE_PANEL_WIDTH > screen.width - PANEL_PADDING) {
            x = Math.max(PANEL_PADDING, leftPos - SIDE_PANEL_WIDTH - PANEL_GAP);
            placement = PanelPlacement.LEFT;
        }

        int y = Mth.clamp(topPos, PANEL_PADDING, Math.max(PANEL_PADDING, screen.height - height - PANEL_PADDING));
        return new PanelLayout(x, y, SIDE_PANEL_WIDTH, height, visibleRows, placement);
    }

    private static int getHoveredRow(PanelLayout layout, int entryCount, double mouseX, double mouseY) {
        int visibleRows = Math.min(layout.visibleRows, entryCount - scrollOffset);
        for (int row = 0; row < visibleRows; row++) {
            int rowY = layout.contentY + row * ROW_HEIGHT;
            if (isWithin(mouseX, mouseY, layout.x + PANEL_PADDING, rowY,
                    layout.width - (PANEL_PADDING * 2) - 4, ROW_HEIGHT - 2)) {
                return row;
            }
        }

        return -1;
    }

    private static int getVisibleRows(int entryCount, int availableHeight) {
        int rowsForSpace = Math.max(1, (availableHeight - HEADER_HEIGHT - FOOTER_HEIGHT) / ROW_HEIGHT);
        return Math.max(1, Math.min(Math.min(MAX_VISIBLE_ROWS, entryCount), rowsForSpace));
    }

    private static int getPanelHeight(int visibleRows) {
        return HEADER_HEIGHT + (visibleRows * ROW_HEIGHT) + FOOTER_HEIGHT;
    }

    private static int getMaxScroll(int entryCount, int visibleRows) {
        return Math.max(0, entryCount - visibleRows);
    }

    private static void syncScreenState(Screen screen) {
        if (trackedScreen != screen) {
            trackedScreen = screen;
            panelOpen = false;
            scrollOffset = 0;
        }
    }

    private static String trimToWidth(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }

        String ellipsis = "...";
        String trimmed = text;
        while (!trimmed.isEmpty() && font.width(trimmed + ellipsis) > maxWidth) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }

        return trimmed + ellipsis;
    }

    private static void drawBorder(GuiGraphics guiGraphics, int x, int y, int width, int height, int color) {
        guiGraphics.fill(x, y, x + width, y + 1, color);
        guiGraphics.fill(x, y + height - 1, x + width, y + height, color);
        guiGraphics.fill(x, y, x + 1, y + height, color);
        guiGraphics.fill(x + width - 1, y, x + width, y + height, color);
    }

    private static boolean isWithin(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    private static class RelicEntry {
        private final int slotId;
        private final ItemStack stack;

        private RelicEntry(int slotId, ItemStack stack) {
            this.slotId = slotId;
            this.stack = stack;
        }
    }

    private static class PanelLayout {
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final int visibleRows;
        private final int contentY;
        private final PanelPlacement placement;

        private PanelLayout(int x, int y, int width, int height, int visibleRows, PanelPlacement placement) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.visibleRows = visibleRows;
            this.contentY = y + HEADER_HEIGHT + 4;
            this.placement = placement;
        }

        private boolean contains(double mouseX, double mouseY) {
            return isWithin(mouseX, mouseY, x, y, width, height);
        }
    }

    private static class TabLayout {
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private TabLayout(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        private boolean contains(double mouseX, double mouseY) {
            return isWithin(mouseX, mouseY, x, y, width, height);
        }
    }

    private enum PanelPlacement {
        BELOW,
        ABOVE,
        RIGHT,
        LEFT
    }
}
