package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.chindeaone.collectiontracker.mixins.AccessorGuiContainer;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
//? if = 1.21.10{
import net.minecraft.client.input.MouseButtonEvent;
//? }
import net.minecraft.network.chat.Component;

public class DummyOverlay extends Screen {

    private boolean draggingSingle = false;
    private boolean draggingCommissions = false;
    private int dragOffsetX, dragOffsetY;
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
        if (CommissionsOverlay.isVisible()) {
            CommissionsOverlay.setVisible(false);
        }
        super.init();
    }

    @Override
    public void onClose() {
        CollectionOverlay.setVisible(true);
        CommissionsOverlay.setVisible(true);
        Minecraft.getInstance().setScreen(oldScreen);
    }

    @Override
    public void render(GuiGraphics context, int mouseX, int mouseY, float partialTicks) {
        if (CollectionOverlay.isVisible() || CommissionsOverlay.isVisible()) {
            return;
        }

        renderMenuBackground(context);

        if (oldScreen != null) {
            ((AccessorGuiContainer) oldScreen)
                    .invokeDrawGuiContainerBackgroundLayer_sct(context, partialTicks, -1, -1);
        }

        // Draw all dummies
        RenderUtils.INSTANCE.drawRectDummy(context);
        RenderUtils.INSTANCE.drawCommissionsDummy(context);

        Position activePos = null;

        // Update dragging positions
        if (draggingSingle) {
            RenderUtils.INSTANCE.getPosition().setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
        }
        if (draggingCommissions) {
            RenderUtils.INSTANCE.getCommissionsPosition().setPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
        }

        if (isMouseOverOverlay(mouseX, mouseY)) {
            activePos = RenderUtils.INSTANCE.getPosition();
        } else if (isMouseOverCommissionsOverlay(mouseX, mouseY)) {
            activePos = RenderUtils.INSTANCE.getCommissionsPosition();
        }

        RenderUtils.INSTANCE.drawStaticText(context, activePos);
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
        if (isMouseOverCommissionsOverlay(mx, my)) {
            float next = RenderUtils.INSTANCE.getCommissionsPosition().getScale() + (verticalAmount > 0 ? scaleChange : -scaleChange);
            RenderUtils.INSTANCE.getCommissionsPosition().setScaling(clamp(next));
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    //? if = 1.21.8{
    
    /*@Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 0) {
            int mx = (int) mouseX;
            int my = (int) mouseY;

            if (isMouseOverOverlay(mx, my)) {
                draggingSingle = true;
                dragOffsetX = mx - RenderUtils.INSTANCE.getPosition().getX();
                dragOffsetY = my - RenderUtils.INSTANCE.getPosition().getY();
                return true;
            }
            if (isMouseOverCommissionsOverlay(mx, my)) {
                draggingCommissions = true;
                dragOffsetX = mx - RenderUtils.INSTANCE.getCommissionsPosition().getX();
                dragOffsetY = my - RenderUtils.INSTANCE.getCommissionsPosition().getY();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int state) {
        draggingSingle = false;
        draggingCommissions = false;
        return super.mouseReleased(mouseX, mouseY, state);
    }
     
    *///? } else {
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 0) {
            int mx = (int) event.x();
            int my = (int) event.y();

            if (isMouseOverOverlay(mx, my)) {
                draggingSingle = true;
                dragOffsetX = mx - RenderUtils.INSTANCE.getPosition().getX();
                dragOffsetY = my - RenderUtils.INSTANCE.getPosition().getY();
                return true;
            }
            if (isMouseOverCommissionsOverlay(mx, my)) {
                draggingCommissions = true;
                dragOffsetX = mx - RenderUtils.INSTANCE.getCommissionsPosition().getX();
                dragOffsetY = my - RenderUtils.INSTANCE.getCommissionsPosition().getY();
                return true;
            }
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        draggingSingle = false;
        draggingCommissions = false;
        return super.mouseReleased(event);
    }
    //? }

    private boolean isMouseOverOverlay(int mouseX, int mouseY) {
        int x = RenderUtils.INSTANCE.getPosition().getX();
        int y = RenderUtils.INSTANCE.getPosition().getY();
        int w = RenderUtils.INSTANCE.getPosition().getWidth();
        int h = RenderUtils.INSTANCE.getPosition().getHeight();
        float s = RenderUtils.INSTANCE.getPosition().getScale();
        return mouseX >= x && mouseX <= x + Math.round(w * s) && mouseY >= y && mouseY <= y + Math.round(h * s);
    }

    private boolean isMouseOverCommissionsOverlay(int mouseX, int mouseY) {
        int x = RenderUtils.INSTANCE.getCommissionsPosition().getX();
        int y = RenderUtils.INSTANCE.getCommissionsPosition().getY();
        int w = RenderUtils.INSTANCE.getCommissionsPosition().getWidth();
        int h = RenderUtils.INSTANCE.getCommissionsPosition().getHeight();
        float s = RenderUtils.INSTANCE.getCommissionsPosition().getScale();
        return mouseX >= x && mouseX <= x + Math.round(w * s) && mouseY >= y && mouseY <= y + Math.round(h * s);
    }

    private static float clamp(float v) {
        return Math.max((float) 0.1, Math.min((float) 10.0, v));
    }
}