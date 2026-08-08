package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.parser.ForagingStatsParser;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.tab.ForagingStatsWidget;
import io.github.chindeaone.collectiontracker.utils.world.BlockWatcher;
import io.github.chindeaone.collectiontracker.utils.world.ForagingMapping;
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ForagingStatsOverlay extends AbstractOverlay{

    private final Position position = ConfigAccess.getForagingStatsPosition();
    private final List<String> formattedForagingStats = new ArrayList<>();

    @Override
    public String overlayLabel() {
        return "Foraging Stats";
    }

    @Override public Position position() {
        return position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isForagingStatsOverlayEnabled() && HypixelUtils.isOnSkyblock();
    }

    @Override
    public void render(GuiGraphicsExtractor context) {
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
        if (ConfigAccess.foragingStatsOverlayInForagingIslandsOnly() && !ForagingMapping.getForagingIslands().contains(IslandTracker.getCurrentForagingIsland())) return Collections.emptyList();

        List<String> raw = ForagingStatsWidget.getRawStats();
        List<String> rawBeacon = ForagingStatsWidget.getRawBeaconStats();
        String rawStarbornTemple = ForagingStatsWidget.getRawStarbornTempleStats();
        if (raw.isEmpty()) return Collections.emptyList();

        formattedForagingStats.clear();
        formattedForagingStats.addAll(ForagingStatsParser.parse(raw, rawBeacon, rawStarbornTemple, BlockWatcher.getForagingBlockType()));
        return formattedForagingStats;
    }
}
