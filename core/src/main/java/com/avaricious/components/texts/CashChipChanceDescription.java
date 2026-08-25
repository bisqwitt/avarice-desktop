package com.avaricious.components.texts;

import com.avaricious.utility.CollectibleValues;

public class CashChipChanceDescription extends ExtraCollectibleChanceDescription {

    public CashChipChanceDescription() {
        super(
            CollectibleValues.I()::getCashChipSpawnChance,
            () -> Math.min(
                CollectibleValues.MAX_CASH_CHIP_SPAWN_CHANCE,
                CollectibleValues.I().getCashChipSpawnChance()
                    + CollectibleValues.CASH_CHIP_CHANCE_STEP
            ),
            CollectibleValues.I()::addCashChipSpawnChanceChangeListener,
            CollectibleValues.CASH_CHIP_SPAWN_CHANCE
        );
    }
}
