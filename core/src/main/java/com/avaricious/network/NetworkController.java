package com.avaricious.network;

import com.avaricious.network.match.MatchController;
import com.avaricious.network.matchmaking.MatchmakingController;
import com.avaricious.app.navigation.ScreenManager;

public class NetworkController {

    private final SocketClient socketClient;
    private final MatchmakingController matchmakingController;
    private final MatchController matchController;

    public NetworkController(ScreenManager screens) {
        socketClient = new SocketClient();
        matchmakingController = new MatchmakingController(
            socketClient,
            screens
        );
        matchController = new MatchController(socketClient, screens);
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
