package com.softwaremagico.kt.core.tests;

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

import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.BubbleSortTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

@SpringBootTest
@Test(groups = {"bubleSortTest"})
public class BubbleSortTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 4;
    private static final String TOURNAMENT_NAME = "scoreChampionshipTest";
    private Tournament tournament = null;


    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private ParticipantProvider participantProvider;

    @Autowired
    private RoleProvider roleProvider;

    @Autowired
    private TeamProvider teamProvider;

    @Autowired
    private FightProvider fightProvider;

    @Autowired
    private GroupProvider groupProvider;

    @Autowired
    private ClubProvider clubProvider;

    @Autowired
    private DuelProvider duelProvider;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private BubbleSortTournamentHandler bubbleSortTournamentHandler;

    private Club club;

    @Test
    public void addClub() {
        club = clubProvider.save(new Club(CLUB_NAME, CLUB_COUNTRY, CLUB_CITY));
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            participantProvider.save(new Participant(String.format("0000%s", i), String.format("name%s", i), String.format("lastname%s", i), club));
        }
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() {
        Assert.assertEquals(tournamentProvider.count(), 0);
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.BUBBLE_SORT, null);
        tournament = tournamentProvider.save(newTournament);
        Assert.assertEquals(tournamentProvider.count(), 1);
    }

    @Test(dependsOnMethods = "addTournament")
    public void addGroup() {
        final Group group = new Group();
        group.setTournament(tournament);
        group.setIndex(0);
        group.setShiaijo(0);
        groupProvider.addGroup(tournament, group);
        Assert.assertEquals(groupProvider.count(), 1);
    }

    @Test(dependsOnMethods = {"addTournament"})
    public void addRoles() {
        for (Participant competitor : participantProvider.getAll()) {
            roleProvider.save(new Role(tournament, competitor, RoleType.COMPETITOR));
        }
        Assert.assertEquals(roleProvider.count(tournament), participantProvider.count());
    }

    @Test(dependsOnMethods = {"addGroup"})
    public void addTeams() {
        int teamIndex = 0;
        Team team = null;
        int teamMember = 0;

        final Group group = groupProvider.getGroups(tournament).get(0);

        for (Participant competitor : participantProvider.getAll()) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new Team("Team" + String.format("%02d", teamIndex), tournament);
                teamMember = 0;
            }

            // Add member.
            team.addMember(competitor);
            team = teamProvider.save(team);

            if (teamMember == 0) {
                groupProvider.addTeams(group.getId(), Collections.singletonList(team), null);
            }

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        Assert.assertEquals(teamProvider.count(tournament), TEAMS);
    }

    @Test(dependsOnMethods = {"addTeams"})
    public void createFights() {
        final List<Fight> tournamentFights = bubbleSortTournamentHandler.createFights(tournament, TeamsOrder.SORTED, 0, null);
        //Check group has been created.
        Assert.assertEquals(bubbleSortTournamentHandler.getGroups(tournament).size(), 1);
        Assert.assertEquals(groupProvider.getGroups(tournament).get(0).getFights().size(), tournamentFights.size());

        Assert.assertEquals(tournamentFights.size(), 1);
    }

    @Test(dependsOnMethods = {"createFights"})
    public void firstIteration() {
        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 1);

        //The First fight is team1 vs. team2. It is won by team1
        Assert.assertEquals(tournamentFights.get(tournamentFights.size() - 1).getTeam1().getName(), "Team01");
        Assert.assertEquals(tournamentFights.get(tournamentFights.size() - 1).getTeam2().getName(), "Team02");
        tournamentFights.get(tournamentFights.size() - 1).getDuels().get(0).addCompetitor1Score(Score.DO);
        tournamentFights.forEach(fight -> fight.getDuels().forEach(duel -> duel.setFinished(true)));
        fightProvider.save(tournamentFights.get(tournamentFights.size() - 1));

        //The Second fight must be done team1 vs. team3. It is won by team1
        bubbleSortTournamentHandler.generateNextFights(tournament, null);
        tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 2);
        Assert.assertEquals(bubbleSortTournamentHandler.getGroups(tournament).size(), 1);
        List<Fight> groupFights = bubbleSortTournamentHandler.getGroups(tournament).get(0).getFights();
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam1().getName(), "Team01");
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam2().getName(), "Team03");
        groupFights.get(groupFights.size() - 1).getDuels().get(0).addCompetitor1Score(Score.DO);
        groupFights.get(groupFights.size() - 1).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(groupFights.get(groupFights.size() - 1));

        //The Third fight must be done team1 vs. team4. It is won by team4
        bubbleSortTournamentHandler.generateNextFights(tournament, null);
        tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 3);
        Assert.assertEquals(bubbleSortTournamentHandler.getGroups(tournament).size(), 1);
        groupFights = bubbleSortTournamentHandler.getGroups(tournament).get(0).getFights();
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam1().getName(), "Team01");
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam2().getName(), "Team04");
        groupFights.get(groupFights.size() - 1).getDuels().get(0).addCompetitor2Score(Score.KOTE);
        groupFights.get(groupFights.size() - 1).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(groupFights.get(groupFights.size() - 1));

        //Check calculated order
        final List<Team> ranking = bubbleSortTournamentHandler.getTeamsOrderedByRanks(tournament, groupProvider.getGroups(tournament).get(0),
                bubbleSortTournamentHandler.getDrawResolution(tournament));
        Assert.assertEquals(ranking.size(), 4);
        Assert.assertEquals(ranking.get(0).getName(), "Team02");
        Assert.assertEquals(ranking.get(1).getName(), "Team03");
        Assert.assertEquals(ranking.get(2).getName(), "Team01");
        Assert.assertEquals(ranking.get(3).getName(), "Team04");
    }

    @Test(dependsOnMethods = {"firstIteration"})
    public void secondIteration() {
        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 1);
        Assert.assertEquals(groupProvider.getGroups(tournament).get(0).getLevel(), 0);
        bubbleSortTournamentHandler.generateNextFights(tournament, null);
        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 2);
        Assert.assertEquals(groupProvider.getGroups(tournament).get(1).getLevel(), 1);

        //The First fight is team2 vs. team3. It is won by team3
        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 4);
        Assert.assertEquals(bubbleSortTournamentHandler.getGroups(tournament).size(), 2);
        List<Fight> groupFights = bubbleSortTournamentHandler.getGroups(tournament).get(1).getFights();
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam1().getName(), "Team02");
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam2().getName(), "Team03");
        groupFights.get(groupFights.size() - 1).getDuels().get(0).addCompetitor2Score(Score.KOTE);
        groupFights.get(groupFights.size() - 1).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(groupFights.get(groupFights.size() - 1));

        //The Second fight must be done team3 vs. team1. It is won by team1
        bubbleSortTournamentHandler.generateNextFights(tournament, null);
        tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 5);
        Assert.assertEquals(bubbleSortTournamentHandler.getGroups(tournament).size(), 2);
        groupFights = bubbleSortTournamentHandler.getGroups(tournament).get(1).getFights();
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam1().getName(), "Team03");
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam2().getName(), "Team01");
        groupFights.get(groupFights.size() - 1).getDuels().get(0).addCompetitor1Score(Score.DO);
        groupFights.get(groupFights.size() - 1).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(groupFights.get(groupFights.size() - 1));

        //Check calculated order previous group
        List<Team> ranking = bubbleSortTournamentHandler.getTeamsOrderedByRanks(tournament, groupProvider.getGroups(tournament).get(0),
                bubbleSortTournamentHandler.getDrawResolution(tournament));
        Assert.assertEquals(ranking.size(), 4);
        Assert.assertEquals(ranking.get(0).getName(), "Team02");
        Assert.assertEquals(ranking.get(1).getName(), "Team03");
        Assert.assertEquals(ranking.get(2).getName(), "Team01");
        Assert.assertEquals(ranking.get(3).getName(), "Team04");

        //Check calculated order current group
        ranking = bubbleSortTournamentHandler.getTeamsOrderedByRanks(tournament, groupProvider.getGroups(tournament).get(1),
                bubbleSortTournamentHandler.getDrawResolution(tournament));
        Assert.assertEquals(ranking.size(), 4);
        Assert.assertEquals(ranking.get(0).getName(), "Team02");
        Assert.assertEquals(ranking.get(1).getName(), "Team01");
        Assert.assertEquals(ranking.get(2).getName(), "Team03");
        Assert.assertEquals(ranking.get(3).getName(), "Team04");
    }

    @Test(dependsOnMethods = {"secondIteration"})
    public void thirdIteration() {
        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 2);
        Assert.assertEquals(groupProvider.getGroups(tournament).get(1).getLevel(), 1);
        bubbleSortTournamentHandler.generateNextFights(tournament, null);
        Assert.assertEquals(groupProvider.getGroups(tournament).size(), 3);
        Assert.assertEquals(groupProvider.getGroups(tournament).get(2).getLevel(), 2);

        //The First fight is team2 vs. team3. It is won by team2
        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 6);
        Assert.assertEquals(bubbleSortTournamentHandler.getGroups(tournament).size(), 3);
        List<Fight> groupFights = bubbleSortTournamentHandler.getGroups(tournament).get(2).getFights();
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam1().getName(), "Team02");
        Assert.assertEquals(groupFights.get(groupFights.size() - 1).getTeam2().getName(), "Team01");
        groupFights.get(groupFights.size() - 1).getDuels().get(0).addCompetitor1Score(Score.DO);
        groupFights.get(groupFights.size() - 1).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(groupFights.get(groupFights.size() - 1));

        //Does not generate more fights. The Tournament is over.
        Assert.assertTrue(bubbleSortTournamentHandler.generateNextFights(tournament, null).isEmpty());

        //Check calculated order
        final List<Team> ranking = bubbleSortTournamentHandler.getTeamsOrderedByRanks(tournament, groupProvider.getGroups(tournament).get(2),
                bubbleSortTournamentHandler.getDrawResolution(tournament));
        Assert.assertEquals(ranking.size(), 4);
        Assert.assertEquals(ranking.get(0).getName(), "Team01");
        Assert.assertEquals(ranking.get(1).getName(), "Team02");
        Assert.assertEquals(ranking.get(2).getName(), "Team03");
        Assert.assertEquals(ranking.get(3).getName(), "Team04");
    }


    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        groupProvider.delete(tournament);
        fightProvider.delete(tournament);
        duelProvider.delete(tournament);
        teamProvider.delete(tournament);
        roleProvider.delete(tournament);
        tournamentProvider.delete(tournament);
        participantProvider.deleteAll();
        clubProvider.delete(club);
        Assert.assertEquals(fightProvider.count(), 0);
        Assert.assertEquals(duelProvider.count(), 0);
    }
}
