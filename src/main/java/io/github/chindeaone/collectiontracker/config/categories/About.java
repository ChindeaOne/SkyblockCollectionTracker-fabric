package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.version.VersionDisplay;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class About {

    public enum UpdateType {
        NONE,
        RELEASE,
        BETA
    }

    @ConfigOption(name = "Current Version", desc = "This is the SkyblockCollectionTracker version you are currently running.")
    @VersionDisplay
    @SuppressWarnings("unused")
    public transient Void currentVersion = null;

    @Expose
    @ConfigOption(
            name = "§aInfo",
            desc = "§eThis mod is meant to track (almost) all collections in Hypixel Skyblock.")
    @ConfigEditorInfoText
    public boolean info = true;

    @Expose
    @ConfigOption(
            name = "§bUpdate Stream",
            desc = "Choose update checks: NONE = no update checks; RELEASE = check stable releases only; BETA = include beta updates."
    )
    @ConfigEditorDropdown
    public UpdateType update = UpdateType.RELEASE; //Default to RELEASE updates

    @Expose
    public boolean hasCheckedUpdate = true;
}
