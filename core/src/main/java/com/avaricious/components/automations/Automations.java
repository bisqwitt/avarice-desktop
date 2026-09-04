package com.avaricious.components.automations;

import com.avaricious.components.slot.ChestManager;
import com.avaricious.utility.CollectibleValues;
import com.avaricious.utility.CriticalHitValues;
import com.avaricious.utility.DoubleHitValues;

public class Automations {

    private static Automations instance;

    public static Automations I() {
        return instance == null ? instance = new Automations() : instance;
    }

    private final AutoSpinAutomation autoSpin = new AutoSpinAutomation();
    private final AutoSpinCapacity autoSpinCapacity = new AutoSpinCapacity();
    private final FullAutoSpinAutomation fullAutoSpin =
        new FullAutoSpinAutomation();
    private final SpinBuyerAutomation spinBuyer = new SpinBuyerAutomation();
    private final SpinBuyerSpeed spinBuyerSpeed = new SpinBuyerSpeed();

    private final SlotMachineSpeed slotMachineSpeed = new SlotMachineSpeed();
    private final Luck luck = new Luck();
    private final XpMultiplier xpMultiplier = new XpMultiplier();
    private final CollectorCapacity collectorCapacity = new CollectorCapacity();
    private final PatternUnlockUpgrade patternUnlock =
        new PatternUnlockUpgrade();

    private final ShopStatUpgrade extraCollectibleChance =
        new ShopStatUpgrade(
            2_500f,
            3f,
            CollectibleValues.I()::increaseExtraCollectibleSpawnChance,
            () -> CollectibleValues.I().getExtraCollectibleSpawnChance()
                >= CollectibleValues.MAX_EXTRA_COLLECTIBLE_SPAWN_CHANCE
        );
    private final ShopStatUpgrade extraSpadeChance =
        new ShopStatUpgrade(
            5_000f,
            3f,
            CollectibleValues.I()::increaseExtraSpadeSpawnChance,
            () -> CollectibleValues.I().getExtraSpadeSpawnChance()
                >= CollectibleValues.MAX_EXTRA_SPADE_SPAWN_CHANCE
        );
    private final ShopStatUpgrade criticalHitChance =
        new ShopStatUpgrade(
            10_000f,
            3f,
            CriticalHitValues.I()::increaseCriticalHitChance,
            () -> CriticalHitValues.I().getCriticalHitChance()
                >= CriticalHitValues.MAX_CRITICAL_HIT_CHANCE
        );
    private final ShopStatUpgrade criticalDamage =
        new ShopStatUpgrade(
            25_000f,
            3f,
            CriticalHitValues.I()::increaseCriticalDamage,
            CriticalHitValues.I()::isCriticalDamageMaxed,
            () -> CriticalHitValues.I().getCriticalHitChance() > 0
        );
    private final ShopStatUpgrade doubleHitChance =
        new ShopStatUpgrade(
            50_000f,
            3.5f,
            DoubleHitValues.I()::increaseDoubleHitChance,
            () -> DoubleHitValues.I().getDoubleHitChance()
                >= DoubleHitValues.MAX_DOUBLE_HIT_CHANCE
        );
    private final ShopStatUpgrade cashChipChance =
        new ShopStatUpgrade(
            25_000f,
            3.5f,
            CollectibleValues.I()::increaseCashChipSpawnChance,
            () -> CollectibleValues.I().getCashChipSpawnChance()
                >= CollectibleValues.MAX_CASH_CHIP_SPAWN_CHANCE
        );
    private final ShopStatUpgrade chestDropChance =
        new ShopStatUpgrade(
            50_000f,
            3.5f,
            ChestManager.I()::increaseDropChance,
            () -> ChestManager.I().getDropChancePercent()
                >= ChestManager.MAX_DROP_CHANCE_PERCENT
        );

    private final HandCapacity handCapacity = new HandCapacity();

    private Automations() {
    }

    public AutoSpinAutomation getAutoSpin() {
        return autoSpin;
    }

    public AutoSpinAutomation getSpinQueuer() {
        return autoSpin;
    }

    public AutoSpinCapacity getAutoSpinCapacity() {
        return autoSpinCapacity;
    }

    public FullAutoSpinAutomation getFullAutoSpin() {
        return fullAutoSpin;
    }

    public SpinBuyerAutomation getSpinBuyer() {
        return spinBuyer;
    }

    public SpinBuyerSpeed getSpinBuyerSpeed() {
        return spinBuyerSpeed;
    }

    public SlotMachineSpeed getSlotMachineSpeed() {
        return slotMachineSpeed;
    }

    public Luck getLuck() {
        return luck;
    }

    public XpMultiplier getXpMultiplier() {
        return xpMultiplier;
    }

    public CollectorCapacity getCollectorCapacity() {
        return collectorCapacity;
    }

    public PatternUnlockUpgrade getPatternUnlock() {
        return patternUnlock;
    }

    public ShopStatUpgrade getExtraCollectibleChance() {
        return extraCollectibleChance;
    }

    public ShopStatUpgrade getExtraSpadeChance() {
        return extraSpadeChance;
    }

    public ShopStatUpgrade getCriticalHitChance() {
        return criticalHitChance;
    }

    public ShopStatUpgrade getCriticalDamage() {
        return criticalDamage;
    }

    public ShopStatUpgrade getDoubleHitChance() {
        return doubleHitChance;
    }

    public ShopStatUpgrade getCashChipChance() {
        return cashChipChance;
    }

    public ShopStatUpgrade getChestDropChance() {
        return chestDropChance;
    }

    public HandCapacity getHandCapacity() {
        return handCapacity;
    }
}
