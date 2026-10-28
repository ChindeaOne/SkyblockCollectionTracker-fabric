package io.github.chindeaone.collectiontracker.config.categories.foraging

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class BeekeeperConfig {
    @Expose
    @ConfigOption(
        name = "Beekeeper Display",
        desc = "Display current Beekeeper perks and time left and compacts Beekeeper chat messages."
    )
    @ConfigEditorBoolean
    var enableBeekeeper: Boolean = false

    @Expose
    @ConfigOption(
        name = "Foraging Islands Only",
        desc = "Show the Beekeeper display only on Foraging Islands."
    )
    @ConfigEditorBoolean
    var beekeeperInForagingIslandsOnly: Boolean = true

    @Expose
    @ConfigOption(
        name = "Disable Beekeeper chat messages",
        desc = "Hide Beekeeper chat messages while allowing other mods to process the them."
    )
    @ConfigEditorBoolean
    var disableBeekeeperChatMessages: Boolean = false

    @Expose
    @ConfigLink(owner = BeekeeperConfig::class, field = "enableBeekeeper")
    var beekeeperPosition: Position = Position(500, 150)
}
