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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum TournamentExtraPropertyKey {
    MAXIMIZE_FIGHTS(TournamentType.LEAGUE, TournamentType.CHAMPIONSHIP, TournamentType.CUSTOM_CHAMPIONSHIP, TournamentType.TREE),
    AVOID_DUPLICATES(TournamentType.LOOP),
    KING_INDEX(TournamentType.KING_OF_THE_MOUNTAIN, TournamentType.BUBBLE_SORT),
    KING_DRAW_RESOLUTION(TournamentType.KING_OF_THE_MOUNTAIN, TournamentType.BUBBLE_SORT),
    DIPLOMA_NAME_HEIGHT(TournamentType.values()),
    NUMBER_OF_WINNERS(TournamentType.CHAMPIONSHIP, TournamentType.TREE, TournamentType.CUSTOM_CHAMPIONSHIP, TournamentType.LEAGUE),
    LEAGUE_FIGHTS_ORDER_GENERATION(TournamentType.LEAGUE, TournamentType.CHAMPIONSHIP, TournamentType.TREE, TournamentType.CUSTOM_CHAMPIONSHIP),
    ODD_FIGHTS_RESOLVED_ASAP(TournamentType.CHAMPIONSHIP, TournamentType.TREE, TournamentType.CUSTOM_CHAMPIONSHIP),
    BUBBLE_SORT_ITERATION(TournamentType.BUBBLE_SORT);

    private final Set<TournamentType> allowedTournaments;

    TournamentExtraPropertyKey(TournamentType... allowedTournaments) {
        this.allowedTournaments = new HashSet<>(Arrays.asList(allowedTournaments));
    }


    public static TournamentExtraPropertyKey getType(String name) {
        for (final TournamentExtraPropertyKey type : TournamentExtraPropertyKey.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

    public Set<TournamentType> getAllowedTournaments() {
        return allowedTournaments;
    }
}
