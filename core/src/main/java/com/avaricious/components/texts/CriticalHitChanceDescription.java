package com.avaricious.components.texts;

import com.avaricious.utility.CriticalHitValues;

public class CriticalHitChanceDescription extends ExtraCollectibleChanceDescription {

    public CriticalHitChanceDescription() {
        this(CriticalHitValues.CRITICAL_HIT_CHANCE_STEP);
    }

    public CriticalHitChanceDescription(int increaseAmount) {
        super(
            () -> CriticalHitValues.I().getCriticalHitChance(),
            () -> CriticalHitValues.I()
                .getNextCriticalHitChance(increaseAmount),
            CriticalHitValues.I()::addCriticalHitChanceChangeListener,
            CriticalHitValues.CRITICAL_HIT_CHANCE
        );
    }
}
