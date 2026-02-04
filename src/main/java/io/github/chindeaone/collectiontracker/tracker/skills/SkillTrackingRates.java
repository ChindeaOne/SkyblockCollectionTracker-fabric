package io.github.chindeaone.collectiontracker.tracker.skills;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.util.SkillUtils;

import static io.github.chindeaone.collectiontracker.commands.SkillTracker.skillName;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.getUptimeInSeconds;

public class SkillTrackingRates {

    public static volatile int skillLevel; // session start level and api level
    public static volatile long skillXp; // session start xp and api xp

    public static volatile long skillXpGained = 0L;
    public static volatile long skillPerHour = 0L;
    private static long lastXpGained = 0L;

    public static volatile int tamingLevel; // session start level and api level
    public static volatile long tamingXp; // session start xp and api xp

    public static volatile long tamingXpGained = 0L;
    public static volatile long tamingPerHour = 0L;
    private static long lastTamingXpGained = 0L;

    public static boolean afk = false;
    private static int unchangedStreak = 0;
    private static final int THRESHOLD = 2; // Number of checks before considering AFK

    public static void initTracking(int level, long xp) {
        skillLevel = level;
        skillXp = xp;

        if (ConfigAccess.isTamingTrackingEnabled()) {
            tamingLevel = SkillUtils.getTamingLevel();
            tamingXp = SkillUtils.getTamingValue().longValue();
        }
    }

    public static synchronized void calculateSkillRates(long value) {
        skillXpGained = value - (skillXp - (SkillTrackingHandler.isSkillMaxed ? SkillUtils.getMaxXpForSkill(skillName) : 0L)); // total gained since tracking started

        // AFK detection (API calls only)
        if (!SkillTrackingHandler.isSkillMaxed) {
            if (lastXpGained != skillXpGained) {
                lastXpGained = skillXpGained;
                unchangedStreak = 0;
                afk = false;
            } else {
                unchangedStreak++;
                if (unchangedStreak >= THRESHOLD) {
                    afk = true;
                    SkillTrackingHandler.stopTracking();
                    return;
                }
            }
        }
        long uptime = getUptimeInSeconds();
        skillPerHour = uptime > 0 ? (long) Math.floor(skillXpGained / (uptime / 3600.0)) : 0;
    }

    public static void calculateTamingRates(long value) {
        tamingXpGained = value - tamingXp; // total gained since tracking started

        // AFK detection in case taming tracking is enabled
        if (lastTamingXpGained != tamingXpGained) {
            lastTamingXpGained = tamingXpGained;
            unchangedStreak = 0;
            afk = false;
        } else {
            unchangedStreak++;
            if (unchangedStreak >= THRESHOLD) {
                afk = true;
                SkillTrackingHandler.stopTracking();
                return;
            }
        }

        System.out.println(unchangedStreak);
        long uptime = getUptimeInSeconds();
        tamingPerHour = uptime > 0 ? (long) Math.floor(tamingXpGained / (uptime / 3600.0)) : 0;
    }

    public static void resetSession() {
        skillLevel = 0;
        skillXp = 0L;
        skillXpGained = 0L;
        skillPerHour = 0L;

        tamingLevel = 0;
        tamingXp = 0L;
        tamingXpGained = 0L;
        tamingPerHour = 0L;

        lastXpGained = 0L;
        lastTamingXpGained = 0L;
        afk = false;
        unchangedStreak = 0;
    }
}