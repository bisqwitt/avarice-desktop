package com.avaricious.game.run;

import com.avaricious.persistence.rundata.RunData;
import com.avaricious.persistence.rundata.RunDataFileManager;

import java.util.UUID;

public class RunManager {

    private final RoundsManager roundsManager;
    private final RunDataFileManager runDataFiles;

    private String runId;

    private RunData opponentsRun;

    public RunManager(
        RoundStats roundStats,
        RunDataFileManager runDataFiles
    ) {
        roundsManager = new RoundsManager(roundStats);
        this.runDataFiles = runDataFiles;
    }

    public void newRun() {
        runId = UUID.randomUUID().toString();
        opponentsRun = runDataFiles.findOpponentsRun(runId);

        roundsManager.startNewRun();
    }

    public void continueRun(int round, float secondsRemaining) {
        continueRun(
            round,
            secondsRemaining,
            RoundsManager.RoundOutcome.IN_PROGRESS
        );
    }

    public void continueRun(
        int round,
        float secondsRemaining,
        RoundsManager.RoundOutcome outcome
    ) {
        runId = UUID.randomUUID().toString();
        opponentsRun = runDataFiles.findOpponentsRun(runId);
        roundsManager.restoreRun(round, secondsRemaining, outcome);
    }

    public RoundsManager getRoundsManager() {
        return roundsManager;
    }

    public String getRunId() {
        return runId;
    }

    public RunData getOpponentsRun() {
        return opponentsRun;
    }
}
