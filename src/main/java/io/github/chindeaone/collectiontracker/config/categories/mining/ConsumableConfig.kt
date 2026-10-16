package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorInfoText
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class ConsumableConfig {
    @ConfigOption(
        name = "§eConsumables",
        desc = "§9Filet O' Fortune, §5Chilled Pristine Potato, §aPowder Pie, §6Fiesta Flask"
    )
    @ConfigEditorInfoText
    val info: Boolean = true

    @Expose
    @ConfigOption(
        name = "Enable Overlay",
        desc = "Toggles an overlay that tracks mining consumables."
    )
    @ConfigEditorBoolean
    var enableConsumableTracker: Boolean = false

    @Expose
    @ConfigOption(
        name = "Show Expired Title",
        desc = "Shows a title when a mining consumable expires."
    )
    @ConfigEditorBoolean
    var showConsumableExpiredTitle: Boolean = false

    @Expose
    @ConfigLink(owner = ConsumableConfig::class, field = "enableConsumableTracker")
    var consumablePosition: Position = Position(400, 100)

    @Expose
    var filetTime: Long = 0L

    @Expose
    var pristinePotatoTime: Long = 0L

    @Expose
    var powderPumpkinTime: Long = 0L

    @Expose
    var fiestaFlaskTime: Long = 0L
}