package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingHandler;
import io.github.chindeaone.collectiontracker.tracker.coleweight.ColeweightTrackingRates;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.NumbersUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;

import static io.github.chindeaone.collectiontracker.utils.rendering.TextUtils.formatFloatOrPlaceholder;

public class ColeweightOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getColeweightTrackerPosition();
    private final List<String> trackerLines = new ArrayList<>();
    private boolean renderingAllowed  = true;
    public static volatile boolean trackingDirty = false;

    @Override
    public String overlayLabel() {
        return "Coleweight Tracker";
    }

    @Override
    public Position position() {
        return position;
    }

    @Override
    public boolean isEnabled() {
        return ColeweightTrackingHandler.isTracking && HypixelUtils.isOnSkyblock();
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

        List<String> lines = getLines();
        if (lines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
                RenderUtils.renderColeweightStrings(context, lines));
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = getLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }

    public List<String> getLines() {
        trackerLines.clear();

        trackerLines.add("Coleweight: " + formatFloatOrPlaceholder(ColeweightTrackingRates.getColeweightAmount()));
        trackerLines.add("CW (Session): " + formatFloatOrPlaceholder(ColeweightTrackingRates.getColeweightGained()));
        trackerLines.add("CW/h: " + formatFloatOrPlaceholder(ColeweightTrackingRates.getColeweightPerHour()));
        trackerLines.add("Since Last: " + NumbersUtils.formatFloat(ColeweightTrackingRates.getColeweightSinceLast()));

        long lastUpdateTime = ColeweightTrackingRates.getLastColeweightTime();
        if (lastUpdateTime > 0) {
            long totalSeconds = (System.currentTimeMillis() - lastUpdateTime) / 1000;
            String timeAgo;
            if (totalSeconds < 60) {
                timeAgo = totalSeconds + "s ago";
            } else {
                long min = totalSeconds / 60;
                long sec = totalSeconds % 60;
                timeAgo = String.format("%dm %ds ago", min, sec);
            }
            trackerLines.add("Last updated: " + timeAgo);
        }
        trackerLines.add("Uptime: " + ColeweightTrackingHandler.getUptime());

        return trackerLines;
    }
}
