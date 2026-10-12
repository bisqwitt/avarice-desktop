package com.avaricious.utility;

import com.badlogic.gdx.math.RandomXS128;

/** Shared deterministic random source for run-affecting decisions. */
public final class SeededRandomizer {

    private static RandomXS128 random;
    private static long seed;

    private SeededRandomizer() {
    }

    public static void setSeed(long seed) {
        random = new RandomXS128(seed);
        SeededRandomizer.seed = seed;
    }

    public static float nextFloat(float min, float max) {
        if (max < min) {
            throw new IllegalArgumentException("max must be at least min");
        }
        return min + get().nextFloat() * (max - min);
    }

    public static int nextInt(int min, int max) {
        if (max < min) {
            throw new IllegalArgumentException("max must be at least min");
        }
        return min + get().nextInt(max - min + 1);
    }

    public static RandomXS128 get() {
        if (random == null) {
            throw new IllegalStateException("Randomizer seed has not been set");
        }
        return random;
    }

    public static long getSeed() {
        get();
        return seed;
    }

}
