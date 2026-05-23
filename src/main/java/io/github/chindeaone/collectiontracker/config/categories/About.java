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

    @ConfigOption(
            name = "§aInfo",
            desc = "§eThis mod is meant to track all collections in Hypixel Skyblock and provide some nice qol features.\n\n\nUse §b/sct commands §eto see all available commands."
    )
    @ConfigEditorInfoText
    public boolean info = true;

    @Expose
    @ConfigOption(
            name = "§bUpdate Stream",
            desc = """
                    Choose how you want to be notified about updates:§f
                     - §cNONE§e: No update checks§f
                     - §aRELEASE§e: Notifies when a new stable version is available§f
                     - §bBETA§e: Notifies about both stable and beta versions
                    """
    )
    @ConfigEditorDropdown
    public UpdateType update = UpdateType.RELEASE; //Default to RELEASE updates

    @Expose
    public boolean hasCheckedUpdate = true;
}
