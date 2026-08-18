package com.avaricious.network.matchmaking;

import com.avaricious.screens.InQueueScreen;
import com.avaricious.screens.MainScreen;
import com.avaricious.screens.ScreenManager;
import com.avaricious.screens.SlotScreen;
import com.badlogic.gdx.Gdx;

public class MatchmakingService {

    public void onQueueJoined() {
        Gdx.app.postRunnable(() -> {
            Gdx.app.log("MATCHMAKING", "Queue joined");
            ScreenManager.I().setScreen(InQueueScreen.class);
        });
    }

    public void onMatchFound(String roomId, String playerId, String opponentId) {
        Gdx.app.postRunnable(() -> {
            Gdx.app.log("MATCHMAKING", "Start match");
            Gdx.app.log("MATCHMAKING", "Room: " + roomId);
            Gdx.app.log("MATCHMAKING", "Me: " + playerId);
            Gdx.app.log("MATCHMAKING", "Opponent: " + opponentId);

            ScreenManager.I().setScreen(SlotScreen.class);
        });
    }

    public void onOpponentLeft(String roomId) {
        Gdx.app.postRunnable(() -> {
            Gdx.app.log("GAME", "Opponent left match");
            ScreenManager.I().setScreen(MainScreen.class);
        });
    }

}
