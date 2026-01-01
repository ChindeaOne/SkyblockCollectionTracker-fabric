package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.mixins.AccessorGuiContainer;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;

public class DummyOverlay extends Screen {

    private boolean draggingSingle = false;
    private boolean draggingList = false;
    private int dragOffsetX, dragOffsetY;
    private int dragOffsetListX, dragOffsetListY;
    private AbstractContainerScreen<?> oldScreen = null;

    public DummyOverlay(AbstractContainerScreen<?> oldScreen) {
        super(Component.empty());
        this.oldScreen = oldScreen;
    }

    @Override
    protected void init() {
        if (CollectionOverlay.isVisible()) {
            CollectionOverlay.setVisible(false);
        }
        super.init();
    }

    @Override
    public void onClose() {
        CollectionOverlay.setVisible(true);
        Minecraft.getInstance().setScreen(oldScreen);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        if (CollectionOverlay.isVisible()) {
            return;
        }

        renderMenuBackground(context);

        if (oldScreen != null) {
            ((AccessorGuiContainer) oldScreen)
                    .invokeDrawGuiContainerBackgroundLayer_sct(context, partialTicks, -1, -1);
        }

        // Draw both single and list dummy overlays
        RenderUtils.INSTANCE.drawRectDummy(context);
        RenderUtils.INSTANCE.drawRectDummyList(context);

        // Update dragging positions
        if (draggingSingle) {
            RenderUtils.INSTANCE.getPosition().setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
        }
        if (draggingList) {
            RenderUtils.INSTANCE.getPositionList().setPosition(mouseX - dragOffsetListX, mouseY - dragOffsetListY);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount == 0) return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);

        int mx = (int) mouseX;
        int my = (int) mouseY;

        float scaleChange = 0.05f;

        if (isMouseOverOverlay(mx, my)) {
            float next = RenderUtils.INSTANCE.getPosition().getScale() + (verticalAmount > 0 ? scaleChange : -scaleChange);
            RenderUtils.INSTANCE.getPosition().setScaling(clamp(next));
            return true;
        }

        if (isMouseOverOverlayList(mx, my)) {
            float next = RenderUtils.INSTANCE.getPositionList().getScale() + (verticalAmount > 0 ? scaleChange : -scaleChange);
            RenderUtils.INSTANCE.getPositionList().setScaling(clamp(next));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            int mx = (int) mouseX;
            int my = (int) mouseY;

            if (isMouseOverOverlayList(mx, my)) {
                draggingList = true;
                dragOffsetListX = mx - RenderUtils.INSTANCE.getPositionList().getX();
                dragOffsetListY = my - RenderUtils.INSTANCE.getPositionList().getY();
                return true;
            }

            if (isMouseOverOverlay(mx, my)) {
                draggingSingle = true;
                dragOffsetX = mx - RenderUtils.INSTANCE.getPosition().getX();
                dragOffsetY = my - RenderUtils.INSTANCE.getPosition().getY();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        draggingSingle = false;
        draggingList = false;
        return super.mouseReleased(mouseX, mouseY, state);
    }

    private boolean isMouseOverOverlay(int mouseX, int mouseY) {
        int x = RenderUtils.INSTANCE.getPosition().getX();
        int y = RenderUtils.INSTANCE.getPosition().getY();
        int w = RenderUtils.INSTANCE.getPosition().getWidth();
        int h = RenderUtils.INSTANCE.getPosition().getHeight();
        float s = RenderUtils.INSTANCE.getPosition().getScale();
        return mouseX >= x && mouseX <= x + Math.round(w * s) && mouseY >= y && mouseY <= y + Math.round(h * s);
    }

    private boolean isMouseOverOverlayList(int mouseX, int mouseY) {
        int x = RenderUtils.INSTANCE.getPositionList().getX();
        int y = RenderUtils.INSTANCE.getPositionList().getY();
        int w = RenderUtils.INSTANCE.getPositionList().getWidth();
        int h = RenderUtils.INSTANCE.getPositionList().getHeight();
        float s = RenderUtils.INSTANCE.getPositionList().getScale();
        return mouseX >= x && mouseX <= x + Math.round(w * s) && mouseY >= y && mouseY <= y + Math.round(h * s);
    }

    private static float clamp(float v) {
        return Math.max((float) 0.1, Math.min((float) 10.0, v));
    }
}