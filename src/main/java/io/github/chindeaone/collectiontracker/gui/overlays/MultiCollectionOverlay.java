package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigHelper;
import io.github.chindeaone.collectiontracker.config.categories.Bazaar;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MultiCollectionOverlay implements AbstractOverlay{

    public static volatile boolean trackingDirty = false;
    private final Position position = ConfigAccess.getMultiOverlayPosition();
    public final List<String> multiOverlayLines = new ArrayList<>();
    private boolean renderingAllowed  = true;
    private final List<String> expandedCollections = new ArrayList<>();

    @Override
    public String overlayLabel() {
        return "Multi-Collection Tracker";
    }

    @Override public Position position() {
        return position;
    }

    @Override
    public boolean isEnabled() {
        return MultiTrackingHandler.isMultiTracking() && HypixelUtils.isOnSkyblock();
    }

    @Override
    public boolean isRenderingAllowed() {
        return renderingAllowed;
    }

    @Override
    public void setRenderingAllowed(boolean allowed) {
        renderingAllowed = allowed;
    }

    @Override
    public void render(GuiGraphics context) {
        if (!isEnabled() || !trackingDirty) return;

        List<String> mainLines = getMultiCollectionLines();

        if (mainLines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderMultiTrackingStringsWithColor(context, mainLines, ConfigAccess.isOverlayTextColorEnabled())
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getMultiCollectionLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    @Override
    public List<String> getLines() {
        return getMultiCollectionLines();
    }

    @Override
    public void handleLineAction(String line) {
        String cleanLine = StringUtil.stripColor(line);
        if (cleanLine.startsWith("[+] ") || cleanLine.startsWith("[-] ")) {
            String content = cleanLine.substring(4);
            String collName;

            if (content.startsWith("Gemstone Coll/h") || content.startsWith("Gemstone collection") || content.startsWith("Gemstone $ made") || content.startsWith("Gemstone $/h") || content.startsWith("Gemstones:")) {
                collName = "gemstone";
            } else {
                collName = content.split(":")[0].trim().toLowerCase().replace(" ", "_");
            }

            if (expandedCollections.contains(collName)) {
                expandedCollections.remove(collName);
            } else {
                expandedCollections.add(collName);
            }
            return;
        }

        if (cleanLine.startsWith("Gemstones:") || cleanLine.startsWith("Gemstone:")) {
            String collName = "gemstone";
            if (expandedCollections.contains(collName)) {
                expandedCollections.remove(collName);
            } else {
                expandedCollections.add(collName);
            }
            return;
        }

        switch (line) {
            case "§e[Bazaar Prices]" -> ConfigHelper.setBazaar(true);
            case "§e[NPC Prices]" -> ConfigHelper.setBazaar(false);
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

        updateDimensions();

        Position position = this.position();
        int x = position.getX();
        int y = position.getY();
        float scale = position.getScale();

        int height = (int) (position.getHeight() * scale);
        int width = (int) (position.getWidth() * scale);

        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private @NotNull List<String> getMultiCollectionLines() {
        boolean isChatOpened = Minecraft.getInstance().screen instanceof ChatScreen;
        TextUtils.updateMultiTrackingLines(multiOverlayLines, expandedCollections, isChatOpened);
        List<String> lines = new ArrayList<>(multiOverlayLines);
        lines.add("Uptime: " + MultiTrackingHandler.getMultiUptime());
        if (isChatOpened) {
            TextUtils.addToggleableSettingsLines(lines);
        }
        return lines;
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
