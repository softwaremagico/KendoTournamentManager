package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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


import com.softwaremagico.kt.core.controller.FightStatisticsController;
import com.softwaremagico.kt.core.controller.models.TournamentFightStatisticsDTO;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.core.tournaments.LoopLeagueHandler;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
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
@Test(groups = {"loopChampionshipTest"})
public class LoopLeagueTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 6;
    private static final String TOURNAMENT_NAME = "simpleChampionshipTest";
    private static final boolean MAXIMIZE_FIGHTS = true;
    private Tournament tournament = null;

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private ParticipantProvider participantProvider;

    @Autowired
    private RoleProvider roleProvider;

    @Autowired
    private TeamProvider teamProvider;

    @Autowired
    private LoopLeagueHandler loopLeagueHandler;

    @Autowired
    private FightProvider fightProvider;

    @Autowired
    private GroupProvider groupProvider;

    @Autowired
    private ClubProvider clubProvider;

    @Autowired
    private DuelProvider duelProvider;

    @Autowired
    private GroupConverter groupConverter;

    @Autowired
    private TeamConverter teamConverter;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private FightStatisticsController fightStatisticsController;

    private Club club;

    public static int getNumberOfCombats(Integer numberOfTeams) {
        return numberOfTeams * (numberOfTeams - 1);
    }

    private void resetGroup(Group group, String createdBy) {
        group.getFights().forEach(fight -> {
            fight.getDuels().clear();
            fight.generateDuels(createdBy);
        });
        group.getUnties().clear();
        groupProvider.save(group);
    }

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
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LOOP, null);
        tournament = tournamentProvider.save(newTournament);
        TournamentExtraProperty tournamentExtraProperty =
                new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, MAXIMIZE_FIGHTS + "");
        tournamentExtraPropertyProvider.save(tournamentExtraProperty);
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

        Assert.assertEquals(TEAMS, teamProvider.count(tournament));
    }

    @Test(dependsOnMethods = {"addTeams"})
    public void createFights() {
        List<Fight> tournamentFights = loopLeagueHandler.createFights(tournament, TeamsOrder.SORTED, 0, null);
        //Check group has been created.
        Assert.assertEquals(loopLeagueHandler.getGroups(tournament).size(), 1);
        Assert.assertEquals(groupProvider.getGroups(tournament).get(0).getFights().size(), tournamentFights.size());

        Assert.assertEquals(tournamentFights.size(), getNumberOfCombats(TEAMS));

        // Checker than teams are in loop
        Team teamInLoop = null;
        for (int i = 0; i < tournamentFights.size(); i++) {
            if (i % (TEAMS - 1) == 0) {
                teamInLoop = tournamentFights.get(i).getTeam1();
            }
            Assert.assertEquals(teamInLoop, tournamentFights.get(i).getTeam1());
        }
    }

    @Test(dependsOnMethods = {"createFights"})
    public void checkStatistics() {
        final Group group = groupProvider.getGroups(tournament).get(0);
        TournamentFightStatisticsDTO tournamentFightStatisticsDTO =
                fightStatisticsController.estimate(tournamentConverter.convert(new TournamentConverterRequest(tournament)),
                        MEMBERS,
                        teamConverter.convertAll(group.getTeams().stream().map(TeamConverterRequest::new).toList()));
        Assert.assertEquals(tournamentFightStatisticsDTO.getFightsNumber().intValue(), group.getFights().size());
        Assert.assertEquals(tournamentFightStatisticsDTO.getDuelsNumber().intValue(), group.getFights().size() * MEMBERS);
    }

    @Test(dependsOnMethods = {"createFights"})
    public void testSimpleWinner() {
        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        int counter = 0;
        //First team win all fights, second all fights -1 third all fights -2, ...
        for (int i = 0; i < tournamentFights.size(); i++) {
            Fight currentFight = tournamentFights.get(i);
            if (i % TEAMS < TEAMS - counter) {
                // First duel won
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            }
            currentFight.getDuels().forEach(duel -> duel.setFinished(true));
            fightProvider.save(currentFight);
            if (i % TEAMS == TEAMS - 1) {
                counter++;
            }
        }

        List<ScoreOfTeam> teamsScore = rankingProvider.getTeamsScoreRanking(tournament);
        Assert.assertEquals(teamsScore.size(), TEAMS);

        for (int i = 0; i < teamsScore.size() - 1; i++) {
            Assert.assertTrue(teamsScore.get(i).getWonFights() >= teamsScore.get(i + 1).getWonFights());
            Assert.assertTrue(teamsScore.get(i).getWonDuels() >= teamsScore.get(i + 1).getWonDuels());
            Assert.assertTrue(teamsScore.get(i).getHits() >= teamsScore.get(i + 1).getHits());
        }

        resetGroup(groupProvider.getGroups(tournament).get(0), null);
    }


    @AfterClass
    public void deleteTournament() {
        groupProvider.delete(tournament);
        fightProvider.delete(tournament);
        duelProvider.delete(tournament);
        teamProvider.delete(tournament);
        roleProvider.delete(tournament);
        participantProvider.deleteAll();
        clubProvider.delete(club);
        tournamentProvider.delete(tournament);
        Assert.assertEquals(fightProvider.count(), 0);
        Assert.assertEquals(duelProvider.count(), 0);
    }
}
