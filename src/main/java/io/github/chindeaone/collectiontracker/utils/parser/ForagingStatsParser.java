package io.github.chindeaone.collectiontracker.utils.parser;

import io.github.chindeaone.collectiontracker.config.ConfigAccess;
import io.github.chindeaone.collectiontracker.utils.tab.ForagingStatsWidget;
import io.github.chindeaone.collectiontracker.utils.world.ForagingMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForagingStatsParser {

    private static String lastDisplayedSpecificFortune = "";
    private static int lastDisplayedSpecificFortuneValue = 0;
    private static int lastDisplayedBeaconFortuneValue = 0;
    private static String lastDisplayedFortuneColor = "§6";

    private ForagingStatsParser() {}

    public static List<String> parse(List<String> raw, List<String> rawBeacon, String blockType) {
        List<String> formatted = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return formatted;

        ForagingContext ctx = new ForagingContext(blockType);

        if (rawBeacon != null && !rawBeacon.isEmpty()) {
            for (String line : rawBeacon) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                processFortuneLine(trimmed, ctx, true);
            }
        }

        for (String line : raw) {
            boolean isForagingStat = false;
            for (String stat : ForagingMapping.getForagingStats()) {
                if (line.contains(stat)) {
                    isForagingStat = true;
                    break;
                }
            }
            if (!isForagingStat) continue;

            if (line.contains("Sweep")) {
                ctx.sweep.parse(line);
            } else if (line.contains("Fortune")) {
                processFortuneLine(line, ctx, false);
            } else if (line.contains("Foraging Wisdom")) {
                ctx.wisdom.parse(line);
            }
        }

        if (!"0".equals(ctx.sweep.value)) {
            formatted.add(ctx.sweep.format());
        }

        formatted.add(ctx.formatTotalFortune());

        if (!"0".equals(ctx.wisdom.value)) {
            formatted.add(ctx.wisdom.format());
        }

        return formatted;
    }

    private static void processFortuneLine(String line, ForagingContext ctx, boolean isBeacon) {
        int value = extractFortune(line);

        if (isBeacon && (line.contains("Stacks") || line.contains("Stack"))) {
            ctx.beaconStacks = line.replaceAll(".*: ", "");
            return;
        }

        if (line.contains("Foraging Fortune") && !line.contains("Fig") && !line.contains("Mangrove")) {
            ctx.globalFortune = value;
            return;
        }

        if (line.contains("Fig Fortune")) {
            if (isBeacon) ctx.beaconFigFortune += value;
            else ctx.figFortune += value;

            if ("fig".equals(ctx.blockType)) {
                lastDisplayedSpecificFortune = "Fig Fortune";
                lastDisplayedSpecificFortuneValue = ctx.figFortune;
                lastDisplayedBeaconFortuneValue = ctx.beaconFigFortune;
                lastDisplayedFortuneColor = "§e";
            }
        } else if (line.contains("Mangrove Fortune")) {
            if (isBeacon) ctx.beaconMangroveFortune += value;
            else ctx.mangroveFortune += value;

            if ("mangrove".equals(ctx.blockType)) {
                lastDisplayedSpecificFortune = "Mangrove Fortune";
                lastDisplayedSpecificFortuneValue = ctx.mangroveFortune;
                lastDisplayedBeaconFortuneValue = ctx.beaconMangroveFortune;
                lastDisplayedFortuneColor = "§c";
            }
        }
    }

    private static int extractFortune(String line) {
        try {
            String digits = line.replaceAll("[^0-9]", "");
            return digits.isEmpty() ? 0 : Integer.parseInt(digits);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private static class ForagingContext {
        final String blockType;

        int globalFortune = 0;
        int figFortune = 0;
        int mangroveFortune = 0;
        int beaconFigFortune = 0;
        int beaconMangroveFortune = 0;
        String beaconStacks = "";

        Stat sweep = new Stat("Sweep", "∮", "§2");
        Stat wisdom = new Stat("Foraging Wisdom", "☯", "§3");

        ForagingContext(String blockType) {
            this.blockType = blockType;
        }

        String formatTotalFortune() {
            String symbol = "☘";
            int baseSpecific = 0;
            int beaconSpecific = 0;
            String specificFortuneName = "";
            String specificColor = "§6";

            if ("fig".equals(blockType)) {
                baseSpecific = figFortune;
                beaconSpecific = beaconFigFortune;
                specificFortuneName = "Fig Fortune";
                specificColor = "§e";
            } else if ("mangrove".equals(blockType)) {
                baseSpecific = mangroveFortune;
                beaconSpecific = beaconMangroveFortune;
                specificFortuneName = "Mangrove Fortune";
                specificColor = "§c";
            }

            int total = globalFortune + (specificFortuneName.isEmpty() ? (lastDisplayedSpecificFortuneValue + lastDisplayedBeaconFortuneValue) : (baseSpecific + beaconSpecific));
            String stackDisplay = beaconStacks.isEmpty() ? "" : " §3(" + beaconStacks + ")";

            // If player isn't on Galatea, use global fortune only
            if (!ForagingStatsWidget.INSTANCE.isInGalatea()) {
                return "§aForaging Fortune: §6" + symbol + globalFortune;
            }

            boolean showDetailed = ConfigAccess.isShowDetailedForagingFortune();

            if (!specificFortuneName.isEmpty()) {
                String base = "§a" + specificFortuneName + ": §6" + symbol + total + stackDisplay;
                if (showDetailed) {
                    base += " §7(§6" + globalFortune + " §7+ " + specificColor + baseSpecific;
                    if (beaconSpecific > 0) base += " §7+ §3" + beaconSpecific;
                    base += "§7)";
                }
                return base;
            }

            if (!lastDisplayedSpecificFortune.isEmpty()) {
                String base = "§a" + lastDisplayedSpecificFortune + ": §6" + symbol + total + stackDisplay;
                if (showDetailed) {
                    base += " §7(§6" + globalFortune + " §7+ " + lastDisplayedFortuneColor + lastDisplayedSpecificFortuneValue;
                    if (lastDisplayedBeaconFortuneValue > 0) base += " §7+ §3" + lastDisplayedBeaconFortuneValue;
                    base += "§7)";
                }
                return base;
            }

            return "§aForaging Fortune: §6" + symbol + globalFortune;
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
