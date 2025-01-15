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


import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
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
import com.softwaremagico.kt.persistence.entities.TournamentScore;
import com.softwaremagico.kt.persistence.values.LeagueFightsOrder;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.ScoreType;
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
@Test(groups = {"scoreTests"})
public class ScoreTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 4;
    private static final String TOURNAMENT_NAME = "scoreChampionshipTest";
    private static Tournament tournament = null;

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

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
    private GroupConverter groupConverter;

    @Autowired
    private TeamConverter teamConverter;

    @Autowired
    private RankingProvider rankingProvider;

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
    public void testEuropeanScore() {
        tournament.setTournamentScore(new TournamentScore(ScoreType.EUROPEAN));
        tournament = tournamentProvider.save(tournament);

        final Group groupTest = groupProvider.getGroups(tournament).get(0);

        // Team1 vs Team2
        groupTest.getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(0).getDuels().get(1).addCompetitor2Score(Score.MEN);
        // Team3 vs Team2
        groupTest.getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        // Team3 vs Team4
        // Team1 vs Team4
        groupTest.getFights().get(3).getDuels().get(0).addCompetitor2Score(Score.MEN);

        // Total Team1 0/1F 1/3D 1H
        // Total Team2 0/1F 2/2D 2H
        // Total Team3 1/1F 1/4D 2H
        // Total Team4 1/1F 1/5D 1H

        // finish fights.
        groupTest.getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightProvider.save(fight);
        });

        // Team01 has more duels won, but less draw fights than team3 and team4.
        // Tam03 has less draw duels but more hits than team4. In European,
        // win Team04
        List<ScoreOfTeam> scores = rankingProvider.getTeamsScoreRanking(tournament);
        Assert.assertEquals(scores.get(0).getTeam(), teamProvider.get(tournament, "Team04").get());
        Assert.assertEquals(scores.get(1).getTeam(), teamProvider.get(tournament, "Team03").get());
        Assert.assertEquals(scores.get(2).getTeam(), teamProvider.get(tournament, "Team02").get());
        Assert.assertEquals(scores.get(3).getTeam(), teamProvider.get(tournament, "Team01").get());

        resetGroup(groupProvider.getGroups(tournament).get(0), null);
    }

    @Test(dependsOnMethods = {"createFights"})
    public void testInternationalScore() {
        tournament.setTournamentScore(new TournamentScore(ScoreType.INTERNATIONAL));
        tournament = tournamentProvider.save(tournament);

        final Group groupTest = groupProvider.getGroups(tournament).get(0);

        // Team1 vs Team2
        groupTest.getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(0).getDuels().get(1).addCompetitor2Score(Score.MEN);
        // Team3 vs Team2
        groupTest.getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        // Team3 vs Team4
        // Team1 vs Team4
        groupTest.getFights().get(3).getDuels().get(0).addCompetitor2Score(Score.MEN);

        // Total Team1 0/1F 1/3D 1H
        // Total Team2 0/1F 2/2D 2H
        // Total Team3 1/1F 1/4D 2H
        // Total Team4 1/1F 1/5D 1H

        // finish fights.
        groupTest.getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightProvider.save(fight);
        });

        // Team01 has more duels won, but less draw fights than team3 and team4.
        // Tam03 has less draw duels but more hits than team4. In International,
        // win Team03
        List<ScoreOfTeam> scores = rankingProvider.getTeamsScoreRanking(tournament);
        Assert.assertEquals(scores.get(0).getTeam(), teamProvider.get(tournament, "Team03").get());
        Assert.assertEquals(scores.get(1).getTeam(), teamProvider.get(tournament, "Team04").get());
        Assert.assertEquals(scores.get(2).getTeam(), teamProvider.get(tournament, "Team02").get());
        Assert.assertEquals(scores.get(3).getTeam(), teamProvider.get(tournament, "Team01").get());

        resetGroup(groupProvider.getGroups(tournament).get(0), null);
    }

    @Test(dependsOnMethods = {"createFights"})
    public void testClassicScore() {
        tournament.setTournamentScore(new TournamentScore(ScoreType.CLASSIC));
        tournament = tournamentProvider.save(tournament);

        final Group groupTest = groupProvider.getGroups(tournament).get(0);

        // Team1 vs Team2
        groupTest.getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(0).getDuels().get(1).addCompetitor2Score(Score.MEN);
        // Team3 vs Team2
        groupTest.getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        // Team3 vs Team4
        // Team1 vs Team4
        groupTest.getFights().get(3).getDuels().get(0).addCompetitor2Score(Score.MEN);

        // Total Team1 0/1F 1/3D 1H
        // Total Team2 0/1F 2/2D 2H
        // Total Team3 1/1F 1/4D 2H
        // Total Team4 1/1F 1/5D 1H

        // finish fights.
        groupTest.getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightProvider.save(fight);
        });

        // Team01 has more duels won, but less draw fights than team3 and team4.
        // Tam03 has less draw duels but more hits than team4. In Classic,
        // win Team03
        List<ScoreOfTeam> scores = rankingProvider.getTeamsScoreRanking(tournament);
        Assert.assertEquals(scores.get(0).getTeam(), teamProvider.get(tournament, "Team03").get());
        Assert.assertEquals(scores.get(1).getTeam(), teamProvider.get(tournament, "Team04").get());
        Assert.assertEquals(scores.get(2).getTeam(), teamProvider.get(tournament, "Team02").get());
        Assert.assertEquals(scores.get(3).getTeam(), teamProvider.get(tournament, "Team01").get());

        resetGroup(groupProvider.getGroups(tournament).get(0), null);
    }

    @Test(dependsOnMethods = {"createFights"})
    public void testWinOverDrawsScore() {
        tournament.setTournamentScore(new TournamentScore(ScoreType.WIN_OVER_DRAWS));
        tournament = tournamentProvider.save(tournament);

        final Group groupTest = groupProvider.getGroups(tournament).get(0);

        // Team1 vs Team2
        groupTest.getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(0).getDuels().get(1).addCompetitor2Score(Score.MEN);
        // Team3 vs Team2
        groupTest.getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groupTest.getFights().get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        // Team3 vs Team4
        // Team1 vs Team4
        groupTest.getFights().get(3).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groupTest.getFights().get(3).getDuels().get(0).addCompetitor2Score(Score.MEN);

        // Total Team1 0/1F 1/4D 1H
        // Total Team2 0/1F 2/3D 2H
        // Total Team3 1/1F 1/3D 2H
        // Total Team4 1/1F 1/5D 2H

        // finish fights.
        groupTest.getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightProvider.save(fight);
        });

        // Team01 has more duels won, but less draw fights than team3 and team4.
        // Tam03 has less draw duels but more hits than team4. In WInOverDraws,
        // win Team03
        List<ScoreOfTeam> scores = rankingProvider.getTeamsScoreRanking(tournament);
        Assert.assertEquals(scores.get(0).getTeam(), teamProvider.get(tournament, "Team04").get());
        Assert.assertEquals(scores.get(1).getTeam(), teamProvider.get(tournament, "Team03").get());
        Assert.assertEquals(scores.get(2).getTeam(), teamProvider.get(tournament, "Team02").get());
        Assert.assertEquals(scores.get(3).getTeam(), teamProvider.get(tournament, "Team01").get());

        resetGroup(groupProvider.getGroups(tournament).get(0), null);
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
