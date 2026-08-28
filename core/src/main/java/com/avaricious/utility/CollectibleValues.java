package com.avaricious.utility;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

public class CollectibleValues {

    public static final String EXTRA_COLLECTIBLE_SPAWN_CHANCE =
        "extraCollectibleSpawnChance";
    public static final int EXTRA_COLLECTIBLE_CHANCE_STEP = 10;
    public static final int MAX_EXTRA_COLLECTIBLE_SPAWN_CHANCE = 100;
    public static final String EXTRA_SPADE_SPAWN_CHANCE =
        "extraSpadeSpawnChance";
    public static final int EXTRA_SPADE_CHANCE_STEP = 10;
    public static final int MAX_EXTRA_SPADE_SPAWN_CHANCE = 100;
    public static final String CASH_CHIP_SPAWN_CHANCE =
        "cashChipSpawnChance";
    public static final int CASH_CHIP_CHANCE_STEP = 10;
    public static final int MAX_CASH_CHIP_SPAWN_CHANCE = 50;

    private static CollectibleValues instance;

    public static CollectibleValues I() {
        return instance == null ? instance = new CollectibleValues() : instance;
    }

    private int extraCollectibleSpawnChance = 0;
    private int extraSpadeSpawnChance = 0;
    private int cashChipSpawnChance = 0;

    private final PropertyChangeSupport extraCollectibleSpawnChanceChangeSupport =
        new PropertyChangeSupport(this);
    private final PropertyChangeSupport extraSpadeSpawnChanceChangeSupport =
        new PropertyChangeSupport(this);
    private final PropertyChangeSupport cashChipSpawnChanceChangeSupport =
        new PropertyChangeSupport(this);

    private CollectibleValues() {
    }

    public int getExtraCollectibleSpawnChance() {
        return extraCollectibleSpawnChance;
    }

    public void increaseExtraCollectibleSpawnChance() {
        increaseExtraCollectibleSpawnChance(EXTRA_COLLECTIBLE_CHANCE_STEP);
    }

    public int getNextExtraCollectibleSpawnChance(int amount) {
        return Math.min(
            MAX_EXTRA_COLLECTIBLE_SPAWN_CHANCE,
            extraCollectibleSpawnChance + amount
        );
    }

    public void increaseExtraCollectibleSpawnChance(int amount) {
        int oldChance = extraCollectibleSpawnChance;
        extraCollectibleSpawnChance =
            getNextExtraCollectibleSpawnChance(amount);
        extraCollectibleSpawnChanceChangeSupport.firePropertyChange(
            EXTRA_COLLECTIBLE_SPAWN_CHANCE,
            oldChance,
            extraCollectibleSpawnChance
        );
    }

    public void addExtraCollectibleSpawnChanceChangeListener(
        PropertyChangeListener listener
    ) {
        extraCollectibleSpawnChanceChangeSupport.addPropertyChangeListener(listener);
    }

    public int getExtraSpadeSpawnChance() {
        return extraSpadeSpawnChance;
    }

    public void increaseExtraSpadeSpawnChance() {
        increaseExtraSpadeSpawnChance(EXTRA_SPADE_CHANCE_STEP);
    }

    public int getNextExtraSpadeSpawnChance(int amount) {
        return Math.min(
            MAX_EXTRA_SPADE_SPAWN_CHANCE,
            extraSpadeSpawnChance + amount
        );
    }

    public void increaseExtraSpadeSpawnChance(int amount) {
        int oldChance = extraSpadeSpawnChance;
        extraSpadeSpawnChance = getNextExtraSpadeSpawnChance(amount);
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

    public int getCashChipSpawnChance() {
        return cashChipSpawnChance;
    }

    public void increaseCashChipSpawnChance() {
        increaseCashChipSpawnChance(CASH_CHIP_CHANCE_STEP);
    }

    public int getNextCashChipSpawnChance(int amount) {
        return Math.min(
            MAX_CASH_CHIP_SPAWN_CHANCE,
            cashChipSpawnChance + amount
        );
    }

    public void increaseCashChipSpawnChance(int amount) {
        int oldChance = cashChipSpawnChance;
        cashChipSpawnChance = getNextCashChipSpawnChance(amount);
        cashChipSpawnChanceChangeSupport.firePropertyChange(
            CASH_CHIP_SPAWN_CHANCE,
            oldChance,
            cashChipSpawnChance
        );
    }

    public void addCashChipSpawnChanceChangeListener(
        PropertyChangeListener listener
    ) {
        cashChipSpawnChanceChangeSupport.addPropertyChangeListener(listener);
    }
}
