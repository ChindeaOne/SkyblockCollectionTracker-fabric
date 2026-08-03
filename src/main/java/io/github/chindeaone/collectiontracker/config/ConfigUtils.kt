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
import io.github.chindeaone.collectiontracker.config.categories.overlay.MiningStatsConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.MultiCollectionConfig
import io.github.chindeaone.collectiontracker.config.categories.overlay.SkillConfig
import io.github.chindeaone.collectiontracker.config.core.Position
import io.github.notenoughupdates.moulconfig.ChromaColour

/**
 * Global accessors for the configuration.
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
val claimTitle: Boolean get() = commissionsConfig.claimTitle
val enableCommissionsTracking: Boolean get() = commissionsConfig.enableCommissionsTracking
val keybindConfig: KeybindConfig get() = commissionsConfig.keybindConfig
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

// Coleweight Config Accessors
val coleweightConfig: ColeweightConfig get() = miningConfig.coleweightConfig
val coleweightAbilityFormat: Boolean get() = coleweightConfig.coleweightAbilityFormat
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
val enableMiningStatsConfig: Boolean get() = miningStatsConfig.enableMiningStatsOverlay
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
val collectionConfig: CollectionConfig get() = trackingConfig.collectionConfig
val statsText: List<CollectionConfig.OverlayText> get() = collectionConfig.statsText
val extraStatsText: List<CollectionConfig.OverlayExtraText> get() = collectionConfig.extraStatsText
val showExtraStats: Boolean get() = collectionConfig.showExtraStats
val explicitValues: Boolean get() = trackingConfig.explicitValues
val leaderboardOverlay: LeaderboardConfig get() = trackingConfig.leaderboardConfig
val collectionLeaderboard: Boolean get() = leaderboardOverlay.collectionLeaderboard
val skillLeaderboard: Boolean get() = leaderboardOverlay.skillLeaderboard
val customGoal: Boolean get() = leaderboardOverlay.customGoal
val customGoalType: LeaderboardConfig.CustomGoalType get() = leaderboardOverlay.customGoalType
val customGoals: Map<String, LeaderboardConfig.CustomGoalEntry> get() = leaderboardOverlay.customGoals

// Multi Collection Tracking Config Accessors
val multiCollectionOverlay: MultiCollectionConfig get() = trackingConfig.multiCollectionConfig
val trackingOptions: MultiCollectionConfig.TrackingOptions get() = multiCollectionOverlay.trackingOptions
val multiTrackingSummary: Boolean get() = multiCollectionOverlay.multiTrackingSummary
val multiDetailedSummary: Boolean get() = multiCollectionOverlay.multiDetailedSummary
val summaryStats: MultiCollectionConfig.SummaryStats get() = multiCollectionOverlay.summaryStats

// Skills Tracking Config Accessors
val skillConfig: SkillConfig get() = trackingConfig.skillConfig
val enableTamingTracking: Boolean get() = skillConfig.enableTamingTracking

// Misc Config Accessors
val miscConfig: Misc get() = modConfig.misc
val precision: Int get() = miscConfig.abilityPrecision
val titleDisplayTimer: Int get() = miscConfig.titleDisplayTimer
val titleScale: Misc.TitleScale get() = miscConfig.titleScale
val abilityCooldownOnly: Boolean get() = miscConfig.abilityCooldownOnly
val showTimerTitle: Boolean get() = miscConfig.showTimerTitle

/**
 * Accessors for configuration sections.
 */
object ConfigAccess {

    @JvmStatic
    fun getTrackingPosition(): Position = trackingPosition

    @JvmStatic
    fun getMiningStatsPosition(): Position = miningStatsPosition

    @JvmStatic
    fun getForagingStatsPosition(): Position = foragingStatsPosition

    @JvmStatic
    fun getCommissionsPosition(): Position = commissionsPosition

    @JvmStatic
    fun getSkyMallPosition(): Position = skyMallPosition

    @JvmStatic
    fun getLotteryPosition(): Position = lotteryPosition

    @JvmStatic
    fun getBeekeeperPosition(): Position = beekeeperPosition

    @JvmStatic
    fun getSkillPosition(): Position = skillPosition

    @JvmStatic
    fun getPickaxeAbilityPosition(): Position = pickaxeAbilityPosition

    @JvmStatic
    fun getAxeAbilityPosition(): Position = axeAbilityPosition

    @JvmStatic
    fun getDeployablePosition(): Position = deployablePosition

    @JvmStatic
    fun getTempBuffPosition(): Position = tempBuffPosition

    @JvmStatic
    fun getTitlePosition(): Position = titlePosition

    @JvmStatic
    fun getMultiOverlayPosition(): Position = multiOverlayPosition

    @JvmStatic
    fun getColeweightTimerPosition(): Position = coleweightTimerPosition

    @JvmStatic
    fun getColeweightStopwatchPosition(): Position = coleweightStopwatchPosition

    @JvmStatic
    fun getColeweightTrackerPosition(): Position = coleweightTrackerPosition

    fun getUpdateStream(): About.UpdateStream = updateStream

    fun getUpdateType(): About.UpdateType = updateType

    @JvmStatic
    fun getBazaarType(): Bazaar.BazaarType = bazaarType

    @JvmStatic
    fun getGemstoneVariant(): Bazaar.GemstoneVariant = gemstoneVariant

    @JvmStatic
    fun isUsingBazaar(): Boolean = useBazaar

    fun getKeybindConfig(): KeybindConfig = keybindConfig

    fun hasCheckedUpdate(): Boolean = hasCheckedUpdate

    @JvmStatic
    fun isApiTrackingEnabled(): Boolean = trackingConfig.apiTracking

    @JvmStatic
    fun isOverlayTextColorEnabled(): Boolean = trackingConfig.overlayTextColor

    @JvmStatic
    fun isShowTrackingRatesAtEndOfSession(): Boolean = collectionConfig.showTrackingRatesAtEndOfSession

    @JvmStatic
    fun isCommissionsEnabled(): Boolean = enableCommissionsOverlay

    fun isCompletionTitleEnabled(): Boolean = completionTitle

    fun isClaimTitleEnabled(): Boolean = claimTitle

    @JvmStatic
    fun isCommissionsTrackingEnabled(): Boolean = enableCommissionsTracking

    fun isCommissionsKeybindsEnabled(): Boolean = keybindConfig.enableCommissionsKeybinds

    @JvmStatic
    fun isMiningStatsEnabled(): Boolean = enableMiningStatsConfig

    @JvmStatic
    fun isMiningStatsOverlayInMiningIslandsOnly(): Boolean = miningStatsOverlayInMiningIslandsOnly

    @JvmStatic
    fun getStatsText(): List<CollectionConfig.OverlayText> = statsText

    @JvmStatic
    fun isShowExtraStats(): Boolean = showExtraStats

    @JvmStatic
    fun getExtraStatsText(): List<CollectionConfig.OverlayExtraText> = extraStatsText

    @JvmStatic
    fun isExplicitValues(): Boolean = explicitValues

    @JvmStatic
    fun isTamingTrackingEnabled(): Boolean = enableTamingTracking

    @JvmStatic
    fun getBazaarPriceType(): Bazaar.BazaarPriceType = bazaarPriceType

    fun isColeweightRankingInChat(): Boolean = coleweightRankingInChat

    @JvmStatic
    fun isColeweightRankInNameTag(): Boolean = coleweightRankInNameTag

    fun isOnlyOnMiningIslands(): Boolean = onlyOnMiningIslands

    fun isCustomCwColorEnabled(): Boolean = enableCustomCWColor

    fun getCustomCWColor(): ChromaColour = customCWColor

    fun isFarmingweightRankingInChat(): Boolean = farmingweightRankingInChat

    @JvmStatic
    fun isFarmingweightRankInNameTag(): Boolean = farmingweightRankInNameTag

    fun isOnlyOnFarmingIslands(): Boolean = onlyOnFarmingIslands

    fun isCustomFWColorEnabled(): Boolean = enableCustomFWColor

    fun getCustomFWColor(): ChromaColour = customFWColor

    @JvmStatic
    fun isShowDetailedMiningFortune(): Boolean = showDetailedMiningFortune

    @JvmStatic
    fun getProfessionalMS(): Int = professionalMS

    @JvmStatic
    fun getStrongArmMS(): Int = strongArmMS

    @JvmStatic
    fun isForagingStatsOverlayEnabled(): Boolean = enableForagingStatsOverlay

    @JvmStatic
    fun isShowDetailedForagingFortune(): Boolean = showDetailedForagingFortune

    fun getCotmLevel(): Int = cotmLevel

    @JvmStatic
    fun isSkyMallEnabled(): Boolean = enableSkyMall

    fun isDisableSkyMallChatMessages(): Boolean = disableSkyMallChatMessages

    @JvmStatic
    fun isLotteryEnabled(): Boolean = enableLottery

    fun isDisableLotteryChatMessages(): Boolean = disableLotteryChatMessages

    @JvmStatic
    fun isBeekeeperEnabled(): Boolean = enableBeekeeper

    fun isDisableBeekeeperChatMessages(): Boolean = disableBeekeeperChatMessages

    @JvmStatic
    fun isSkyMallInMiningIslandsOnly(): Boolean = skyMallInMiningIslandsOnly

    @JvmStatic
    fun isLotteryInForagingIslandsOnly(): Boolean = lotteryInForagingIslandsOnly

    @JvmStatic
    fun isBeekeeperInForagingIslandsOnly(): Boolean = beekeeperInForagingIslandsOnly

    @JvmStatic
    fun isPickaxeAbilityDisplayed(): Boolean = displayPickaxeAbility

    @JvmStatic
    fun getPickaxeAbilityDisplayIndicator(): Misc.AbilityDisplayIndicator = pickaxeAbilityDisplayIndicator

    @JvmStatic
    fun getPickaxeAbilityName(): String = abilityName

    fun hasCooldownAttribute(): Boolean = getAttributeLevel() > 0

    fun getAttributeLevel(): Int = attributeLevel

    fun getLastSkyMallBuff(): String = lastSkyMallBuff

    fun getLastLotteryBuff(): String = lastLotteryBuff

    fun getLastBeekeeperBuff(): String = lastBeekeeperBuff

    @JvmStatic
    fun isAxeAbilityDisplayed(): Boolean = displayAxeAbility

    @JvmStatic
    fun getAxeAbilityDisplayIndicator(): Misc.AbilityDisplayIndicator = axeAbilityDisplayIndicator

    @JvmStatic
    fun getAxeAbilityName(): String = abilityNameAxe

    fun getCotfLevel(): Int = cotfLevel

    @JvmStatic
    fun getAbilityPrecision(): Int = precision

    @JvmStatic
    fun isColeweightAbilityFormat(): Boolean = coleweightAbilityFormat

    @JvmStatic
    fun getTitleDisplayTimer(): Long = titleDisplayTimer * 1000L

    @JvmStatic
    fun isShowPickaxeReadyAbilityTitle(): Boolean = showPickaxeReadyAbilityTitle

    @JvmStatic
    fun isShowPickaxeExpiredAbilityTitle(): Boolean = showPickaxeExpiredAbilityTitle

    @JvmStatic
    fun isShowAxeReadyAbilityTitle(): Boolean = showAxeReadyAbilityTitle

    @JvmStatic
    fun isShowAxeExpiredAbilityTitle(): Boolean = showAxeExpiredAbilityTitle

    fun getTitleScale(): Misc.TitleScale = titleScale

    @JvmStatic
    fun isPickaxeAbilityInMiningIslandsOnly(): Boolean = pickaxeAbilityInMiningIslandsOnly

    @JvmStatic
    fun isAxeAbilityInForagingIslandsOnly(): Boolean = axeAbilityInForagingIslandsOnly

    @JvmStatic
    fun isAbilityCooldownOnly(): Boolean = abilityCooldownOnly

    fun isServerLagProtectionEnabled(): Boolean = miscConfig.serverLagProtection

    @JvmStatic
    fun isShowTimerTitle(): Boolean = showTimerTitle

    @JvmStatic
    fun isDeployableEnabled(): Boolean = enableDeployable

    fun isShowDeployableTitle(): Boolean = showDeployableTitle

    fun isDeployableOutOfRangeWarningEnabled(): Boolean = deployableOutOfRangeWarning

    fun isMineshaftRoutesEnabled(): Boolean = enableMineshaftRoutes

    fun isMineshaftSpawnRoutesEnabled(): Boolean = enableMineshaftSpawnRoutes

    fun getMineshaftSpawnRoutes(): MineshaftRoutes.MineshaftSpawnRoutes = mineshaftSpawnRoutes

    fun isDwarvenMetalRoutesEnabled(): Boolean = enableDwarvenMetalRoutes

    fun getDwarvenMetalRoutes(): DwarvenMetalsRoutes.DwarvenMetalRoutes = dwarvenMetalRoutes

    fun isPureOresRoutesEnabled(): Boolean = enablePureOresRoutes

    fun getPureOresRoutes(): PureOresRoutes.PureOreRoutes = pureOresRoutes

    @JvmStatic
    fun isTempBuffTrackerEnabled(): Boolean = enableTempBuffTracker

    @JvmStatic
    fun isShowTempBuffExpiredTitle(): Boolean = showTempBuffExpiredTitle

    fun getRefinedCacaoTime(): Long = refinedCacaoTime

    fun getFiletTime(): Long = filetTime

    fun getPristinePotatoTime(): Long = pristinePotatoTime

    fun getPowderPumpkinTime(): Long = powderPumpkinTime

    fun isHeatmapEnabled(): Boolean = enableHeatmap

    fun getHeatmapOpacity(): Float = heatmapOpacity

    fun isPrecisionMiningHighlightEnabled(): Boolean = enablePrecisionMiningHighlight

    fun isDrawLineToPrecisionMiningEnabled(): Boolean = drawLineToPrecisionMining

    @JvmStatic
    fun getTrackingOptions(): MultiCollectionConfig.TrackingOptions = trackingOptions

    fun isMultiTrackingSummaryEnabled(): Boolean = multiTrackingSummary

    fun isMultiDetailedSummaryEnabled(): Boolean = multiDetailedSummary

    fun getSummaryStats(): MultiCollectionConfig.SummaryStats = summaryStats

    @JvmStatic
    fun isCollectionLeaderboardEnabled(): Boolean = collectionLeaderboard

    @JvmStatic
    fun isSkillLeaderboardEnabled(): Boolean = skillLeaderboard

    @JvmStatic
    fun isCustomGoalEnabled(): Boolean = customGoal

    @JvmStatic
    fun getCustomGoalType(): LeaderboardConfig.CustomGoalType = customGoalType

    @JvmStatic
    fun getCustomGoals(): Map<String, LeaderboardConfig.CustomGoalEntry> = customGoals

    @JvmStatic
    fun getCustomGoalEntry(name: String): LeaderboardConfig.CustomGoalEntry? {
        return customGoals[name.lowercase()]
    }
}

/**
 * Utility helper for common configuration operations.
 */
object ConfigHelper {

    @JvmStatic
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

    fun disableCommissions() {
        commissionsConfig.enableCommissionsOverlay = false
    }

    fun disableMiningStats() {
        miningConfig.miningStatsConfig.enableMiningStatsOverlay = false
    }

    @JvmStatic
    fun disableTamingTracking() {
        skillConfig.enableTamingTracking = false
    }

    fun disableForagingStats() {
        foragingStatsConfig.enableForagingStatsOverlay = false
    }

    fun disableCollectionLeaderboardTracking() {
        leaderboardOverlay.collectionLeaderboard = false
    }

    fun disableSkillLeaderboardTracking() {
        leaderboardOverlay.skillLeaderboard = false
    }

    @JvmStatic
    fun setCustomGoalType(type: LeaderboardConfig.CustomGoalType) {
        leaderboardOverlay.customGoalType = type
    }

    @JvmStatic
    fun setCustomGoal(name: String, position: Int?, amount: Long?) {
        val lowercase = name.lowercase()
        if (position == null && amount == null) {
            leaderboardOverlay.customGoals.remove(lowercase)
        } else {
            val existingEntry = leaderboardOverlay.customGoals[lowercase]
            val finalPosition = position ?: existingEntry?.position
            val finalAmount = amount ?: existingEntry?.amount
            leaderboardOverlay.customGoals[lowercase] = LeaderboardConfig.CustomGoalEntry(finalPosition, finalAmount)
        }
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

    @JvmStatic
    fun disableSkyMall() {
        skyMallConfig.enableSkyMall = false
    }

    fun setLastLotteryBuff(buff: String) {
        lotteryConfig.lastLotteryBuff = buff
    }

    @JvmStatic
    fun disableLottery() {
        lotteryConfig.enableLottery = false
    }

    fun setLastBeekeeperBuff(buff: String) {
        beekeeperConfig.lastBeekeeperBuff = buff
    }

    @JvmStatic
    fun disableBeekeeper() {
        beekeeperConfig.enableBeekeeper = false
    }

    fun setAxeAbilityName(name: String) {
        axeAbilityConfig.abilityNameAxe = name
    }

    fun setDuration(refined: Long = -1, filet: Long = -1, potato: Long = -1, pumpkin: Long = -1) {
        if (refined != -1L) temporaryBuffsConfig.refinedCacaoTime = refined
        if (filet != -1L) temporaryBuffsConfig.filetTime = filet
        if (potato != -1L) temporaryBuffsConfig.pristinePotatoTime = potato
        if (pumpkin != -1L) temporaryBuffsConfig.powderPumpkinTime = pumpkin
    }

    fun setProfessionalMS(ms: Int) {
        hotmConfig.professionalMS = ms
    }

    fun setStrongArmMS(ms: Int) {
        hotmConfig.strongArmMS = ms
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

    @JvmStatic
    fun setBazaar(enabled: Boolean) {
        bazaarConfig.useBazaar = enabled
    }

    @JvmStatic
    fun setGemstoneVariant(variant: Bazaar.GemstoneVariant) {
        bazaarConfig.gemstoneVariant = variant
    }

    @JvmStatic
    fun setShowExtraStats(show: Boolean) {
        collectionConfig.showExtraStats = show
    }

    @JvmStatic
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

    @JvmStatic
    fun setApiTracking(enabled: Boolean) {
        trackingConfig.apiTracking = enabled
    }
}
