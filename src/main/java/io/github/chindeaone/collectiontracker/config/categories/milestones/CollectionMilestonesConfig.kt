package io.github.chindeaone.collectiontracker.config.categories.milestones

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CollectionMilestonesConfig {

    @Expose
    @ConfigOption(
        name = "Collection Milestones Overlay",
        desc = "Enables the milestones overlay during collection tracking."
    )
    @ConfigEditorBoolean
    var collectionMilestones: Boolean = false

    @Expose
    @ConfigOption(
        name = "Title Notification",
        desc = "Send a title when you reach a collection milestone."
    )
    @ConfigEditorBoolean
    var collectionMilestonesTitleNotification: Boolean = false

    @Expose
    @ConfigOption(
        name = "Sound Notification",
        desc = "Play a sound when you reach a collection milestone."
    )
    @ConfigEditorBoolean
    var collectionMilestonesSoundNotification: Boolean = false

    @Expose
    @ConfigLink(owner = CollectionMilestonesConfig::class, field = "collectionMilestones")
    var collectionMilestonesPosition: Position = Position(250, 100)
}