package com.softwaremagico.kt.core.providers.links;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

public enum Pool11To16winners2 {
    WINNER_1A(0, 0, 0),
    WINNER_1B(1, 0, 6),
    WINNER_2A(0, 1, 1),
    WINNER_2B(1, 1, 7),
    WINNER_3A(0, 2, 2),
    WINNER_3B(1, 2, 8),
    WINNER_4A(0, 3, 3),
    WINNER_4B(1, 3, 9),
    WINNER_5A(0, 4, 4),
    WINNER_5B(1, 4, 10),
    WINNER_6A(0, 5, 5),
    WINNER_6B(1, 5, 11),
    WINNER_7A(0, 6, 11),
    WINNER_7B(1, 6, 6),
    WINNER_8A(0, 7, 12),
    WINNER_8B(1, 7, 7),
    WINNER_9A(0, 8, 13),
    WINNER_9B(1, 8, 8),
    WINNER_10A(0, 9, 14),
    WINNER_10B(1, 9, 9),
    WINNER_11A(0, 10, 15),
    WINNER_11B(1, 10, 10);

    private final int winner;
    private final int source;
    private final int destination;

    Pool11To16winners2(int winner, int source, int destination) {
        this.winner = winner;
        this.source = source;
        this.destination = destination;
    }

    public static int getDestination(int sourceGroupLevelIndex, int winnerOrder) {
        for (Pool11To16winners2 pool11To16winners2 : Pool11To16winners2.values()) {
            if (pool11To16winners2.winner == winnerOrder && pool11To16winners2.source == sourceGroupLevelIndex) {
                return pool11To16winners2.destination;
            }
        }
        return -1;
    }
}
