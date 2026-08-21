package com.avaricious.components.automations;

import com.avaricious.components.slot.SlotMachine;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class Luck extends AbstractAutomationUpgrade {

    public static final int BONUS_PER_UPGRADE = 10;
    private static final int MAX_BONUS_PERCENT = 30;

    private final PropertyChangeSupport propertyChangeSupport =
        new PropertyChangeSupport(this);

    private int bonusPercent = 0;

    public Luck() {
        super(400);
        activate();
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
