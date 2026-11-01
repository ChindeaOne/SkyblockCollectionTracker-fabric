package io.github.chindeaone.collectiontracker.config.categories.overlay

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDraggableList
import io.github.notenoughupdates.moulconfig.annotations.ConfigLink
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class CollectionConfig {

    enum class OverlayText(private val text: String) {
        COLLECTION("§aGold collection§f: 200.000M"),
        COLLECTION_SESSION("§aGold collection (session)§f: 10.000M"),
        COLL_PER_HOUR("§aColl/h§f: Calculating..."),
        MONEY_PER_HOUR("§a$/h (NPC/Bazaar)§f: 100k/h"),
        MONEY_MADE("§a$ made (NPC/Bazaar)§f: 1.000M"),
        COLLECTION_SINCE_LAST("§aCollected since last§f: 200k"),
        COLLECTION_SINCE_LAST_TIMER("§aLast collection§f: 20 seconds ago");

        override fun toString(): String = text
    }

    @Expose
    @ConfigOption(name = "Overlay Text", desc = "Drag the lines of text to change the appearance of the overlay.")
    @ConfigEditorDraggableList
    var statsText: MutableList<OverlayText> = mutableListOf(
        OverlayText.COLLECTION,
        OverlayText.COLLECTION_SESSION,
        OverlayText.COLL_PER_HOUR,
        OverlayText.MONEY_PER_HOUR,
        OverlayText.MONEY_MADE,
        OverlayText.COLLECTION_SINCE_LAST,
        OverlayText.COLLECTION_SINCE_LAST_TIMER
    )

    @Expose
    @ConfigOption(
        name = "Extra Stats",
        desc = "Show extra stats in the overlay."
    )
    @ConfigEditorBoolean
    var showExtraStats: Boolean = false

    @Expose
    @ConfigOption(
        name = "Tracking Summary",
        desc = "Show tracking rates summary in chat when stopping a tracking session."
    )
    @SerializedName("showTrackingRatesAtEndOfSession")
    @ConfigEditorBoolean
    var showTrackingRatesAtEndOfSession: Boolean = false

    @Expose
    @ConfigLink(owner = CollectionConfig::class, field = "statsText")
    var overlayPosition: Position = Position(50, 100)
}