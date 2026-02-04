package io.github.chindeaone.collectiontracker.commands;

import io.github.chindeaone.collectiontracker.api.hypixelapi.SkillApiFetcher;
import io.github.chindeaone.collectiontracker.util.SkillUtils;
import io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler;
import io.github.chindeaone.collectiontracker.util.ChatUtils;
import io.github.chindeaone.collectiontracker.util.HypixelUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletableFuture;

import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.isPaused;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.isTracking;

public class SkillTracker {

    public static String skillName = "";

    public static Logger logger = LogManager.getLogger(SkillTracker.class);

    public static void startTracking(String skill) {
        try {
            if (!HypixelUtils.isOnSkyblock()) {
                ChatUtils.INSTANCE.sendMessage("§cYou must be on Hypixel Skyblock to use this command!", true);
                return;
            }

            try {
                if (isTracking || isPaused) {
                    ChatUtils.INSTANCE.sendMessage("§cAlready tracking a skill.", true);
                    return;
                }

                skillName = skill;
                if (!SkillUtils.isValidSkill(skillName)) {
                    ChatUtils.INSTANCE.sendMessage("§4" + skillName + " skill is not a real skill!", true);
                    return;
                }

                // Fetch skill data asynchronously and start tracking once done
                CompletableFuture.runAsync(SkillApiFetcher::fetchSkillsData)
                        .thenRun(SkillTrackingHandler::startTracking);
            } catch (Exception e) {
                logger.error("An error occurred while starting skill tracking: ", e);
                ChatUtils.INSTANCE.sendMessage("§cAn error occurred while starting skill tracking. Please try again later.", true);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}