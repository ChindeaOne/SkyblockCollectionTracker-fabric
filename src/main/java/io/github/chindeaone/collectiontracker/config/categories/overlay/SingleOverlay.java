package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.chindeaone.collectiontracker.config.core.ConfigLink;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SingleOverlay {

    public enum OverlayText {
        COLLECTION("§aGold collection§f: 200.000M"),
        COLLECTION_SESSION("§aGold collection (session)§f: 10.000M"),
        COLL_PER_HOUR("§aColl/h§f: Calculating..."),
        MONEY_PER_HOUR("§a$/h (NPC/Bazaar)§f: 100k/h"),
        MONEY_MADE("§a$ made (NPC/Bazaar)§f: 1.000M"),
        COLLECTION_SINCE_LAST("§aCollected since last§f: 200k");

        private final String text;

        OverlayText(String text) {
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
            desc = "Drag the lines of text to change the appearance of the overlay."
    )
    @ConfigEditorDraggableList
    public List<OverlayText> statsText = new ArrayList<>(Arrays.asList(
            OverlayText.COLLECTION,
            OverlayText.COLLECTION_SESSION,
            OverlayText.COLL_PER_HOUR,
            OverlayText.MONEY_PER_HOUR,
            OverlayText.MONEY_MADE,
            OverlayText.COLLECTION_SINCE_LAST
    ));

    @Expose
    @ConfigOption(
            name = "Extra Stats",
            desc = "Show extra stats in the overlay."
    )
    @ConfigEditorBoolean
    public boolean showExtraStats = false;

    public enum OverlayExtraText {

        BAZAAR_ITEM("§aItem/Variant§f: Enchanted gold"),
        BAZAAR_PRICE("§aItem/Variant price§f: 100k");

        private final String text;

        OverlayExtraText(String text) { this.text = text; }

        @Override
        public String toString() { return text; }
    }

    @Expose
    @ConfigOption(
            name = "Overlay Extra Text",
            desc = "Drag the lines of text to change the appearance of the extra stats of the overlay.\n§eDoesn't work if 'Extra Stats' is disabled!"
    )
    @ConfigEditorDraggableList
    public List<OverlayExtraText> extraStatsText = new ArrayList<>(Arrays.asList(
            OverlayExtraText.BAZAAR_ITEM,
            OverlayExtraText.BAZAAR_PRICE
    ));

    @Expose
    @ConfigOption(
            name = "Explicit values",
            desc = "Show full values instead of rounded values in the overlay and summary."
    )
    @SerializedName("explicitValues")
    @ConfigEditorBoolean
    public boolean explicitValues = false;

    @Expose
    @ConfigLink(owner = SingleOverlay.class, field = "singleStatsOverlay")
    public Position overlayPosition = new Position(50, 100);
}
