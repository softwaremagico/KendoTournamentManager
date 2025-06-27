package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.stereotype.Service;

@Service
public class TournamentHandlerSelector {
    private final SimpleLeagueHandler simpleLeagueHandler;
    private final CustomLeagueHandler customTournamentHandler;
    private final LoopLeagueHandler loopLeagueHandler;
    private final TreeTournamentHandler treeTournamentHandler;
    private final KingOfTheMountainHandler kingOfTheMountainHandler;
    private final BubbleSortTournamentHandler bubbleSortTournamentHandler;
    private final SenbatsuTournamentHandler senbatsuTournamentHandler;

    public TournamentHandlerSelector(SimpleLeagueHandler simpleLeagueHandler, CustomLeagueHandler customTournamentHandler,
                                     LoopLeagueHandler loopLeagueHandler, TreeTournamentHandler treeTournamentHandler,
                                     KingOfTheMountainHandler kingOfTheMountainHandler, BubbleSortTournamentHandler bubbleSortTournamentHandler,
                                     SenbatsuTournamentHandler senbatsuTournamentHandler) {
        this.simpleLeagueHandler = simpleLeagueHandler;
        this.customTournamentHandler = customTournamentHandler;
        this.loopLeagueHandler = loopLeagueHandler;
        this.treeTournamentHandler = treeTournamentHandler;
        this.kingOfTheMountainHandler = kingOfTheMountainHandler;
        this.bubbleSortTournamentHandler = bubbleSortTournamentHandler;
        this.senbatsuTournamentHandler = senbatsuTournamentHandler;
    }

    public ITournamentManager selectManager(TournamentType type) {
        switch (type) {
            case LOOP:
                return loopLeagueHandler;
            case TREE, CHAMPIONSHIP:
                return treeTournamentHandler;
            case CUSTOM_CHAMPIONSHIP:
                break;
            case CUSTOMIZED:
                return customTournamentHandler;
            case KING_OF_THE_MOUNTAIN:
                return kingOfTheMountainHandler;
            case LEAGUE:
                return simpleLeagueHandler;
            case BUBBLE_SORT:
                return bubbleSortTournamentHandler;
            case SENBATSU:
                return senbatsuTournamentHandler;
            default:
                break;
        }
        return null;
    }
}
