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

public enum Pool11To8winners1 {
    WINNER_1A(0, 0, 0),
    WINNER_2A(0, 1, 0),
    WINNER_3A(0, 2, 1),
    WINNER_4A(0, 3, 1),
    WINNER_5A(0, 4, 2),
    WINNER_6A(0, 5, 3),
    WINNER_7A(0, 6, 4),
    WINNER_8A(0, 7, 5),
    WINNER_9A(0, 8, 6),
    WINNER_10A(0, 9, 7),
    WINNER_11A(0, 10, 7);

    private final int winner;
    private final int source;
    private final int destination;

    Pool11To8winners1(int winner, int source, int destination) {
        this.winner = winner;
        this.source = source;
        this.destination = destination;
    }

    public static int getDestination(int sourceGroupLevelIndex, int winnerOrder) {
        for (Pool11To8winners1 pool11To8winners1 : Pool11To8winners1.values()) {
            if (pool11To8winners1.winner == winnerOrder && pool11To8winners1.source == sourceGroupLevelIndex) {
                return pool11To8winners1.destination;
            }
        }
        return -1;
    }
}
