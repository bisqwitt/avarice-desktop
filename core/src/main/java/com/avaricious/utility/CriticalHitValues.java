package com.avaricious.utility;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CriticalHitValues {

    public static final String CRITICAL_HIT_CHANCE = "criticalHitChance";
    public static final String CRITICAL_DAMAGE = "criticalDamage";
    public static final int CRITICAL_HIT_CHANCE_STEP = 10;
    public static final int MAX_CRITICAL_HIT_CHANCE = 100;
    public static final int BASE_CRITICAL_DAMAGE_PERCENT = 150;
    public static final int CRITICAL_DAMAGE_STEP = 50;
    public static final int MAX_CRITICAL_DAMAGE_PERCENT = 300;

    private static CriticalHitValues instance;

    public static CriticalHitValues I() {
        return instance == null ? instance = new CriticalHitValues() : instance;
    }

    private final PropertyChangeSupport changeSupport =
        new PropertyChangeSupport(this);

    private int criticalHitChance = 0;
    private int criticalDamagePercent = BASE_CRITICAL_DAMAGE_PERCENT;

    private CriticalHitValues() {
    }

    public int getCriticalHitChance() {
        return criticalHitChance;
    }

    public void increaseCriticalHitChance() {
        int oldChance = criticalHitChance;
        criticalHitChance = Math.min(
            MAX_CRITICAL_HIT_CHANCE,
            criticalHitChance + CRITICAL_HIT_CHANCE_STEP
        );
        changeSupport.firePropertyChange(
            CRITICAL_HIT_CHANCE,
            oldChance,
            criticalHitChance
        );
    }

    public boolean rollCriticalHit() {
        return criticalHitChance >= 100 ||
            criticalHitChance > 0 &&
                SeededRandomizer.get().nextFloat() * 100f < criticalHitChance;
    }

    public int getCriticalDamagePercent() {
        return criticalDamagePercent;
    }

    public int getNextCriticalDamagePercent() {
        return Math.min(
            MAX_CRITICAL_DAMAGE_PERCENT,
            criticalDamagePercent + CRITICAL_DAMAGE_STEP
        );
    }

    public void increaseCriticalDamage() {
        int oldDamage = criticalDamagePercent;
        criticalDamagePercent = getNextCriticalDamagePercent();
        changeSupport.firePropertyChange(
            CRITICAL_DAMAGE,
            oldDamage,
            criticalDamagePercent
        );
    }

    public int applyCriticalDamage(int basePoints) {
        return Math.round(basePoints * criticalDamagePercent / 100f);
    }

    public boolean isCriticalDamageMaxed() {
        return criticalDamagePercent >= MAX_CRITICAL_DAMAGE_PERCENT;
    }

    public void addCriticalHitChanceChangeListener(
        PropertyChangeListener listener
    ) {
        changeSupport.addPropertyChangeListener(listener);
    }

    public void addCriticalDamageChangeListener(
        PropertyChangeListener listener
    ) {
        changeSupport.addPropertyChangeListener(listener);
    }
}
