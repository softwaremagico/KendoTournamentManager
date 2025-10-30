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

public enum Pool6to4winners1 {
    WINNER_1A(0, 0, 0),
    WINNER_2A(0, 1, 0),
    WINNER_3A(0, 2, 1),
    WINNER_4A(0, 3, 2),
    WINNER_5A(0, 4, 3),
    WINNER_6A(0, 5, 3);

    private final int winner;
    private final int source;
    private final int destination;

    Pool6to4winners1(int winner, int source, int destination) {
        this.winner = winner;
        this.source = source;
        this.destination = destination;
    }

    public static int getDestination(int sourceGroupLevelIndex, int winnerOrder) {
        for (Pool6to4winners1 pool6To4Winners1 : Pool6to4winners1.values()) {
            if (pool6To4Winners1.winner == winnerOrder && pool6To4Winners1.source == sourceGroupLevelIndex) {
                return pool6To4Winners1.destination;
            }
        }
        return -1;
    }
}
