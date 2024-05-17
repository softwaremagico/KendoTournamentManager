package com.softwaremagico.kt.persistence.values;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

public enum TournamentExtraPropertyKey {
    MAXIMIZE_FIGHTS,
    AVOID_DUPLICATES,
    KING_INDEX,
    KING_DRAW_RESOLUTION,
    DIPLOMA_NAME_HEIGHT,
    NUMBER_OF_WINNERS,
    LEAGUE_FIGHTS_ORDER_GENERATION;


    public static TournamentExtraPropertyKey getType(String name) {
        for (final TournamentExtraPropertyKey type : TournamentExtraPropertyKey.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
