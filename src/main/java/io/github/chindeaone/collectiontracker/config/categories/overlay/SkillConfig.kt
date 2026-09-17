package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SkillConfig {
    @Expose
    @ConfigOption(
        name = "Enable Taming Tracking",
        desc = "Toggles additional tracking for Taming."
    )
    @ConfigEditorBoolean
    var enableTamingTracking: Boolean = false

    @Expose
    @ConfigLink(owner = SkillConfig::class, field = "enableTamingTracking")
    var skillOverlayPosition: Position = Position(50, 250)
}