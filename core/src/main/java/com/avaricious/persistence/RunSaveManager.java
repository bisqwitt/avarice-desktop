package com.avaricious.persistence;

import com.avaricious.game.run.RoundsManager;
import com.avaricious.game.run.RoundStats;
import com.avaricious.components.ItemBag;
import com.avaricious.components.automations.Automations;
import com.avaricious.components.slot.ChestManager;
import com.avaricious.components.slot.Symbol;
import com.avaricious.components.slot.pattern.PatternUnlocks;
import com.avaricious.game.progression.SkillTreeProgress;
import com.avaricious.game.progression.SkillTreeUnlock;
import com.avaricious.game.run.GameSession;
import com.avaricious.utility.CollectibleValues;
import com.avaricious.utility.CriticalHitValues;
import com.avaricious.utility.DoubleHitValues;
import com.avaricious.utility.SymbolValues;
import com.avaricious.utility.VaultManager;
import com.avaricious.items.AbstractItem;
import com.avaricious.items.upgrades.Deck;
import com.avaricious.items.upgrades.Hand;
import com.avaricious.items.upgrades.cards.AbstractCard;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

import java.util.ArrayList;
import java.util.List;

/** Persistent, stable checkpoint for the current single-player run. */
public final class RunSaveManager {

    private static final String PREFERENCES = "avarice-run-save";
    private static final int SAVE_VERSION = 1;
    private static final float AUTOSAVE_INTERVAL = 1f;

    private final GameSession session;
    private final Preferences preferences;
    private float autosaveTimer;
    private boolean trackingRun;

    public RunSaveManager(GameSession session) {
        this.session = session;
        preferences = Gdx.app.getPreferences(PREFERENCES);
    }

    public boolean hasSave() {
        return preferences.getBoolean("valid", false)
            && preferences.getInteger("version", 0) == SAVE_VERSION;
    }

    public void beginNewRun() {
        clear();
        trackingRun = true;
    }

    public boolean restore() {
        if (!hasSave()) return false;

        try {
            session.runManager().continueRun(
                preferences.getInteger("round", 1),
                preferences.getFloat(
                    "secondsRemaining",
                    com.avaricious.components.roundInfoPanel.RoundTimer
                        .ROUND_DURATION_SECONDS
                ),
                RoundsManager.RoundOutcome.valueOf(preferences.getString(
                    "roundOutcome",
                    RoundsManager.RoundOutcome.IN_PROGRESS.name()
                ))
            );
            session.roundStats().restore(
                preferences.getInteger("stats.symbolsHit", 0),
                preferences.getInteger("stats.spins", 0),
                preferences.getFloat("stats.moneyGained", 0f),
                preferences.getInteger("stats.collectiblesClaimed", 0),
                preferences.getInteger("stats.symbolsCollected", 0),
                preferences.getFloat("stats.totalClaimTime", 0f)
            );
            session.cash().set(preferences.getFloat("cash", 0f));
            session.chips().restore(
                preferences.getInteger("chipLevel", 1),
                preferences.getInteger("chips", 0)
            );
            VaultManager.I().restore(
                preferences.getFloat("vaultPrincipal", 0f),
                preferences.getInteger("vaultTerm", 0),
                preferences.getInteger("vaultMaturityRound", 0)
            );

            restoreSymbolValues();
            restoreProgression();
            restoreCardsAndItems();

            trackingRun = true;
            autosaveTimer = 0f;
            return true;
        } catch (RuntimeException exception) {
            Gdx.app.error("RUN SAVE", "Could not restore run", exception);
            clear();
            return false;
        }
    }

    public void update(float delta) {
        if (!trackingRun) return;

        autosaveTimer += Math.max(0f, delta);
        if (autosaveTimer >= AUTOSAVE_INTERVAL) {
            saveNow();
            autosaveTimer = 0f;
        }
    }

    public void saveNow() {
        if (!trackingRun) return;

        RoundsManager rounds = session.runManager().getRoundsManager();
        preferences.putInteger("version", SAVE_VERSION);
        preferences.putBoolean("valid", true);
        preferences.putFloat("cash", session.cash().get());
        preferences.putInteger("round", rounds.getCurrentRound());
        preferences.putString("roundOutcome", rounds.getOutcome().name());
        preferences.putFloat(
            "secondsRemaining",
            rounds.getRoundTimer().getPreciseSecondsRemaining()
        );
        preferences.putInteger("chipLevel", session.chips().getLevel());
        preferences.putInteger("chips", session.chips().getChips());
        RoundStats stats = session.roundStats();
        preferences.putInteger("stats.symbolsHit", stats.getSymbolsHit());
        preferences.putInteger("stats.spins", stats.getSpins());
        preferences.putFloat("stats.moneyGained", stats.getMoneyGained());
        preferences.putInteger(
            "stats.collectiblesClaimed",
            stats.getCollectiblesClaimed()
        );
        preferences.putInteger(
            "stats.symbolsCollected",
            stats.getSymbolsCollected()
        );
        preferences.putFloat(
            "stats.totalClaimTime",
            stats.getTotalCollectibleClaimTime()
        );
        preferences.putFloat(
            "vaultPrincipal",
            VaultManager.I().getPrincipal()
        );
        preferences.putInteger("vaultTerm", VaultManager.I().getTermRounds());
        preferences.putInteger(
            "vaultMaturityRound",
            VaultManager.I().getMaturityRound()
        );

        saveSymbolValues();
        saveProgression();
        saveCardsAndItems();

        preferences.flush();
    }

    public void clear() {
        trackingRun = false;
        autosaveTimer = 0f;
        preferences.clear();
        preferences.flush();
    }

    private void saveSymbolValues() {
        for (Symbol symbol : Symbol.values()) {
            preferences.putFloat(
                "symbol." + symbol.name(),
                SymbolValues.I().getValue(symbol)
            );
        }
    }

    private void restoreSymbolValues() {
        for (Symbol symbol : Symbol.values()) {
            float target = preferences.getFloat(
                "symbol." + symbol.name(),
                SymbolValues.I().getValue(symbol)
            );
            while (SymbolValues.I().getValue(symbol) + 0.001f < target) {
                SymbolValues.I().increaseValue(symbol);
            }
        }
    }

    private void saveProgression() {
        Automations automations = Automations.I();
        preferences.putBoolean(
            "autoSpin",
            automations.getAutoSpin().isActive()
        );
        preferences.putBoolean(
            "fullAutoSpin",
            automations.getFullAutoSpin().isActive()
        );
        preferences.putBoolean(
            "spinBuyer",
            automations.getSpinBuyer().isActive()
        );
        preferences.putBoolean(
            "quickSpin",
            automations.getQuickSpin().isActive()
        );
        preferences.putInteger(
            "autoSpinCapacity",
            automations.getAutoSpinCapacity().getCapacity()
        );
        preferences.putInteger(
            "slotMachineSpeedTier",
            automations.getSlotMachineSpeed().getTier()
        );
        preferences.putInteger("luck", automations.getLuck().getBonusPercent());
        preferences.putInteger(
            "xpMultiplier",
            automations.getXpMultiplier().getMultiplier()
        );
        preferences.putInteger(
            "collectorCount",
            automations.getCollectorCapacity().getCount()
        );
        preferences.putInteger(
            "handCapacity",
            automations.getHandCapacity().getCapacity()
        );
        preferences.putInteger(
            "patterns",
            PatternUnlocks.I().getUnlockedCount()
        );
        SkillTreeProgress skillTree = session.skillTree();
        for (SkillTreeUnlock unlock : SkillTreeUnlock.values()) {
            preferences.putBoolean(
                "skillTree." + unlock.name(),
                skillTree.isUnlocked(unlock)
            );
        }
        preferences.putInteger(
            "skillTree.timeGainSeconds",
            skillTree.getTimeGainSeconds()
        );
        preferences.putInteger(
            "extraCollectibleChance",
            CollectibleValues.I().getExtraCollectibleSpawnChance()
        );
        preferences.putInteger(
            "extraSpadeChance",
            CollectibleValues.I().getExtraSpadeSpawnChance()
        );
        preferences.putInteger(
            "cashChipChance",
            CollectibleValues.I().getCashChipSpawnChance()
        );
        preferences.putInteger(
            "criticalChance",
            CriticalHitValues.I().getCriticalHitChance()
        );
        preferences.putInteger(
            "criticalDamage",
            CriticalHitValues.I().getCriticalDamagePercent()
        );
        preferences.putInteger(
            "doubleHitChance",
            DoubleHitValues.I().getDoubleHitChance()
        );
        preferences.putInteger(
            "chestDropChance",
            ChestManager.I().getDropChancePercent()
        );
    }

    private void restoreProgression() {
        Automations automations = Automations.I();
        restoreSkillTreeProgress();

        while (automations.getAutoSpinCapacity().getCapacity()
            < preferences.getInteger("autoSpinCapacity", 3)) {
            automations.getAutoSpinCapacity().upgrade();
        }
        while (automations.getSlotMachineSpeed().getTier()
            < preferences.getInteger("slotMachineSpeedTier", 0)) {
            automations.getSlotMachineSpeed().upgrade();
        }
        while (automations.getLuck().getBonusPercent()
            < preferences.getInteger("luck", 0)) {
            automations.getLuck().upgrade();
        }
        while (automations.getXpMultiplier().getMultiplier()
            < preferences.getInteger("xpMultiplier", 1)) {
            automations.getXpMultiplier().upgrade();
        }
        while (automations.getCollectorCapacity().getCount()
            < preferences.getInteger("collectorCount", 0)) {
            automations.getCollectorCapacity().upgrade();
        }
        while (automations.getHandCapacity().getCapacity()
            < preferences.getInteger("handCapacity", 1)) {
            automations.getHandCapacity().upgrade();
        }

        restorePatterns(preferences.getInteger("patterns", 0));
        restoreStatUpgrades(automations);

        if (preferences.getBoolean("spinBuyer", false)) {
            automations.getSpinBuyer().activate();
        }
        if (preferences.getBoolean("quickSpin", false)) {
            automations.getQuickSpin().activate();
        }
        if (preferences.getBoolean("autoSpin", false)) {
            automations.getAutoSpin().activate();
        }
        if (preferences.getBoolean("fullAutoSpin", false)) {
            automations.getFullAutoSpin().activate();
        }
    }

    private void restoreSkillTreeProgress() {
        SkillTreeProgress skillTree = session.skillTree();
        skillTree.reset();
        for (SkillTreeUnlock unlock : SkillTreeUnlock.values()) {
            if (preferences.getBoolean(
                "skillTree." + unlock.name(),
                legacyUnlockDefault(unlock)
            )) {
                skillTree.unlock(unlock);
            }
        }
        skillTree.restoreTimeGainSeconds(preferences.getInteger(
            "skillTree.timeGainSeconds",
            SkillTreeProgress.BASE_TIME_GAIN_SECONDS
        ));
    }

    private boolean legacyUnlockDefault(SkillTreeUnlock unlock) {
        switch (unlock) {
            case CASH_CHIP_DROP:
                return preferences.getInteger("cashChipChance", 0) > 0;
            case CHEST_DROP:
                return preferences.getInteger("chestDropChance", 0) > 0;
            case CRITICAL_HIT:
                return preferences.getInteger("criticalChance", 0) > 0;
            case DOUBLE_TRIGGER:
                return preferences.getInteger("doubleHitChance", 0) > 0;
            case EXTRA_COLLECTIBLE:
                return preferences.getInteger("extraCollectibleChance", 0) > 0;
            case TIME_GAIN:
                return false;
            default:
                throw new IllegalArgumentException("Unknown unlock: " + unlock);
        }
    }

    private void restorePatterns(int targetCount) {
        while (PatternUnlocks.I().getUnlockedCount() < targetCount) {
            Automations.I().getPatternUnlock().upgrade();
        }
    }

    private void restoreStatUpgrades(Automations automations) {
        while (CollectibleValues.I().getExtraCollectibleSpawnChance()
            < preferences.getInteger("extraCollectibleChance", 0)) {
            automations.getExtraCollectibleChance().upgrade();
        }
        while (CollectibleValues.I().getExtraSpadeSpawnChance()
            < preferences.getInteger("extraSpadeChance", 0)) {
            automations.getExtraSpadeChance().upgrade();
        }
        while (CollectibleValues.I().getCashChipSpawnChance()
            < preferences.getInteger("cashChipChance", 0)) {
            automations.getCashChipChance().upgrade();
        }
        while (CriticalHitValues.I().getCriticalHitChance()
            < preferences.getInteger("criticalChance", 0)) {
            automations.getCriticalHitChance().upgrade();
        }
        while (CriticalHitValues.I().getCriticalDamagePercent()
            < preferences.getInteger(
                "criticalDamage",
                CriticalHitValues.BASE_CRITICAL_DAMAGE_PERCENT
            )) {
            automations.getCriticalDamage().upgrade();
        }
        while (DoubleHitValues.I().getDoubleHitChance()
            < preferences.getInteger("doubleHitChance", 0)) {
            automations.getDoubleHitChance().upgrade();
        }
        while (ChestManager.I().getDropChancePercent()
            < preferences.getInteger("chestDropChance", 0)) {
            automations.getChestDropChance().upgrade();
        }
    }

    private void saveCardsAndItems() {
        preferences.putString("deck", classNames(Deck.I().getDeck()));
        preferences.putString("hand", classNames(Hand.I().getHand()));
        preferences.putString("items", classNames(ItemBag.I().getItems()));
    }

    private void restoreCardsAndItems() {
        List<AbstractCard> deck = instantiate(
            preferences.getString("deck", ""),
            AbstractCard.class
        );
        List<AbstractCard> hand = instantiate(
            preferences.getString("hand", ""),
            AbstractCard.class
        );
        List<AbstractItem> items = instantiate(
            preferences.getString("items", ""),
            AbstractItem.class
        );
        Deck.I().setDeck(deck);
        Hand.I().setCards(hand);
        ItemBag.I().setItems(items);
    }

    private String classNames(List<?> values) {
        StringBuilder result = new StringBuilder();
        for (Object value : values) {
            if (result.length() > 0) result.append('\n');
            result.append(value.getClass().getName());
        }
        return result.toString();
    }

    private <T> List<T> instantiate(String encoded, Class<T> expectedType) {
        List<T> values = new ArrayList<>();
        if (encoded == null || encoded.isEmpty()) return values;

        for (String className : encoded.split("\\n")) {
            if (className.isEmpty()) continue;
            values.add(AbstractItem.instantiateItem(className, expectedType));
        }
        return values;
    }
}
