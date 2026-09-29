package com.avaricious.network.match;

import com.avaricious.app.navigation.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.badlogic.gdx.Gdx;

public class MatchService {

    private final ScreenManager screens;

    public MatchService(ScreenManager screens) {
        this.screens = screens;
    }

    public void onRoundEndWaiting() {
        Gdx.app.postRunnable(() -> {
            screens.getScreen(SlotScreen.class).showWaitingForOpponentText();
        });
    }

    public void onBothPlayersEndedRound() {
        Gdx.app.postRunnable(() -> {
            screens.getScreen(SlotScreen.class).onBothPlayersEndedRound();
        });
    }

    public void onOpponentHealthChanged(int newHealth) {
        Gdx.app.postRunnable(() -> {

        });
    }

    public void onOpponentScoreChanged(int newScore) {
        Gdx.app.postRunnable(() -> {
            screens.getScreen(SlotScreen.class).setOpponentScore(newScore);
        });
    }

}
