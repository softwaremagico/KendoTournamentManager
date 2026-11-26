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
import org.testng.annotations.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test(groups = {"scoreTests"})
public class ScoreOfCompetitorTest {

    private Participant competitor() {
        return new Participant("ID-1", "Taro", "Yamada", new Club("Club", "ES", "Madrid"));
    }

    @Test
    public void shouldComputeStatisticsFromFightsAndUnties() {
        final Participant competitor = competitor();

        final Team team1 = mock(Team.class);
        final Team team2 = mock(Team.class);
        final Fight fightOver = mock(Fight.class);
        when(fightOver.isOver()).thenReturn(true);
        when(fightOver.getDuels(competitor)).thenReturn(List.of(mock(Duel.class), mock(Duel.class)));
        when(fightOver.getDuelsWon(competitor)).thenReturn(1);
        when(fightOver.isWon(competitor)).thenReturn(true);
        when(fightOver.getWinner()).thenReturn(null);
        when(fightOver.getTeam1()).thenReturn(team1);
        when(fightOver.getTeam2()).thenReturn(team2);
        when(team1.isMember(competitor)).thenReturn(true);
        when(team2.isMember(competitor)).thenReturn(false);
        when(fightOver.getDrawDuels(competitor)).thenReturn(1);
        when(fightOver.getScore(competitor)).thenReturn(3);
        when(fightOver.getScoreAgainst(competitor)).thenReturn(1);

        final Team team1b = mock(Team.class);
        final Team team2b = mock(Team.class);
        final Fight fightNotOver = mock(Fight.class);
        when(fightNotOver.isOver()).thenReturn(false);
        when(fightNotOver.getDuels(competitor)).thenReturn(List.of(mock(Duel.class)));
        when(fightNotOver.getDuelsWon(competitor)).thenReturn(1);
        when(fightNotOver.isWon(competitor)).thenReturn(false);
        when(fightNotOver.getWinner()).thenReturn(team2b);
        when(fightNotOver.getTeam1()).thenReturn(team1b);
        when(fightNotOver.getTeam2()).thenReturn(team2b);
        when(team1b.isMember(competitor)).thenReturn(false);
        when(team2b.isMember(competitor)).thenReturn(true);
        when(fightNotOver.getDrawDuels(competitor)).thenReturn(0);
        when(fightNotOver.getScore(competitor)).thenReturn(4);
        when(fightNotOver.getScoreAgainst(competitor)).thenReturn(2);

        final Duel untie1 = mock(Duel.class);
        when(untie1.getCompetitor1()).thenReturn(competitor);
        when(untie1.getCompetitor2()).thenReturn(null);
        when(untie1.getWinner()).thenReturn(-1);
        when(untie1.getCompetitor1ScoreValue()).thenReturn(1);
        when(untie1.getCompetitor2ScoreValue()).thenReturn(0);

        final Duel untie2 = mock(Duel.class);
        when(untie2.getCompetitor1()).thenReturn(null);
        when(untie2.getCompetitor2()).thenReturn(competitor);
        when(untie2.getWinner()).thenReturn(1);
        when(untie2.getCompetitor1ScoreValue()).thenReturn(0);
        when(untie2.getCompetitor2ScoreValue()).thenReturn(2);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(
                competitor,
                List.of(fightOver, fightNotOver),
                List.of(untie1, untie2),
                false);

        assertThat(score.getDuelsDone()).isEqualTo(2);
        assertThat(score.getWonDuels()).isEqualTo(1);
        assertThat(score.getDrawDuels()).isEqualTo(1);
        assertThat(score.getWonFights()).isEqualTo(1);
        assertThat(score.getDrawFights()).isEqualTo(1);
        assertThat(score.getUntieDuels()).isEqualTo(2);
        assertThat(score.getUntieHits()).isEqualTo(3);
        assertThat(score.getHits()).isEqualTo(7);
        assertThat(score.getHitsLost()).isEqualTo(3);
        assertThat(score.getTotalFights()).isEqualTo(1);
        assertThat(score.toString()).contains("HL:");
    }

    @Test
    public void shouldIncludeNotOverFightsWhenCountNotOverIsTrue() {
        final Participant competitor = competitor();

        final Team team1 = mock(Team.class);
        final Team team2 = mock(Team.class);
        final Fight fightNotOver = mock(Fight.class);
        when(fightNotOver.isOver()).thenReturn(false);
        when(fightNotOver.getDuels(competitor)).thenReturn(List.of(mock(Duel.class), mock(Duel.class)));
        when(fightNotOver.getDuelsWon(competitor)).thenReturn(2);
        when(fightNotOver.isWon(competitor)).thenReturn(true);
        when(fightNotOver.getWinner()).thenReturn(null);
        when(fightNotOver.getTeam1()).thenReturn(team1);
        when(fightNotOver.getTeam2()).thenReturn(team2);
        when(team1.isMember(competitor)).thenReturn(true);
        when(team2.isMember(competitor)).thenReturn(false);
        when(fightNotOver.getDrawDuels(competitor)).thenReturn(1);
        when(fightNotOver.getScore(competitor)).thenReturn(5);
        when(fightNotOver.getScoreAgainst(competitor)).thenReturn(1);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(
                competitor,
                List.of(fightNotOver),
                List.of(),
                true);

        assertThat(score.getDuelsDone()).isEqualTo(2);
        assertThat(score.getWonDuels()).isEqualTo(2);
        assertThat(score.getWonFights()).isEqualTo(1);
        assertThat(score.getDrawFights()).isEqualTo(1);
        assertThat(score.getDrawDuels()).isEqualTo(1);
        assertThat(score.getHits()).isEqualTo(5);
        assertThat(score.getHitsLost()).isEqualTo(1);

        score.setCountNotOver(false);
        score.update();
        assertThat(score.getDuelsDone()).isZero();
    }

    // ========== New Tests ==========

    @Test
    public void testEmptyConstructorInitializesNulls() {
        final ScoreOfCompetitor score = new ScoreOfCompetitor();

        assertThat(score.getCompetitor()).isNull();
        assertThat(score.getWonDuels()).isNull();
        assertThat(score.getDrawDuels()).isNull();
        assertThat(score.getHits()).isNull();
        assertThat(score.getHitsLost()).isNull();
        assertThat(score.getWonFights()).isNull();
        assertThat(score.getDrawFights()).isNull();
        assertThat(score.getTotalFights()).isNull();
        assertThat(score.getUntieDuels()).isNull();
        assertThat(score.getUntieHits()).isNull();
    }

    @Test
    public void testSettersAndGetters() {
        final ScoreOfCompetitor score = new ScoreOfCompetitor();
        final Participant competitor = competitor();

        score.setCompetitor(competitor);
        score.setWonDuels(5);
        score.setDrawDuels(2);
        score.setHits(10);
        score.setHitsLost(3);
        score.setWonFights(4);
        score.setDrawFights(1);
        score.setTotalFights(6);
        score.setUntieDuels(2);
        score.setUntieHits(3);
        score.setDuelsDone(8);
        score.setFights(List.of());
        score.setUnties(List.of());

        assertThat(score.getCompetitor()).isEqualTo(competitor);
        assertThat(score.getWonDuels()).isEqualTo(5);
        assertThat(score.getDrawDuels()).isEqualTo(2);
        assertThat(score.getHits()).isEqualTo(10);
        assertThat(score.getHitsLost()).isEqualTo(3);
        assertThat(score.getWonFights()).isEqualTo(4);
        assertThat(score.getDrawFights()).isEqualTo(1);
        assertThat(score.getTotalFights()).isEqualTo(6);
        assertThat(score.getUntieDuels()).isEqualTo(2);
        assertThat(score.getUntieHits()).isEqualTo(3);
        assertThat(score.getDuelsDone()).isEqualTo(8);
        assertThat(score.getFights()).isEmpty();
        assertThat(score.getUnties()).isEmpty();
    }

    @Test
    public void testZeroStatsWithEmptyFights() {
        final Participant competitor = competitor();
        final ScoreOfCompetitor score = new ScoreOfCompetitor(competitor, List.of(), List.of(), false);

        assertThat(score.getDuelsDone()).isZero();
        assertThat(score.getWonDuels()).isZero();
        assertThat(score.getDrawDuels()).isZero();
        assertThat(score.getWonFights()).isZero();
        assertThat(score.getDrawFights()).isZero();
        assertThat(score.getHits()).isZero();
        assertThat(score.getHitsLost()).isZero();
        assertThat(score.getTotalFights()).isZero();
        assertThat(score.getUntieDuels()).isZero();
        assertThat(score.getUntieHits()).isZero();
    }

    @Test
    public void testCountNotOverFlag() {
        final ScoreOfCompetitor score = new ScoreOfCompetitor();
        score.setCountNotOver(true);
        assertThat(score.isCountNotOver()).isTrue();

        score.setCountNotOver(false);
        assertThat(score.isCountNotOver()).isFalse();
    }

    @Test
    public void testUpdateResetsAndRecalculates() {
        final Participant competitor = competitor();
        final Fight fight = mock(Fight.class);
        final Team team1 = mock(Team.class);
        final Team team2 = mock(Team.class);

        when(fight.isOver()).thenReturn(true);
        when(fight.getDuels(competitor)).thenReturn(List.of(mock(Duel.class)));
        when(fight.getDuelsWon(competitor)).thenReturn(1);
        when(fight.isWon(competitor)).thenReturn(true);
        when(fight.getWinner()).thenReturn(team1);
        when(fight.getTeam1()).thenReturn(team1);
        when(fight.getTeam2()).thenReturn(team2);
        when(team1.isMember(competitor)).thenReturn(true);
        when(team2.isMember(competitor)).thenReturn(false);
        when(fight.getDrawDuels(competitor)).thenReturn(0);
        when(fight.getScore(competitor)).thenReturn(2);
        when(fight.getScoreAgainst(competitor)).thenReturn(0);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(competitor, List.of(fight), List.of(), false);

        assertThat(score.getWonDuels()).isEqualTo(1);
        assertThat(score.getHits()).isEqualTo(2);

        // After update, values are recalculated
        score.update();
        assertThat(score.getWonDuels()).isEqualTo(1);
        assertThat(score.getHits()).isEqualTo(2);
    }

    @Test
    public void testUntieWonAsCompetitor2() {
        final Participant competitor = competitor();
        final Participant other = new Participant("ID-2", "Other", "Player", new Club("Club2", "ES", "City"));
        other.setId(2);

        final Duel untie = mock(Duel.class);
        // competitor is competitor2, winner=1 means competitor2 wins (per setUntieDuels: competitor2 && winner==1)
        when(untie.getCompetitor1()).thenReturn(other);
        when(untie.getCompetitor2()).thenReturn(competitor);
        when(untie.getWinner()).thenReturn(1);
        when(untie.getCompetitor1ScoreValue()).thenReturn(0);
        when(untie.getCompetitor2ScoreValue()).thenReturn(2);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(competitor, List.of(), List.of(untie), false);

        assertThat(score.getUntieDuels()).isEqualTo(1);
        assertThat(score.getUntieHits()).isEqualTo(2);
    }

    @Test
    public void testDrawFightCountedOnlyWhenBothTeamsHaveMember() {
        final Participant competitor = competitor();
        final Team team1 = mock(Team.class);
        final Team team2 = mock(Team.class);
        final Fight drawFight = mock(Fight.class);

        when(drawFight.isOver()).thenReturn(true);
        when(drawFight.getDuels(competitor)).thenReturn(List.of());
        when(drawFight.getDuelsWon(competitor)).thenReturn(0);
        when(drawFight.isWon(competitor)).thenReturn(false);
        when(drawFight.getWinner()).thenReturn(null); // draw
        when(drawFight.getTeam1()).thenReturn(team1);
        when(drawFight.getTeam2()).thenReturn(team2);
        when(team1.isMember(competitor)).thenReturn(true);
        when(team2.isMember(competitor)).thenReturn(false);
        when(drawFight.getDrawDuels(competitor)).thenReturn(0);
        when(drawFight.getScore(competitor)).thenReturn(1);
        when(drawFight.getScoreAgainst(competitor)).thenReturn(1);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(competitor, List.of(drawFight), List.of(), false);

        assertThat(score.getDrawFights()).isEqualTo(1);
        assertThat(score.getWonFights()).isZero();
    }

    @Test
    public void testToStringContainsCompetitorInfo() {
        final Participant competitor = competitor();
        final ScoreOfCompetitor score = new ScoreOfCompetitor(competitor, List.of(), List.of(), false);

        final String str = score.toString();
        assertThat(str).contains("D:").contains("H:").contains("HL:");
    }

    @Test
    public void testNotOverFightNotCountedWhenFlagFalse() {
        final Participant competitor = competitor();
        final Team team1 = mock(Team.class);
        final Team team2 = mock(Team.class);
        final Fight notOverFight = mock(Fight.class);

        when(notOverFight.isOver()).thenReturn(false);
        when(notOverFight.getDuels(competitor)).thenReturn(List.of(mock(Duel.class)));
        when(notOverFight.getDuelsWon(competitor)).thenReturn(1);
        when(notOverFight.isWon(competitor)).thenReturn(true);
        when(notOverFight.getTeam1()).thenReturn(team1);
        when(notOverFight.getTeam2()).thenReturn(team2);
        when(team1.isMember(competitor)).thenReturn(true);
        when(team2.isMember(competitor)).thenReturn(false);
        when(notOverFight.getScore(competitor)).thenReturn(2);
        when(notOverFight.getScoreAgainst(competitor)).thenReturn(0);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(competitor, List.of(notOverFight), List.of(), false);

        // Not over fights should NOT be counted when countNotOver = false
        assertThat(score.getDuelsDone()).isZero();
        assertThat(score.getWonDuels()).isZero();
        assertThat(score.getWonFights()).isZero();
    }

    @Test
    public void testTotalFightsCountsOnlyOverFights() {
        final Participant competitor = competitor();
        final Team team1 = mock(Team.class);
        final Team team2 = mock(Team.class);

        final Fight overFight = mock(Fight.class);
        when(overFight.isOver()).thenReturn(true);
        when(overFight.getDuels(competitor)).thenReturn(List.of());
        when(overFight.getDuelsWon(competitor)).thenReturn(0);
        when(overFight.isWon(competitor)).thenReturn(false);
        when(overFight.getWinner()).thenReturn(null);
        when(overFight.getTeam1()).thenReturn(team1);
        when(overFight.getTeam2()).thenReturn(team2);
        when(team1.isMember(competitor)).thenReturn(true);
        when(team2.isMember(competitor)).thenReturn(false);
        when(overFight.getDrawDuels(competitor)).thenReturn(0);
        when(overFight.getScore(competitor)).thenReturn(0);
        when(overFight.getScoreAgainst(competitor)).thenReturn(0);

        final Fight notOverFight = mock(Fight.class);
        when(notOverFight.isOver()).thenReturn(false);
        when(notOverFight.getDuels(competitor)).thenReturn(List.of());
        when(notOverFight.getDuelsWon(competitor)).thenReturn(0);
        when(notOverFight.getScore(competitor)).thenReturn(0);
        when(notOverFight.getScoreAgainst(competitor)).thenReturn(0);
        when(notOverFight.getTeam2()).thenReturn(team2);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(competitor, List.of(overFight, notOverFight), List.of(), false);

        assertThat(score.getTotalFights()).isEqualTo(1);
    }

    @Test
    public void testTotalFightsDoesNotCountNotOverFightWhenCompetitorIsInTeam2() {
        final Participant competitor = competitor();
        final Team team1 = mock(Team.class);
        final Team team2 = mock(Team.class);

        final Fight notOverFight = mock(Fight.class);
        when(notOverFight.isOver()).thenReturn(false);
        when(notOverFight.getDuels(competitor)).thenReturn(List.of());
        when(notOverFight.getDuelsWon(competitor)).thenReturn(0);
        when(notOverFight.getTeam1()).thenReturn(team1);
        when(notOverFight.getTeam2()).thenReturn(team2);
        when(team1.isMember(competitor)).thenReturn(false);
        when(team2.isMember(competitor)).thenReturn(true);
        when(notOverFight.getScore(competitor)).thenReturn(0);
        when(notOverFight.getScoreAgainst(competitor)).thenReturn(0);

        final ScoreOfCompetitor score = new ScoreOfCompetitor(competitor, List.of(notOverFight), List.of(), false);

        assertThat(score.getTotalFights()).isZero();
    }
}
