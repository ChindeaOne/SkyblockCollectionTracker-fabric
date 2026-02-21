package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TemporaryBuffsOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getTempBuffPosition();
    private final List<String> tempBuffLines = new ArrayList<>();
    private boolean renderingAllowed = true;

    @Override
    public String overlayLabel() {
        return "Temporary Buffs";
    }

    @Override
    public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return ConfigAccess.isTempBuffTrackerEnabled() && HypixelUtils.isOnSkyblock();
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
        List<String> lines = getTempBuffLines();

        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderStrings(context, lines)
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getTempBuffLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    private List<String> getTempBuffLines() {
        tempBuffLines.clear();

        if (!ConfigAccess.isTempBuffTrackerEnabled()) return Collections.emptyList();

        long now = System.currentTimeMillis();

        addBuffLine("§6Refined Dark Cacao Truffle", ConfigAccess.getRefinedCacaoTime(), now);
        addBuffLine("§9Filet O' Fortune", ConfigAccess.getFiletTime(), now);
        addBuffLine("§5Chilled Pristine Potato", ConfigAccess.getPristinePotatoTime(), now);
        addBuffLine("§aPowder Pumpkin", ConfigAccess.getPowderPumpkinTime(), now);

        return tempBuffLines;
    }

    private void addBuffLine(String buffName, long expireTime, long now) {
        long diff = expireTime - now;
        if (diff <= 0) return;

        long totalSeconds = diff / 1000;
        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        String formattedTime;
        if (minutes >= 5) {
            formattedTime = minutes + "m";
        } else {
            formattedTime = String.format("%d:%02d", minutes, seconds);
        }

        tempBuffLines.add(buffName + " §e" + formattedTime);
    }
}
