package com.avaricious.utility;

public class Bot {

    public static int health = 1000;

    public static int getRoundEndScore() {
        return SeededRandomizer.nextInt(100, 300) * RunManager.I().getRoundsManager().getCurrentRound();
    }

}
