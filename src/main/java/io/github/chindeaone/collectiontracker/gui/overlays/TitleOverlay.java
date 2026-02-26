package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

public class TitleOverlay implements AbstractOverlay{

    private boolean renderingAllowed  = true;

    @Override
    public String overlayLabel() {
        return "Global Title";
    }

    @Override
    public Position position() {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return HypixelUtils.isOnSkyblock();
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
        RenderUtils.drawActiveTitle(context);
    }

    @Override
    public void updateDimensions() {
    }
}