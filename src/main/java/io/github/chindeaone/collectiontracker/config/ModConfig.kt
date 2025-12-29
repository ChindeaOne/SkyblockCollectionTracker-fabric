package io.github.chindeaone.collectiontracker.config

import com.google.gson.annotations.Expose;
import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.categories.*;
import io.github.notenoughupdates.moulconfig.Config;
import io.github.notenoughupdates.moulconfig.annotations.Category;
import io.github.notenoughupdates.moulconfig.common.text.StructuredText

class ModConfig : Config() {

    @Expose
    @Category(name = "About", desc = "")
    val about: About = About()
//    @Expose
//    @Category(name = "GUI", desc = "Change the location of GUI")
//    public GUIConfig gui = new GUIConfig();
//    @Expose
//    @Category(name = "Overlay", desc = "Overlay settings")
//    public Overlay overlay = new Overlay();
//    @Expose
//    @Category(name = "Bazaar", desc = "Toggle bazaar prices")
//    public Bazaar bazaar = new Bazaar();
//    @Expose
//    @Category(name = "Mining", desc = "Mining related settings")
//    @Accordion
//    public Mining mining = new Mining();


    override fun getTitle() : StructuredText {
        val modName = "SkyblockCollectionTracker";
        return StructuredText.of("$modName by §3Chindea_YTB§r, config by §5Moulberry §rand §5nea89");
    }

    override fun saveNow() {
        SkyblockCollectionTracker.configManager.save();
    }

}
