package com.avaricious.network.match;

import com.avaricious.components.roundInfoPanel.PlayerScores;
import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.badlogic.gdx.Gdx;

public class MatchService {

    public void onRoundEndWaiting() {
        Gdx.app.postRunnable(() -> {
            ScreenManager.I().getScreen(SlotScreen.class).showWaitingForOpponentText();
        });
    }

    public void onBothPlayersEndedRound() {
        Gdx.app.postRunnable(() -> {
            ScreenManager.I().getScreen(SlotScreen.class).onBothPlayersEndedRound();
        });
    }

    public void onOpponentHealthChanged(int newHealth) {
        Gdx.app.postRunnable(() -> {

        });
    }

    public void onOpponentScoreChanged(int newScore) {
        Gdx.app.postRunnable(() -> {
            PlayerScores.I().setEnemyScoreNumber(newScore);
        });
    }

}
