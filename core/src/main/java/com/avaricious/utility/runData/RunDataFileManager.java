package com.avaricious.utility.runData;

import com.avaricious.utility.Listener;
import com.avaricious.utility.RunManager;
import com.avaricious.utility.Seq;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.ArrayList;

public class RunDataFileManager {

    private static RunDataFileManager instance;

    public static RunDataFileManager I() {
        return instance == null ? instance = new RunDataFileManager() : instance;
    }

    private final FileHandle file;
    private final Json json = new Json();

    private boolean isDirty = false;
    private float saveTimer = 0f;

    private ArrayList<RunData> runs = new ArrayList<>();

    private RunDataFileManager() {
        file = Gdx.files.local("runs.json");
        json.setOutputType(JsonWriter.OutputType.json);

//        observeValue(ScoreDisplay.I()::onChange, scoreState ->
//            onScoreChange(
//                RunManager.I().getRoundsManager().getCurrentRound(),
//                scoreState.sum,
//                RunManager.I().getRoundsManager().getRoundTimer().msSinceRoundStart()));
        loadRuns();
    }

    public void update(float delta) {
        if (!isDirty) return;

        saveTimer += delta;

        if (saveTimer >= 1f) {
            saveRunData();
            isDirty = false;
            saveTimer = 0f;
        }
    }

    public void onScoreChange(int round, float newScore, long msSinceRoundStart) {
        RunData currentRun = getCurrentRun();

        if (currentRun == null) {
            currentRun = new RunData().setRunId(RunManager.I().getRunId());
            runs.add(currentRun);
        }

        currentRun.scoreChangeData.add(new ScoreChangeData(round, newScore, msSinceRoundStart));
        isDirty = true;
    }

    public RunData findOpponentsRun() {
        RunData opponentsRun = Seq.of(runs)
            .filter(run -> !run.runId.equals(RunManager.I().getRunId()))
            .findAnyOrNull();

        return opponentsRun == null ? RunData.defaultRun() : opponentsRun;
    }

    private void loadRuns() {
        if (!file.exists() || file.length() == 0) {
            saveRunData();
            return;
        }

        runs = json.fromJson(ArrayList.class, RunData.class, file.readString());
        clearUnfinishedRuns();
    }

    private RunData getCurrentRun() {
        return Seq.of(runs)
            .filter(runData -> runData.runId.equals(RunManager.I().getRunId()))
            .findAnyOrNull();
    }

    private void saveRunData() {
        file.writeString(json.toJson(runs), false);
    }

    private void clearUnfinishedRuns() {
        runs = Seq.of(runs).filter(run -> {
            int highestRound = Seq.of(run.scoreChangeData)
                .mapToInt(scoreChange -> scoreChange.round)
                .maxOrDefault(0);

            return highestRound > 15;
        }).toList();

        isDirty = true;
    }

    private <T> void observeValue(
        Listener<Listener<T>> register,
        Listener<T> setter
    ) {
        register.accept(value -> {
            setter.accept(value);
            isDirty = true;
        });
    }

}
