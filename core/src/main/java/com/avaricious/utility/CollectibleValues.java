package com.avaricious.utility;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CollectibleValues {

    public static final String EXTRA_SPADE_SPAWN_CHANCE =
        "extraSpadeSpawnChance";
    public static final int EXTRA_SPADE_CHANCE_STEP = 10;
    public static final int MAX_EXTRA_SPADE_SPAWN_CHANCE = 100;

    private static CollectibleValues instance;

    public static CollectibleValues I() {
        return instance == null ? instance = new CollectibleValues() : instance;
    }

    private int extraSpadeSpawnChance = 0;

    private final PropertyChangeSupport extraSpadeSpawnChanceChangeSupport =
        new PropertyChangeSupport(this);

    private CollectibleValues() {
    }

    public int getExtraSpadeSpawnChance() {
        return extraSpadeSpawnChance;
    }

    public void increaseExtraSpadeSpawnChance() {
        int oldChance = extraSpadeSpawnChance;
        extraSpadeSpawnChance = Math.min(
            MAX_EXTRA_SPADE_SPAWN_CHANCE,
            oldChance + EXTRA_SPADE_CHANCE_STEP
        );
        extraSpadeSpawnChanceChangeSupport.firePropertyChange(
            EXTRA_SPADE_SPAWN_CHANCE,
            oldChance,
            extraSpadeSpawnChance
        );
    }

    public void addExtraSpadeSpawnChanceChangeListener(
        PropertyChangeListener listener
    ) {
        extraSpadeSpawnChanceChangeSupport.addPropertyChangeListener(listener);
    }
}
