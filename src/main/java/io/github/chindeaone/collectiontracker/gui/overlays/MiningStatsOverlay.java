package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker;
import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.config.ConfigUtilsKt;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;

public class MiningStatsOverlay {

    private static boolean visible = true;

    public static boolean isVisible() {
        return visible;
    }

    public static void setVisible(boolean visibility) {
        visible = visibility;
    }

    public static void render(GuiGraphics guiGraphics, DeltaTracker deltaTracker) {
        if (!visible || !ConfigAccess.isMiningStatsEnabled()) return;

        RenderUtils.INSTANCE.drawStats(guiGraphics);
    }
}
