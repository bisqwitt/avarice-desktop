package com.avaricious.utility.runData;

public class ScoreChangeData {

    public int round;
    public float newScore;
    public long msSinceRoundStart;

    public ScoreChangeData(int round, float newScore, long msSinceRoundStart) {
        this.round = round;
        this.newScore = newScore;
        this.msSinceRoundStart = msSinceRoundStart;
    }

    public ScoreChangeData() {
    }

}
