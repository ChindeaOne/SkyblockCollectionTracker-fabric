package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class LotteryConfig {
    @Expose
    @ConfigOption(name = "Better Lottery", desc = "Displays current Lottery perks and compacts Lottery chat messages.")
    @ConfigEditorBoolean
    var enableLottery: Boolean = false

    @Expose
    var lastLotteryBuff: String = ""

    @Expose
    @ConfigOption(
        name = "Foraging Islands Only",
        desc = "Allows the Lottery overlay to be rendered only in Foraging Islands."
    )
    @ConfigEditorBoolean
    var lotteryInForagingIslandsOnly: Boolean = true

    @Expose
    @ConfigOption(
        name = "Disable Lottery chat messages",
        desc = "Hides Lottery chat messages while displaying perks in the overlay and allowing other mods to process the messages."
    )
    @ConfigEditorBoolean
    var disableLotteryChatMessages: Boolean = false

    @Expose
    @ConfigLink(owner = LotteryConfig::class, field = "enableLottery")
    var lotteryPosition: Position = Position(500, 100)
}
