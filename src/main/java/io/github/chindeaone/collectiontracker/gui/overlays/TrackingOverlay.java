package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass;
import io.github.chindeaone.collectiontracker.tracker.TrackingRenderData;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.util.rendering.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class TrackingOverlay implements AbstractOverlay{

    private final Position position = ConfigAccess.getTrackingPosition();
    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Tracking Overlay";
    }

    @Override public Position position() {
        return this.position;
    }

    @Override
    public boolean isEnabled() {
        return TrackingHandlerClass.isTracking;
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

        TrackingRenderData data = TextUtils.getRenderData();
        if (data.mainLines().isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
            RenderUtils.renderTrackingStringsWithColor(context, data.mainLines(), data.extraLines(), ConfigAccess.isOverlayTextColorEnabled())
        );
    }

    @Override
    public void updateDimensions() {
        List<String> lines = TextUtils.getRenderData().mainLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }
}
