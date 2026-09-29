package com.avaricious.game.run;

import com.badlogic.gdx.math.MathUtils;

/** Non-visual casino progression state for one game run. */
public final class ChipProgress {

    private static final float FIRST_LEVEL_REQUIREMENT = 12f;
    private static final float LEVEL_REQUIREMENT_GROWTH = 1.27f;

    private int level = 1;
    private int chips;
    private int chipsRequired = calculateChipsRequired(level);

    /** Returns how many levels were gained. */
    public int add(int amount) {
        chips += amount;
        int levelsGained = 0;

        while (chips >= chipsRequired) {
            chips -= chipsRequired;
            level++;
            levelsGained++;
            chipsRequired = calculateChipsRequired(level);
        }

        return levelsGained;
    }

    public void restore(int savedLevel, int savedChips) {
        level = Math.max(1, savedLevel);
        chipsRequired = calculateChipsRequired(level);
        chips = MathUtils.clamp(savedChips, 0, chipsRequired - 1);
    }

    public int getLevel() {
        return level;
    }

    public int getChips() {
        return chips;
    }

    public int getChipsRequired() {
        return chipsRequired;
    }

    public float getProgress() {
        return (float) chips / chipsRequired;
    }

    private int calculateChipsRequired(int targetLevel) {
        double rawRequirement = FIRST_LEVEL_REQUIREMENT
            * Math.pow(
                LEVEL_REQUIREMENT_GROWTH,
                Math.max(0, targetLevel - 1)
            );
        double roundingStep = rawRequirement < 50d
            ? 1d : rawRequirement < 1_000d ? 5d : 50d;
        return (int) Math.min(
            Integer.MAX_VALUE,
            Math.ceil(rawRequirement / roundingStep) * roundingStep
        );
    }
}
