package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentFightStatisticsDTO;
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
import com.softwaremagico.kt.core.tournaments.SimpleLeagueHandler;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.values.LeagueFightsOrder;
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
@Test(groups = {"leagueTest"})
public class SimpleLeagueTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 6;
    private static final String TOURNAMENT_NAME = "simpleChampionshipTest";
    private static Tournament tournament = null;
    private static Tournament clonedTournament = null;

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private ParticipantProvider participantProvider;

    @Autowired
    private RoleProvider roleProvider;

    @Autowired
    private TeamProvider teamProvider;

    @Autowired
    private SimpleLeagueHandler simpleLeagueHandler;

    @Autowired
    private FightProvider fightProvider;

    @Autowired
    private GroupProvider groupProvider;

    @Autowired
    private ClubProvider clubProvider;

    @Autowired
    private DuelProvider duelProvider;

    @Autowired
    private TeamConverter teamConverter;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private FightStatisticsController fightStatisticsController;

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    private Club club;

    public static int getNumberOfCombats(Integer numberOfTeams) {
        return factorial(numberOfTeams) / (2 * factorial(numberOfTeams - 2));
    }


    private static int factorial(Integer n) {
        int total = 1;
        while (n > 1) {
            total = total * n;
            n--;
        }
        return total;
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
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LEAGUE, null);
        tournament = tournamentProvider.save(newTournament);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournament,
                TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION, LeagueFightsOrder.FIFO.name()));
        Assert.assertEquals(tournamentProvider.count(), 1);
    }


    @Test(dependsOnMethods = "addTournament")
    public void checkingCache() {
        tournamentProvider.get(tournament.getId());
        tournamentProvider.get(tournament.getId());
        tournamentProvider.get(tournament.getId());
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
        List<Fight> tournamentFights = simpleLeagueHandler.createFights(tournament, TeamsOrder.SORTED, 0, null);
        //Check group has been created.
        Assert.assertEquals(simpleLeagueHandler.getGroups(tournament).size(), 1);
        Assert.assertEquals(groupProvider.getGroups(tournament).get(0).getFights().size(), tournamentFights.size());

        Assert.assertEquals(tournamentFights.size(), getNumberOfCombats(TEAMS));

        // Checker than teams have not crossed colors.
        for (int i = 0; i < tournamentFights.size() - 1; i++) {
            Assert.assertNotEquals(tournamentFights.get(i + 1).getTeam2(), tournamentFights.get(i).getTeam1());
            Assert.assertNotEquals(tournamentFights.get(i + 1).getTeam1(), tournamentFights.get(i).getTeam2());
        }
    }


    @Test(dependsOnMethods = {"createFights"})
    public void cloneTournament() {
        final TournamentDTO clonedTournamentDTO = tournamentController.clone(tournament.getId(), null);
        clonedTournament = tournamentConverter.reverse(clonedTournamentDTO);
        Assert.assertEquals(teamProvider.count(clonedTournament), teamProvider.count(tournament));
        Assert.assertEquals(roleProvider.count(clonedTournament), roleProvider.count(tournament));
        Assert.assertEquals(fightProvider.count(clonedTournament), 0);
        Assert.assertEquals(groupProvider.count(clonedTournament), 1);
    }


    @Test(dependsOnMethods = {"cloneTournament"})
    public void createFightsOnCloned() {
        List<Team> tournamentTeams = teamProvider.getAll(clonedTournament);
        final Group group = groupProvider.getGroups(clonedTournament).get(0);

        groupProvider.addTeams(group.getId(), tournamentTeams, null);

        List<Fight> tournamentFights = simpleLeagueHandler.createFights(clonedTournament, TeamsOrder.SORTED, 0, null);
        //Check group has been created.
        Assert.assertEquals(simpleLeagueHandler.getGroups(clonedTournament).size(), 1);
        Assert.assertEquals(groupProvider.getGroups(clonedTournament).get(0).getFights().size(), tournamentFights.size());

        Assert.assertEquals(tournamentFights.size(), getNumberOfCombats(TEAMS));

        // Checker than teams have not crossed colors.
        for (int i = 0; i < tournamentFights.size() - 1; i++) {
            Assert.assertNotEquals(tournamentFights.get(i + 1).getTeam2(), tournamentFights.get(i).getTeam1());
            Assert.assertNotEquals(tournamentFights.get(i + 1).getTeam1(), tournamentFights.get(i).getTeam2());
        }
    }


    @Test(dependsOnMethods = {"createFights"})
    public void checkStatistics() {
        final Group group = groupProvider.getGroups(tournament).get(0);
        TournamentFightStatisticsDTO tournamentFightStatisticsDTO =
                fightStatisticsController.estimate(tournamentConverter.convert(new TournamentConverterRequest(tournament)), MEMBERS,
                        teamConverter.convertAll(group.getTeams().stream().map(TeamConverterRequest::new).toList()));
        Assert.assertEquals(tournamentFightStatisticsDTO.getFightsNumber().intValue(), group.getFights().size());
        Assert.assertEquals(tournamentFightStatisticsDTO.getDuelsNumber().intValue(), group.getFights().size() * MEMBERS);
    }


    @Test(dependsOnMethods = {"createFights", "cloneTournament"})
    public void testSimpleWinner() {
        while (!fightProvider.areOver(tournament)) {
            Fight currentFight = fightProvider.getCurrent(tournament);

            // First duel won
            currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            currentFight.getDuels().forEach(duel -> duel.setFinished(true));

            fightProvider.save(currentFight);
        }

        List<ScoreOfTeam> teamsScore = rankingProvider.getTeamsScoreRanking(tournament);

        for (int i = 0; i < teamsScore.size() - 1; i++) {
            Assert.assertTrue(teamsScore.get(i).getWonFights() >= teamsScore.get(i + 1).getWonFights());
            Assert.assertTrue(teamsScore.get(i).getWonDuels() >= teamsScore.get(i + 1).getWonDuels());
            Assert.assertTrue(teamsScore.get(i).getHits() >= teamsScore.get(i + 1).getHits());
        }

        resetGroup(groupProvider.getGroups(tournament).get(0), null);
    }

    @Test(dependsOnMethods = {"testSimpleWinner", "createFightsOnCloned"})
    public void testDifferentWinnerOnCloned() {
        while (!fightProvider.areOver(clonedTournament)) {
            Fight currentFight = fightProvider.getCurrent(clonedTournament);

            // Second duel won
            currentFight.getDuels().get(1).addCompetitor2Score(Score.KOTE);
            currentFight.getDuels().get(1).addCompetitor2Score(Score.KOTE);
            currentFight.getDuels().forEach(duel -> duel.setFinished(true));

            fightProvider.save(currentFight);
        }

        List<ScoreOfTeam> teamsScore = rankingProvider.getTeamsScoreRanking(clonedTournament);

        for (int i = 0; i < teamsScore.size() - 1; i++) {
            Assert.assertTrue(teamsScore.get(i).getWonFights() >= teamsScore.get(i + 1).getWonFights());
            Assert.assertTrue(teamsScore.get(i).getWonDuels() >= teamsScore.get(i + 1).getWonDuels());
            Assert.assertTrue(teamsScore.get(i).getHits() >= teamsScore.get(i + 1).getHits());
        }

        resetGroup(groupProvider.getGroups(clonedTournament).get(0), null);
    }


    /**
     * Draw team1 and team3.
     */
    @Test(dependsOnMethods = {"createFights", "testSimpleWinner"})
    public void testDrawWinner() {
        while (!fightProvider.areOver(tournament)) {
            Fight currentFight = fightProvider.getCurrent(tournament);
            // First duel
            if (currentFight.getTeam1().equals(teamProvider.get(tournament, "Team01").get())
                    && currentFight.getTeam2().equals(teamProvider.get(tournament, "Team02").get())) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            } else if (currentFight.getTeam1()
                    .equals(teamProvider.get(tournament, "Team03").get())
                    && currentFight.getTeam2()
                    .equals(teamProvider.get(tournament, "Team04").get())) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            }
            currentFight.getDuels().forEach(duel -> duel.setFinished(true));
            fightProvider.save(currentFight);
        }

        // Team1 is first one because the name.
        List<Team> drawTeams = rankingProvider.getFirstTeamsWithDrawScore(groupProvider.getGroups(tournament).get(0), 1);
        Assert.assertEquals(drawTeams.size(), 2);
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team01").get()));
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team03").get()));
        Assert.assertEquals(teamProvider.get(tournament, "Team01").get(), drawTeams.get(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team03").get(), drawTeams.get(1));

        // Finally wins Team3 at an untied duel
        Group group = simpleLeagueHandler.getGroups(tournament).get(0);
        group.createUntieDuel(
                teamProvider.get(tournament, "Team01").get().getMembers().get(0),
                teamProvider.get(tournament, "Team03").get().getMembers().get(0), null);
        group.getUnties().get(0).addCompetitor2Score(Score.MEN);
        groupProvider.save(group);

        groupProvider.save(simpleLeagueHandler.getGroups(tournament).get(0));

        simpleLeagueHandler.getGroups(tournament).get(0).getUnties().get(0).addCompetitor2Score(Score.MEN);

        List<ScoreOfTeam> scores = rankingProvider.getTeamsScoreRanking(tournament);
        Assert.assertEquals(teamProvider.get(tournament, "Team03").get(), scores.get(0).getTeam());

        resetGroup(groupProvider.getGroups(tournament).get(0), null);
    }


    /**
     * Draw team1, team3 and team5.
     */
    @Test(dependsOnMethods = {"createFights", "testDrawWinner"})
    public void testDrawVariousWinner() {
        while (!fightProvider.areOver(tournament)) {
            Fight currentFight = fightProvider.getCurrent(tournament);
            // First duel
            if (currentFight.getTeam1()
                    .equals(teamProvider.get(tournament, "Team01").get())
                    && currentFight.getTeam2()
                    .equals(teamProvider.get(tournament, "Team02").get())) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            } else if (currentFight.getTeam1()
                    .equals(teamProvider.get(tournament, "Team03").get())
                    && currentFight.getTeam2()
                    .equals(teamProvider.get(tournament, "Team04").get())) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            } else if (currentFight.getTeam1()
                    .equals(teamProvider.get(tournament, "Team05").get())
                    && currentFight.getTeam2()
                    .equals(teamProvider.get(tournament, "Team06").get())) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            }
            currentFight.getDuels().forEach(duel -> duel.setFinished(true));
            fightProvider.save(currentFight);
        }

        // Team1 is first one because the name.
        List<Team> drawTeams = rankingProvider.getFirstTeamsWithDrawScore(groupProvider.getGroups(tournament).get(0), 1);
        Assert.assertEquals(drawTeams.size(), 3);
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team01").get()));
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team03").get()));
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team05").get()));
        Assert.assertEquals(teamProvider.get(tournament, "Team01").get(), drawTeams.get(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team03").get(), drawTeams.get(1));
        Assert.assertEquals(teamProvider.get(tournament, "Team05").get(), drawTeams.get(2));

        // Finally wins Team3, Team5, Team1
        Group group = simpleLeagueHandler.getGroups(tournament).get(0);
        group.createUntieDuel(
                teamProvider.get(tournament, "Team05").get().getMembers().get(0),
                teamProvider.get(tournament, "Team03").get().getMembers().get(0), null);

        group.getUnties().get(0).addCompetitor2Score(Score.MEN);

        group.createUntieDuel(
                teamProvider.get(tournament, "Team05").get().getMembers().get(0),
                teamProvider.get(tournament, "Team01").get().getMembers().get(0), null);

        group.getUnties().get(1).addCompetitor1Score(Score.MEN);

        group.createUntieDuel(
                teamProvider.get(tournament, "Team03").get().getMembers().get(0),
                teamProvider.get(tournament, "Team01").get().getMembers().get(0), null);

        group.getUnties().get(2).addCompetitor1Score(Score.MEN);

        groupProvider.save(group);

        List<Team> rankingTeams = rankingProvider.getTeamsRanking(groupProvider.getGroups(tournament).get(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team03").get(), rankingTeams.get(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team05").get(), rankingTeams.get(1));
        Assert.assertEquals(teamProvider.get(tournament, "Team01").get(), rankingTeams.get(2));
    }


    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        groupProvider.delete(tournament);
        fightProvider.delete(tournament);
        duelProvider.delete(tournament);
        teamProvider.delete(tournament);
        roleProvider.delete(tournament);
        tournamentProvider.delete(tournament);

        groupProvider.delete(clonedTournament);
        fightProvider.delete(clonedTournament);
        duelProvider.delete(clonedTournament);
        teamProvider.delete(clonedTournament);
        roleProvider.delete(clonedTournament);
        tournamentProvider.delete(clonedTournament);

        participantProvider.deleteAll();
        clubProvider.delete(club);
        Assert.assertEquals(fightProvider.count(), 0);
        Assert.assertEquals(duelProvider.count(), 0);
    }
}
