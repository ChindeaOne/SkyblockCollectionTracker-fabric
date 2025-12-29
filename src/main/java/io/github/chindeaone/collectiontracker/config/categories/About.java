package io.github.chindeaone.collectiontracker.config.categories;

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.config.version.VersionDisplay;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown;
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText;
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption;

public class About {
    @ConfigOption(name = "Current Version", desc = "This is the SkyblockCollectionTracker version you are currently running")
    @VersionDisplay
    public transient Void currentVersion = null;

    @Expose
    @ConfigOption(name = "§aInfo", desc = "This mod is meant to track (almost) any collection that exists. You can also use it as a money tracker.")
    @ConfigEditorInfoText()
    public boolean info = true;

    @Expose
    @ConfigOption(
            name = "Update Stream",
            desc = "Choose which type of notifications you want to receive about newer versions of the mod."
    )
    @ConfigEditorDropdown(
            values = {"None", "Full releases", "Beta releases"}
    )
    public int update = 0;

    @Expose
    public boolean hasCheckedUpdate = false;
}
