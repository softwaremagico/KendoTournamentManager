package com.softwaremagico.kt.core.providers.links;

public enum Pool3to4 {
    WINNER_1A(0, 0, 0),
    WINNER_1B(1, 0, 4),
    WINNER_2A(0, 1, 2),
    WINNER_2B(1, 1, 1),
    WINNER_3A(0, 2, 3),
    WINNER_3B(1, 2, 1);

    private final int winner;
    private final int source;
    private final int destination;

    Pool3to4(int winner, int source, int destination) {
        this.winner = winner;
        this.source = source;
        this.destination = destination;
    }

    public static int getDestination(int sourceGroupLevelIndex, int winnerOrder) {
        for (Pool3to4 pool3to4 : Pool3to4.values()) {
            if (pool3to4.winner == winnerOrder && pool3to4.source == sourceGroupLevelIndex) {
                return pool3to4.destination;
            }
        }
        return -1;
    }
}
