package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.ConfigLink;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OverlaySingle {

    public enum OverlayExampleText {
        GOLD_COLLECTION("§aGold collection §f> 200.000M"),
        GOLD_COLLECTION_SESSION("§aGold collection (session) §f> 10.000M"),
        COLL_PER_HOUR("§aColl/h §f> Calculating..."),
        MONEY_PER_HOUR("§a$/h (NPC/Bazaar) §f> 100k/h"),
        EXTRAS("§aExtras");

        private final String text;

        OverlayExampleText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Expose
    @ConfigOption(
            name = "Overlay Text",
            desc = "Drag text to change the appearance of the overlaySingle."
    )
    @ConfigEditorDraggableList
    public List<OverlayExampleText> statsText = new ArrayList<>(Arrays.asList(
            OverlayExampleText.GOLD_COLLECTION,
            OverlayExampleText.GOLD_COLLECTION_SESSION,
            OverlayExampleText.COLL_PER_HOUR,
            OverlayExampleText.MONEY_PER_HOUR,
            OverlayExampleText.EXTRAS
    ));

    @Expose
    @ConfigLink(owner = OverlaySingle.class, field = "singleStatsOverlay")
    public Position overlayPosition = new Position(100, 150);
}
