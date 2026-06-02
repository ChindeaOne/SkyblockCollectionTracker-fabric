package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.core.Position;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import java.util.Collections;
import java.util.List;

/**
 * A general abstract class for all overlays.
 */
public abstract class AbstractOverlay {
    protected boolean renderingAllowed = true;

    public abstract String overlayLabel();

    public abstract Position position();

    public abstract boolean isEnabled();

    public boolean isRenderingAllowed() {
        return renderingAllowed;
    }

    public void setRenderingAllowed(boolean allowed) {
        this.renderingAllowed = allowed;
    }

    public boolean shouldRender() {
        return isEnabled() && isRenderingAllowed();
    }

    public abstract void render(GuiGraphics context);

    public abstract void updateDimensions();

    public void handleLineAction(String line) {}

    public List<String> getLines() {
        return Collections.emptyList();
    }

    public boolean handleMouseClick(double mouseX, double mouseY) {
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

    public boolean isHovered(double mouseX, double mouseY) {
        Position pos = this.position();
        if (pos == null) return false;

        int yPadding = 4;
        int x = pos.getX();
        int y = pos.getY();
        float s = pos.getScale();
        int w = Math.round(pos.getWidth() * s);

        double x2 = x + w;
        double y1 = y - (yPadding * s);
        double y2 = y + (pos.getHeight() + yPadding) * s;

        return mouseX >= x && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    }
}
