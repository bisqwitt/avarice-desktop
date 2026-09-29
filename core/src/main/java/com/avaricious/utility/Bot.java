package com.avaricious.utility;

import com.avaricious.game.run.RoundsManager;

public class Bot {

    public static int health = 1000;

    public static int getRoundEndScore(RoundsManager rounds) {
        return SeededRandomizer.nextInt(100, 300)
            * rounds.getCurrentRound();
    }

}
