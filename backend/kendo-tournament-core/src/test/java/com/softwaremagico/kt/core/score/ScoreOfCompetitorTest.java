package com.softwaremagico.kt.core.score;

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

import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = {"scoreTests"})
public class ScoreOfCompetitorTest {

    private Participant competitor() {
        return new Participant("ID-1", "Taro", "Yamada", new Club("Club", "ES", "Madrid"));
    }

    @Test
    public void shouldComputeStatisticsFromFightsAndUnties() {
        final Participant competitor = competitor();

        final Team team1 = Mockito.mock(Team.class);
        final Team team2 = Mockito.mock(Team.class);
        final Fight fightOver = Mockito.mock(Fight.class);
        Mockito.when(fightOver.isOver()).thenReturn(true);
        Mockito.when(fightOver.getDuels(competitor)).thenReturn(List.of(Mockito.mock(Duel.class), Mockito.mock(Duel.class)));
        Mockito.when(fightOver.getDuelsWon(competitor)).thenReturn(1);
        Mockito.when(fightOver.isWon(competitor)).thenReturn(true);
        Mockito.when(fightOver.getWinner()).thenReturn(null);
        Mockito.when(fightOver.getTeam1()).thenReturn(team1);
        Mockito.when(fightOver.getTeam2()).thenReturn(team2);
        Mockito.when(team1.isMember(competitor)).thenReturn(true);
        Mockito.when(team2.isMember(competitor)).thenReturn(false);
        Mockito.when(fightOver.getDrawDuels(competitor)).thenReturn(1);
        Mockito.when(fightOver.getScore(competitor)).thenReturn(3);
        Mockito.when(fightOver.getScoreAgainst(competitor)).thenReturn(1);

        final Team team1b = Mockito.mock(Team.class);
        final Team team2b = Mockito.mock(Team.class);
        final Fight fightNotOver = Mockito.mock(Fight.class);
        Mockito.when(fightNotOver.isOver()).thenReturn(false);
        Mockito.when(fightNotOver.getDuels(competitor)).thenReturn(List.of(Mockito.mock(Duel.class)));
        Mockito.when(fightNotOver.getDuelsWon(competitor)).thenReturn(1);
        Mockito.when(fightNotOver.isWon(competitor)).thenReturn(false);
        Mockito.when(fightNotOver.getWinner()).thenReturn(team2b);
        Mockito.when(fightNotOver.getTeam1()).thenReturn(team1b);
        Mockito.when(fightNotOver.getTeam2()).thenReturn(team2b);
        Mockito.when(team1b.isMember(competitor)).thenReturn(false);
        Mockito.when(team2b.isMember(competitor)).thenReturn(true);
        Mockito.when(fightNotOver.getDrawDuels(competitor)).thenReturn(0);
        Mockito.when(fightNotOver.getScore(competitor)).thenReturn(4);
        Mockito.when(fightNotOver.getScoreAgainst(competitor)).thenReturn(2);

        final Duel untie1 = Mockito.mock(Duel.class);
        Mockito.when(untie1.getCompetitor1()).thenReturn(competitor);
        Mockito.when(untie1.getCompetitor2()).thenReturn(null);
        Mockito.when(untie1.getWinner()).thenReturn(-1);
        Mockito.when(untie1.getCompetitor1ScoreValue()).thenReturn(1);
        Mockito.when(untie1.getCompetitor2ScoreValue()).thenReturn(0);

        final Duel untie2 = Mockito.mock(Duel.class);
        Mockito.when(untie2.getCompetitor1()).thenReturn(null);
        Mockito.when(untie2.getCompetitor2()).thenReturn(competitor);
        Mockito.when(untie2.getWinner()).thenReturn(1);
        Mockito.when(untie2.getCompetitor1ScoreValue()).thenReturn(0);
        Mockito.when(untie2.getCompetitor2ScoreValue()).thenReturn(2);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(
                competitor,
                List.of(fightOver, fightNotOver),
                List.of(untie1, untie2),
                false);

        assertEquals(score.getDuelsDone().intValue(), 2);
        assertEquals(score.getWonDuels().intValue(), 1);
        assertEquals(score.getDrawDuels().intValue(), 1);
        assertEquals(score.getWonFights().intValue(), 1);
        assertEquals(score.getDrawFights().intValue(), 1);
        assertEquals(score.getUntieDuels().intValue(), 2);
        assertEquals(score.getUntieHits().intValue(), 3);
        assertEquals(score.getHits().intValue(), 7);
        assertEquals(score.getHitsLost().intValue(), 3);
        assertEquals(score.getTotalFights().intValue(), 2);
        assertTrue(score.toString().contains("HL:"));
    }

    @Test
    public void shouldIncludeNotOverFightsWhenCountNotOverIsTrue() {
        final Participant competitor = competitor();

        final Team team1 = Mockito.mock(Team.class);
        final Team team2 = Mockito.mock(Team.class);
        final Fight fightNotOver = Mockito.mock(Fight.class);
        Mockito.when(fightNotOver.isOver()).thenReturn(false);
        Mockito.when(fightNotOver.getDuels(competitor)).thenReturn(List.of(Mockito.mock(Duel.class), Mockito.mock(Duel.class)));
        Mockito.when(fightNotOver.getDuelsWon(competitor)).thenReturn(2);
        Mockito.when(fightNotOver.isWon(competitor)).thenReturn(true);
        Mockito.when(fightNotOver.getWinner()).thenReturn(null);
        Mockito.when(fightNotOver.getTeam1()).thenReturn(team1);
        Mockito.when(fightNotOver.getTeam2()).thenReturn(team2);
        Mockito.when(team1.isMember(competitor)).thenReturn(true);
        Mockito.when(team2.isMember(competitor)).thenReturn(false);
        Mockito.when(fightNotOver.getDrawDuels(competitor)).thenReturn(1);
        Mockito.when(fightNotOver.getScore(competitor)).thenReturn(5);
        Mockito.when(fightNotOver.getScoreAgainst(competitor)).thenReturn(1);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(
                competitor,
                List.of(fightNotOver),
                List.of(),
                true);

        assertEquals(score.getDuelsDone().intValue(), 2);
        assertEquals(score.getWonDuels().intValue(), 2);
        assertEquals(score.getWonFights().intValue(), 1);
        assertEquals(score.getDrawFights().intValue(), 1);
        assertEquals(score.getDrawDuels().intValue(), 1);
        assertEquals(score.getHits().intValue(), 5);
        assertEquals(score.getHitsLost().intValue(), 1);

        score.setCountNotOver(false);
        score.update();
        assertEquals(score.getDuelsDone().intValue(), 0);
    }
}

