package io.github.chindeaone.collectiontracker.utils

import io.github.chindeaone.collectiontracker.ModLoader
import io.github.chindeaone.collectiontracker.collections.BazaarCollectionsManager
import io.github.chindeaone.collectiontracker.collections.CollectionsManager
import io.github.chindeaone.collectiontracker.commands.CollectionTracker
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.config.ConfigHelper
import io.github.chindeaone.collectiontracker.tracker.collection.TrackingHandler
import io.github.chindeaone.collectiontracker.tracker.collection.multi_tracking.MultiTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingRates
import io.github.chindeaone.collectiontracker.utils.chat.ChatListener
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils.sendMessage

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
    }

    private fun checkApiTracking() {
        val trackingActive = TrackingHandler.isTracking || MultiTrackingHandler.isMultiTracking

        if (trackingActive && !CollectionTracker.isApiTracking && ConfigAccess.isApiTrackingEnabled()) {
            ConfigHelper.setApiTracking(CollectionTracker.isApiTracking)
            sendMessage("§cCan't enable API tracking while tracking is active!", true)
        }
    }

    private fun checkCollectionLeaderboard() {
        if (TrackingHandler.isTracking  && ConfigAccess.isCollectionLeaderboardEnabled() && !TrackingHandler.leaderboardTrackingInitialized) {
            ConfigHelper.disableCollectionLeaderboardTracking()
            sendMessage("§cCan't enable collection leaderboard mid tracking. Enable this before tracking a collection!", true)
        }
    }

    private fun checkGemstoneLeaderboard() {
        val trackingGemstoneOnly = CollectionTracker.collectionList.size == 1 && CollectionTracker.collectionList.contains("gemstone")

        if (MultiTrackingHandler.isMultiTracking && trackingGemstoneOnly &&
            ConfigAccess.isCollectionLeaderboardEnabled() &&
            !MultiTrackingHandler.leaderboardTrackingInitialized) {

            ConfigHelper.disableCollectionLeaderboardTracking()
            sendMessage("§cCan't enable collection leaderboard mid tracking. Enable this before tracking a collection!", true)
        }
    }

    private fun checkCollectionBazaar() {
        if (!TrackingHandler.isTracking) return

        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar()
            sendMessage("§cYou cannot use Bazaar prices for this collection!", true)
        }

        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isShowExtraStats()) {
            ConfigHelper.disableExtraStats()
            sendMessage("§cNo Bazaar data available for extra stats!", true)
            return
        }

        if (CollectionsManager.collectionType == "normal" && ConfigAccess.isShowExtraStats()) {
            ConfigHelper.disableExtraStats()
            sendMessage("§cExtra stats are redundant here!", true)
            return
        }

        if (ConfigAccess.isShowExtraStats() && !ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableExtraStats()
            sendMessage("§cDisabled extra stats since you don't use Bazaar prices!", true)
        }
    }

    private fun checkMultiCollectionBazaar() {
        if (!MultiTrackingHandler.isMultiTracking) return

        if (!BazaarCollectionsManager.hasBazaarData && ConfigAccess.isUsingBazaar()) {
            ConfigHelper.disableBazaar()
            sendMessage("§cYou cannot use Bazaar prices for this collection!", true)
        }
    }

    private fun checkSkillTracking() {
        if (!SkillTrackingHandler.isTracking) return

        if (ConfigAccess.isSkillLeaderboardEnabled() && !SkillTrackingHandler.leaderboardTrackingInitialized) {
            sendMessage("§cCan't enable skill leaderboard mid tracking. Enable this before tracking a skill!", true)
            ConfigHelper.disableSkillLeaderboardTracking()
        }

        if (ConfigAccess.isTamingTrackingEnabled() && SkillTrackingHandler.uptimeInSeconds > 1 && SkillTrackingRates.tamingXp == 0L) {
            sendMessage("§cCan't enable taming mid tracking. Enable this before tracking a skill!", true)
            ConfigHelper.disableTamingTracking()
        }
    }

    private fun checkSkyMallPerk() {
        if (ChatListener.currentSkyMallBuff.isEmpty()) {
            if (ConfigAccess.isSkyMallEnabled()) {
                ConfigHelper.disableSkyMall()
                sendMessage("§cYou don't have the Sky Mall perk unlocked.", true)
            }
        }
    }

    private fun checkLotteryPerk() {
        if (ChatListener.currentLotteryBuff.isEmpty()) {
            if (ConfigAccess.isLotteryEnabled()) {
                ConfigHelper.disableLottery()
                sendMessage("§cYou don't have the Lottery perk unlocked.", true)
            }
        }
    }

    private fun checkBeekeeperPerk() {
        if (ChatListener.currentBeekeeperBuff.isEmpty()) {
            if (ConfigAccess.isBeekeeperEnabled()) {
                ConfigHelper.disableBeekeeper()
                sendMessage("§cYou don't have the Beekeeper perk unlocked.", true)
            }
        }
    }
}