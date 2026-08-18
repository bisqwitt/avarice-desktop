package com.avaricious.network.match;

import com.avaricious.network.SocketClient;
import com.avaricious.network.SocketEvents;
import com.badlogic.gdx.Gdx;

public class MatchController {

    private final SocketClient socketClient;
    private final MatchService service = new MatchService();

    public MatchController(SocketClient socketClient) {
        this.socketClient = socketClient;
    }

    public void registerListeners() {
        socketClient.onJson(SocketEvents.OPPONENT_HEALTH_CHANGED, data -> {
            int opponentHealth = data.getInt("health");
            service.onOpponentHealthChanged(opponentHealth);
        });

        socketClient.onJson(SocketEvents.OPPONENT_SCORE_CHANGED, data -> {
            int opponentScore = data.getInt("score");
            service.onOpponentScoreChanged(opponentScore);
        });

        socketClient.on(SocketEvents.ROUND_END_WAITING, args -> {
            service.onRoundEndWaiting();
        });

        socketClient.on(SocketEvents.BOTH_PLAYERS_ENDED_ROUND, args -> {
            service.onBothPlayersEndedRound();
        });
    }

    public void sendRoundEnded() {
        socketClient.emit(SocketEvents.ROUND_END);
    }

    public void onHealthChanged(int newHealth) {
        socketClient.emitJson(SocketEvents.HEALTH_UPDATE, payload -> {
            payload.put("newHealth", newHealth);
        });
    }

    public void onScoreChanged(int round, int score) {
        Gdx.app.log("SCORE_CHANGE", "socketId=" + socketClient.getSocketId());
        socketClient.emitJson(SocketEvents.SCORE_UPDATE, payload -> {
//            payload.put("round", round);
            payload.put("score", score);
//            payload.put("msSinceRoundStart", 0);
        });
    }

}
