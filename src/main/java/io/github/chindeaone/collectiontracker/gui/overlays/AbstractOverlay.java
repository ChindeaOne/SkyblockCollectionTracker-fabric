package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.core.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Collections;
import java.util.List;

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

    default void handleLineAction(String line) {}

    default List<String> getLines() {
        return Collections.emptyList();
    }

    default boolean handleMouseClick(double mouseX, double mouseY) {
        if (!isEnabled() || !isHovered(mouseX, mouseY)) return false;

        List<String> lines = getLines();
        if (lines.isEmpty()) return false;

        Position position = this.position();
        int x = position.getX();
        int y = position.getY();
        float scale = position.getScale();

        Font fr = Minecraft.getInstance().font;
        int height = (int) (fr.lineHeight * lines.size() * scale);
        int width = (int) (position.getWidth() * scale);

        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height) {
            double relativeY = mouseY - y;
            int lineClicked = (int) (relativeY / (fr.lineHeight * scale));

            if (lineClicked >= 0 && lineClicked < lines.size()) {
                handleLineAction(lines.get(lineClicked));
                return true;
            }
        }
        return false;
    }

    default boolean isHovered(double mouseX, double mouseY) {
        return false;
    }
}
