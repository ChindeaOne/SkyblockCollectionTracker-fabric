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

    @Expose
    @ConfigOption(
            name = "Tracking Summary",
            desc = "§eShow tracking rates summary in chat when stopping a tracking session."
    )
    @SerializedName("showTrackingRatesAtEndOfSession")
    @ConfigEditorBoolean
    public boolean showTrackingRatesAtEndOfSession = true;

    @Expose
    @ConfigOption(
            name = "Sacks Tracking",
            desc = "Toggle this to use Hypixel's sacks messages instead of api calls when tracking.\n§cIt's a WIP feature and it may not display accurate rates."
    )
    @SerializedName("enableSacksTracking")
    @ConfigEditorBoolean
    public boolean enableSacksTracking = false;

}
