package io.github.chindeaone.collectiontracker.config.categories.mining.routes

import com.google.gson.annotations.Expose
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorBoolean
import io.github.notenoughupdates.moulconfig.annotations.ConfigEditorDropdown
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class MineshaftRoutes {
    @Suppress("unused")
    enum class MineshaftSpawnRoutes(val type: String) {
        GEMSTONES("gemstone_spawn"),
        TUNGSTEN("tungsten_spawn"),
        MITHRIL("mithril_spawn");

        override fun toString(): String {
            return name
        }
    }

    @Expose
    @ConfigOption(
        name = "Enable Mineshaft Routes",
        desc = "Displays ordered waypoints for the Mineshaft type you enter.\n§eRoutes provided by Mining Cult."
    )
    @ConfigEditorBoolean
    var enableMineshaftRoutes: Boolean = false

    @Expose
    @ConfigOption(name = "Enable Mineshaft Spawn Routes", desc = "Enables routes for spawning Mineshafts.")
    @ConfigEditorBoolean
    var enableMineshaftSpawnRoutes: Boolean = false

    @Expose
    @ConfigOption(
        name = "Mineshaft Spawn Route",
        desc = "Select the route you want to use to spawn Mineshafts.\n§eRoutes provided by Mining Cult."
    )
    @ConfigEditorDropdown
    var selectedMineshaftSpawnRoute: MineshaftSpawnRoutes = MineshaftSpawnRoutes.GEMSTONES
}