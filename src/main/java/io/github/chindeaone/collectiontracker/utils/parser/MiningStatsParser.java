package io.github.chindeaone.collectiontracker.utils.parser;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.utils.ScoreboardUtils;
import io.github.chindeaone.collectiontracker.utils.tab.MiningStatsWidget;
import io.github.chindeaone.collectiontracker.utils.world.MiningMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiningStatsParser {

    private static String lastDisplayedSpecificFortune = "";
    private static int lastDisplayedSpecificFortuneValue = 0;

    private MiningStatsParser() {}

    public static List<String> parse(List<String> raw, String blockType) {
        List<String> formatted = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return formatted;

        MiningContext ctx = new MiningContext(blockType);

        for (String line : raw) {
            if (line.contains("Mining Speed")) {
                addMiningSpeedPerks(line, ctx);
                continue;
            }

            if (line.contains("Fortune")) {
                processFortuneLine(line, ctx);
                continue;
            }

            if (line.contains("Mining Spread") || line.contains("Gemstone Spread")) {
                ctx.spread.parse(line);
            } else if (line.contains("Pristine")) {
                ctx.pristine.parse(line);
            } else if (line.contains("Mining Wisdom")) {
                ctx.wisdom.parse(line);
            } else if (line.contains("Cold Resistance")) {
                ctx.cold.parse(line);
            } else if (line.contains("Heat Resistance")) {
                ctx.heat.parse(line);
            } else if (line.contains("Breaking Power")) {
                ctx.breakingPower.parse(line);
            }
        }

        if (!"0".equals(ctx.speed.value)) formatted.add(ctx.formatTotalSpeed());
        formatted.add(ctx.formatTotalFortune());

        if (ctx.isGemstone) {
            if (!"0".equals(ctx.spread.value)) formatted.add(ctx.spread.format());
            if (!"0".equals(ctx.pristine.value)) formatted.add(ctx.pristine.format());
        } else {
            if (!"0".equals(ctx.spread.value)) formatted.add(ctx.spread.format());
        }

        if (!"0".equals(ctx.wisdom.value)) formatted.add(ctx.wisdom.format());
        if (!"0".equals(ctx.cold.value) && ScoreboardUtils.isColdStatRelevant() ) formatted.add(ctx.cold.format());
        if (!"0".equals(ctx.heat.value) && ScoreboardUtils.isHeatStatRelevant()) formatted.add(ctx.heat.format());
        if (!"0".equals(ctx.breakingPower.value)) formatted.add(ctx.breakingPower.format());

        return formatted;
    }

    private static int extractFortune(String line) {
        try {
            String digits = line.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static void processFortuneLine(String line, MiningContext ctx) {
        int value = extractFortune(line);

        if (line.contains("Mining Fortune")) {
            ctx.globalFortune = value;
            return;
        }

        if (!ctx.shouldShowSpecificFortune()) return;

        boolean match = switch (ctx.blockType) {
            case "dwarven_metals" -> line.contains("Dwarven Metal Fortune");
            case "pure_ores", "ores" -> line.contains("Ore Fortune");
            case "gemstones" -> line.contains("Gemstone Fortune");
            case "blocks" -> line.contains("Block Fortune");
            default -> false;
        };

        if (match) {
            ctx.specificFortune = value;
            ctx.specificFortuneName = ctx.getFortuneLabel();

            // Update last displayed specific fortune
            lastDisplayedSpecificFortune = ctx.specificFortuneName;
            lastDisplayedSpecificFortuneValue = ctx.specificFortune;
        }
    }

    private static void addMiningSpeedPerks(String line, MiningContext ctx) {
        int value = extractMiningSpeed(line);

        int professional = ConfigAccess.getProfessionalMS();
        int strongArm = ConfigAccess.getStrongArmMS();

        int total = switch (ctx.blockType) {
            case "dwarven_metals" -> value + strongArm;
            case "gemstones" -> value + professional;
            default -> value;
        };

        ctx.speed.value = String.valueOf(total);
    }

    private static int extractMiningSpeed(String line) {
        try {
            String digits = line.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class MiningContext {
        final String blockType;
        final String island;
        final boolean isGemstone;
        final boolean allowSpecificFortune;

        int globalFortune = 0;
        int specificFortune = 0;
        String specificFortuneName = "";

        Stat speed = new Stat("Mining Speed", "⸕", "§6");
        Stat spread = new Stat("Mining Spread", "▚", "§e");
        Stat pristine = new Stat("Pristine", "✧", "§5");
        Stat wisdom = new Stat("Mining Wisdom", "☯", "§3");
        Stat cold = new Stat("Cold Resistance", "❄", "§b");
        Stat heat = new Stat("Heat Resistance", "♨", "§c");
        Stat breakingPower = new Stat("Breaking Power", "Ⓟ", "§2");

        MiningContext(String blockType) {
            this.blockType = blockType;
            this.island = MiningStatsWidget.getCurrentMiningIsland();
            this.isGemstone = "gemstones".equals(blockType);

            if (isGemstone) {
                this.spread = new Stat("Gemstone Spread", "▚", "§e");
            }

            Set<String> allowed = MiningMapping.getMiningBlocksPerArea().get(blockType);
            this.allowSpecificFortune = allowed != null && island != null && allowed.contains(island);
        }

        boolean shouldShowSpecificFortune() {
            return allowSpecificFortune;
        }

        String getFortuneLabel() {
            return switch (blockType) {
                case "dwarven_metals" -> "Dwarven Metal Fortune";
                case "pure_ores", "ores" -> "Ore Fortune";
                case "gemstones" -> "Gemstone Fortune";
                case "blocks" -> "Block Fortune";
                default -> "";
            };
        }

        String getFortuneColor() {
            return switch (blockType) {
                case "dwarven_metals" -> "§a"; // Green
                case "pure_ores", "ores" -> "§e"; // Yellow
                case "gemstones" -> "§5"; // Purple
                case "blocks" -> "§8"; // Dark Gray
                default -> ""; // No color
            };
        }

        String formatTotalFortune() {
            String symbol = "☘";
            String color = getFortuneColor();
            int total = globalFortune + specificFortune;
            boolean showDetailed = ConfigAccess.isShowDetailedMiningFortune();

            // Show specific fortune if available
            if (!specificFortuneName.isEmpty()) {
                if (total == 0) return ""; // Don't show if total is 0
                String base = "§a" + specificFortuneName + ": §6" + symbol + total;
                if (showDetailed) {
                    base += " §7(§6" + globalFortune + " §7+ " + color + specificFortune + "§7)";
                }
                return base;
            }
            // Fallback to last displayed specific fortune
            if (!lastDisplayedSpecificFortune.isEmpty()) {
                if (total == 0) return ""; // Don't show if total is 0
                String base = "§a" + lastDisplayedSpecificFortune + ": §6" + symbol + total;
                if (showDetailed) {
                    base += " §7(§6" + globalFortune + " §7+ " + color + lastDisplayedSpecificFortuneValue + "§7)";
                }
                return base;
            }

            // Fallback to mining fortune
            return "§aMining Fortune: §6" + symbol + total;
        }

        String formatTotalSpeed() {
            return speed.format();
        }

        private static class Stat {
            String label;
            String symbol;
            String value = "0";
            String valueColor;

            Stat(String label, String defaultSymbol, String valueColor) {
                this.label = label;
                this.symbol = defaultSymbol;
                this.valueColor = valueColor;
            }

            void parse(String line) {
                Matcher symbolMatcher = Pattern.compile("(\\D)\\d").matcher(line);
                if (symbolMatcher.find()) {
                    this.symbol = symbolMatcher.group(1).trim();
                }
                this.value = line.replaceAll(".*" + Pattern.quote(this.symbol), "").trim();
            }

            String format() {
                return "§a" + label + ": " + valueColor + symbol + value;
            }
        }
    }
}