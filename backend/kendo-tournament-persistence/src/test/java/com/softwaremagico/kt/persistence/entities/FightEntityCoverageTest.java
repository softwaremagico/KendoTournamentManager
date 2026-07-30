package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "fightEntity")
public class FightEntityCoverageTest {

    private Tournament tournament;
    private Team team1;
    private Team team2;
    private Participant competitor1;
    private Participant competitor2;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        this.tournament = new Tournament("Fight Entity Tournament", 1, 1, TournamentType.LEAGUE, "tester");
        this.team1 = new Team("Team1", this.tournament);
        this.team2 = new Team("Team2", this.tournament);
        this.competitor1 = new Participant("ID1", "Name1", "Lastname1", null);
        this.competitor2 = new Participant("ID2", "Name2", "Lastname2", null);
        this.team1.addMember(this.competitor1);
        this.team2.addMember(this.competitor2);
    }

    private Fight newFight() {
        return new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
    }

    private Duel newDuel(int team1Points, int team2Points) {
        final Duel duel = new Duel(this.competitor1, this.competitor2, this.tournament, "tester");
        for (int i = 0; i < team1Points; i++) {
            duel.addCompetitor1Score(Score.MEN);
        }
        for (int i = 0; i < team2Points; i++) {
            duel.addCompetitor2Score(Score.MEN);
        }
        duel.setFinished(true);
        return duel;
    }

    @Test
    public void shouldGenerateDuelsOnConstruction() {
        final Fight fight = this.newFight();

        assertEquals(fight.getDuels().size(), 1);
        assertEquals(fight.getTeam1(), this.team1);
        assertEquals(fight.getTeam2(), this.team2);
    }

    @Test
    public void shouldNotGenerateDuelsWhenTeamsAreNull() {
        final Fight fight = new Fight();
        fight.setTournament(this.tournament);
        fight.generateDuels("tester");

        assertTrue(fight.getDuels().isEmpty());
    }

    @Test
    public void shouldReturnDuelsOfCompetitor() {
        final Fight fight = this.newFight();

        assertEquals(fight.getDuels(this.competitor1).size(), 1);
        assertTrue(fight.getDuels(new Participant("OTHER", "X", "Y", null)).isEmpty());
    }

    @Test
    public void shouldDeclareTeam1WinnerByDuelCount() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(2, 0))));

        assertEquals(fight.getWinner(), this.team1);
        assertEquals(fight.getLoser(), this.team2);
        assertFalse(fight.isDrawFight());
        assertTrue(fight.isWon(this.competitor1));
        assertFalse(fight.isWon(this.competitor2));
    }

    @Test
    public void shouldDeclareTeam2WinnerByDuelCount() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(0, 2))));

        assertEquals(fight.getWinner(), this.team2);
        assertEquals(fight.getLoser(), this.team1);
        assertTrue(fight.isWon(this.competitor2));
    }

    @Test
    public void shouldDeclareTeam1WinnerByPointsWhenDuelsTied() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(2, 0), this.newDuel(0, 1))));

        assertEquals(fight.getWinner(), this.team1);
    }

    @Test
    public void shouldDeclareTeam2WinnerByPointsWhenDuelsTied() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(0, 3), this.newDuel(1, 0))));

        assertEquals(fight.getWinner(), this.team2);
    }

    @Test
    public void shouldBeDrawWhenEverythingIsTied() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(1, 1))));

        assertNull(fight.getWinner());
        assertNull(fight.getLoser());
        assertTrue(fight.isDrawFight());
        assertFalse(fight.isWon(this.competitor1));
        assertFalse(fight.isWon(null));
    }

    @Test
    public void shouldBeOverWhenAllDuelsAreFinished() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(2, 0))));

        assertTrue(fight.isOver());
        assertTrue(fight.toString().contains("[F]"));
    }

    @Test
    public void shouldNotBeOverWhenDuelUnfinished() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        final Duel duel = new Duel(this.competitor1, this.competitor2, this.tournament, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(duel)));

        assertFalse(fight.isOver());
        assertFalse(fight.toString().contains("[F]"));
    }

    @Test
    public void shouldComputeScoreAndScoreAgainstForCompetitor() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(2, 1))));

        assertEquals(fight.getScore(this.competitor1), Integer.valueOf(2));
        assertEquals(fight.getScoreAgainst(this.competitor1), Integer.valueOf(1));
        assertEquals(fight.getScore(this.competitor2), Integer.valueOf(1));
        assertEquals(fight.getScoreAgainst(this.competitor2), Integer.valueOf(2));
    }

    @Test
    public void shouldComputeScoreAndScoreAgainstForTeams() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(2, 1))));

        assertEquals(fight.getScore(this.team1), Integer.valueOf(2));
        assertEquals(fight.getScore(this.team2), Integer.valueOf(1));
        assertEquals(fight.getScoreAgainst(this.team1), Integer.valueOf(1));
        assertEquals(fight.getScoreAgainst(this.team2), Integer.valueOf(2));
        assertEquals(fight.getScoreTeam1(), Integer.valueOf(2));
        assertEquals(fight.getScoreTeam2(), Integer.valueOf(1));

        final Team unrelatedTeam = new Team("Unrelated", this.tournament);
        assertEquals(fight.getScore(unrelatedTeam), Integer.valueOf(0));
        assertEquals(fight.getScoreAgainst(unrelatedTeam), Integer.valueOf(0));
    }

    @Test
    public void shouldCountDrawDuelsForCompetitorAndTeam() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(1, 1), this.newDuel(2, 0))));

        assertEquals(fight.getDrawDuels(this.competitor1), Integer.valueOf(1));
        assertEquals(fight.getDrawDuels(this.team1), Integer.valueOf(1));
        assertEquals(fight.getDrawDuels(this.team2), Integer.valueOf(1));

        final Team unrelatedTeam = new Team("Unrelated2", this.tournament);
        assertEquals(fight.getDrawDuels(unrelatedTeam), Integer.valueOf(0));
    }

    @Test
    public void shouldCountDuelsWonAndWonDuelsPerTeam() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.setDuels(new java.util.ArrayList<>(java.util.List.of(this.newDuel(2, 0), this.newDuel(0, 1))));

        assertEquals(fight.getDuelsWon(this.competitor1), Integer.valueOf(1));
        assertEquals(fight.getDuelsWon(this.competitor2), Integer.valueOf(1));
        assertEquals(fight.getWonDuels(this.team1), 1);
        assertEquals(fight.getWonDuels(this.team2), 1);

        final Team unrelatedTeam = new Team("Unrelated3", this.tournament);
        assertEquals(fight.getWonDuels(unrelatedTeam), 0);
    }
}

