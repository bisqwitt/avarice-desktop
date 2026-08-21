package com.avaricious.components.texts;

import com.avaricious.utility.CriticalHitValues;

public class CriticalDamageDescription extends ExtraCollectibleChanceDescription {

    public CriticalDamageDescription() {
        super(
            () -> CriticalHitValues.I().getCriticalDamagePercent(),
            () -> CriticalHitValues.I().getNextCriticalDamagePercent(),
            CriticalHitValues.I()::addCriticalDamageChangeListener,
            CriticalHitValues.CRITICAL_DAMAGE
        );
    }
}
