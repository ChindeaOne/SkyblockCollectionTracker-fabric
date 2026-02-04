package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.gui.OverlayManager;
import io.github.chindeaone.collectiontracker.mixins.AccessorGuiContainer;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//? if = 1.21.11 {
import org.jetbrains.annotations.NotNull;
//? }
//? if >= 1.21.10 {
import net.minecraft.client.input.MouseButtonEvent;
//? }
import net.minecraft.network.chat.Component;

public class DummyOverlay extends Screen {

    private AbstractOverlay dragging;
    private int dragOffsetX, dragOffsetY;
    private final AbstractContainerScreen<?> oldScreen;

    public DummyOverlay(AbstractContainerScreen<?> oldScreen) {
        super(Component.empty());
        this.oldScreen = oldScreen;
    }

    @Override
    public void onClose() {
        OverlayManager.setGlobalRendering(true);
        Minecraft.getInstance().setScreen(oldScreen);
    }

    @Override
    public void render(/*? if = 1.21.11 {*/@NotNull /*?}*/GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        if (!OverlayManager.isInEditorMode()) {
            return;
        }

        renderMenuBackground(context);

        if (oldScreen != null) {
            ((AccessorGuiContainer) oldScreen)
                    .invokeDrawGuiContainerBackgroundLayer_sct(context, partialTicks, -1, -1);
        }

        AbstractOverlay hovered = null;
        // Draw all dummies
        for (AbstractOverlay overlay : OverlayManager.all()) {
            overlay.updateDimensions();

            RenderUtils.drawDummyFrame(context,overlay.position(), overlay.overlayLabel());

            if (isMouseOver(mouseX, mouseY, overlay.position())) {
                hovered = overlay;
            }
        }
        // Update dragging positions
        if (dragging != null) {
            dragging.position().setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
        }

        RenderUtils.drawEditorHudText(context, hovered != null ? hovered.position() : null);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!OverlayManager.isInEditorMode()) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        if (verticalAmount == 0) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        int mx = (int) mouseX;
        int my = (int) mouseY;

        for (AbstractOverlay overlay : OverlayManager.all()) {
            Position pos = overlay.position();

            if (isMouseOver(mx, my, pos)) {
                float scaleChange = 0.05f;
                float next = pos.getScale() + (verticalAmount > 0 ? scaleChange : -scaleChange);
                pos.setScaling(clamp(next));
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    //? if = 1.21.8 {
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            int mx = (int) mouseX;
            int my = (int) mouseY;

            for (AbstractOverlay overlay : OverlayManager.all()) {
                if (isMouseOver(mx, my, overlay.position())) {
                    dragging = overlay;
                    dragOffsetX = mx - overlay.position().getX();
                    dragOffsetY = my - overlay.position().getY();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        dragging = null;
        return super.mouseReleased(mouseX, mouseY, state);
    }
     
    *///? } else {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();

            for (AbstractOverlay overlay : OverlayManager.all()) {
                if (isMouseOver(mx, my, overlay.position())) {
                    dragging = overlay;
                    dragOffsetX = mx - overlay.position().getX();
                    dragOffsetY = my - overlay.position().getY();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseReleased(/*? if = 1.21.11 {*/@NotNull /*?}*/MouseButtonEvent event) {
        dragging = null;
        return super.mouseReleased(event);
    }
    //? }

    private boolean isMouseOver(int mouseX, int mouseY, Position pos) {
        int x = pos.getX();
        int y = pos.getY();
        int w = pos.getWidth();
        int h = pos.getHeight();
        float s = pos.getScale();
        return mouseX >= x && mouseX <= x + Math.round(w * s)
                && mouseY >= y && mouseY <= y + Math.round(h * s);
    }

    private static float clamp(float v) {
        return Math.max((float) 0.1, Math.min((float) 10.0, v));
    }
}