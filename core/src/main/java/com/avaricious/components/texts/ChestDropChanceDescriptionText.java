package com.avaricious.components.texts;

import com.avaricious.components.slot.ChestManager;

/** Displays the chest drop chance as current percentage -> next percentage. */
public final class ChestDropChanceDescriptionText
    extends ExtraCollectibleChanceDescription {

    public ChestDropChanceDescriptionText() {
        this(ChestManager.DROP_CHANCE_STEP);
    }

    public ChestDropChanceDescriptionText(int increaseAmount) {
        super(
            ChestManager.I()::getDropChancePercent,
            () -> ChestManager.I().getNextDropChancePercent(increaseAmount),
            ChestManager.I()::addDropChanceChangeListener,
            ChestManager.DROP_CHANCE
        );
    }
}
