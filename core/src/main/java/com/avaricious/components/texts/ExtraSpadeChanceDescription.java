package com.avaricious.components.texts;

import com.avaricious.utility.CollectibleValues;

public class ExtraSpadeChanceDescription extends ExtraCollectibleChanceDescription {

    public ExtraSpadeChanceDescription() {
        this(CollectibleValues.EXTRA_SPADE_CHANCE_STEP);
    }

    public ExtraSpadeChanceDescription(int increaseAmount) {
        super(
            () -> CollectibleValues.I().getExtraSpadeSpawnChance(),
            () -> CollectibleValues.I()
                .getNextExtraSpadeSpawnChance(increaseAmount),
            CollectibleValues.I()::addExtraSpadeSpawnChanceChangeListener,
            CollectibleValues.EXTRA_SPADE_SPAWN_CHANCE
        );
    }
}
