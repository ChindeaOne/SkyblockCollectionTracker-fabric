package io.github.chindeaone.collectiontracker.gui.overlays;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.util.rendering.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

public class CommissionsOverlay {

    private static boolean visible = true;

    public static boolean isVisible() {
        return visible;
    }

    public static void setVisible(boolean visibility) {
        visible = visibility;
    }

    public static void render (GuiGraphics guiGraphics) {
        if (!isVisible() || !ConfigAccess.isCommissionsEnabled()) return;

        RenderUtils.INSTANCE.drawCommissions(guiGraphics);
    }
}