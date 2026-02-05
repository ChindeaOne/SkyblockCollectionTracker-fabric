package io.github.chindeaone.collectiontracker.config

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.categories.*
import io.github.notenoughupdates.moulconfig.Config
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.common.text.StructuredText

class ModConfig : Config() {

    @Expose
    @Category(name = "About", desc = "")
    @SerializedName("about")
    val about: About = About()
    @Expose
    @Category(name = "GUI", desc = "GUI config")
    val gui: GUIConfig = GUIConfig()
    @Expose
    @Category(name = "Tracking", desc = "Tracking config")
    @SerializedName("trackingOverlay")
    val trackingOverlay: Tracking = Tracking()
    @Expose
    @Category(name = "Bazaar", desc = "Bazaar config")
    @SerializedName("bazaar")
    val bazaar: Bazaar = Bazaar()
    @Expose
    @Category(name = "Mining", desc = "Mining config")
    @SerializedName("mining")
    @Accordion
    val mining: Mining = Mining()

    override fun getTitle() : StructuredText {
        val modName = "SkyblockCollectionTracker"
        return StructuredText.of("$modName by §3Chindea_YTB§r, config by §5Moulberry §rand §5nea89")
    }

    override fun saveNow() {
        SkyblockCollectionTracker.configManager.save()
    }
}
