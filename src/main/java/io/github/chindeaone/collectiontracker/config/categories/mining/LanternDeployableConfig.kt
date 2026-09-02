package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class LanternDeployableConfig {
    @Expose
    @ConfigOption(name = "Enable Lantern Deployable", desc = "Displays name and timer for the deployable.")
    @ConfigEditorBoolean
    var enableDeployable: Boolean = false

    @Expose
    @ConfigOption(name = "Show Title", desc = "Shows a title when it expires.")
    @ConfigEditorBoolean
    var showDeployableTitle: Boolean = false

    @Expose
    @ConfigOption(
        name = "Out of Range Warning",
        desc = "Shows a title warning when you move out of range of a deployable."
    )
    @ConfigEditorBoolean
    var deployableOutOfRangeWarning: Boolean = false

    @Expose
    @ConfigLink(owner = LanternDeployableConfig::class, field = "enableDeployable")
    var deployablePosition: Position = Position(300, 100)
}
