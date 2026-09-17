package io.github.chindeaone.collectiontracker.config

import io.github.chindeaone.collectiontracker.SkyblockCollectionTracker
import io.github.chindeaone.collectiontracker.config.categories.About
import io.github.chindeaone.collectiontracker.config.categories.Bazaar
import io.github.chindeaone.collectiontracker.config.categories.Farming
import io.github.chindeaone.collectiontracker.config.categories.Foraging
import io.github.chindeaone.collectiontracker.config.categories.farmingweight.FarmingweightConfig
import io.github.chindeaone.collectiontracker.config.categories.Mining
import io.github.chindeaone.collectiontracker.config.categories.Misc
import io.github.chindeaone.collectiontracker.config.categories.Tracking
import io.github.chindeaone.collectiontracker.config.categories.coleweight.ColeweightConfig
import io.github.chindeaone.collectiontracker.config.categories.coleweight.ColeweightColorConfig
import io.github.chindeaone.collectiontracker.config.categories.coleweight.HeatmapConfig
import io.github.chindeaone.collectiontracker.config.categories.coleweight.PrecisionMiningConfig
import io.github.chindeaone.collectiontracker.config.categories.farmingweight.FarmingweightColorConfig
import io.github.chindeaone.collectiontracker.config.categories.foraging.AxeAbilityConfig
import io.github.chindeaone.collectiontracker.config.categories.foraging.BeekeeperConfig
import io.github.chindeaone.collectiontracker.config.categories.foraging.HotfConfig
import io.github.chindeaone.collectiontracker.config.categories.foraging.LotteryConfig
import io.github.chindeaone.collectiontracker.config.categories.milestones.CollectionMilestonesConfig
import io.github.chindeaone.collectiontracker.config.categories.milestones.Milestone
import io.github.chindeaone.collectiontracker.config.categories.milestones.SkillMilestonesConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.HotmConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.KeybindConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.LanternDeployableConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.MineshaftRoutes
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.MiningRoutesConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.PickaxeAbilityConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.SkyMallConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.TemporaryBuffsConfig
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.DwarvenMetalsRoutes
import io.github.chindeaone.collectiontracker.config.categories.mining.routes.PureOresRoutes
import io.github.chindeaone.collectiontracker.config.categories.overlay.CollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.CommissionsConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.ForagingStatsConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.LeaderboardConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MilestonesConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.SkillConfig
import io.github.chindeaone.collectiontracker.config.categories.party.PartyNotifierConfig
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.ChromaColour

/**
 * Global accessors for configs
 */
val modConfig: ModConfig get() = SkyblockCollectionTracker.configManager.config ?: throw IllegalStateException("[SCT]: Config not initialized.")

// Position Config Accessor
val trackingPosition: Position get() = modConfig.tracking.collectionConfig.overlayPosition
val miningStatsPosition: Position get() = modConfig.mining.miningStatsConfig.miningStatsOverlayPosition
val foragingStatsPosition: Position get() = modConfig.foraging.foragingStatsConfig.foragingStatsOverlayPosition
val commissionsPosition: Position get() = modConfig.mining.commissionsConfig.commissionsOverlayPosition
val skillPosition: Position get() = modConfig.tracking.skillConfig.skillOverlayPosition
val skyMallPosition: Position get() = modConfig.mining.hotmConfig.skyMallConfig.skyMallPosition
val lotteryPosition: Position get() = modConfig.foraging.hotfConfig.lotteryConfig.lotteryPosition
val beekeeperPosition: Position get() = modConfig.foraging.hotfConfig.beekeeperConfig.beekeeperPosition
val pickaxeAbilityPosition: Position get() = modConfig.mining.hotmConfig.pickaxeAbilityConfig.pickaxeAbilityPosition
val axeAbilityPosition: Position get() = modConfig.foraging.hotfConfig.axeAbilityConfig.axeAbilityPosition
val deployablePosition: Position get() = modConfig.mining.lanternDeployableConfig.deployablePosition
val tempBuffPosition: Position get() = modConfig.mining.temporaryBuffsConfig.tempBuffPosition
val titlePosition: Position get() = modConfig.misc.titlePosition
val multiOverlayPosition: Position get() = modConfig.tracking.multiCollectionConfig.multiOverlayPosition
val coleweightTimerPosition: Position get() = modConfig.mining.coleweightConfig.coleweightTimerPosition
val coleweightStopwatchPosition: Position get() = modConfig.mining.coleweightConfig.coleweightStopwatchPosition
val coleweightTrackerPosition: Position get() = modConfig.mining.coleweightConfig.coleweightTrackerPosition
val collectionMilestonesPosition: Position get() = modConfig.tracking.milestonesConfig.collectionMilestonesConfig.collectionMilestonesPosition
val skillMilestonesPosition: Position get() = modConfig.tracking.milestonesConfig.skillMilestonesConfig.skillMilestonesPosition

// About Config Accessor
val aboutConfig: About get() = modConfig.about
val updateStream: About.UpdateStream get() = aboutConfig.update
val updateType: About.UpdateType get() = aboutConfig.updateType
val hasCheckedUpdate: Boolean get() = aboutConfig.hasCheckedUpdate

// Bazaar Config Accessors
val bazaarConfig: Bazaar get() = modConfig.bazaar
val bazaarPriceType: Bazaar.BazaarPriceType get() = bazaarConfig.bazaarPriceType
val useBazaar: Boolean get() = bazaarConfig.useBazaar
val bazaarType: Bazaar.BazaarType get() = bazaarConfig.bazaarType
val gemstoneVariant: Bazaar.GemstoneVariant get() = bazaarConfig.gemstoneVariant

// Mining Config Accessors
val miningConfig: Mining get() = modConfig.mining
val miningRoutesConfig: MiningRoutesConfig get() = miningConfig.miningRoutesConfig
val commissionsConfig: CommissionsConfig get() = miningConfig.commissionsConfig
val enableCommissionsOverlay: Boolean get() = commissionsConfig.enableCommissionsOverlay
val completionTitle: Boolean get() = commissionsConfig.completionTitle
val newCommissionTitle: Boolean get() = commissionsConfig.newCommissionTitle
val enableCommissionsTracking: Boolean get() = commissionsConfig.enableCommissionsTracking
val keybindConfig: KeybindConfig get() = commissionsConfig.keybindConfig
val enableCommissionsKeybinds: Boolean get() = keybindConfig.enableCommissionsKeybinds
val hotmConfig: HotmConfig get() = miningConfig.hotmConfig
val skyMallConfig: SkyMallConfig get() = hotmConfig.skyMallConfig
val pickaxeAbilityConfig: PickaxeAbilityConfig get() = hotmConfig.pickaxeAbilityConfig
val lanternDeployableConfig: LanternDeployableConfig get() = miningConfig.lanternDeployableConfig
val temporaryBuffsConfig: TemporaryBuffsConfig get() = miningConfig.temporaryBuffsConfig
val cotmLevel: Int get() = hotmConfig.cotmLevel
val displayPickaxeAbility: Boolean get() = hotmConfig.pickaxeAbilityConfig.displayPickaxeAbility
val pickaxeAbilityDisplayIndicator: Misc.AbilityDisplayIndicator get() = hotmConfig.pickaxeAbilityConfig.indicator
val abilityName: String get() = pickaxeAbilityConfig.abilityName
val attributeLevel: Int get() = pickaxeAbilityConfig.attributeLevel
val enableSkyMall: Boolean get() = skyMallConfig.enableSkyMall
val lastSkyMallBuff: String get() = skyMallConfig.lastSkyMallBuff
val skyMallInMiningIslandsOnly: Boolean get() = skyMallConfig.skyMallInMiningIslandsOnly
val disableSkyMallChatMessages: Boolean get() = skyMallConfig.disableSkyMallChatMessages
val showPickaxeReadyAbilityTitle: Boolean get() = pickaxeAbilityConfig.showPickaxeReadyAbilityTitle
val showPickaxeExpiredAbilityTitle: Boolean get() = pickaxeAbilityConfig.showPickaxeExpiredAbilityTitle
val pickaxeAbilityInMiningIslandsOnly: Boolean get() = pickaxeAbilityConfig.pickaxeAbilityInMiningIslandsOnly
val mineshaftRoutesConfig: MineshaftRoutes get() = miningRoutesConfig.mineshaftRoutes
val enableMineshaftRoutes: Boolean get() = mineshaftRoutesConfig.enableMineshaftRoutes
val enableMineshaftSpawnRoutes: Boolean get() = mineshaftRoutesConfig.enableMineshaftSpawnRoutes
val mineshaftSpawnRoutes: MineshaftRoutes.MineshaftSpawnRoutes get() = mineshaftRoutesConfig.selectedMineshaftSpawnRoute
val dwarvenMetalsRoutesConfig: DwarvenMetalsRoutes get() = miningRoutesConfig.dwarvenMetalsRoutes
val enableDwarvenMetalRoutes: Boolean get() = dwarvenMetalsRoutesConfig.enableDwarvenMetalRoutes
val dwarvenMetalRoutes: DwarvenMetalsRoutes.DwarvenMetalRoutes get() = dwarvenMetalsRoutesConfig.selectedDwarvenMetalRoute
val pureOresRoutesConfig: PureOresRoutes get() = miningRoutesConfig.pureOresRoutes
val enablePureOresRoutes: Boolean get() = pureOresRoutesConfig.enablePureOresRoutes
val pureOresRoutes: PureOresRoutes.PureOreRoutes get() = pureOresRoutesConfig.selectedPureOresRoute
val enableTempBuffTracker: Boolean get() = temporaryBuffsConfig.enableTempBuffTracker
val showTempBuffExpiredTitle: Boolean get() = temporaryBuffsConfig.showTempBuffExpiredTitle
val refinedCacaoTime: Long get() = temporaryBuffsConfig.refinedCacaoTime
val filetTime: Long get() = temporaryBuffsConfig.filetTime
val pristinePotatoTime: Long get() = temporaryBuffsConfig.pristinePotatoTime
val powderPumpkinTime: Long get() = temporaryBuffsConfig.powderPumpkinTime
val fiestaFlaskTime: Long get() = temporaryBuffsConfig.fiestaFlaskTime

// Coleweight Config Accessors
val coleweightConfig: ColeweightConfig get() = miningConfig.coleweightConfig
val coleweightColorConfig: ColeweightColorConfig get() = coleweightConfig.coleweightColorConfig
val coleweightRankingInChat: Boolean get() = coleweightConfig.coleweightRankingInChat
val coleweightRankInNameTag: Boolean get() = coleweightConfig.coleweightRankInNameTag
val enableCustomCWColor: Boolean get() = coleweightColorConfig.enableCustomColor
val customCWColor: ChromaColour get() = coleweightColorConfig.customColor
val heatmapConfig: HeatmapConfig get() = coleweightConfig.heatmapConfig
val enableHeatmap: Boolean get() = heatmapConfig.enableHeatmap
val heatmapOpacity: Float get() = heatmapConfig.heatmapOpacity
val precisionMiningConfig: PrecisionMiningConfig get() = coleweightConfig.precisionMiningConfig
val enablePrecisionMiningHighlight: Boolean get() = precisionMiningConfig.enablePrecisionMiningHighlight
val drawLineToPrecisionMining: Boolean get() = precisionMiningConfig.drawLineToPrecisionMining

// Overlays
val miningStatsConfig: MiningStatsConfig get() = miningConfig.miningStatsConfig
val enableMiningStatsOverlay: Boolean get() = miningStatsConfig.enableMiningStatsOverlay
val miningStatsOverlayInMiningIslandsOnly: Boolean get() = miningStatsConfig.miningStatsOverlayInMiningIslandsOnly
val onlyOnMiningIslands: Boolean get() = coleweightConfig.onlyOnMiningIslands
val showDetailedMiningFortune : Boolean get() = miningStatsConfig.showDetailedFortune
val professionalMS: Int get() = hotmConfig.professionalMS
val strongArmMS: Int get() = hotmConfig.strongArmMS
val enableDeployable: Boolean get() = lanternDeployableConfig.enableDeployable
val showDeployableTitle: Boolean get() = lanternDeployableConfig.showDeployableTitle
val deployableOutOfRangeWarning : Boolean get() = lanternDeployableConfig.deployableOutOfRangeWarning

// Foraging Config Accessors
val foragingConfig: Foraging get() = modConfig.foraging
val foragingStatsConfig: ForagingStatsConfig get() = foragingConfig.foragingStatsConfig
val enableForagingStatsOverlay: Boolean get() = foragingStatsConfig.enableForagingStatsOverlay
val foragingStatsOverlayInForagingIslandsOnly: Boolean get() = foragingStatsConfig.foragingStatsOverlayInForagingIslandsOnly
val showDetailedForagingFortune: Boolean get() = foragingStatsConfig.showDetailedFortune
val hotfConfig: HotfConfig get() = foragingConfig.hotfConfig
val axeAbilityConfig: AxeAbilityConfig get() = hotfConfig.axeAbilityConfig
val cotfLevel: Int get() = hotfConfig.cotfLevel
val abilityNameAxe: String get() = axeAbilityConfig.abilityNameAxe
val displayAxeAbility: Boolean get() = axeAbilityConfig.displayAxeAbility
val axeAbilityDisplayIndicator: Misc.AbilityDisplayIndicator get() = axeAbilityConfig.indicator
val lotteryConfig: LotteryConfig get() = hotfConfig.lotteryConfig
val enableLottery: Boolean get() = lotteryConfig.enableLottery
val lastLotteryBuff: String get() = lotteryConfig.lastLotteryBuff
val lotteryInForagingIslandsOnly: Boolean get() = lotteryConfig.lotteryInForagingIslandsOnly
val disableLotteryChatMessages: Boolean get() = lotteryConfig.disableLotteryChatMessages
val beekeeperConfig: BeekeeperConfig get() = hotfConfig.beekeeperConfig
val enableBeekeeper: Boolean get() = beekeeperConfig.enableBeekeeper
val lastBeekeeperBuff: String get() = beekeeperConfig.lastBeekeeperBuff
val beekeeperInForagingIslandsOnly: Boolean get() = beekeeperConfig.beekeeperInForagingIslandsOnly
val disableBeekeeperChatMessages: Boolean get() = beekeeperConfig.disableBeekeeperChatMessages
val showAxeReadyAbilityTitle: Boolean get() = axeAbilityConfig.showAxeReadyAbilityTitle
val showAxeExpiredAbilityTitle: Boolean get() = axeAbilityConfig.showAxeExpiredAbilityTitle
val axeAbilityInForagingIslandsOnly: Boolean get() = axeAbilityConfig.axeAbilityInForagingIslandsOnly

// Farming Config Accessors
val farmingConfig: Farming get() = modConfig.farming
val farmingweightConfig: FarmingweightConfig get() = farmingConfig.farmingweightConfig
val farmingweightRankingInChat: Boolean get() = farmingweightConfig.farmingweightRankingInChat
val farmingweightRankInNameTag: Boolean get() = farmingweightConfig.farmingweightRankInNameTag
val onlyOnFarmingIslands: Boolean get() = farmingweightConfig.onlyOnFarmingIslands
val farmingweightColorConfig: FarmingweightColorConfig get() = farmingweightConfig.farmingweightColorConfig
val customFWColor: ChromaColour get() = farmingweightColorConfig.customColor
val enableCustomFWColor: Boolean get() = farmingweightColorConfig.enableCustomColor

// Collection Tracking Config Accessors
val trackingConfig: Tracking get() = modConfig.tracking
val apiTracking: Boolean get() = trackingConfig.apiTracking
val overlayTextColor: Boolean get() = trackingConfig.overlayTextColor
val explicitValues: Boolean get() = trackingConfig.explicitValues
val collectionConfig: CollectionConfig get() = trackingConfig.collectionConfig
val statsText: List<CollectionConfig.OverlayText> get() = collectionConfig.statsText
val extraStatsText: List<CollectionConfig.OverlayExtraText> get() = collectionConfig.extraStatsText
val showExtraStats: Boolean get() = collectionConfig.showExtraStats

// Leaderboard Tracking Config Accessors
val leaderboardOverlay: LeaderboardConfig get() = trackingConfig.leaderboardConfig
val collectionLeaderboard: Boolean get() = leaderboardOverlay.collectionLeaderboard
val skillLeaderboard: Boolean get() = leaderboardOverlay.skillLeaderboard
val previousPosition: Boolean get() = leaderboardOverlay.previousPosition
val includeWipedProfiles: Boolean get() = leaderboardOverlay.includeWipedProfiles
val leaderboardPosition: Boolean get() = leaderboardOverlay.leaderboardPosition
val leaderboardPositions: Map<String, Int> get() = leaderboardOverlay.leaderboardPositions

// Multi Collection Tracking Config Accessors
val multiCollectionOverlay: MultiCollectionConfig get() = trackingConfig.multiCollectionConfig
val trackingOptions: MultiCollectionConfig.TrackingOptions get() = multiCollectionOverlay.trackingOptions
val multiTrackingSummary: Boolean get() = multiCollectionOverlay.multiTrackingSummary
val multiDetailedSummary: Boolean get() = multiCollectionOverlay.multiDetailedSummary
val summaryStats: MultiCollectionConfig.SummaryStats get() = multiCollectionOverlay.summaryStats

// Skills Tracking Config Accessors
val skillConfig: SkillConfig get() = trackingConfig.skillConfig
val enableTamingTracking: Boolean get() = skillConfig.enableTamingTracking

// Milestones Config Accessors
val milestonesConfig: MilestonesConfig get() = trackingConfig.milestonesConfig
val milestones: Map<String, Milestone> get() = milestonesConfig.milestones

// Collection Milestones
val collectionMilestonesConfig: CollectionMilestonesConfig get() = milestonesConfig.collectionMilestonesConfig
val collectionMilestones: Boolean get() = collectionMilestonesConfig.collectionMilestones
val collectionMilestonesTitleNotification: Boolean get() = collectionMilestonesConfig.collectionMilestonesTitleNotification
val collectionMilestonesSoundNotification: Boolean get() = collectionMilestonesConfig.collectionMilestonesSoundNotification

// Skill Milestones
val skillMilestonesConfig: SkillMilestonesConfig get() = milestonesConfig.skillMilestonesConfig
val skillMilestones: Boolean get() = skillMilestonesConfig.skillMilestones
val skillMilestonesTitleNotification: Boolean get() = skillMilestonesConfig.skillMilestonesTitleNotification
val skillMilestonesSoundNotification: Boolean get() = skillMilestonesConfig.skillMilestonesSoundNotification

// Misc Config Accessors
val miscConfig: Misc get() = modConfig.misc
val precision: Int get() = miscConfig.abilityPrecision
val titleDisplayTimer: Long get() = miscConfig.titleDisplayTimer * 1000L
val titleScale: Misc.TitleScale get() = miscConfig.titleScale
val abilityCooldownOnly: Boolean get() = miscConfig.abilityCooldownOnly
val serverLagProtection: Boolean get() = miscConfig.serverLagProtection
val showTimerTitle: Boolean get() = miscConfig.showTimerTitle

// Party Notifier Accessors
val partyNotifierConfig: PartyNotifierConfig get() = miscConfig.partyNotifierConfig
val timerNotifier: Boolean get() = partyNotifierConfig.timerNotifier
val timerNotifierInterval: Int get() = partyNotifierConfig.timerNotifierInterval
val stopwatchNotifier: Boolean get() = partyNotifierConfig.stopwatchNotifier
val stopwatchNotifierInterval: Int get() = partyNotifierConfig.stopwatchNotifierInterval

/**
 * Accessors for configs
 */
object ConfigAccess {

    @JvmStatic
    fun isColeweightRankInNameTag(): Boolean = coleweightRankInNameTag

    @JvmStatic
    fun isFarmingweightRankInNameTag(): Boolean = farmingweightRankInNameTag
}

/**
 * Helper functions for configs
 */
object ConfigHelper {

    fun setBazaarType(type: Bazaar.BazaarType) {
        bazaarConfig.bazaarType = type
    }

    fun enableUpdateChecks() {
        aboutConfig.hasCheckedUpdate = true
    }

    fun disableUpdateChecks() {
        aboutConfig.hasCheckedUpdate = false
    }

    fun disableBazaar() {
        bazaarConfig.useBazaar = false
    }

    fun disableExtraStats() {
        collectionConfig.showExtraStats = false
    }

    fun disableTamingTracking() {
        skillConfig.enableTamingTracking = false
    }

    fun disableCollectionLeaderboardTracking() {
        leaderboardOverlay.collectionLeaderboard = false
    }

    fun disableSkillLeaderboardTracking() {
        leaderboardOverlay.skillLeaderboard = false
    }

    fun setAbilityName(name: String) {
        pickaxeAbilityConfig.abilityName = name
    }

    fun setAttributeLevel(level: Int) {
        pickaxeAbilityConfig.attributeLevel = level
    }

    fun setLastSkyMallBuff(buff: String) {
        skyMallConfig.lastSkyMallBuff = buff
    }

    fun disableSkyMall() {
        skyMallConfig.enableSkyMall = false
    }

    fun setLastLotteryBuff(buff: String) {
        lotteryConfig.lastLotteryBuff = buff
    }

    fun disableLottery() {
        lotteryConfig.enableLottery = false
    }

    fun setLastBeekeeperBuff(buff: String) {
        beekeeperConfig.lastBeekeeperBuff = buff
    }

    fun disableBeekeeper() {
        beekeeperConfig.enableBeekeeper = false
    }

    fun toggleMiningStats(): Boolean {
        miningStatsConfig.enableMiningStatsOverlay = !enableMiningStatsOverlay
        return enableMiningStatsOverlay
    }

    fun toggleMiningStatsOnlyOnMiningIslands(): Boolean {
        miningStatsConfig.miningStatsOverlayInMiningIslandsOnly = !miningStatsOverlayInMiningIslandsOnly
        return miningStatsOverlayInMiningIslandsOnly
    }

    fun togglePickaxeAbility(): Boolean {
        pickaxeAbilityConfig.displayPickaxeAbility = !displayPickaxeAbility
        return displayPickaxeAbility
    }

    fun togglePickaxeAbilityOnlyOnMiningIslands(): Boolean {
        pickaxeAbilityConfig.pickaxeAbilityInMiningIslandsOnly = !pickaxeAbilityInMiningIslandsOnly
        return pickaxeAbilityInMiningIslandsOnly
    }

    fun togglePickaxeAbilityReadyTitle(): Boolean {
        pickaxeAbilityConfig.showPickaxeReadyAbilityTitle = !showPickaxeReadyAbilityTitle
        return pickaxeAbilityConfig.showPickaxeReadyAbilityTitle
    }

    fun togglePickaxeAbilityExpiredTitle(): Boolean {
        pickaxeAbilityConfig.showPickaxeExpiredAbilityTitle = !showPickaxeExpiredAbilityTitle
        return showPickaxeExpiredAbilityTitle
    }

    fun toggleSkyMall(): Boolean {
        skyMallConfig.enableSkyMall = !enableSkyMall
        return enableSkyMall
    }

    fun toggleSkyMallOnlyOnMiningIslands(): Boolean {
        skyMallConfig.skyMallInMiningIslandsOnly = !skyMallInMiningIslandsOnly
        return skyMallInMiningIslandsOnly
    }

    fun toggleSkyMallChatMessages(): Boolean {
        skyMallConfig.disableSkyMallChatMessages = !disableSkyMallChatMessages
        return disableSkyMallChatMessages
    }

    fun toggleCommissionsOverlay(): Boolean {
        commissionsConfig.enableCommissionsOverlay = !enableCommissionsOverlay
        return enableCommissionsOverlay
    }

    fun toggleCommissionsTracking(): Boolean {
        commissionsConfig.enableCommissionsTracking = !enableCommissionsTracking
        return enableCommissionsTracking
    }

    fun toggleCommissionsKeybinds(): Boolean {
        keybindConfig.enableCommissionsKeybinds = !keybindConfig.enableCommissionsKeybinds
        return keybindConfig.enableCommissionsKeybinds
    }

    fun toggleTempBuffTracker(): Boolean {
        temporaryBuffsConfig.enableTempBuffTracker = !enableTempBuffTracker
        return enableTempBuffTracker
    }

    fun toggleTempBuffExpiredTitle(): Boolean {
        temporaryBuffsConfig.showTempBuffExpiredTitle = !showTempBuffExpiredTitle
        return showTempBuffExpiredTitle
    }

    fun toggleForagingStats(): Boolean {
        foragingStatsConfig.enableForagingStatsOverlay = !enableForagingStatsOverlay
        return enableForagingStatsOverlay
    }

    fun toggleForagingStatsOnlyOnForagingIslands(): Boolean {
        foragingStatsConfig.foragingStatsOverlayInForagingIslandsOnly = !foragingStatsOverlayInForagingIslandsOnly
        return foragingStatsOverlayInForagingIslandsOnly
    }

    fun toggleAxeAbility(): Boolean {
        axeAbilityConfig.displayAxeAbility = !displayAxeAbility
        return displayAxeAbility
    }

    fun toggleAxeAbilityOnlyOnForagingIslands(): Boolean {
        axeAbilityConfig.axeAbilityInForagingIslandsOnly = !axeAbilityInForagingIslandsOnly
        return axeAbilityInForagingIslandsOnly
    }

    fun toggleAxeAbilityReadyTitle(): Boolean {
        axeAbilityConfig.showAxeReadyAbilityTitle = !showAxeReadyAbilityTitle
        return showAxeReadyAbilityTitle
    }

    fun toggleAxeAbilityExpiredTitle(): Boolean {
        axeAbilityConfig.showAxeExpiredAbilityTitle = !showAxeExpiredAbilityTitle
        return showAxeExpiredAbilityTitle
    }

    fun toggleLottery(): Boolean {
        lotteryConfig.enableLottery = !enableLottery
        return enableLottery
    }

    fun toggleLotteryOnlyOnForagingIslands(): Boolean {
        lotteryConfig.lotteryInForagingIslandsOnly = !lotteryInForagingIslandsOnly
        return lotteryInForagingIslandsOnly
    }

    fun toggleLotteryChatMessages(): Boolean {
        lotteryConfig.disableLotteryChatMessages = !disableLotteryChatMessages
        return disableLotteryChatMessages
    }

    fun toggleBeekeeper(): Boolean {
        beekeeperConfig.enableBeekeeper = !enableBeekeeper
        return enableBeekeeper
    }

    fun toggleBeekeeperOnlyOnForagingIslands(): Boolean {
        beekeeperConfig.beekeeperInForagingIslandsOnly = !beekeeperInForagingIslandsOnly
        return beekeeperInForagingIslandsOnly
    }

    fun toggleBeekeeperChatMessages(): Boolean {
        beekeeperConfig.disableBeekeeperChatMessages = !disableBeekeeperChatMessages
        return disableBeekeeperChatMessages
    }

    fun toggleColeweightRankingInChat(): Boolean {
        coleweightConfig.coleweightRankingInChat = !coleweightRankingInChat
        return coleweightConfig.coleweightRankingInChat
    }

    fun toggleColeweightRankInNameTag(): Boolean {
        coleweightConfig.coleweightRankInNameTag = !coleweightRankInNameTag
        return coleweightConfig.coleweightRankInNameTag
    }

    fun toggleColeweightOnlyOnMiningIslands(): Boolean {
        coleweightConfig.onlyOnMiningIslands = !onlyOnMiningIslands
        return coleweightConfig.onlyOnMiningIslands
    }

    fun toggleFarmingweightRankingInChat(): Boolean {
        farmingweightConfig.farmingweightRankingInChat = !farmingweightRankingInChat
        return farmingweightConfig.farmingweightRankingInChat
    }

    fun toggleFarmingweightRankInNameTag(): Boolean {
        farmingweightConfig.farmingweightRankInNameTag = !farmingweightRankInNameTag
        return farmingweightConfig.farmingweightRankInNameTag
    }

    fun toggleFarmingweightOnlyOnFarmingIslands(): Boolean {
        farmingweightConfig.onlyOnFarmingIslands = !onlyOnFarmingIslands
        return farmingweightConfig.onlyOnFarmingIslands
    }

    fun toggleHeatmap(): Boolean {
        heatmapConfig.enableHeatmap = !enableHeatmap
        return heatmapConfig.enableHeatmap
    }

    fun toggleCollectionLeaderboard(): Boolean {
        leaderboardOverlay.collectionLeaderboard = !collectionLeaderboard
        return leaderboardOverlay.collectionLeaderboard
    }

    fun toggleSkillLeaderboard(): Boolean {
        leaderboardOverlay.skillLeaderboard = !skillLeaderboard
        return leaderboardOverlay.skillLeaderboard
    }

    fun toggleCollectionMilestones(): Boolean {
        collectionMilestonesConfig.collectionMilestones = !collectionMilestones
        return collectionMilestonesConfig.collectionMilestones
    }

    fun toggleCollectionMilestonesTitleNotification(): Boolean {
        collectionMilestonesConfig.collectionMilestonesTitleNotification = !collectionMilestonesTitleNotification
        return collectionMilestonesConfig.collectionMilestonesTitleNotification
    }

    fun toggleCollectionMilestonesSoundNotification(): Boolean {
        collectionMilestonesConfig.collectionMilestonesSoundNotification = !collectionMilestonesSoundNotification
        return collectionMilestonesConfig.collectionMilestonesSoundNotification
    }

    fun toggleSkillMilestones(): Boolean {
        skillMilestonesConfig.skillMilestones = !skillMilestones
        return skillMilestonesConfig.skillMilestones
    }

    fun toggleSkillMilestonesTitleNotification(): Boolean {
        skillMilestonesConfig.skillMilestonesTitleNotification = !skillMilestonesTitleNotification
        return skillMilestonesConfig.skillMilestonesTitleNotification
    }

    fun toggleSkillMilestonesSoundNotification(): Boolean {
        skillMilestonesConfig.skillMilestonesSoundNotification = !skillMilestonesSoundNotification
        return skillMilestonesConfig.skillMilestonesSoundNotification
    }

    fun toggleTimerNotifier(): Boolean {
        partyNotifierConfig.timerNotifier = !timerNotifier
        return partyNotifierConfig.timerNotifier
    }

    fun toggleStopwatchNotifier(): Boolean {
        partyNotifierConfig.stopwatchNotifier = !stopwatchNotifier
        return partyNotifierConfig.stopwatchNotifier
    }

    fun setAxeAbilityName(name: String) {
        axeAbilityConfig.abilityNameAxe = name
    }

    fun setDuration(refined: Long = 0L, filet: Long = 0L, potato: Long = 0L, pumpkin: Long = 0L, fiesta: Long = 0L) {
        if (temporaryBuffsConfig.refinedCacaoTime != refined) temporaryBuffsConfig.refinedCacaoTime = refined
        if (temporaryBuffsConfig.filetTime != filet) temporaryBuffsConfig.filetTime = filet
        if (temporaryBuffsConfig.pristinePotatoTime != potato) temporaryBuffsConfig.pristinePotatoTime = potato
        if (temporaryBuffsConfig.powderPumpkinTime != pumpkin) temporaryBuffsConfig.powderPumpkinTime = pumpkin
        if (temporaryBuffsConfig.fiestaFlaskTime != fiesta) temporaryBuffsConfig.fiestaFlaskTime = fiesta
    }

    fun setProfessionalMS(level: Int) {
        if (level == 0) {
            hotmConfig.professionalMS = 0
            return
        }
        hotmConfig.professionalMS = 50 + level * 5
    }

    fun setStrongArmMS(level: Int) {
        hotmConfig.strongArmMS = level * 5
    }

    fun setCotmLevel(level: Int) {
        hotmConfig.cotmLevel = level
    }

    fun setCotfLevel(level: Int) {
        hotfConfig.cotfLevel = level
    }

    fun setTitlePosition(x: Int, y: Int) {
        modConfig.misc.titlePosition = Position(x, y)
    }

    fun setBazaar(enabled: Boolean) {
        bazaarConfig.useBazaar = enabled
    }

    fun setMultiTrackingOption(option: MultiCollectionConfig.TrackingOptions) {
        multiCollectionOverlay.trackingOptions = option
    }

    fun setGemstoneVariant(variant: Bazaar.GemstoneVariant) {
        bazaarConfig.gemstoneVariant = variant
    }

    fun setShowExtraStats(show: Boolean) {
        collectionConfig.showExtraStats = show
    }

    fun changeBazaarPrice(type: Bazaar.BazaarPriceType) {
        bazaarConfig.bazaarPriceType = type
    }

    fun setColeweightCustomColor(player: String, color: String) {
        coleweightColorConfig.customColors[player] = color
    }

    fun getColeweightColor(player: String): String? {
        return coleweightColorConfig.customColors[player]
    }

    fun removeColeweightCustomColor(player: String) {
        coleweightColorConfig.customColors.remove(player)
    }

    fun setFarmingweightCustomColor(player: String, color: String) {
        farmingweightColorConfig.customColors[player] = color
    }

    fun getFarmingweightColor(player: String): String? {
        return farmingweightColorConfig.customColors[player]
    }

    fun removeFarmingweightColor(player: String) {
        farmingweightColorConfig.customColors.remove(player)
    }

    fun setMineshaftSpawnRoutesEnabled(enabled: Boolean) {
        mineshaftRoutesConfig.enableMineshaftSpawnRoutes = enabled
    }

    fun setDwarvenMetalRoutesEnabled(enabled: Boolean) {
        dwarvenMetalsRoutesConfig.enableDwarvenMetalRoutes = enabled
    }

    fun setPureOresRoutesEnabled(enabled: Boolean) {
        pureOresRoutesConfig.enablePureOresRoutes = enabled
    }

    fun setApiTracking(enabled: Boolean) {
        trackingConfig.apiTracking = enabled
    }

    fun saveMilestones(map: Map<String, Milestone>) {
        milestonesConfig.milestones = map
    }

    fun saveLeaderboardPositions(map : Map<String, Int>) {
        leaderboardOverlay.leaderboardPositions = map
    }
}
