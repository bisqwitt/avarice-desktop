package com.avaricious.components.texts;

import com.avaricious.utility.CollectibleValues;

public class CashChipChanceDescription extends ExtraCollectibleChanceDescription {

    public CashChipChanceDescription() {
        this(CollectibleValues.CASH_CHIP_CHANCE_STEP);
    }

    public CashChipChanceDescription(int increaseAmount) {
        super(
            CollectibleValues.I()::getCashChipSpawnChance,
            () -> CollectibleValues.I()
                .getNextCashChipSpawnChance(increaseAmount),
            CollectibleValues.I()::addCashChipSpawnChanceChangeListener,
            CollectibleValues.CASH_CHIP_SPAWN_CHANCE
        );
    }
}
