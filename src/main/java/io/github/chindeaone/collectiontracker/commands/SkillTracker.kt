package io.github.chindeaone.collectiontracker.commands

import io.github.chindeaone.collectiontracker.api.hypixelapi.SkillApiFetcher
import io.github.chindeaone.collectiontracker.config.ConfigAccess
import io.github.chindeaone.collectiontracker.tracker.skills.SkillFetcher
import io.github.chindeaone.collectiontracker.utils.SkillUtils
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler
import io.github.chindeaone.collectiontracker.utils.chat.ChatUtils
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.concurrent.CompletableFuture

import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.isTracking

object SkillTracker {

    var skillName = ""
    private val logger: Logger = LogManager.getLogger(SkillTracker::class.java)

    fun startTracking(skill: String) {
        try {
            if (isTracking) {
                ChatUtils.sendMessage("§cAlready tracking a skill.", true)
                return
            }

            skillName = skill
            if (!SkillUtils.isValidSkill(skillName)) {
                ChatUtils.sendMessage("§4$skillName skill is not a real skill!", true)
                skillName = ""
                return
            }

            // Fetch skill data and leaderboard data asynchronously
            SkillApiFetcher.fetchSkillsData()
                    .thenCompose { SkillFetcher.fetchSkillLeaderboardData(skillName) }
                    .thenCompose {
                        if (ConfigAccess.isTamingTrackingEnabled()) SkillFetcher.fetchSkillLeaderboardData("Taming")
                        else CompletableFuture.completedFuture(null)
                    }
                    .thenRun(SkillTrackingHandler::startTracking)
        } catch (e: Exception) {
            logger.error("An error occurred while starting skill tracking: ", e)
            ChatUtils.sendMessage("§cAn error occurred while starting skill tracking. Please try again later.", true)
        }
    }
}