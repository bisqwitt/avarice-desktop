package com.avaricious.game.run;

import com.avaricious.game.progression.SkillTreeProgress;
import com.avaricious.persistence.rundata.RunDataFileManager;

/**
 * State whose lifetime is one playable run. UI components render this state;
 * persistence reads and restores it directly.
 */
public final class GameSession {

    private final CashBalance cash = new CashBalance();
    private final ChipProgress chips = new ChipProgress();
    private final RoundStats roundStats = new RoundStats();
    private final SkillTreeProgress skillTree = new SkillTreeProgress();
    private final RunManager runManager;

    public GameSession(RunDataFileManager runDataFiles) {
        runManager = new RunManager(roundStats, runDataFiles);
    }

    public CashBalance cash() {
        return cash;
    }

    public ChipProgress chips() {
        return chips;
    }

    public RoundStats roundStats() {
        return roundStats;
    }

    public SkillTreeProgress skillTree() {
        return skillTree;
    }

    public RunManager runManager() {
        return runManager;
    }

    public void startNewRun() {
        reset();
        runManager.newRun();
    }

    public void reset() {
        cash.set(0f);
        chips.restore(1, 0);
        roundStats.reset();
        skillTree.reset();
    }
}
