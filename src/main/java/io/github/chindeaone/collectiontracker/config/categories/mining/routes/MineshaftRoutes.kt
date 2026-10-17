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
        name = "Mineshaft Routes",
        desc = "Display ordered waypoints for the Mineshaft type you enter.\n§eNote: §5Crystal§e, §dJasper §eand §7Tungsten §eMineshafts support only."
    )
    @ConfigEditorBoolean
    var enableMineshaftRoutes: Boolean = false

    @Expose
    @ConfigOption(
        name = "Mineshaft Spawn Routes",
        desc = "Enable routes for spawning Mineshafts."
    )
    @ConfigEditorBoolean
    var enableMineshaftSpawnRoutes: Boolean = false

    @Expose
    @ConfigOption(name = "Select Route", desc = "")
    @ConfigEditorDropdown
    var selectedMineshaftSpawnRoute: MineshaftSpawnRoutes = MineshaftSpawnRoutes.GEMSTONES
}