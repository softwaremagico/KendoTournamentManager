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

public enum Pool6to8winners2 {
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

    Pool6to8winners2(int winner, int source, int destination) {
        this.winner = winner;
        this.source = source;
        this.destination = destination;
    }

    public static int getDestination(int sourceGroupLevelIndex, int winnerOrder) {
        for (Pool6to8winners2 pool6To8Winners2 : Pool6to8winners2.values()) {
            if (pool6To8Winners2.winner == winnerOrder && pool6To8Winners2.source == sourceGroupLevelIndex) {
                return pool6To8Winners2.destination;
            }
        }
        return -1;
    }
}
