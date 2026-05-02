package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.parser.MiningStatsParser;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.tab.MiningStatsWidget;
import io.github.chindeaone.collectiontracker.utils.world.BlockWatcher;
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker;
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MiningStatsOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getMiningStatsPosition();
    private final List<String> formattedMiningStats = new ArrayList<>();
    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Mining Stats";
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isMiningStatsEnabled() && HypixelUtils.isOnSkyblock();
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
        if (!isEnabled()) return;
        List<String> lines = getMiningLines();

        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
            RenderUtils.renderStrings(context, lines)
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getMiningLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    private List<String> getMiningLines() {
        if (ConfigAccess.isMiningStatsOverlayInMiningIslandsOnly() && !MiningMapping.INSTANCE.getMiningIslands().contains(IslandTracker.getCurrentMiningIsland())) return Collections.emptyList();
        List<String> raw = MiningStatsWidget.INSTANCE.getRawStats();
        if (raw.isEmpty()) return Collections.emptyList();

        formattedMiningStats.clear();
        formattedMiningStats.addAll(MiningStatsParser.parse(raw, BlockWatcher.INSTANCE.getMiningBlockType()));
        return formattedMiningStats;
    }
}
