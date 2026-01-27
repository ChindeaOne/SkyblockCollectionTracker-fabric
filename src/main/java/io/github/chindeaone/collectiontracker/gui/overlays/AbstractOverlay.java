package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.core.Position;
import net.minecraft.client.gui.GuiGraphics;

/**
 * A general interface for all overlays.
 */
public interface AbstractOverlay {
    String overlayLabel();

    Position position();

    boolean isEnabled();

    boolean isRenderingAllowed();
    void setRenderingAllowed(boolean allowed);

    default boolean shouldRender() {
        return isEnabled() && isRenderingAllowed();
    }

    void render(GuiGraphics context);

    void updateDimensions();
}
