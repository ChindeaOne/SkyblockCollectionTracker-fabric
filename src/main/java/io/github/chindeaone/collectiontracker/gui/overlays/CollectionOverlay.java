package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import io.github.chindeaone.collectiontracker.util.rendering.TextUtils;
import net.minecraft.client.gui.GuiGraphics;

import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.isTracking;

public class CollectionOverlay  {

    private static boolean visible = true;
    public static volatile boolean overlayDirty = false;

    public static boolean isVisible() {
        return visible;
    }

    public static void setVisible(boolean visibility) {
        visible = visibility;
    }

    public static void stopTracking() {
        TextUtils.updateStats();
        setVisible(false);
    }

    public static void render(GuiGraphics guiGraphics) {
        if (!isTracking || !visible) return;

        if (overlayDirty) {
            try {
                TextUtils.updateStats();
            } finally {
                overlayDirty = false;
            }
        }

        RenderUtils.INSTANCE.drawRect(guiGraphics);
    }
}
