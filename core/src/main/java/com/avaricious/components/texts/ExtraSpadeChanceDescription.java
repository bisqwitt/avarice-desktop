package com.avaricious.components.texts;

import com.avaricious.utility.CollectibleValues;

public class ExtraSpadeChanceDescription extends ExtraCollectibleChanceDescription {

    public ExtraSpadeChanceDescription() {
        super(
            () -> CollectibleValues.I().getExtraSpadeSpawnChance(),
            CollectibleValues.I()::addExtraSpadeSpawnChanceChangeListener,
            CollectibleValues.EXTRA_SPADE_SPAWN_CHANCE
        );
    }
}
