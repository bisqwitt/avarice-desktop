package com.avaricious.components.texts;

import com.avaricious.utility.CriticalHitValues;

public class CriticalDamageDescription extends ExtraCollectibleChanceDescription {

    public CriticalDamageDescription() {
        this(CriticalHitValues.CRITICAL_DAMAGE_STEP);
    }

    public CriticalDamageDescription(int increaseAmount) {
        super(
            () -> CriticalHitValues.I().getCriticalDamagePercent(),
            () -> CriticalHitValues.I()
                .getNextCriticalDamagePercent(increaseAmount),
            CriticalHitValues.I()::addCriticalDamageChangeListener,
            CriticalHitValues.CRITICAL_DAMAGE
        );
    }
}
