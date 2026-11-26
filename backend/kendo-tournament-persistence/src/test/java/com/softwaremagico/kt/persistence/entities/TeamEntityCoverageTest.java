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

import com.softwaremagico.kt.persistence.values.TournamentType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

@Test(groups = "teamEntity")
public class TeamEntityCoverageTest {

    private Tournament tournament;
    private Participant participant;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        this.tournament = new Tournament("Team Entity Tournament", 1, 1, TournamentType.LEAGUE, "tester");
        this.participant = new Participant("ID1", "Name1", "Lastname1", null);
    }

    @Test
    public void shouldCheckMembership() {
        final Team team = new Team("Team1", this.tournament);
        team.addMember(this.participant);

        assertTrue(team.isMember(this.participant));
        assertFalse(team.isMember(new Participant("OTHER", "X", "Y", null)));
    }

    @Test
    public void shouldBeEqualToItself() {
        final Team team = new Team("Team1", this.tournament);
        assertEquals(team, team);
    }

    @Test
    public void shouldNotBeEqualToNullOrDifferentClass() {
        final Team team = new Team("Team1", this.tournament);
        assertNotEquals(team, null);
        assertNotEquals(team, "not-a-team");
    }

    @Test
    public void shouldBeEqualByIdWhenBothPersisted() {
        final Team team1 = new Team("Team1", this.tournament);
        team1.setId(1);
        final Team team2 = new Team("DifferentName", null);
        team2.setId(1);

        assertEquals(team1, team2);
        assertEquals(team1.hashCode(), team2.hashCode());
    }

    @Test
    public void shouldNotBeEqualByNameWhenNamesDiffer() {
        final Team team1 = new Team("Team1", this.tournament);
        final Team team2 = new Team("Team2", this.tournament);

        assertNotEquals(team1, team2);
    }

    @Test
    public void shouldBeEqualWhenSameTournamentReference() {
        final Team team1 = new Team("Team1", this.tournament);
        final Team team2 = new Team("Team1", this.tournament);

        assertEquals(team1, team2);
    }

    @Test
    public void shouldNotBeEqualWhenOneTournamentIsNull() {
        final Team team1 = new Team("Team1", this.tournament);
        final Team team2 = new Team("Team1", null);

        assertNotEquals(team1, team2);
        assertNotEquals(team2, team1);
    }

    @Test
    public void shouldBeEqualByTournamentIdWhenBothPersisted() {
        this.tournament.setId(5);
        final Tournament otherTournamentInstance = new Tournament("Team Entity Tournament", 1, 1, TournamentType.LEAGUE, "tester");
        otherTournamentInstance.setId(5);

        final Team team1 = new Team("Team1", this.tournament);
        final Team team2 = new Team("Team1", otherTournamentInstance);

        assertEquals(team1, team2);
    }

    @Test
    public void shouldNotBeEqualByDifferentTournamentIds() {
        this.tournament.setId(5);
        final Tournament otherTournament = new Tournament("Other", 1, 1, TournamentType.LEAGUE, "tester");
        otherTournament.setId(6);

        final Team team1 = new Team("Team1", this.tournament);
        final Team team2 = new Team("Team1", otherTournament);

        assertNotEquals(team1, team2);
    }

    @Test
    public void shouldComputeHashCodeWithoutId() {
        final Team team = new Team("Team1", this.tournament);
        assertEquals(team.hashCode(), team.hashCode());
    }

    @Test
    public void shouldComputeHashCodeWithTournamentIdWithoutTeamId() {
        this.tournament.setId(9);
        final Team team = new Team("Team1", this.tournament);
        assertEquals(team.hashCode(), team.hashCode());
    }

    @Test
    public void shouldCompareByNameFirst() {
        final Team team1 = new Team("Alpha", this.tournament);
        final Team team2 = new Team("Beta", this.tournament);

        assertTrue(team1.compareTo(team2) < 0);
        assertTrue(team2.compareTo(team1) > 0);
    }

    @Test
    public void shouldCompareByIdWhenNamesAreEqual() {
        final Team team1 = new Team("Alpha", this.tournament);
        team1.setId(1);
        final Team team2 = new Team("Alpha", this.tournament);
        team2.setId(2);

        assertTrue(team1.compareTo(team2) < 0);
    }

    @Test
    public void shouldCompareByTournamentIdWhenNamesAndIdsMatchOrMissing() {
        final Tournament tournamentA = new Tournament("A", 1, 1, TournamentType.LEAGUE, "tester");
        tournamentA.setId(1);
        final Tournament tournamentB = new Tournament("B", 1, 1, TournamentType.LEAGUE, "tester");
        tournamentB.setId(2);

        final Team team1 = new Team("Alpha", tournamentA);
        final Team team2 = new Team("Alpha", tournamentB);

        assertTrue(team1.compareTo(team2) < 0);
    }

    @Test
    public void shouldFallBackToIdentityHashWhenNoOtherCriteriaApply() {
        final Team team1 = new Team("Alpha", null);
        final Team team2 = new Team("Alpha", null);

        // Should not throw and should be consistent with itself.
        assertEquals(Integer.signum(team1.compareTo(team1)), 0);
        team1.compareTo(team2);
    }
}

