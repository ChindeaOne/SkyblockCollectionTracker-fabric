package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.chindeaone.collectiontracker.gui.GuiManager
import io.github.notenoughupdates.moulconfig.annotations.*

class Misc {
    @Suppress("unused")
    enum class TitleScale(val scale: Float) {
        SMALL(0.5f),
        MEDIUM(1.0f),
        LARGE(2.0f),
        HUGE(3.0f)
    }

    enum class AbilityDisplayIndicator {
        CROSSHAIR_CIRCLE,
        CROSSHAIR_BAR,
        NONE
    }

    @Expose
    @ConfigOption(name = "Timer precision", desc = "Change how many decimals cooldown and duration will show.")
    @ConfigEditorSlider(minValue = 0f, maxValue = 2f, minStep = 1f)
    var abilityPrecision: Int = 0

    @Expose
    @ConfigOption(name = "Title Duration", desc = "How long (in seconds) will titles remain on screen.")
    @ConfigEditorSlider(minValue = 1f, maxValue = 8f, minStep = 1f)
    var titleDisplayTimer: Int = 3

    @Expose
    @ConfigOption(
        name = "Title Scale",
        desc = "Change the scale of titles.\n§eSmall = 0.5x, Medium = 1x, Large = 2x, Huge = 3x"
    )
    @ConfigEditorDropdown
    var titleScale: TitleScale = TitleScale.MEDIUM // Default to MEDIUM

    @ConfigOption(name = "Title Position GUI", desc = "Edit the position of titles.")
    @ConfigEditorButton(buttonText = "Edit")
    @Suppress("unused")
    var editTitlePosition: Runnable = Runnable { GuiManager.openGuiTitlePositionEditor() }

    @Expose
    @ConfigOption(name = "Ability Cooldown Only", desc = "Only display ability cooldowns.")
    @ConfigEditorBoolean
    var abilityCooldownOnly: Boolean = false

    @Expose
    @ConfigOption(
        name = "Server Lag Protection",
        desc = "Prevents ability timers from counting down during server lag.\n§eMight desync timers if you swap lobbies a lot!"
    )
    @ConfigEditorBoolean
    var serverLagProtection: Boolean = false

    @Expose
    @ConfigOption(name = "Timer Title", desc = "Shows a title when the timer ends.")
    @ConfigEditorBoolean
    var showTimerTitle: Boolean = false

    @Expose
    var titlePosition: Position = Position(0, 0)
}
