package com.avaricious.network.matchmaking;

import com.avaricious.network.SocketClient;
import com.avaricious.network.SocketEvents;
import com.avaricious.app.navigation.ScreenManager;
import com.badlogic.gdx.Gdx;

public class MatchmakingController {

    private final SocketClient socketClient;
    private final MatchmakingService service;

    public MatchmakingController(
        SocketClient socketClient,
        ScreenManager screens
    ) {
        this.socketClient = socketClient;
        this.service = new MatchmakingService(screens);
    }

    public void registerListeners() {
        socketClient.on(SocketEvents.QUEUE_JOINED, args -> {
            service.onQueueJoined();
        });

        socketClient.onJson(SocketEvents.MATCH_FOUND, data -> {
            String roomId = data.getString("roomId");
            String playerId = data.getString("playerId");
            String opponentId = data.getString("opponentId");

            service.onMatchFound(roomId, playerId, opponentId);
        });

        socketClient.onJson(SocketEvents.OPPONENT_LEFT, data -> {
            String roomId = data.getString("roomId");
            service.onOpponentLeft(roomId);
        });
    }

    public void joinQueue() {
        socketClient.emit(SocketEvents.JOIN_QUEUE);
        Gdx.app.log("SOCKET", "Joining queue...");
    }

}
