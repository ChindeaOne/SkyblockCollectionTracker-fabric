package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.chindeaone.collectiontracker.config.categories.overlay.SingleOverlay;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class Tracking {

    @Expose
    @ConfigOption(name = "Single Overlay", desc = "")
    @SerializedName("single_overlay")
    @Accordion
    public SingleOverlay singleOverlay = new SingleOverlay();

    @Expose
    @ConfigOption(
            name = "Overlay Text Color",
            desc = "Toggle this to enable color-coded overlay."
    )
    @SerializedName("overlayTextColor")
    @ConfigEditorBoolean
    public boolean overlayTextColor = false;
}
