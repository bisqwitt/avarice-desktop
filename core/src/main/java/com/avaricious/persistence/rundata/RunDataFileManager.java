package com.avaricious.persistence.rundata;

import com.avaricious.utility.Seq;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.ArrayList;

public class RunDataFileManager {

    private final FileHandle file;
    private final Json json = new Json();

    private boolean isDirty = false;
    private float saveTimer = 0f;

    private ArrayList<RunData> runs = new ArrayList<>();

    public RunDataFileManager() {
        file = Gdx.files.local("runs.json");
        json.setOutputType(JsonWriter.OutputType.json);

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

    public void onScoreChange(
        String runId,
        int round,
        float newScore,
        long msSinceRoundStart
    ) {
        RunData currentRun = getCurrentRun(runId);

        if (currentRun == null) {
            currentRun = new RunData().setRunId(runId);
            runs.add(currentRun);
        }

        currentRun.scoreChangeData.add(new ScoreChangeData(round, newScore, msSinceRoundStart));
        isDirty = true;
    }

    public RunData findOpponentsRun(String currentRunId) {
        RunData opponentsRun = Seq.of(runs)
            .filter(run -> !run.runId.equals(currentRunId))
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

    private RunData getCurrentRun(String runId) {
        return Seq.of(runs)
            .filter(runData -> runData.runId.equals(runId))
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

}
