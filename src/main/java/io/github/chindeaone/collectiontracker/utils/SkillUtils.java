/*
    Calculations based on Skyhanni's logic
 */
package io.github.chindeaone.collectiontracker.utils;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SkillUtils {

    public enum Skills {
        MINING("Mining"),
        FORAGING("Foraging"),
        FISHING("Fishing"),
        FARMING("Farming"),
        COMBAT("Combat"),
        ALCHEMY("Alchemy"),
        ENCHANTING("Enchanting"),
        TAMING("Taming"),
        HUNTING("Hunting"),
        CARPENTRY("Carpentry"),
        RUNECRAFTING("Runecrafting"),
        SOCIAL("Social");

        private final String displayName;

        Skills(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    public enum MaxSkillLevels {
        MINING(60),
        FORAGING(54),
        FISHING(50),
        FARMING(60),
        COMBAT(60),
        ALCHEMY(50),
        ENCHANTING(60),
        TAMING(60),
        HUNTING(25),
        CARPENTRY(50),
        RUNECRAFTING(25),
        SOCIAL(25);

        private final int maxLevel;

        MaxSkillLevels(int maxLevel) {
            this.maxLevel = maxLevel;
        }

        public int getMaxLevel() {
            return maxLevel;
        }
    }

    public static final EnumMap<Skills, Double> SKILL_VALUES = new EnumMap<>(Skills.class);
    public static final EnumMap<Skills, Integer> SKILL_LEVELS = new EnumMap<>(Skills.class);
    public static final EnumMap<Skills, Long> MAX_SKILL_XP = new EnumMap<>(Skills.class);

    private static final double XP_LVL_60 = 111_672_425D;

    // Precompute max XP for each skill
    public static void initializeMaxSkillXp() {
        for (Skills skill : Skills.values()) {
            int maxLevel = getMaxLevelForSkill(skill);
            long maxXp = getTotalXpForMaxLevel(skill, maxLevel);
            MAX_SKILL_XP.put(skill, maxXp);
        }
    }

    public static List<String> getDisplayNames() {
        return Arrays.stream(Skills.values())
                .map(Skills::getDisplayName)
                .collect(Collectors.toList());
    }

    public static boolean isValidSkill(String skillName) {
        return getDisplayNames().contains(skillName);
    }

    public static void updateFromApi(Map<String, Double> apiValues) {
        if (apiValues == null || apiValues.isEmpty()) return;
        synchronized (SKILL_VALUES) {
            synchronized (SKILL_LEVELS) {
                for (Map.Entry<String, Double> entry : apiValues.entrySet()) {
                    Skills skill = fromDisplayName(entry.getKey());
                    Double value = entry.getValue();
                    if (skill != null && value != null) {
                        int level = getLevelFromXp(skill, value);
                        SKILL_VALUES.put(skill, value);
                        SKILL_LEVELS.put(skill, level);
                    }
                }
            }
        }
        initializeMaxSkillXp();
    }

    public static Double getSkillValue(String skillName) {
        Skills skill = fromDisplayName(skillName);
        return skill == null ? null : getSkillValue(skill);
    }

    public static Integer getSkillLevel(String skillName) {
        Skills skill = fromDisplayName(skillName);
        return skill == null ? null : getSkillLevel(skill);
    }

    public static Double getTamingValue() {
        return getSkillValue(Skills.TAMING);
    }

    public static Integer getTamingLevel() {
        return getSkillLevel(Skills.TAMING);
    }

    public static long getMaxXpForSkill(String skillName) {
        Skills skill = fromDisplayName(skillName);
        return skill == null ? 0L : MAX_SKILL_XP.get(skill);
    }

    private static long getTotalXpForMaxLevel(Skills skill, int level) {
        if (level <=0) return 0L;
        if (skill == Skills.RUNECRAFTING) {
            return getCumulativeFromTable(RUNECRAFTING_LEVELING_XP, level);
        }
        if (level <=60) {
            return getCumulativeFromTable(LEVELING_XP, level);
        }
        return 0L;
    }

    private static long getCumulativeFromTable(int[] table, int level) {
        long total =0L;
        int capped = Math.min(level, table.length);
        for (int i =0; i < capped; i++) {
            total += table[i];
        }
        return total;
    }

    public static Boolean isSkillMaxed(String skillName) {
        Skills skill = toSkill(skillName);
        return skill == null ? null : isSkillMaxed(skill);
    }

    private static Boolean isSkillMaxed(Skills skill) {
        synchronized (SKILL_LEVELS) {
            Integer level = SKILL_LEVELS.get(skill);
            return level != null && level >= getMaxLevelForSkill(skill);
        }
    }

    private static Double getSkillValue(Skills skill) {
        synchronized (SKILL_VALUES) {
            return SKILL_VALUES.get(skill);
        }
    }

    private static Integer getSkillLevel(Skills skill) {
        synchronized (SKILL_LEVELS) {
            return SKILL_LEVELS.get(skill);
        }
    }

    private static Skills toSkill(String skillName) {
        return fromDisplayName(skillName);
    }

    private static Skills fromDisplayName(String skillName) {
        if (skillName == null) return null;
        for (Skills skill : Skills.values()) {
            if (skill.getDisplayName().equalsIgnoreCase(skillName)) {
                return skill;
            }
        }
        return null;
    }

    private static int getMaxLevelForSkill(Skills skill) {
        return MaxSkillLevels.valueOf(skill.name()).getMaxLevel();
    }

    private static int getLevelFromXp(Skills skill, double totalXp) {
        if (skill == Skills.RUNECRAFTING) {
            return getLevelFromTable(RUNECRAFTING_LEVELING_XP, totalXp);
        }
        if (totalXp < XP_LVL_60) {
            return getLevelFromTable(LEVELING_XP, totalXp);
        }
        return getLevelAbove60(totalXp);
    }

    private static int getLevelFromTable(int[] table, double totalXp) {
        long xp = (long) Math.floor(totalXp);
        long cumulative = 0L;
        for (int i = 0; i < table.length; i++) {
            cumulative += table[i];
            if (xp < cumulative) {
                return i;
            }
        }
        return table.length;
    }

    private static int getLevelAbove60(double totalXp) {
        long remaining = (long) Math.floor(totalXp - XP_LVL_60);
        int level = 60;
        while (remaining >= xpForLevelAbove60(level + 1)) {
            remaining -= xpForLevelAbove60(level + 1);
            level++;
        }
        return level;
    }

    private static long xpForLevelAbove60(int level) {
        if (level <= 60) return 0L;
        int k = level - 60; // 1 for lvl 61
        int block = (k - 1) / 10; // which 10-level block
        int posInBlock = (k - 1) % 10; // index inside the block (0-9)
        long baseXp = 7_600_000L;
        long baseSlope = 600_000L;
        long slope = baseSlope << block; // slope * 2^block
        long blockStartXp = baseXp + (10L * baseSlope * ((1L << block) - 1));
        return blockStartXp + slope * posInBlock;
    }

    private static final int[] LEVELING_XP = new int[] {
            50, 125, 200, 300, 500, 750, 1000, 1500, 2000, 3500,
            5000, 7500, 10000, 15000, 20000, 30000, 50000, 75000, 100000, 200000,
            300000, 400000, 500000, 600000, 700000, 800000, 900000, 1000000, 1100000, 1200000,
            1300000, 1400000, 1500000, 1600000, 1700000, 1800000, 1900000, 2000000, 2100000, 2200000,
            2300000, 2400000, 2500000, 2600000, 2750000, 2900000, 3100000, 3400000, 3700000, 4000000,
            4300000, 4600000, 4900000, 5200000, 5500000, 5800000, 6100000, 6400000, 6700000, 7000000
    };

    private static final int [] RUNECRAFTING_LEVELING_XP = new int[] {
            50, 100, 125, 160, 200, 250, 315, 400, 500, 625,
            785, 1000, 1250, 1600, 2000, 2465, 3125, 4000, 5000, 6200,
            7800, 9800, 12200, 15300, 19050
    };
}