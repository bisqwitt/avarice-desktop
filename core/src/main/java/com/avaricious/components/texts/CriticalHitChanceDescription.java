package com.avaricious.components.texts;

import com.avaricious.utility.CriticalHitValues;

public class CriticalHitChanceDescription extends ExtraCollectibleChanceDescription {

    public CriticalHitChanceDescription() {
        super(
            () -> CriticalHitValues.I().getCriticalHitChance(),
            CriticalHitValues.I()::addCriticalHitChanceChangeListener,
            CriticalHitValues.CRITICAL_HIT_CHANCE
        );
    }
}
