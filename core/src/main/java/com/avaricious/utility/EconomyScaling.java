package com.avaricious.utility;

import java.util.Locale;

/** Shared economy rounding and display rules for long incremental runs. */
public final class EconomyScaling {
    private static final float COMPACT_THRESHOLD = 1_000f;
    private static final float[] DIVISORS = {
        1_000f,
        1_000_000f,
        1_000_000_000f,
        1_000_000_000_000f,
        1_000_000_000_000_000f
    };
    private static final char[] SUFFIXES = {'k', 'm', 'b', 't', 'q'};

    private EconomyScaling() {
    }

    /** Rounds upward to three significant digits so prices remain readable. */
    public static float roundPrice(double rawPrice) {
        if (rawPrice <= 0d) return 0f;
        if (rawPrice < 1_000d) return (float) Math.ceil(rawPrice / 5d) * 5f;
        if (rawPrice < 10_000d) return (float) Math.ceil(rawPrice / 10d) * 10f;

        double magnitude = Math.pow(10d, Math.floor(Math.log10(rawPrice)) - 2d);
        return (float) (Math.ceil(rawPrice / magnitude) * magnitude);
    }

    /** Compact, non-overstating notation used by popups and descriptions. */
    public static String compact(float value) {
        float absoluteValue = Math.abs(value);
        if (absoluteValue < COMPACT_THRESHOLD) {
            return Long.toString((long) Math.floor(absoluteValue));
        }

        int suffixIndex = 0;
        while (suffixIndex < DIVISORS.length - 1
            && absoluteValue >= DIVISORS[suffixIndex] * 1_000f) {
            suffixIndex++;
        }

        float scaled = absoluteValue / DIVISORS[suffixIndex];
        if (scaled < 10f) {
            scaled = (float) Math.floor(scaled * 100f) / 100f;
            if (Math.abs(scaled - Math.round(scaled)) >= 0.0001f) {
                String pattern = Math.abs(scaled * 10f - Math.round(scaled * 10f)) < 0.001f
                    ? "%.1f%c" : "%.2f%c";
                return String.format(Locale.ROOT, pattern, scaled, SUFFIXES[suffixIndex]);
            }
        } else if (scaled < 100f) {
            scaled = (float) Math.floor(scaled * 10f) / 10f;
            if (Math.abs(scaled - Math.round(scaled)) >= 0.0001f) {
                return String.format(Locale.ROOT, "%.1f%c", scaled, SUFFIXES[suffixIndex]);
            }
        } else {
            scaled = (float) Math.floor(scaled);
        }
        return String.format(Locale.ROOT, "%.0f%c", scaled, SUFFIXES[suffixIndex]);
    }
}
