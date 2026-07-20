package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils

object ConfigStateUtils {

    var tickCounter: Int = 0

    fun onTick() {
        tickCounter = (tickCounter + 1) % 2
        if (tickCounter != 0) return

        checkApiTracking()
        checkCollectionLeaderboard()
        checkGemstoneLeaderboard()
        checkCollectionBazaar()
        checkMultiCollectionBazaar()
        checkSkillTracking()
    }

    private fun checkApiTracking() {
        val trackingActive = TrackingHandler.isTracking || MultiTrackingHandler.isMultiTracking

        if (trackingActive && !CollectionTracker.isApiTracking && ConfigAccess.isApiTrackingEnabled()) {
            ConfigHelper.setApiTracking(CollectionTracker.isApiTracking)
            ChatUtils.sendMessage("§cCan't enable API tracking while tracking is active!", true)
        }
    }

    private fun checkCollectionLeaderboard() {
        if (TrackingHandler.isTracking  && ConfigAccess.isCollectionLeaderboardEnabled() && !TrackingHandler.leaderboardTrackingInitialized) {
            ConfigHelper.disableCollectionLeaderboardTracking()
            ChatUtils.sendMessage("§cCan't enable collection leaderboard mid tracking. Enable this before tracking a collection!", true)
        }
    }

    private fun checkGemstoneLeaderboard() {
        val trackingGemstoneOnly = CollectionTracker.collectionList.size == 1 && CollectionTracker.collectionList.contains("gemstone")

        if (MultiTrackingHandler.isMultiTracking && trackingGemstoneOnly &&
            ConfigAccess.isCollectionLeaderboardEnabled() &&
            !MultiTrackingHandler.leaderboardTrackingInitialized) {

            ConfigHelper.disableCollectionLeaderboardTracking()
            ChatUtils.sendMessage("§cCan't enable collection leaderboard mid tracking. Enable this before tracking a collection!", true)
        }
    }

    private fun checkCollectionBazaar() {
        if (!TrackingHandler.isTracking) return

        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar()
            ChatUtils.sendMessage("§cYou cannot use Bazaar prices for this collection!", true)
        }

        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isShowExtraStats()) {
            ConfigHelper.disableExtraStats()
            ChatUtils.sendMessage("§cNo Bazaar data available for extra stats!", true)
            return
        }

        if (CollectionsManager.collectionType == "normal" && ConfigAccess.isShowExtraStats()) {
            ConfigHelper.disableExtraStats()
            ChatUtils.sendMessage("§cExtra stats are redundant here!", true)
            return
        }

        if (ConfigAccess.isShowExtraStats() && !ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableExtraStats()
            ChatUtils.sendMessage("§cDisabled extra stats since you don't use Bazaar prices!", true)
        }
    }

    private fun checkMultiCollectionBazaar() {
        if (!MultiTrackingHandler.isMultiTracking) return

        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar()
            ChatUtils.sendMessage("§cYou cannot use Bazaar prices for this collection!", true)
        }
    }

    private fun checkSkillTracking() {
        if (!SkillTrackingHandler.isTracking) return

        if (ConfigAccess.isSkillLeaderboardEnabled() && !SkillTrackingHandler.leaderboardTrackingInitialized) {
            ChatUtils.sendMessage("§cCan't enable skill leaderboard mid tracking. Enable this before tracking a skill!", true)
            ConfigHelper.disableSkillLeaderboardTracking()
        }

        if (ConfigAccess.isTamingTrackingEnabled() && SkillTrackingHandler.getUptimeInSeconds() > 1 && SkillTrackingRates.tamingXp == 0L) {
            ChatUtils.sendMessage("§cCan't enable taming mid tracking. Enable this before tracking a skill!", true)
            ConfigHelper.disableTamingTracking()
        }
    }
}