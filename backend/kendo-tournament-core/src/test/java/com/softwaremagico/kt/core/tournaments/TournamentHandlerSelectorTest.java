package com.softwaremagico.kt.core.tournaments;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

@Test(groups = "tournamentHandlerSelectorTests")
public class TournamentHandlerSelectorTest {

    @Mock
    private SimpleLeagueHandler simpleLeagueHandler;
    @Mock
    private CustomLeagueHandler customLeagueHandler;
    @Mock
    private LoopLeagueHandler loopLeagueHandler;
    @Mock
    private TreeTournamentHandler treeTournamentHandler;
    @Mock
    private KingOfTheMountainHandler kingOfTheMountainHandler;
    @Mock
    private BubbleSortTournamentHandler bubbleSortTournamentHandler;
    @Mock
    private SenbatsuTournamentHandler senbatsuTournamentHandler;

    private TournamentHandlerSelector selector;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        selector = new TournamentHandlerSelector(
                simpleLeagueHandler, customLeagueHandler, loopLeagueHandler,
                treeTournamentHandler, kingOfTheMountainHandler,
                bubbleSortTournamentHandler, senbatsuTournamentHandler);
    }

    @Test
    public void shouldReturnLoopHandlerForLoopType() {
        assertSame(selector.selectManager(TournamentType.LOOP), loopLeagueHandler);
    }

    @Test
    public void shouldReturnTreeHandlerForTreeType() {
        assertSame(selector.selectManager(TournamentType.TREE), treeTournamentHandler);
    }

    @Test
    public void shouldReturnTreeHandlerForChampionshipType() {
        assertSame(selector.selectManager(TournamentType.CHAMPIONSHIP), treeTournamentHandler);
    }

    @Test
    public void shouldReturnNullForCustomChampionshipType() {
        assertNull(selector.selectManager(TournamentType.CUSTOM_CHAMPIONSHIP));
    }

    @Test
    public void shouldReturnCustomHandlerForCustomizedType() {
        assertSame(selector.selectManager(TournamentType.CUSTOMIZED), customLeagueHandler);
    }

    @Test
    public void shouldReturnKingOfTheMountainHandlerForKingType() {
        assertSame(selector.selectManager(TournamentType.KING_OF_THE_MOUNTAIN), kingOfTheMountainHandler);
    }

    @Test
    public void shouldReturnSimpleLeagueHandlerForLeagueType() {
        assertSame(selector.selectManager(TournamentType.LEAGUE), simpleLeagueHandler);
    }

    @Test
    public void shouldReturnBubbleSortHandlerForBubbleSortType() {
        assertSame(selector.selectManager(TournamentType.BUBBLE_SORT), bubbleSortTournamentHandler);
    }

    @Test
    public void shouldReturnSenbatsuHandlerForSenbatsuType() {
        assertSame(selector.selectManager(TournamentType.SENBATSU), senbatsuTournamentHandler);
    }
}

