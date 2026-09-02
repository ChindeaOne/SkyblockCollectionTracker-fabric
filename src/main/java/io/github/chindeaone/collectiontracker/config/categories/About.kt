package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.version.VersionDisplay
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class About {
    enum class UpdateStream {
        NONE,
        RELEASE,
        BETA
    }

    enum class UpdateType {
        MANUAL,
        AUTOMATIC
    }

    @ConfigOption(
        name = "Current Version",
        desc = "This is the SkyblockCollectionTracker version you are currently running."
    )
    @VersionDisplay
    @Suppress("unused")
    @Transient
    var currentVersion: Void? = null

    @ConfigOption(
        name = "§aInfo",
        desc = "§eThis mod is meant to track all collections in Hypixel Skyblock and provide some nice qol features.\n\n\nUse §b/sct commands §eto see all available commands."
    )
    @ConfigEditorInfoText
    var info: Boolean = true

    @Expose
    @ConfigOption(
        name = "§bUpdate Stream",
        desc = """
        Choose which updates you want to receive notifications for:§f
                
        - §cNONE§7: Disable update notifications§f
        - §aRELEASE§7: Notify only for full releases§f
        - §eBETA§7: Notify for both full and beta releases
        """
    )
    @ConfigEditorDropdown
    var update: UpdateStream = UpdateStream.RELEASE //Default to RELEASE updates

    @Expose
    @ConfigOption(
        name = "§eUpdate Type",
        desc = """
        Choose how updates should be installed:§f
        
        - §2MANUAL§7: Provide a link to the Modrinth page§f
        - §4AUTOMATIC§7: Download updates after closing the game
        """
    )
    @ConfigEditorDropdown
    var updateType: UpdateType = UpdateType.MANUAL //Default to MANUAL updates

    @Expose
    var hasCheckedUpdate: Boolean = true
}
