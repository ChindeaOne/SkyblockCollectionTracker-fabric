package io.github.chindeaone.collectiontracker.util.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiningStatsParser {

    private MiningStatsParser() {}

    public static List<String> parse(List<String> raw, String blockType) {
        List<String> formatted = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return formatted;

        MiningContext ctx = new MiningContext(blockType);

        for (String line : raw) {
            if (line.contains("Fortune")) {
                processFortuneLine(line, ctx);
                continue;
            }

            if (line.contains("Mining Speed")) {
                ctx.speed.parse(line);
            } else if (line.contains("Mining Spread") || line.contains("Gemstone Spread")) {
                ctx.spread.parse(line);
            } else if (line.contains("Pristine")) {
                ctx.pristine.parse(line);
            } else if (line.contains("Breaking Power")) {
                ctx.breakingPower.parse(line);
            } else if (line.contains("Mining Wisdom")) {
                ctx.wisdom.parse(line);
            }
        }

        if (!"0".equals(ctx.speed.value)) formatted.add(ctx.speed.format());
        formatted.add(ctx.formatTotalFortune());

        if (ctx.isGemstone) {
            if (!"0".equals(ctx.spread.value)) formatted.add(ctx.spread.format());
            if (!"0".equals(ctx.pristine.value)) formatted.add(ctx.pristine.format());
        } else {
            if (!"0".equals(ctx.spread.value)) formatted.add(ctx.spread.format());
        }

        if (!"0".equals(ctx.breakingPower.value)) formatted.add(ctx.breakingPower.format());
        if (!"0".equals(ctx.wisdom.value)) formatted.add(ctx.wisdom.format());

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

        if (ctx.blockType == null) return;

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
        }
    }

    private static class MiningContext {
        final String blockType;
        final boolean isGemstone;

        int globalFortune = 0;
        int specificFortune = 0;
        String specificFortuneName = "";

        Stat speed = new Stat("Mining Speed", "⸕", "§6");
        Stat spread = new Stat("Mining Spread", "▚", "§e");
        Stat pristine = new Stat("Pristine", "✧", "§5");
        Stat breakingPower = new Stat("Breaking Power", "Ⓟ", "§2");
        Stat wisdom = new Stat("Mining Wisdom", "☯", "§3");

        MiningContext(String blockType) {
            this.blockType = blockType;
            this.isGemstone = "gemstones".equals(blockType);
            if (isGemstone) {
                this.spread = new Stat("Gemstone Spread", "▚", "§e");
            }
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

        String formatTotalFortune() {
            String symbol = "☘";
            String label = specificFortuneName.isEmpty() ? "Mining Fortune" : specificFortuneName;
            int total = globalFortune + specificFortune;
            return "§a" + label + ": §6" + symbol + total + " §7(" + globalFortune + "+" + specificFortune + ")";
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
