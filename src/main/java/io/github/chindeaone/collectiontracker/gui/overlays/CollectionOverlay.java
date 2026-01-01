package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

import static io.github.chindeaone.collectiontracker.tracker.TrackingHandlerClass.isTracking;
import static io.github.chindeaone.collectiontracker.util.rendering.TextUtils.updateStats;

public class CollectionOverlay  {

    private static boolean visible = true;

    public static boolean isVisible() {
        return visible;
    }

    public static void setVisible(boolean visibility) {
        visible = visibility;
    }

    public static void stopTracking() {
        updateStats();
        setVisible(false);
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!isTracking || !visible || SkyblockCollectionTracker.configManager.getConfig() == null) return;

        RenderUtils.INSTANCE.drawRect(guiGraphics);
    }
}
