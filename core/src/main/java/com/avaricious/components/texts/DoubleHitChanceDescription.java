package com.avaricious.components.texts;

import com.avaricious.utility.DoubleHitValues;

/** Displays Double Hit chance as current percentage -> next percentage. */
public final class DoubleHitChanceDescription
    extends ExtraCollectibleChanceDescription {

    public DoubleHitChanceDescription() {
        this(DoubleHitValues.DOUBLE_HIT_CHANCE_STEP);
    }

    public DoubleHitChanceDescription(int increaseAmount) {
        super(
            DoubleHitValues.I()::getDoubleHitChance,
            () -> DoubleHitValues.I().getNextDoubleHitChance(increaseAmount),
            DoubleHitValues.I()::addDoubleHitChanceChangeListener,
            DoubleHitValues.DOUBLE_HIT_CHANCE
        );
    }
}
