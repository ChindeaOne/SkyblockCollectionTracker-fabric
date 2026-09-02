package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.categories.mining.KeybindConfig
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CommissionsConfig {
    @Expose
    @ConfigOption(name = "Enable Commissions Overlay", desc = "Toggles an overlay for mining commissions.")
    @ConfigEditorBoolean
    var enableCommissionsOverlay: Boolean = false

    @Expose
    @ConfigOption(name = "New Commission Title", desc = "Display a title when a new commission is received.")
    @ConfigEditorBoolean
    var newCommissionTitle: Boolean = false

    @Expose
    @ConfigOption(name = "Completion Notification Title", desc = "Display a title when a commission is completed.")
    @ConfigEditorBoolean
    var completionTitle: Boolean = false

    @Expose
    @ConfigOption(name = "Enable Commissions Tracking", desc = "Toggles tracking for mining commissions.")
    @ConfigEditorBoolean
    var enableCommissionsTracking: Boolean = false

    @Expose
    @ConfigOption(name = "Commissions Keybinds", desc = "")
    @Accordion
    var keybindConfig: KeybindConfig = KeybindConfig()

    @Expose
    @ConfigLink(owner = CommissionsConfig::class, field = "enableCommissionsOverlay")
    var commissionsOverlayPosition: Position = Position(50, 50)
}
