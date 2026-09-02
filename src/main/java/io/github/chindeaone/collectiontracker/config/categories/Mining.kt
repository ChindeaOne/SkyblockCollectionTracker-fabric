package io.github.chindeaone.collectiontracker.config.categories

import com.google.gson.annotations.Expose
import io.github.chindeaone.collectiontracker.config.categories.coleweight.ColeweightConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.HotmConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.LanternDeployableConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.TemporaryBuffsConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.MiningRoutesConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsConfig
import io.github.notenoughupdates.moulconfig.annotations.Accordion
import io.github.notenoughupdates.moulconfig.annotations.Category
import io.github.notenoughupdates.moulconfig.annotations.ConfigOption

class Mining {
    @Expose
    @ConfigOption(name = "Coleweight", desc = "")
    @Accordion
    var coleweightConfig: ColeweightConfig = ColeweightConfig()

    @Expose
    @ConfigOption(name = "Commissions", desc = "")
    @Accordion
    var commissionsConfig: CommissionsConfig = CommissionsConfig()

    @Expose
    @ConfigOption(name = "Mining Stats Overlay", desc = "")
    @Accordion
    var miningStatsConfig: MiningStatsConfig = MiningStatsConfig()

    @Expose
    @ConfigOption(name = "HotM Perks", desc = "")
    @Accordion
    var hotmConfig: HotmConfig = HotmConfig()

    @Expose
    @Category(name = "Mining Routes", desc = "")
    var miningRoutesConfig: MiningRoutesConfig = MiningRoutesConfig()

    @Expose
    @ConfigOption(name = "Lantern Deployable", desc = "")
    @Accordion
    var lanternDeployableConfig: LanternDeployableConfig = LanternDeployableConfig()

    @Expose
    @ConfigOption(name = "Temporary Buffs Tracker", desc = "")
    @Accordion
    var temporaryBuffsConfig: TemporaryBuffsConfig = TemporaryBuffsConfig()
}