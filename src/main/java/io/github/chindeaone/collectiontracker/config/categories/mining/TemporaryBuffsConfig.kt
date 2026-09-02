package io.github.chindeaone.collectiontracker.config.categories.mining

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class TemporaryBuffsConfig {
    @Expose
    @ConfigOption(name = "Enable Tracker", desc = "Toggles an overlay that tracks mining temporary buffs.")
    @ConfigEditorBoolean
    var enableTempBuffTracker: Boolean = false

    @Expose
    @ConfigOption(name = "Show Expired Title", desc = "Shows a title when a temporary buff expires.")
    @ConfigEditorBoolean
    var showTempBuffExpiredTitle: Boolean = false

    @Expose
    @ConfigLink(owner = TemporaryBuffsConfig::class, field = "enableTempBuffTracker")
    var tempBuffPosition: Position = Position(400, 100)

    @Expose
    var refinedCacaoTime: Long = 0L

    @Expose
    var filetTime: Long = 0L

    @Expose
    var pristinePotatoTime: Long = 0L

    @Expose
    var powderPumpkinTime: Long = 0L

    @Expose
    var fiestaFlaskTime: Long = 0L
}