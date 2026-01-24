package io.github.chindeaone.collectiontracker.util;

import java.util.Locale;

public class NumbersUtils {

    // Shared units (future-proofed for larger numbers)
    private static final String[] UNITS = {"", "k", "M", "B", "T"};

    /**
     * Compactly formats a long value using units (k, M, B, T).
     * For values < 1000 returns a plain integer string, otherwise returns with 2 decimals and unit suffix.
     */
    public static String formatNumber(long number) {
        if (number == 0) return "0";

        if (number < 1000) {
            return String.format(Locale.US, "%d", number);
        }

        double compactNumber = (double) number;
        int index = 0;
        while (compactNumber >= 1000.0 && index < UNITS.length - 1) {
            compactNumber /= 1000.0;
            index++;
        }

        return String.format(Locale.US, "%.2f%s", compactNumber, UNITS[index]);
    }
}
