package com.softwaremagico.kt.core.providers.links;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 SoftwareMagico
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

public enum Pool3to4winners2 {
    WINNER_1A(0, 0, 0),
    WINNER_1B(1, 0, 3),
    WINNER_2A(0, 1, 2),
    WINNER_2B(1, 1, 1),
    WINNER_3A(0, 2, 3),
    WINNER_3B(1, 2, 1);

    private final int winner;
    private final int source;
    private final int destination;

    Pool3to4winners2(int winner, int source, int destination) {
        this.winner = winner;
        this.source = source;
        this.destination = destination;
    }

    public static int getDestination(int sourceGroupLevelIndex, int winnerOrder) {
        for (Pool3to4winners2 pool3To4Winners2 : Pool3to4winners2.values()) {
            if (pool3To4Winners2.winner == winnerOrder && pool3To4Winners2.source == sourceGroupLevelIndex) {
                return pool3To4Winners2.destination;
            }
        }
        return -1;
    }
}
