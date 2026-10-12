package com.avaricious.game.run;

import com.badlogic.gdx.math.MathUtils;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Non-visual casino progression state for one game run. */
public final class ChipProgress {

    public static final String SPADES = "spades";

    private static final float FIRST_LEVEL_REQUIREMENT = 12f;
    private static final float LEVEL_REQUIREMENT_GROWTH = 1.27f;
    private static final int MILESTONE_ROUND_INTERVAL = 5;

    private int level = 1;
    private int chips;
    private int chipsRequired = calculateChipsRequired(level);
    private int spades;
    private final PropertyChangeSupport changes =
        new PropertyChangeSupport(this);

    /** Returns how many levels were gained. */
    public int addProgress(int amount) {
        long pendingChips = (long) chips + Math.max(0, amount);
        int levelsGained = 0;

        while (pendingChips >= chipsRequired && level < Integer.MAX_VALUE) {
            pendingChips -= chipsRequired;
            level++;
            levelsGained++;
            chipsRequired = calculateChipsRequired(level);
        }

        chips = (int) Math.min(pendingChips, chipsRequired - 1L);
        return levelsGained;
    }

    /** A claimed spade advances progression and adds one spendable spade. */
    public int collectSpade(int progressionAmount) {
        int levelsGained = addProgress(progressionAmount);
        addSpades(1);
        return levelsGained;
    }

    /** Guaranteed tree income: one per clear, plus one every fifth round. */
    public int grantRoundClearSpades(int round) {
        int reward = 1;
        if (round > 0 && round % MILESTONE_ROUND_INTERVAL == 0) reward++;
        addSpades(reward);
        return reward;
    }

    public void addSpades(int amount) {
        if (amount <= 0) return;

        int oldValue = spades;
        spades = (int) Math.min(Integer.MAX_VALUE, (long) spades + amount);
        changes.firePropertyChange(SPADES, oldValue, spades);
    }

    public boolean spendSpades(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Spade cost cannot be negative");
        }
        if (spades < amount) return false;

        int oldValue = spades;
        spades -= amount;
        changes.firePropertyChange(SPADES, oldValue, spades);
        return true;
    }

    public boolean canAffordSpades(int amount) {
        return amount >= 0 && spades >= amount;
    }

    public void restore(int savedLevel, int savedChips) {
        restore(savedLevel, savedChips, savedChips);
    }

    public void restore(
        int savedLevel,
        int savedChips,
        int savedSpades
    ) {
        level = Math.max(1, savedLevel);
        chipsRequired = calculateChipsRequired(level);
        chips = MathUtils.clamp(savedChips, 0, chipsRequired - 1);
        int oldSpades = spades;
        spades = Math.max(0, savedSpades);
        changes.firePropertyChange(SPADES, oldSpades, spades);
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

    public int getSpades() {
        return spades;
    }

    public void addSpadeChangeListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(SPADES, listener);
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
