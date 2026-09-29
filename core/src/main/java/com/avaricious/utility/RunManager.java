package com.avaricious.utility;

import com.avaricious.RoundsManager;
import com.avaricious.utility.runData.RunData;
import com.avaricious.utility.runData.RunDataFileManager;

import java.util.UUID;

public class RunManager {

    private static RunManager instance;

    public static RunManager I() {
        return instance == null ? instance = new RunManager() : instance;
    }

    private final RoundsManager roundsManager = new RoundsManager();

    private String runId;

    private RunData opponentsRun;

    private RunManager() {
    }

    public void newRun() {
        runId = UUID.randomUUID().toString();
        opponentsRun = RunDataFileManager.I().findOpponentsRun();

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
        opponentsRun = RunDataFileManager.I().findOpponentsRun();
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
