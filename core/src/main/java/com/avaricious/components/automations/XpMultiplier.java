package com.avaricious.components.automations;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/** Permanently increases the XP granted when a spade reaches the XP bar. */
public class XpMultiplier extends AbstractAutomationUpgrade {

    public static final String MULTIPLIER = "xpMultiplier";
    private static final int MAX_MULTIPLIER = 10;
    private static final float[] UPGRADE_PRICES = {
        25_000f,
        100_000f,
        400_000f,
        1_600_000f,
        8_000_000f,
        40_000_000f,
        250_000_000f,
        2_000_000_000f,
        25_000_000_000f
    };

    private final PropertyChangeSupport changeSupport =
        new PropertyChangeSupport(this);
    private int multiplier = 1;

    public XpMultiplier() {
        super(UPGRADE_PRICES[0]);
        activate();
    }

    @Override
    protected float nextPrice(float currentPrice) {
        int nextPriceIndex = Math.min(
            multiplier - 1,
            UPGRADE_PRICES.length - 1
        );
        return UPGRADE_PRICES[nextPriceIndex];
    }

    @Override
    void onUpgrade() {
        if (isMaxed()) return;

        int oldMultiplier = multiplier;
        multiplier++;
        changeSupport.firePropertyChange(
            MULTIPLIER,
            oldMultiplier,
            multiplier
        );
    }

    @Override
    boolean isMaxed() {
        return multiplier >= MAX_MULTIPLIER;
    }

    public int getMultiplier() {
        return multiplier;
    }

    public int getNextMultiplier() {
        return Math.min(MAX_MULTIPLIER, multiplier + 1);
    }

    public void addMultiplierChangeListener(PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(listener);
    }
}
