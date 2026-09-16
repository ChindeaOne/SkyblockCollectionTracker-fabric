package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.commands.SkillTracker
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.config.apiTracking
import io.github.chindeaone.collectiontracker.config.collectionLeaderboard
import io.github.chindeaone.collectiontracker.config.enableBeekeeper
import io.github.chindeaone.collectiontracker.config.enableLottery
import io.github.chindeaone.collectiontracker.config.enableSkyMall
import io.github.chindeaone.collectiontracker.config.enableTamingTracking
import io.github.chindeaone.collectiontracker.config.showExtraStats
import io.github.chindeaone.collectiontracker.config.skillLeaderboard
import io.github.chindeaone.collectiontracker.config.useBazaar
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage
import io.github.chindeaone.collectiontracker.utils.world.WaypointsUtils

object ConfigStateUtils {

    fun onClientTick() {
        if (ModLoader.clientTicks % 4L != 0L) return

        checkApiTracking()
        checkCollectionLeaderboard()
        checkGemstoneLeaderboard()
        checkCollectionBazaar()
        checkMultiCollectionBazaar()
        checkSkillTracking()
        checkSkyMallPerk()
        checkLotteryPerk()
        checkBeekeeperPerk()
        checkTamingTracking()
        WaypointsUtils.checkConfig()
    }

    private fun checkApiTracking() {
        val trackingActive = TrackingHandler.isTracking || MultiTrackingHandler.isMultiTracking

        if (trackingActive && !CollectionTracker.isApiTracking && apiTracking) {
            ConfigHelper.setApiTracking(CollectionTracker.isApiTracking)
            sendMessage("§cCan't enable API tracking while tracking is active!", true)
        }
    }

    private fun checkCollectionLeaderboard() {
        if (TrackingHandler.isTracking  && collectionLeaderboard && !TrackingHandler.leaderboardTrackingInitialized) {
            ConfigHelper.disableCollectionLeaderboardTracking()
            sendMessage("§cCan't enable collection leaderboard mid tracking. Enable this before tracking a collection!", true)
        }
    }

    private fun checkGemstoneLeaderboard() {
        val trackingGemstoneOnly = CollectionTracker.collectionList.size == 1 && CollectionTracker.collectionList.contains("gemstone")

        if (MultiTrackingHandler.isMultiTracking && trackingGemstoneOnly &&
            collectionLeaderboard &&
            !MultiTrackingHandler.leaderboardTrackingInitialized) {

            ConfigHelper.disableCollectionLeaderboardTracking()
            sendMessage("§cCan't enable collection leaderboard mid tracking. Enable this before tracking a collection!", true)
        }
    }

    private fun checkCollectionBazaar() {
        if (!TrackingHandler.isTracking) return

        if (!BazaarCollectionsManager.hasBazaarData && useBazaar) {
            ConfigHelper.disableBazaar()
            sendMessage("§cYou cannot use Bazaar prices for this collection!")
        }

        if (!BazaarCollectionsManager.hasBazaarData && showExtraStats) {
            ConfigHelper.disableExtraStats()
            sendMessage("§cNo Bazaar data available for extra stats!")
            return
        }

        if (CollectionsManager.collectionType == "normal" && showExtraStats) {
            ConfigHelper.disableExtraStats()
            sendMessage("§cExtra stats are redundant here!")
            return
        }

        if (showExtraStats && !useBazaar) {
            ConfigHelper.disableExtraStats()
            sendMessage("§cDisabled extra stats since you don't use Bazaar prices!")
        }
    }

    private fun checkMultiCollectionBazaar() {
        if (!MultiTrackingHandler.isMultiTracking) return

        if (!BazaarCollectionsManager.hasBazaarData && useBazaar) {
            ConfigHelper.disableBazaar()
            sendMessage("§cYou cannot use Bazaar prices for this collection!")
        }
    }

    private fun checkSkillTracking() {
        if (!SkillTrackingHandler.isTracking) return

        if (skillLeaderboard && !SkillTrackingHandler.leaderboardTrackingInitialized) {
            sendMessage("§cCan't enable skill leaderboard mid tracking. Enable this before tracking a skill!")
            ConfigHelper.disableSkillLeaderboardTracking()
        }

        if (enableTamingTracking && SkillTrackingHandler.uptimeInSeconds > 1 && SkillTrackingRates.tamingXp == 0L) {
            sendMessage("§cCan't enable taming mid tracking. Enable this before tracking a skill!")
            ConfigHelper.disableTamingTracking()
        }
    }

    private fun checkSkyMallPerk() {
        if (ChatListener.currentSkyMallBuff.isEmpty()) {
            if (enableSkyMall) {
                ConfigHelper.disableSkyMall()
                sendMessage("§cYou don't have the Sky Mall perk unlocked.")
            }
        }
    }

    private fun checkLotteryPerk() {
        if (ChatListener.currentLotteryBuff.isEmpty()) {
            if (enableLottery) {
                ConfigHelper.disableLottery()
                sendMessage("§cYou don't have the Lottery perk unlocked.")
            }
        }
    }

    private fun checkBeekeeperPerk() {
        if (ChatListener.currentBeekeeperBuff.isEmpty()) {
            if (enableBeekeeper) {
                ConfigHelper.disableBeekeeper()
                sendMessage("§cYou don't have the Beekeeper perk unlocked.")
            }
        }
    }

    private fun checkTamingTracking() {
        if (!SkillTrackingHandler.isTracking) return

        if (enableTamingTracking && SkillTracker.skillName == "Taming") {
            ConfigHelper.disableTamingTracking()
        }
    }
}