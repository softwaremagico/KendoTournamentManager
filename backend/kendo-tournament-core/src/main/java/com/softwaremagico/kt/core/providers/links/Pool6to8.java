package com.softwaremagico.kt.core.providers.links;

public enum Pool6to8 {
    WINNER_1A(0, 0, 0),
    WINNER_1B(1, 0, 6),
    WINNER_2A(0, 1, 2),
    WINNER_2B(1, 1, 5),
    WINNER_3A(0, 2, 3),
    WINNER_3B(1, 2, 5),
    WINNER_4A(0, 3, 4),
    WINNER_4B(1, 3, 2),
    WINNER_5A(0, 4, 6),
    WINNER_5B(1, 4, 1),
    WINNER_6A(0, 5, 7),
    WINNER_6B(1, 5, 1);

    private final int winner;
    private final int source;
    private final int destination;

    Pool6to8(int winner, int source, int destination) {
        this.winner = winner;
        this.source = source;
        this.destination = destination;
    }

    public static int getDestination(int sourceGroupLevelIndex, int winnerOrder) {
        for (Pool6to8 pool6to8 : Pool6to8.values()) {
            if (pool6to8.winner == winnerOrder && pool6to8.source == sourceGroupLevelIndex) {
                return pool6to8.destination;
            }
        }
        return -1;
    }
}
