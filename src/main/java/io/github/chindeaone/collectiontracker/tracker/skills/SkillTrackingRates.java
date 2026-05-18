package io.github.chindeaone.collectiontracker.tracker.skills;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.utils.SkillUtils;
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardManager;
import io.github.chindeaone.collectiontracker.tracker.collection.LeaderboardEntry;

import static io.github.chindeaone.collectiontracker.commands.SkillTracker.skillName;
import static io.github.chindeaone.collectiontracker.tracker.skills.SkillTrackingHandler.getUptimeInSeconds;

public class SkillTrackingRates {

    public static volatile int skillLevel; // session start level and api level
    public static volatile long skillXp; // session start xp and api xp
    public static volatile long totalSkillXp;

    public static volatile long skillXpGained = 0L;
    public static volatile long skillPerHour = 0L;
    private static long lastXpGained = 0L;

    public static volatile int tamingLevel; // session start level and api level
    public static volatile long tamingXp; // session start xp and api xp

    public static volatile long tamingXpGained = 0L;
    public static volatile long tamingPerHour = 0L;
    private static long lastTamingXpGained = 0L;

    public static boolean afk = false;
    private static int skillUnchangedStreak = 0;
    private static int tamingUnchangedStreak = 0;
    private static final int THRESHOLD = 2; // Number of checks before considering AFK

    // Skill Leaderboard tracking data
    public static volatile int skillCurrentRank = -1;
    public static volatile String skillNextRankUsername = null;
    public static volatile long skillNextRankAmount = -1L;
    public static volatile long skillTillNextRank = -1L;
    public static volatile String skillEtaToNextRank = null;

    // Taming Leaderboard tracking data
    public static volatile int tamingCurrentRank = -1;
    public static volatile String tamingNextRankUsername = null;
    public static volatile long tamingNextRankAmount = -1L;
    public static volatile long tamingTillNextRank = -1L;
    public static volatile String tamingEtaToNextRank = null;

    public static void initTracking(int level, long xp) {
        skillLevel = level;
        skillXp = xp;
        totalSkillXp = xp;

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
                skillUnchangedStreak = 0;
                afk = false;
            } else {
                skillUnchangedStreak++;
                if (skillUnchangedStreak >= THRESHOLD) {
                    afk = true;
                    SkillTrackingHandler.stopTracking();
                    return;
                }
            }
        }
        long uptime = getUptimeInSeconds();
        skillPerHour = uptime > 0 ? (long) Math.floor(skillXpGained / (uptime / 3600.0)) : 0;
        totalSkillXp = skillXp + skillXpGained;

        updateSkillLeaderboardStats();
        updateSkillEta();
    }

    public static synchronized void calculateTamingRates(long value) {
        tamingXpGained = value - tamingXp; // total gained since tracking started

        // AFK detection (API calls only)
        if (lastTamingXpGained != tamingXpGained) {
            lastTamingXpGained = tamingXpGained;
            tamingUnchangedStreak = 0;
            afk = false;
        } else {
            tamingUnchangedStreak++;
            if (tamingUnchangedStreak >= THRESHOLD) {
                afk = true;
                SkillTrackingHandler.stopTracking();
                return;
            }
        }

        long uptime = getUptimeInSeconds();
        tamingPerHour = uptime > 0 ? (long) Math.floor(tamingXpGained / (uptime / 3600.0)) : 0;

        updateTamingLeaderboardStats();
        updateTamingEta();
    }

    public static void updateSkillLeaderboardStats() {
        if (!ConfigAccess.isSkillLeaderboardEnabled()) return;

        LeaderboardEntry playerEntry = LeaderboardManager.getPlayerEntry(skillName, totalSkillXp);
        skillCurrentRank = (playerEntry != null) ? playerEntry.rank() : -1;

        LeaderboardEntry nextEntry = LeaderboardManager.getNextRankEntry(skillName, totalSkillXp);
        if (nextEntry != null) {
            skillNextRankUsername = nextEntry.username();
            skillNextRankAmount = nextEntry.amount();
            skillTillNextRank = skillNextRankAmount - totalSkillXp;
            updateSkillEta();
        } else {
            skillNextRankUsername = null;
            skillNextRankAmount = -1L;
            skillTillNextRank = -1L;
            skillEtaToNextRank = null;
        }
    }

    public static void updateTamingLeaderboardStats() {
        if (!ConfigAccess.isSkillLeaderboardEnabled() || !ConfigAccess.isTamingTrackingEnabled()) return;

        LeaderboardEntry playerEntry = LeaderboardManager.getPlayerEntry("Taming", tamingXp + tamingXpGained);
        tamingCurrentRank = (playerEntry != null) ? playerEntry.rank() : -1;

        LeaderboardEntry nextEntry = LeaderboardManager.getNextRankEntry("Taming", tamingXp + tamingXpGained);
        if (nextEntry != null) {
            tamingNextRankUsername = nextEntry.username();
            tamingNextRankAmount = nextEntry.amount();
            tamingTillNextRank = tamingNextRankAmount - (tamingXp + tamingXpGained);
            updateTamingEta();
        } else {
            tamingNextRankUsername = null;
            tamingNextRankAmount = -1L;
            tamingTillNextRank = -1L;
            tamingEtaToNextRank = null;
        }
    }

    public static void updateSkillEta() {
        if (skillPerHour > 0 && skillTillNextRank > 0) {
            double hours = (double) skillTillNextRank / skillPerHour;
            skillEtaToNextRank = formatEta(hours);
        } else {
            skillEtaToNextRank = null;
        }
    }

    public static void updateTamingEta() {
        if (tamingPerHour > 0 && tamingTillNextRank > 0) {
            double hours = (double) tamingTillNextRank / tamingPerHour;
            tamingEtaToNextRank = formatEta(hours);
        } else {
            tamingEtaToNextRank = null;
        }
    }

    public static String formatEta(double hours) {
        if (hours >= 1) {
            long totalSeconds = (long) (hours * 3600);
            long hh = totalSeconds / 3600;
            long mm = (totalSeconds % 3600) / 60;
            long ss = totalSeconds % 60;
            return String.format("%02d:%02d:%02d", hh, mm, ss);
        } else {
            long minutes = (long) (hours * 60);
            long seconds = (long) ((hours * 60 - minutes) * 60);
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static void resetSession() {
        skillLevel = 0;
        skillXp = 0L;
        totalSkillXp = 0L;
        skillXpGained = 0L;
        skillPerHour = 0L;

        tamingLevel = 0;
        tamingXp = 0L;
        tamingXpGained = 0L;
        tamingPerHour = 0L;

        lastXpGained = 0L;
        lastTamingXpGained = 0L;
        afk = false;
        skillUnchangedStreak = 0;
        tamingUnchangedStreak = 0;

        skillCurrentRank = -1;
        skillNextRankUsername = null;
        skillNextRankAmount = -1L;
        skillTillNextRank = -1L;
        skillEtaToNextRank = null;

        tamingCurrentRank = -1;
        tamingNextRankUsername = null;
        tamingNextRankAmount = -1L;
        tamingTillNextRank = -1L;
        tamingEtaToNextRank = null;
    }
}