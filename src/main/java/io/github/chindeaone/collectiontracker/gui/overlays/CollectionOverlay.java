package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler;
import io.github.chindeaone.collectiontracker.util.HypixelUtils;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.util.rendering.TextUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

import java.util.List;

public class CollectionOverlay implements AbstractOverlay{

    public static volatile boolean trackingDirty = false;
    private final Position position = ConfigAccess.getTrackingPosition();
    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Collection Overlay";
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

        List<String> mainLines = TextUtils.getCollectionLines();
        List<String> extraLines = TextUtils.getCollectionExtraLines();

        if (mainLines.isEmpty()) return;

        RenderUtils.drawOverlayFrame(context, position, () ->
            RenderUtils.renderTrackingStringsWithColor(context, mainLines, extraLines, ConfigAccess.isOverlayTextColorEnabled())
        );
    }

    @Override
    public void updateDimensions() {
        if (!isEnabled()) return;
        List<String> lines = TextUtils.getCollectionLines();
        if (lines.isEmpty()) return;

        Font fr = Minecraft.getInstance().font;
        int maxW = 0;
        for (String l : lines) maxW = Math.max(maxW, fr.width(l));
        int h = fr.lineHeight * lines.size();

        position.setDimensions(maxW, h);
    }
}