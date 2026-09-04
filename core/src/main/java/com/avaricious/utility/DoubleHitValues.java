package com.avaricious.utility;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Run-wide chance for a resolved symbol hit to resolve one extra time. */
public final class DoubleHitValues {

    public static final String DOUBLE_HIT_CHANCE = "doubleHitChance";
    public static final int DOUBLE_HIT_CHANCE_STEP = 10;
    public static final int MAX_DOUBLE_HIT_CHANCE = 100;

    private static DoubleHitValues instance;

    public static DoubleHitValues I() {
        return instance == null ? instance = new DoubleHitValues() : instance;
    }

    private final PropertyChangeSupport changeSupport =
        new PropertyChangeSupport(this);
    private int doubleHitChance;

    private DoubleHitValues() {
    }

    public int getDoubleHitChance() {
        return doubleHitChance;
    }

    public int getNextDoubleHitChance() {
        return getNextDoubleHitChance(DOUBLE_HIT_CHANCE_STEP);
    }

    public int getNextDoubleHitChance(int amount) {
        return Math.min(
            MAX_DOUBLE_HIT_CHANCE,
            doubleHitChance + Math.max(0, amount)
        );
    }

    public void increaseDoubleHitChance() {
        increaseDoubleHitChance(DOUBLE_HIT_CHANCE_STEP);
    }

    public void increaseDoubleHitChance(int amount) {
        int oldChance = doubleHitChance;
        doubleHitChance = getNextDoubleHitChance(amount);
        changeSupport.firePropertyChange(
            DOUBLE_HIT_CHANCE,
            oldChance,
            doubleHitChance
        );
    }

    public boolean rollDoubleHit() {
        return doubleHitChance >= 100 ||
            doubleHitChance > 0 &&
                SeededRandomizer.get().nextFloat() * 100f < doubleHitChance;
    }

    public void addDoubleHitChanceChangeListener(
        PropertyChangeListener listener
    ) {
        changeSupport.addPropertyChangeListener(listener);
    }
}
