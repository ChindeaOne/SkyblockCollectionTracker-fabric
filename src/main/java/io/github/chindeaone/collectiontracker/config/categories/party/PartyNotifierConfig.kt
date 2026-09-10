package io.github.chindeaone.collectiontracker.config.categories.party

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorSlider
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class PartyNotifierConfig {

    @Expose
    @ConfigOption(
        name = "Timer Notifier",
        desc = "Sends a party message every x minutes, with the remaining time of the timer."
    )
    @ConfigEditorBoolean
    var timerNotifier: Boolean = false

    @Expose
    @ConfigOption(
        name = "Notifier Interval",
        desc = "The interval (in minutes) at which the timer party notifier will send messages."
    )
    @ConfigEditorSlider(minValue = 1f, maxValue = 60f, minStep = 1f)
    var timerNotifierInterval: Int = 5

    @Expose
    @ConfigOption(
        name = "Stopwatch Notifier",
        desc = "Sends a party message every x minutes, with the elapsed time of the stopwatch."
    )
    @ConfigEditorBoolean
    var stopwatchNotifier: Boolean = false

    @Expose
    @ConfigOption(
        name = "Notifier Interval",
        desc = "The interval (in minutes) at which the stopwatch party notifier will send messages."
    )
    @ConfigEditorSlider(minValue = 1f, maxValue = 60f, minStep = 1f)
    var stopwatchNotifierInterval: Int = 5
}