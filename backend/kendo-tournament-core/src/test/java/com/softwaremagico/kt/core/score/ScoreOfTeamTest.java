package com.softwaremagico.kt.core.score;

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

import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "scoreOfTeam")
public class ScoreOfTeamTest {

    @Mock
    private Team team;

    @Mock
    private Team otherTeam;

    @Mock
    private Tournament tournament;

    @Mock
    private Fight wonFight;

    @Mock
    private Fight drawnUnfinishedFight;

    @Mock
    private Fight drawnFinishedFight;

    @Mock
    private Duel wonUntieDuel;

    @Mock
    private Duel lostUntieDuel;

    @Mock
    private Participant competitor1;

    @Mock
    private Participant competitor2;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(team.getTournament()).thenReturn(tournament);
        when(team.getMembers()).thenReturn(List.of(competitor1));

        // Fight won by "team" against "otherTeam".
        when(wonFight.getTeam1()).thenReturn(team);
        when(wonFight.getTeam2()).thenReturn(otherTeam);
        when(wonFight.getWinner()).thenReturn(team);
        when(wonFight.getLevel()).thenReturn(2);
        when(wonFight.isOver()).thenReturn(true);
        when(wonFight.isDrawFight()).thenReturn(false);
        when(wonFight.getWonDuels(team)).thenReturn(1);
        when(wonFight.getDrawDuels(team)).thenReturn(0);
        when(wonFight.getScore(team)).thenReturn(5);
        when(wonFight.getScoreAgainst(team)).thenReturn(1);

        // Fight involving "team" but not yet finished (should not count as draw).
        when(drawnUnfinishedFight.getTeam1()).thenReturn(team);
        when(drawnUnfinishedFight.getTeam2()).thenReturn(otherTeam);
        when(drawnUnfinishedFight.getWinner()).thenReturn(null);
        when(drawnUnfinishedFight.getLevel()).thenReturn(1);
        when(drawnUnfinishedFight.isOver()).thenReturn(false);
        when(drawnUnfinishedFight.isDrawFight()).thenReturn(true);
        when(drawnUnfinishedFight.getWonDuels(team)).thenReturn(0);
        when(drawnUnfinishedFight.getDrawDuels(team)).thenReturn(1);
        when(drawnUnfinishedFight.getScore(team)).thenReturn(2);
        when(drawnUnfinishedFight.getScoreAgainst(team)).thenReturn(2);

        // Fight finished and drawn.
        when(drawnFinishedFight.getTeam1()).thenReturn(team);
        when(drawnFinishedFight.getTeam2()).thenReturn(otherTeam);
        when(drawnFinishedFight.getWinner()).thenReturn(null);
        when(drawnFinishedFight.getLevel()).thenReturn(3);
        when(drawnFinishedFight.isOver()).thenReturn(true);
        when(drawnFinishedFight.isDrawFight()).thenReturn(true);
        when(drawnFinishedFight.getWonDuels(team)).thenReturn(0);
        when(drawnFinishedFight.getDrawDuels(team)).thenReturn(1);
        when(drawnFinishedFight.getScore(team)).thenReturn(1);
        when(drawnFinishedFight.getScoreAgainst(team)).thenReturn(1);

        when(wonUntieDuel.getCompetitor1()).thenReturn(competitor1);
        when(wonUntieDuel.getWinner()).thenReturn(-1);

        when(lostUntieDuel.getCompetitor1()).thenReturn(competitor2);
        when(lostUntieDuel.getCompetitor2()).thenReturn(competitor2);
        when(lostUntieDuel.getWinner()).thenReturn(1);
    }

    @Test
    public void constructor_expectFullScoreComputed() {
        final List<Fight> fights = List.of(wonFight, drawnUnfinishedFight, drawnFinishedFight);
        final List<Duel> unties = List.of(wonUntieDuel);

        final ScoreOfTeam score = new ScoreOfTeam(team, fights, unties);

        assertEquals(score.getTeam(), team);
        assertEquals(score.getTournament(), tournament);
        assertEquals(score.getWonFights().intValue(), 1);
        assertEquals(score.getDrawFights().intValue(), 1);
        assertEquals(score.getFightsDone().intValue(), 3);
        assertEquals(score.getWonDuels().intValue(), 1);
        assertEquals(score.getDrawDuels().intValue(), 2);
        assertEquals(score.getHits().intValue(), 8);
        assertEquals(score.getHitsLost().intValue(), 4);
        assertEquals(score.getLevel().intValue(), 3);
        assertEquals(score.getUntieDuels().intValue(), 1);
    }

    @Test
    public void setUntieDuels_withCompetitor2Loss_expectNotCounted() {
        final ScoreOfTeam score = new ScoreOfTeam(team, List.of(wonFight), List.of(lostUntieDuel));
        assertEquals(score.getUntieDuels().intValue(), 0);
    }

    @Test
    public void getTournament_withoutTeam_expectNull() {
        final ScoreOfTeam score = new ScoreOfTeam();
        assertEquals(score.getTournament(), null);
    }

    @Test
    public void settersAndGetters_expectRoundTrip() {
        final ScoreOfTeam score = new ScoreOfTeam();
        score.setSortingIndex(4);
        score.setSwissTieBreakValue(1.5);

        assertEquals(score.getSortingIndex().intValue(), 4);
        assertEquals(score.getSwissTieBreakValue(), 1.5);
    }

    @Test
    public void toString_expectContainsTeamNameAndCounters() {
        when(team.getName()).thenReturn("Team A");
        final ScoreOfTeam score = new ScoreOfTeam(team, List.of(wonFight), List.of(wonUntieDuel));

        final String text = score.toString();

        assertNotNull(text);
        assertTrue(text.contains("Team A"));
        assertTrue(text.contains("hits lost:"));
    }
}


