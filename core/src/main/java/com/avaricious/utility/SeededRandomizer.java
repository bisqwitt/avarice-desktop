package com.avaricious.utility;

import com.badlogic.gdx.math.RandomXS128;

public class SeededRandomizer {
    public static RandomXS128 rng;
    public static long seed;

    public static void setSeed(long seed) {
        rng = new RandomXS128(seed);
        SeededRandomizer.seed = seed;
    }

    public static float nextFloat(float min, float max) {
        return min + rng.nextFloat() * (max - min);
    }

    public static int nextInt(int min, int max) {
        return min + rng.nextInt(max - min + 1);
    }

    public static RandomXS128 get() {
        return rng;
    }

}
