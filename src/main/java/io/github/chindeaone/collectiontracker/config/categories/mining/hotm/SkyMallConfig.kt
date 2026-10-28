package io.github.chindeaone.collectiontracker.config.categories.mining.hotm

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class SkyMallConfig {
    @Expose
    @ConfigOption(
        name = "Sky Mall Display",
        desc = "Display current Sky Mall perk and time left and compacts Sky Mall chat messages."
    )
    @ConfigEditorBoolean
    var enableSkyMall: Boolean = false

    @Expose
    @ConfigOption(
        name = "Mining Islands Only",
        desc = "Show the Sky Mall display only on Mining Islands."
    )
    @ConfigEditorBoolean
    var skyMallInMiningIslandsOnly: Boolean = true

    @Expose
    @ConfigOption(
        name = "Disable Sky Mall chat messages",
        desc = "Hide Sky Mall chat messages while allowing other mods to process the them."
    )
    @ConfigEditorBoolean
    var disableSkyMallChatMessages: Boolean = false

    @Expose
    @ConfigLink(owner = SkyMallConfig::class, field = "enableSkyMall")
    var skyMallPosition: Position = Position(500, 50)
}