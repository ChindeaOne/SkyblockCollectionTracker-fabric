package io.github.chindeaone.collectiontracker.config.categories.overlay;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.core.Position;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class MultiCollectionOverlay {

    public enum TrackingOptions {
        COLLECTION("Collection"),
        COLLECTION_RATE("Collection Rate"),
        COLLECTION_MADE("Collection Made"),
        MONEY_RATE("Money Rate"),
        MONEY_MADE("Money Made");

        private final String displayName;

        TrackingOptions(String displayName) {
            this.displayName = displayName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    @Expose
    @ConfigOption(
            name = "Tracking Selection",
            desc = "Select what shows for each collection."
    )
    @ConfigEditorDropdown
    public TrackingOptions trackingOptions = TrackingOptions.COLLECTION_RATE; // Default to collection amount

    @Expose
    @ConfigLink(owner = MultiCollectionOverlay.class, field = "multiCollectionOverlay")
    public Position multiOverlayPosition = new Position(50,100);
}
