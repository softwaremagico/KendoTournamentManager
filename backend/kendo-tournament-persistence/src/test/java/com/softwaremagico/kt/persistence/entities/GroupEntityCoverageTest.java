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

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@Test(groups = "groupEntity")
public class GroupEntityCoverageTest {

    private Tournament tournament;
    private Team team1;
    private Team team2;
    private Participant participant1;
    private Participant participant2;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        this.tournament = new Tournament("Group Entity Tournament", 1, 1, TournamentType.LEAGUE, "tester");
        this.team1 = new Team("Team1", this.tournament);
        this.team2 = new Team("Team2", this.tournament);
        this.participant1 = new Participant("ID1", "Name1", "Lastname1", null);
        this.participant2 = new Participant("ID2", "Name2", "Lastname2", null);
        this.team1.addMember(this.participant1);
        this.team2.addMember(this.participant2);
    }

    @Test
    public void shouldConsiderEmptyOrNullFightListAsOver() {
        assertTrue(Group.areFightsOverOrNull(new ArrayList<>()));
    }

    @Test
    public void shouldConsiderAllFightsOverWhenEveryFightIsFinished() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.getDuels().forEach(duel -> duel.setFinished(true));

        assertTrue(Group.areFightsOverOrNull(List.of(fight)));
    }

    @Test
    public void shouldDetectFightsNotOver() {
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");

        assertFalse(Group.areFightsOverOrNull(List.of(fight)));
    }

    @Test
    public void shouldConsiderGroupOverWhenFewerThanTwoTeams() {
        final Group group = new Group(this.tournament, 0, 0);
        group.setTeams(new ArrayList<>(List.of(this.team1)));
        group.setFights(new ArrayList<>());

        assertTrue(group.areFightsOverOrNull());
    }

    @Test
    public void shouldDelegateToFightsWhenEnoughTeams() {
        final Group group = new Group(this.tournament, 0, 0);
        group.setTeams(new ArrayList<>(List.of(this.team1, this.team2)));
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        group.setFights(new ArrayList<>(List.of(fight)));

        assertTrue(group.areFightsOverOrNull());
        assertTrue(group.isFightOfGroup(fight));
    }

    @Test
    public void shouldReplaceFightsPreservingCollectionReference() {
        final Group group = new Group(this.tournament, 0, 0);
        group.setFights(new ArrayList<>());
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");

        group.setFights(List.of(fight));

        assertEquals(group.getFights().size(), 1);
        assertTrue(group.isFightOfGroup(fight));
    }

    @Test
    public void shouldRemoveTeamsAndFights() {
        final Group group = new Group(this.tournament, 0, 0);
        group.setTeams(new ArrayList<>(List.of(this.team1, this.team2)));
        final Fight fight = new Fight(this.tournament, this.team1, this.team2, 0, 0, "tester");
        group.setFights(new ArrayList<>(List.of(fight)));

        group.removeTeams();
        group.removeFights();

        assertTrue(group.getTeams().isEmpty());
        assertTrue(group.getFights().isEmpty());
    }

    @Test
    public void shouldCreateUntieDuelAndAppendToList() {
        final Group group = new Group(this.tournament, 0, 0);

        group.createUntieDuel(this.participant1, this.participant2, "tester");

        assertEquals(group.getUnties().size(), 1);
        assertEquals(group.getUnties().get(0).getType(), DuelType.UNDRAW);
    }

    @Test
    public void shouldReplaceUntiesPreservingCollectionReference() {
        final Group group = new Group(this.tournament, 0, 0);
        group.createUntieDuel(this.participant1, this.participant2, "tester");
        final List<Duel> newUnties = new ArrayList<>(group.getUnties());

        group.setUnties(newUnties);

        assertEquals(group.getUnties().size(), 1);
    }
}

