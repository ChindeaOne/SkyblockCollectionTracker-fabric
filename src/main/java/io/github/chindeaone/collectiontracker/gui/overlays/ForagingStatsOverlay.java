package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.tab.ForagingStatsWidget;
import io.github.chindeaone.collectiontracker.utils.world.BlockWatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForagingStatsOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getForagingStatsPosition();
    private final List<String> formattedForagingStats = new ArrayList<>();
    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Foraging Stats Overlay";
    }

    @Override public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isForagingStatsOverlayEnabled() && HypixelUtils.isOnSkyblock();
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
        if (!isEnabled()) return;
        List<String> lines = getForagingLines();

        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderStrings(context, lines)
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getForagingLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    private List<String> getForagingLines() {
        List<String> raw = ForagingStatsWidget.INSTANCE.getRawStats();
        List<String> rawBeacon = ForagingStatsWidget.INSTANCE.getRawBeaconStats();
        if (raw.isEmpty()) return Collections.emptyList();

        formattedForagingStats.clear();
        formattedForagingStats.addAll(ForagingStatsParser.parse(raw, rawBeacon, BlockWatcher.INSTANCE.getForagingBlockType()));
        return formattedForagingStats;
    }
}
