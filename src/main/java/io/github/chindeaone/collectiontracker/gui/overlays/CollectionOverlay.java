package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;
import io.github.chindeaone.collectiontracker.config.categories.Bazaar;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CollectionOverlay implements AbstractOverlay{

    public static volatile boolean trackingDirty = false;
    private final Position position = ConfigAccess.getTrackingPosition();
    public final List<String> overlayLines = new ArrayList<>();
    public final List<String> extraOverlayLines = new ArrayList<>();
    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Collection Tracker";
    }

    @Override public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return TrackingHandler.isTracking && HypixelUtils.isOnSkyblock();
    }

    @Override
    public boolean isRenderingAllowed() {
        return renderingAllowed;
    }

    @Override
    public void setRenderingAllowed(boolean allowed) {
        this.renderingAllowed = allowed;
    }

    @Override
    public void render(GuiGraphics context) {
        if (!isEnabled() || !trackingDirty) return;

        List<String> mainLines = getCollectionLines();
        List<String> extraLines = getCollectionExtraLines();

        if (mainLines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
            RenderUtils.renderTrackingStringsWithColor(context, mainLines, extraLines, ConfigAccess.isOverlayTextColorEnabled())
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getCollectionLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    @Override
    public List<String> getLines() {
        if (!isEnabled()) return Collections.emptyList();

        List<String> combinedLines = new ArrayList<>(getCollectionLines());
        if (ConfigAccess.isShowExtraStats()) {
            // append extra lines to create a single list
            combinedLines.add(""); // add separator line
            combinedLines.addAll(getCollectionExtraLines());
        }

        return combinedLines;
    }

    @Override
    public void handleLineAction(String line) {
        switch (line) {
            case "§e[Bazaar Prices]" -> ConfigHelper.setBazaar(true);
            case "§e[NPC Prices]" -> ConfigHelper.setBazaar(false);
            case "§e[Extra Stats]" -> ConfigHelper.setShowExtraStats(!ConfigAccess.isShowExtraStats());
        }
        if (line.contains(ConfigAccess.getGemstoneVariant().toString())) {
            cycleGemstoneVariant();
        }
        if (line.contains("version")) {
            changeEnchantedType();
        }
        if (line.contains("Instant")) {
            changeBazaarPriceType();
        }
    }

    @Override
    public boolean isHovered(double mouseX, double mouseY) {
        if (!isEnabled()) return false;

        List<String> mainLines = getCollectionLines();
        if (mainLines.isEmpty()) return false;

        Position pos = this.position();
        float scale = pos.getScale();
        Font fr = Minecraft.getInstance().font;

        int totalLines = mainLines.size();

        if (ConfigAccess.isShowExtraStats()) {
            List<String> extraLines = getCollectionExtraLines();
            if (!extraLines.isEmpty()) {
                totalLines += 1; // add separator line
                totalLines += extraLines.size();
            }
        }

        int height = (int) (fr.lineHeight * totalLines * scale);
        int width = (int) (pos.getWidth() * scale);
        int x = pos.getX();
        int y = pos.getY();

        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private @NotNull List<String> getCollectionLines() {
        TextUtils.updateTrackingLines(overlayLines);
        if (overlayLines.isEmpty()) return overlayLines;
        List<String> lines = new ArrayList<>(overlayLines);
        lines.add("Uptime: " + TrackingHandler.getUptime());
        if (!ConfigAccess.isShowExtraStats()) {
            if (Minecraft.getInstance().screen instanceof ChatScreen) {
                TextUtils.addToggleableSettingsLines(lines);
            }
        }
        return lines;
    }

    private @NotNull List<String> getCollectionExtraLines() {
        if (!ConfigAccess.isShowExtraStats()) return Collections.emptyList();
        TextUtils.updateTrackingExtraLines(extraOverlayLines);
        if (Minecraft.getInstance().screen instanceof ChatScreen) {
            TextUtils.addToggleableSettingsLines(extraOverlayLines);
        }
        return extraOverlayLines;
    }

    private void cycleGemstoneVariant() {
        Bazaar.GemstoneVariant[] variants = Bazaar.GemstoneVariant.values();
        Bazaar.GemstoneVariant current = ConfigAccess.getGemstoneVariant();
        int nextOrdinal = (current.ordinal() + 1) % variants.length;
        ConfigHelper.setGemstoneVariant(variants[nextOrdinal]);
    }

    private void changeEnchantedType() {
        ConfigHelper.setBazaarType(ConfigAccess.getBazaarType() == Bazaar.BazaarType.ENCHANTED_VERSION ?
                Bazaar.BazaarType.SUPER_ENCHANTED_VERSION : Bazaar.BazaarType.ENCHANTED_VERSION);
    }

    private void changeBazaarPriceType() {
        ConfigHelper.changeBazaarPrice(ConfigAccess.getBazaarPriceType() == Bazaar.BazaarPriceType.INSTANT_BUY ?
                Bazaar.BazaarPriceType.INSTANT_SELL : Bazaar.BazaarPriceType.INSTANT_BUY);
    }
}