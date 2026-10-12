package com.avaricious.game.run;

import com.badlogic.gdx.math.MathUtils;

/** Short-lived manual collection combo used by the Hot Hand skill. */
public final class CollectibleStreak {

    public static final float CLAIM_WINDOW_SECONDS = 1.20f;
    private static final float DEFAULT_VALUE_STEP = 0.25f;
    private static final float DEFAULT_VALUE_LIMIT = 4f;

    private int count;
    private float secondsRemaining;

    /** Records one manual pickup and returns its cash-value multiplier. */
    public float claim() {
        return claim(1, DEFAULT_VALUE_STEP, DEFAULT_VALUE_LIMIT);
    }

    /**
     * Records a manual pickup that may advance several streak steps. The
     * multiplier is calculated after those steps have been awarded.
     */
    public float claim(int streakGain, float valueStep, float valueLimit) {
        if (secondsRemaining <= 0f) count = 0;

        int safeGain = Math.max(1, streakGain);
        count = count > Integer.MAX_VALUE - safeGain
            ? Integer.MAX_VALUE
            : count + safeGain;
        secondsRemaining = CLAIM_WINDOW_SECONDS;
        return getValueMultiplier(valueStep, valueLimit);
    }

    public void update(float delta) {
        if (count == 0 || delta <= 0f) return;

        secondsRemaining = Math.max(0f, secondsRemaining - delta);
        if (secondsRemaining == 0f) count = 0;
    }

    public void reset() {
        count = 0;
        secondsRemaining = 0f;
    }

    public int getCount() {
        return count;
    }

    public float getValueMultiplier() {
        return getValueMultiplier(DEFAULT_VALUE_STEP, DEFAULT_VALUE_LIMIT);
    }

    public float getValueMultiplier(float valueStep, float valueLimit) {
        if (count <= 1) return 1f;
        return Math.min(
            Math.max(1f, valueLimit),
            1f + (count - 1) * Math.max(0f, valueStep)
        );
    }

    public float getTimeRemainingProgress() {
        return MathUtils.clamp(
            secondsRemaining / CLAIM_WINDOW_SECONDS,
            0f,
            1f
        );
    }
}
