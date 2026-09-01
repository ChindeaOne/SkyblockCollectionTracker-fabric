package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.utils.HypixelUtils;
import io.github.chindeaone.collectiontracker.utils.rendering.RenderUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class TitleOverlay extends AbstractOverlay{

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
        return HypixelUtils.isInSkyblock();
    }

    @Override
    public void render(GuiGraphicsExtractor context) {
        if (!isEnabled()) return;
        RenderUtils.drawActiveTitle(context);
    }

    @Override
    public void updateDimensions() {
    }
}