package io.github.chindeaone.collectiontracker.util;

import java.util.Locale;

public class NumbersUtils {

    public static String formatNumber(float number) {
        number = (float) Math.floor(number);

        if (number < 1_000) {
            return String.valueOf((int) number);
        } else if (number < 1_000_000) {
            return String.format("%.2fk", number / 1_000.0);
        } else if (number < 1_000_000_000) {
            return String.format("%.2fM", number / 1_000_000.0);
        } else {
            return String.format("%.2fB", number / 1_000_000_000.0);
        }
    }

    public static String compactFloat(float value) {
        if (Float.isNaN(value)) return "0";
        if (value == -1f) return "N/A";

        double v = value;
        String sign = "";
        if (v < 0) {
            sign = "-";
            v = Math.abs(v);
        }

        if (v < 1000.0) {
            return sign + String.format(Locale.US, "%.0f", v);
        }

        final String[] units = {"", "k", "M", "B", "T"};
        int unitIndex = 0;
        while (v >= 1000.0 && unitIndex < units.length - 1) {
            v /= 1000.0;
            unitIndex++;
        }

        return sign + String.format(Locale.US, "%.2f%s", v, units[unitIndex]);
    }
}
