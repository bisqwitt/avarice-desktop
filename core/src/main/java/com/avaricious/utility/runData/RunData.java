package com.avaricious.utility.runData;

import java.util.ArrayList;
import java.util.List;

public class RunData {

    public String runId;
    public List<ScoreChangeData> scoreChangeData = new ArrayList<>();

    public RunData setRunId(String runId) {
        this.runId = runId;
        return this;
    }

    public static RunData defaultRun() {
        RunData defaultRun = new RunData();
        defaultRun.runId = "DEFAULT";
        for (int i = 0; i < 10; i++) {
            defaultRun.scoreChangeData.add(new ScoreChangeData(i, i * 700, 5000));
        }

        return defaultRun;
    }

}
