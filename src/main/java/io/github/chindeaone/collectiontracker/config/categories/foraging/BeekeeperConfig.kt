package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class BeekeeperConfig {
    @Expose
    @ConfigOption(
        name = "Better Beekeeper",
        desc = "Displays current Beekeeper perks and compacts Beekeeper chat messages."
    )
    @ConfigEditorBoolean
    var enableBeekeeper: Boolean = false

    @Expose
    var lastBeekeeperBuff: String = ""

    @Expose
    @ConfigOption(
        name = "Foraging Islands Only",
        desc = "Allows the Beekeeper overlay to be rendered only in Foraging Islands."
    )
    @ConfigEditorBoolean
    var beekeeperInForagingIslandsOnly: Boolean = true

    @Expose
    @ConfigOption(
        name = "Disable Beekeeper chat messages",
        desc = "Hides Beekeeper chat messages while displaying perks in the overlay and allowing other mods to process the messages."
    )
    @ConfigEditorBoolean
    var disableBeekeeperChatMessages: Boolean = false

    @Expose
    @ConfigLink(owner = BeekeeperConfig::class, field = "enableBeekeeper")
    var beekeeperPosition: Position = Position(500, 150)
}
