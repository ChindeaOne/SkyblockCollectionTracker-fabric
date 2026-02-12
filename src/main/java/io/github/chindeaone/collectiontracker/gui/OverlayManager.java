package io.github.chindeaone.collectiontracker.gui;

import io.github.chindeaone.collectiontracker.gui.overlays.*;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class OverlayManager {

    private static final Map<String, AbstractOverlay> overlays = new LinkedHashMap<>();
    private static boolean globalRenderingAllowed = true;

    public static void add(AbstractOverlay overlay) {
        overlays.put(overlay.overlayLabel(), overlay);
    }

    public static Collection<AbstractOverlay> all() {
        return overlays.values();
    }

    public static void setGlobalRendering(boolean allowed) {
        globalRenderingAllowed = allowed;
        overlays.values().forEach(o -> o.setRenderingAllowed(allowed));
    }

    public static boolean isInEditorMode() {
        return !globalRenderingAllowed;
    }

    public static void setTrackingOverlayRendering(boolean allowed) {
        AbstractOverlay overlay = overlays.get("Collection Overlay");
        if (overlay != null) {
            overlay.setRenderingAllowed(allowed);
        }
    }

    public static void setSkillOverlayRendering(boolean allowed) {
        AbstractOverlay overlay = overlays.get("Skill Overlay");
        if (overlay != null) {
            overlay.setRenderingAllowed(allowed);
        }
    }

    public static void overlayRegistration() {
        OverlayManager.add(new CollectionOverlay());
        OverlayManager.add(new MiningStatsOverlay());
        OverlayManager.add(new CommissionsOverlay());
        OverlayManager.add(new SkillOverlay());
        OverlayManager.add(new ForagingStatsOverlay());
        OverlayManager.add(new SkyMallOverlay());
        OverlayManager.add(new LotteryOverlay());
        OverlayManager.add(new PickaxeAbilityOverlay());
        OverlayManager.add(new AxeAbilityOverlay());
    }
}