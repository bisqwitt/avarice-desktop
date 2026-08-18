package com.avaricious.network;

public class SocketEvents {

    private SocketEvents() {
    }

    // Matchmaking
    public static final String JOIN_QUEUE = "joinQueue";
    public static final String QUEUE_JOINED = "queueJoined";
    public static final String MATCH_FOUND = "matchFound";
    public static final String OPPONENT_LEFT = "opponentLeft";

    // Match
    public static final String ROUND_END = "roundEnd";
    public static final String ROUND_END_WAITING = "roundEndWaiting";
    public static final String BOTH_PLAYERS_ENDED_ROUND = "bothPlayersEndedRound";

    // Health
    public static final String HEALTH_UPDATE = "healthUpdate";
    public static final String OPPONENT_HEALTH_CHANGED = "opponentHealthRequest";

    public static final String SCORE_UPDATE = "scoreUpdate";
    public static final String OPPONENT_SCORE_CHANGED = "opponentScoreChanged";
}
