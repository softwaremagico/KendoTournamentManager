package com.softwaremagico.kt.core.providers.links;

public enum Pool12To16 {
    WINNER_1A(0, 0, 0),
    WINNER_1B(1, 0, 15),
    WINNER_2A(0, 1, 2),
    WINNER_2B(1, 1, 13),
    WINNER_3A(0, 2, 3),
    WINNER_3B(1, 2, 13),
    WINNER_4A(0, 3, 4),
    WINNER_4B(1, 3, 11),
    WINNER_5A(0, 4, 6),
    WINNER_5B(1, 4, 9),
    WINNER_6A(0, 5, 7),
    WINNER_6B(1, 5, 9),
    WINNER_7A(0, 6, 8),
    WINNER_7B(1, 6, 7),
    WINNER_8A(0, 7, 10),
    WINNER_8B(1, 7, 5),
    WINNER_9A(0, 8, 11),
    WINNER_9B(1, 8, 5),
    WINNER_10A(0, 9, 12),
    WINNER_10B(1, 9, 3),
    WINNER_11A(0, 10, 14),
    WINNER_11B(1, 10, 1),
    WINNER_12A(0, 11, 15),
    WINNER_12B(1, 11, 1);

    private final int winner;
    private final int source;
    private final int destination;

    Pool12To16(int winner, int source, int destination) {
        this.winner = winner;
        this.source = source;
        this.destination = destination;
    }

    public static int getDestination(int sourceGroupLevelIndex, int winnerOrder) {
        for (Pool12To16 pool12To16 : Pool12To16.values()) {
            if (pool12To16.winner == winnerOrder && pool12To16.source == sourceGroupLevelIndex) {
                return pool12To16.destination;
            }
        }
        return -1;
    }
}
