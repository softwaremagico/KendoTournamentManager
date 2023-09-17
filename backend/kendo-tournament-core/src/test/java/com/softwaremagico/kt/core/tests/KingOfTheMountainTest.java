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
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.core.tournaments.KingOfTheMountainHandler;
import com.softwaremagico.kt.core.tournaments.SimpleLeagueHandler;
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
@Test(groups = {"kingOfTheMountainTest"})
public class KingOfTheMountainTest extends AbstractTestNGSpringContextTests {

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
    private GroupConverter groupConverter;

    @Autowired
    private TeamConverter teamConverter;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private KingOfTheMountainHandler kingOfTheMountainHandler;

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
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.KING_OF_THE_MOUNTAIN, null);
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

        Assert.assertEquals(TEAMS, teamProvider.count(tournament));
    }

    @Test(dependsOnMethods = {"addTeams"})
    public void createFights() {
        List<Fight> tournamentFights = kingOfTheMountainHandler.createFights(tournament, TeamsOrder.SORTED, 0, null);
        //Check group has been created.
        Assert.assertEquals(kingOfTheMountainHandler.getGroups(tournament).size(), 1);
        Assert.assertEquals(groupProvider.getGroups(tournament).get(0).getFights().size(), tournamentFights.size());

        Assert.assertEquals(tournamentFights.size(), 1);
    }

    @Test(dependsOnMethods = {"createFights"})
    public void testSimpleWinner() {
        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        tournamentFights.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        tournamentFights.forEach(fight -> fight.getDuels().forEach(duel -> duel.setFinished(true)));
        fightProvider.save(tournamentFights.get(0));

        List<ScoreOfTeam> teamsScore = rankingProvider.getTeamsScoreRanking(tournament);
        Assert.assertEquals(teamsScore.size(), TEAMS);

        Assert.assertEquals(teamsScore.get(0).getTeam().getName(), tournamentFights.get(0).getTeam1().getName());
    }

    @Test(dependsOnMethods = {"testSimpleWinner"})
    public void nextFight1() {
        kingOfTheMountainHandler.generateNextFights(tournament, null);
        Assert.assertEquals(kingOfTheMountainHandler.getGroups(tournament).size(), 2);

        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 2);

        //1st team must be the previous one. But the other not
        Assert.assertEquals(tournamentFights.get(1).getTeam1(), tournamentFights.get(0).getTeam1());
        Assert.assertNotEquals(tournamentFights.get(1).getTeam2(), tournamentFights.get(0).getTeam2());
        Assert.assertEquals(tournamentFights.get(1).getTeam2().getName(), "Team03");

        //Finish the fight
        tournamentFights.get(1).getDuels().get(0).addCompetitor1Score(Score.DO);
        tournamentFights.get(1).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(tournamentFights.get(1));
    }

    @Test(dependsOnMethods = {"nextFight1"})
    public void nextFight2() {
        kingOfTheMountainHandler.generateNextFights(tournament, null);
        Assert.assertEquals(kingOfTheMountainHandler.getGroups(tournament).size(), 3);

        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 3);

        //1st team must be the previous one. But the other not
        Assert.assertEquals(tournamentFights.get(2).getTeam1(), tournamentFights.get(1).getTeam1());
        Assert.assertNotEquals(tournamentFights.get(2).getTeam2(), tournamentFights.get(1).getTeam2());
        Assert.assertEquals(tournamentFights.get(2).getTeam2().getName(), "Team04");

        //Finish the fight
        tournamentFights.get(2).getDuels().get(0).addCompetitor1Score(Score.DO);
        tournamentFights.get(2).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(tournamentFights.get(2));

    }

    @Test(dependsOnMethods = {"nextFight2"})
    public void loopStartsAgain() {
        kingOfTheMountainHandler.generateNextFights(tournament, null);
        Assert.assertEquals(kingOfTheMountainHandler.getGroups(tournament).size(), 4);

        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 4);

        //1st team must be the previous one. But the other not
        Assert.assertEquals(tournamentFights.get(3).getTeam1(), tournamentFights.get(2).getTeam1());
        Assert.assertNotEquals(tournamentFights.get(3).getTeam2(), tournamentFights.get(2).getTeam2());
        Assert.assertEquals(tournamentFights.get(3).getTeam2().getName(), "Team02");

        //Finish the fight. Team1 loose now
        tournamentFights.get(3).getDuels().get(0).addCompetitor2Score(Score.KOTE);
        tournamentFights.get(3).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(tournamentFights.get(3));
    }

    @Test(dependsOnMethods = {"loopStartsAgain"})
    public void team1Loose() {
        kingOfTheMountainHandler.generateNextFights(tournament, null);
        Assert.assertEquals(kingOfTheMountainHandler.getGroups(tournament).size(), 5);

        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 5);

        //2nd team must be the previous one. But the 1st must be out.
        Assert.assertNotEquals(tournamentFights.get(4).getTeam1(), tournamentFights.get(3).getTeam1());
        Assert.assertEquals(tournamentFights.get(4).getTeam2(), tournamentFights.get(3).getTeam2());
        Assert.assertEquals(tournamentFights.get(4).getTeam2().getName(), "Team02");
        Assert.assertEquals(tournamentFights.get(4).getTeam1().getName(), "Team03");

        //No winner
        tournamentFights.get(4).getDuels().forEach(duel -> duel.setFinished(true));
        fightProvider.save(tournamentFights.get(4));
    }

    @Test(dependsOnMethods = {"team1Loose"})
    public void afterDraw() {
        kingOfTheMountainHandler.generateNextFights(tournament, null);
        Assert.assertEquals(kingOfTheMountainHandler.getGroups(tournament).size(), 6);

        List<Fight> tournamentFights = fightProvider.getFights(tournament);
        Assert.assertEquals(tournamentFights.size(), 6);

        //Both teams must be replaced
        Assert.assertNotEquals(tournamentFights.get(5).getTeam1(), tournamentFights.get(4).getTeam1());
        Assert.assertNotEquals(tournamentFights.get(5).getTeam2(), tournamentFights.get(4).getTeam2());

        Assert.assertEquals(tournamentFights.get(5).getTeam1().getName(), "Team04");
        Assert.assertEquals(tournamentFights.get(5).getTeam2().getName(), "Team01");
    }


    @AfterClass
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
