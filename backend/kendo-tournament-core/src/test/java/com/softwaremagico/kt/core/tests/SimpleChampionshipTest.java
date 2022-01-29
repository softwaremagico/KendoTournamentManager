package com.softwaremagico.kt.core.tests;


import com.softwaremagico.kt.core.providers.*;
import com.softwaremagico.kt.core.score.Ranking;
import com.softwaremagico.kt.core.tournaments.simple.SimpleTournamentHandler;
import com.softwaremagico.kt.persistence.entities.*;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;


@SpringBootTest
@Test(groups = {"simpleChampionshipTest"})
public class SimpleChampionshipTest extends AbstractTestNGSpringContextTests {

    private static final Integer MEMBERS = 3;
    private static final Integer TEAMS = 6;
    private static final String TOURNAMENT_NAME = "simpleChampionshipTest";
    private static Tournament tournament = null;

    @Autowired
    private TournamentProvider tournamentProvider;

    @Autowired
    private ParticipantProvider participantProvider;

    @Autowired
    private RoleProvider roleProvider;

    @Autowired
    private TeamProvider teamProvider;

    @Autowired
    private SimpleTournamentHandler simpleTournamentHandler;

    @Autowired
    private FightProvider fightProvider;

    @Autowired
    private GroupProvider groupProvider;

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

    private void resetFights(Tournament tournament) {
        fightProvider.getFights(tournament).forEach(fight -> {
            fight.getDuels().clear();
            fight.setOver(false);
            fightProvider.save(fight);
        });
    }

    @Test
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            participantProvider.save(new Participant(String.format("0000%s", i), String.format("name%s", i), String.format("lastname%s", i)));
        }
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() {
        Assert.assertEquals(tournamentProvider.count(), 0);
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.LEAGUE);
        tournament = tournamentProvider.save(newTournament);
        Assert.assertEquals(tournamentProvider.count(), 1);
    }

    @Test(dependsOnMethods = {"addTournament"})
    public void addRoles() {
        for (Participant competitor : participantProvider.getAll()) {
            roleProvider.save(new Role(tournament, competitor, RoleType.COMPETITOR));
        }
        Assert.assertEquals(roleProvider.count(tournament), participantProvider.count());
    }

    @Test(dependsOnMethods = {"addRoles"})
    public void addTeams() {
        int teamIndex = 0;
        Team team = null;
        int teamMember = 0;

        for (Participant competitor : participantProvider.getAll()) {
            roleProvider.save(new Role(tournament, competitor, RoleType.COMPETITOR));

            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new Team("Team" + String.format("%02d", teamIndex), tournament);
                teamMember = 0;
            }

            // Add member.
            team.addMember(competitor);
            team = teamProvider.save(team);
            teamMember++;

            // Team fill up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        Assert.assertEquals((int) TEAMS, teamProvider.count(tournament));
    }

    @Test(dependsOnMethods = {"addTeams"})
    public void createFights() {
        List<Fight> tournamentFights = simpleTournamentHandler.createSortedFights(tournament, true, 0);
        //Check group has been created.
        Assert.assertEquals(simpleTournamentHandler.getGroups(tournament).size(), 1);

        Assert.assertEquals(tournamentFights.size(), getNumberOfCombats(TEAMS));

        // Check than teams have not crossed colors.
        for (int i = 0; i < tournamentFights.size() - 1; i++) {
            Assert.assertNotEquals(tournamentFights.get(i + 1).getTeam2(), tournamentFights.get(i).getTeam1());
            Assert.assertNotEquals(tournamentFights.get(i + 1).getTeam1(), tournamentFights.get(i).getTeam2());
        }
    }

    @Test(dependsOnMethods = {"createFights"})
    public void testSimpleWinner() {
        while (!fightProvider.areOver(tournament)) {
            Fight currentFight = fightProvider.getCurrentFight(tournament);

            // First duel won
            currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            currentFight.setOver(true);

            fightProvider.save(currentFight);
        }

        Ranking ranking = new Ranking(groupProvider.getGroups(tournament).get(0));

        for (int i = 0; i < ranking.getTeamsScoreRanking().size() - 1; i++) {
            Assert.assertTrue(ranking.getTeamsScoreRanking().get(i).getWonFights() >= ranking.getTeamsScoreRanking()
                    .get(i + 1).getWonFights());
            Assert.assertTrue(ranking.getTeamsScoreRanking().get(i).getWonDuels() >= ranking.getTeamsScoreRanking()
                    .get(i + 1).getWonDuels());
            Assert.assertTrue(ranking.getTeamsScoreRanking().get(i).getHits() >= ranking.getTeamsScoreRanking()
                    .get(i + 1).getHits());
        }

        resetFights(tournament);
    }

    /**
     * Draw team1 and team3.
     *
     * @
     */
    @Test(dependsOnMethods = {"createFights", "testSimpleWinner"})
    public void testDrawWinner() {
        while (!fightProvider.areOver(tournament)) {
            Fight currentFight = fightProvider.getCurrentFight(tournament);
            // First duel
            if (currentFight.getTeam1().equals(teamProvider.get(tournament, "Team01"))
                    && currentFight.getTeam2().equals(teamProvider.get(tournament, "Team02"))) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            } else if (currentFight.getTeam1()
                    .equals(teamProvider.get(tournament, "Team03"))
                    && currentFight.getTeam2()
                    .equals(teamProvider.get(tournament, "Team04"))) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            }
            currentFight.setOver(true);
        }
        Ranking ranking = new Ranking(groupProvider.getGroups(tournament).get(0));

        // Team1 is first one because the name.
        List<Team> drawTeams = ranking.getFirstTeamsWithDrawScore(1);
        Assert.assertEquals(drawTeams.size(), 2);
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team01")));
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team03")));
        Assert.assertEquals(teamProvider.get(tournament, "Team01"), ranking.getTeam(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team03"), ranking.getTeam(1));

        // Finally wins Team3 at an untie duel
        simpleTournamentHandler.getGroups(tournament).get(0).createUntieDuel(
                teamProvider.get(tournament, "Team01").getMembers().get(0),
                teamProvider.get(tournament, "Team03").getMembers().get(0));

        simpleTournamentHandler.getGroups(tournament).get(0).getUnties().get(0).addCompetitor2Score(Score.MEN);

        ranking = new Ranking(groupProvider.getGroups(tournament).get(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team03"), ranking.getTeam(0));

        resetFights(tournament);
    }

    /**
     * Draw team1, team3 and team5.
     *
     * @
     */
    @Test(dependsOnMethods = {"createFights", "testDrawWinner"})
    public void testDrawVariousWinner() {
        while (!fightProvider.areOver(tournament)) {
            Fight currentFight = fightProvider.getCurrentFight(tournament);
            // First duel
            if (currentFight.getTeam1()
                    .equals(teamProvider.get(tournament, "Team01"))
                    && currentFight.getTeam2()
                    .equals(teamProvider.get(tournament, "Team02"))) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            } else if (currentFight.getTeam1()
                    .equals(teamProvider.get(tournament, "Team03"))
                    && currentFight.getTeam2()
                    .equals(teamProvider.get(tournament, "Team04"))) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            } else if (currentFight.getTeam1()
                    .equals(teamProvider.get(tournament, "Team05"))
                    && currentFight.getTeam2()
                    .equals(teamProvider.get(tournament, "Team06"))) {
                currentFight.getDuels().get(0).addCompetitor1Score(Score.MEN);
            }
            currentFight.setOver(true);
        }
        Ranking ranking = new Ranking(groupProvider.getGroups(tournament).get(0));

        // Team1 is first one because the name.
        List<Team> drawTeams = ranking.getFirstTeamsWithDrawScore(1);
        Assert.assertEquals(drawTeams.size(), 3);
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team01")));
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team03")));
        Assert.assertTrue(drawTeams.contains(teamProvider.get(tournament, "Team05")));
        Assert.assertEquals(teamProvider.get(tournament, "Team01"), ranking.getTeam(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team03"), ranking.getTeam(1));
        Assert.assertEquals(teamProvider.get(tournament, "Team05"), ranking.getTeam(2));

        // Finally wins Team3, Team5, Team1
        simpleTournamentHandler.getGroups(tournament).get(0).createUntieDuel(
                teamProvider.get(tournament, "Team05").getMembers().get(0),
                teamProvider.get(tournament, "Team03").getMembers().get(0));

        simpleTournamentHandler.getGroups(tournament).get(0).getUnties().get(0).addCompetitor2Score(Score.MEN);

        simpleTournamentHandler.getGroups(tournament).get(0).createUntieDuel(
                teamProvider.get(tournament, "Team05").getMembers().get(0),
                teamProvider.get(tournament, "Team01").getMembers().get(0));

        simpleTournamentHandler.getGroups(tournament).get(0).getUnties().get(1).addCompetitor1Score(Score.MEN);

        simpleTournamentHandler.getGroups(tournament).get(0).createUntieDuel(
                teamProvider.get(tournament, "Team03").getMembers().get(0),
                teamProvider.get(tournament, "Team01").getMembers().get(0));

        simpleTournamentHandler.getGroups(tournament).get(0).getUnties().get(2).addCompetitor1Score(Score.MEN);

        ranking = new Ranking(groupProvider.getGroups(tournament).get(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team03"), ranking.getTeam(0));
        Assert.assertEquals(teamProvider.get(tournament, "Team05"), ranking.getTeam(1));
        Assert.assertEquals(teamProvider.get(tournament, "Team01"), ranking.getTeam(2));
    }
}
