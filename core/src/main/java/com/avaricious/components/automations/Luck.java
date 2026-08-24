package com.avaricious.components.automations;

import com.avaricious.components.slot.SlotMachine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Luck extends AbstractAutomationUpgrade {

    public static final int BONUS_PER_UPGRADE = 5;
    private static final int MAX_BONUS_PERCENT = 35;
    private static final float[] UPGRADE_PRICES = {
        10_000f,
        30_000f,
        100_000f,
        350_000f,
        1_500_000f,
        10_000_000f,
        100_000_000f
    };

    private final PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);

    private int bonusPercent = 0;

    public Luck() {
        super(UPGRADE_PRICES[0]);
        activate();
    }

    @Override
    protected float nextPrice(float currentPrice) {
        int nextTier = bonusPercent / BONUS_PER_UPGRADE;
        return UPGRADE_PRICES[Math.min(nextTier, UPGRADE_PRICES.length - 1)];
    }

    @Override
    void onUpgrade() {
        int oldBonus = bonusPercent;
        bonusPercent = Math.min(
            MAX_BONUS_PERCENT,
            bonusPercent + BONUS_PER_UPGRADE
        );

        SlotMachine.I().setLuckBonus(bonusPercent / 100f);
        propertyChangeSupport.firePropertyChange(
            "bonusPercent",
            oldBonus,
            bonusPercent
        );
    }

    @Override
    boolean isMaxed() {
        return bonusPercent >= MAX_BONUS_PERCENT;
    }

    public int getBonusPercent() {
        return bonusPercent;
    }

    public int getNextBonusPercent() {
        return Math.min(
            MAX_BONUS_PERCENT,
            bonusPercent + BONUS_PER_UPGRADE
        );
    }

    public boolean isMaxBonusReached() {
        return isMaxed();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }
}
