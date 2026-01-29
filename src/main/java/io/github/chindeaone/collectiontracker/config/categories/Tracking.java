package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import io.github.chindeaone.collectiontracker.config.categories.overlay.SingleOverlay;
import io.github.notenoughupdates.moulconfig.annotations.Accordion;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
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
            desc = "Show tracking rates summary in chat when stopping a tracking session."
    )
    @SerializedName("showTrackingRatesAtEndOfSession")
    @ConfigEditorBoolean
    public boolean showTrackingRatesAtEndOfSession = true;

    @Expose
    @ConfigOption(
            name = " §b[WIP]§r Sacks Tracking",
            desc = "Toggle this to use Hypixel's sacks messages instead of api calls when tracking.\n§eRecommended to use only if you have a lot of sacks!"
    )
    @SerializedName("enableSacksTracking")
    @ConfigEditorBoolean
    public boolean enableSacksTracking = false;

    @Expose
    @ConfigOption(
            name = "More Sacks Tracking Info",
            desc = "§eFor some collections, it can be more accurate then API calls. " +
                    "However, for now, API calls will still be used, and every API call will update the rates according to the API data, meaning rates can jump around."
    )
    @ConfigEditorInfoText
    public boolean sacksInfo = true;
}
