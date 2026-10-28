package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class LotteryConfig {
    @Expose
    @ConfigOption(
        name = "Lottery Display",
        desc = "Display current Lottery perk and time left and compacts Lottery chat messages."
    )
    @ConfigEditorBoolean
    var enableLottery: Boolean = false

    @Expose
    @ConfigOption(
        name = "Foraging Islands Only",
        desc = "Show the Lottery display only on Foraging Islands."
    )
    @ConfigEditorBoolean
    var lotteryInForagingIslandsOnly: Boolean = true

    @Expose
    @ConfigOption(
        name = "Disable Lottery chat messages",
        desc = "Hide Lottery chat messages while allowing other mods to process the them."
    )
    @ConfigEditorBoolean
    var disableLotteryChatMessages: Boolean = false

    @Expose
    @ConfigLink(owner = LotteryConfig::class, field = "enableLottery")
    var lotteryPosition: Position = Position(500, 100)
}
