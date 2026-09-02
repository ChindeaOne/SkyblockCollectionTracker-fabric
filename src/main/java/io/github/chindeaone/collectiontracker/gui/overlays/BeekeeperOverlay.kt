package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.TextUtils;
import io.github.chindeaone.collectiontracker.utils.world.IslandTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BeekeeperOverlay extends AbstractOverlay{

    private final Position position = ConfigAccess.getBeekeeperPosition();
    private final List<String> beekeeperOverlayLines = new ArrayList<>();

    @Override
    public String overlayLabel() {
        return "Beekeeper";
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isBeekeeperEnabled() && HypixelUtils.isInSkyblock();
    }

    @Override
    public void render(GuiGraphicsExtractor context) {
        if (!isEnabled()) return;
        List<String> lines = getBeekeeperLines();

        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderStrings(context, lines)
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getBeekeeperLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    private List<String> getBeekeeperLines() {
        beekeeperOverlayLines.clear();

        if (ConfigAccess.isBeekeeperInForagingIslandsOnly() && IslandTracker.getCurrentForagingIsland() == null) return Collections.emptyList();

        beekeeperOverlayLines.add("§6Beekeeper: " + ChatListener.getCurrentBeekeeperBuff());
        beekeeperOverlayLines.add(TextUtils.updateTimer());
        return beekeeperOverlayLines;
    }
}
