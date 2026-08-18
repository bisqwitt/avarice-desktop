package com.avaricious.network;

import com.avaricious.network.match.MatchController;
import com.avaricious.network.matchmaking.MatchmakingController;

public class NetworkController {

    private static NetworkController instance;

    public static NetworkController I() {
        return instance == null ? instance = new NetworkController() : instance;
    }

    private final SocketClient socketClient;
    private final MatchmakingController matchmakingController;
    private final MatchController matchController;

    private NetworkController() {
        socketClient = new SocketClient();
        matchmakingController = new MatchmakingController(socketClient);
        matchController = new MatchController(socketClient);
    }

    public void connect() {
        socketClient.connect();

        matchmakingController.registerListeners();
        matchController.registerListeners();
    }

    public void disconnect() {
        socketClient.disconnect();
    }

    public MatchmakingController matchmaking() {
        return matchmakingController;
    }

    public MatchController match() {
        return matchController;
    }

    public SocketClient getSocketClient() {
        return socketClient;
    }
}
