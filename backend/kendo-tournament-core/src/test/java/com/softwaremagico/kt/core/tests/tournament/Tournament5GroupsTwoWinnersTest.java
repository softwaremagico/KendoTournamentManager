package com.softwaremagico.kt.core.tests.tournament;

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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.FightStatisticsController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
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
@Test(groups = {"tournament5GroupsTwoWinnersTest"})
public class Tournament5GroupsTwoWinnersTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 16;
    private static final int GROUPS = 5;
    private static final String TOURNAMENT_NAME = "TournamentTest";
    private static TournamentDTO tournamentDTO = null;

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private TreeTournamentHandler treeTournamentHandler;

    @Autowired
    private ClubController clubController;

    @Autowired
    private GroupConverter groupConverter;

    @Autowired
    private TeamConverter teamConverter;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private FightStatisticsController fightStatisticsController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private FightController fightController;

    @Autowired
    private DuelController duelController;

    @Autowired
    private FightConverter fightConverter;

    @Autowired
    private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

    private ClubDTO clubDTO;


    @Test
    public void addClub() {
        clubDTO = clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null);
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            participantController.create(new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i), String.format("lastname%s", i), clubDTO), null);
        }
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() {
        Assert.assertEquals(tournamentController.count(), 0);
        TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.TREE);
        tournamentDTO = tournamentController.create(newTournament, null);
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentConverter.reverse(tournamentDTO), TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, "false"));
        tournamentExtraPropertyProvider.save(new TournamentExtraProperty(tournamentConverter.reverse(tournamentDTO), TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, "false"));
        Assert.assertEquals(tournamentController.count(), 1);

        tournamentExtraPropertyController.create(new TournamentExtraPropertyDTO(tournamentDTO, TournamentExtraPropertyKey.NUMBER_OF_WINNERS, "2"), null);
    }

    @Test(dependsOnMethods = {"addTournament"})
    public void addRoles() {
        for (ParticipantDTO competitor : participantController.get()) {
            roleController.create(new RoleDTO(tournamentDTO, competitor, RoleType.COMPETITOR), null);
        }
        Assert.assertEquals(roleController.count(tournamentDTO), participantController.count());
    }

    @Test(dependsOnMethods = "addTournament")
    public void add5Groups() {
        //First group is already inserted.
        treeTournamentHandler.adjustGroupsSizeRemovingOddNumbers(tournamentConverter.reverse(tournamentDTO), 2);

        for (int i = 1; i < GROUPS; i++) {
            final GroupDTO groupDTO = new GroupDTO();
            groupDTO.setTournament(tournamentDTO);
            groupDTO.setIndex(i);
            groupDTO.setLevel(0);
            groupDTO.setShiaijo(0);
            groupController.create(groupDTO, null);
        }
        Assert.assertEquals(groupController.count(), 16);
    }

    @Test(dependsOnMethods = {"add5Groups"})
    public void addTeams() {
        int teamIndex = 0;
        TeamDTO team = null;
        int teamMember = 0;

        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);

        for (ParticipantDTO competitor : participantController.get()) {
            // Create a new team.
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
                teamMember = 0;
            }

            // Add member.
            team.addMember(competitor);
            team = teamController.create(team, null);

            //Add the new team to a group
            if (teamMember == 0) {
                groupController.addTeams(groups.get(((teamIndex - 1) / (TEAMS / GROUPS)) % GROUPS).getId(), Collections.singletonList(team), null);
            }

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        Assert.assertEquals(teamController.count(tournamentDTO), TEAMS);

        final List<Group> tournamentGroups = groupController.getGroups(tournamentDTO, 0);
        for (Group group : tournamentGroups) {
            if (group.getIndex() == 0) {
                Assert.assertEquals(group.getTeams().size(), TEAMS / GROUPS + 1);
            } else {
                Assert.assertEquals(group.getTeams().size(), TEAMS / GROUPS);
            }
        }
    }

    @Test(dependsOnMethods = {"addTeams"})
    public void createFights() {
        List<Fight> tournamentFights = treeTournamentHandler.createFights(tournamentConverter.reverse(tournamentDTO), TeamsOrder.NONE, 0, null);
        Assert.assertEquals(tournamentFights.size(), TEAMS);
        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);
        for (final Group group : groups) {
            if (group.getIndex() == 0) {
                Assert.assertEquals(group.getFights().size(), 4);
            } else {
                Assert.assertEquals(group.getFights().size(), 3);
            }
        }
    }

    @Test(dependsOnMethods = {"createFights"})
    public void solveFights() {
        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);

        // First group has team1 as winner. And Team02 as second one.
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(0).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(0)).get(0).getName(), "Team01");
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(0)).get(1).getName(), "Team02");


        // Second group has team06 winner. And Team04 as second one.
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(1).getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(1).getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(1).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(1)).get(0).getName(), "Team06");
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(1)).get(1).getName(), "Team04");

        // Third group has team09 winner. Team08 as second one
        groups.get(2).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(2).getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(2).getFights().get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(2).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(2)).get(0).getName(), "Team09");
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(2)).get(1).getName(), "Team08");

        // Forth group has team10 winner. Team12 as second one.
        groups.get(3).getFights().get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        groups.get(3).getFights().get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        groups.get(3).getFights().get(2).getDuels().get(1).addCompetitor1Score(Score.MEN);
        groups.get(3).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(3)).get(0).getName(), "Team10");
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(3)).get(1).getName(), "Team12");

        // Fifth group has team13 winner. Team14 the second one.
        groups.get(4).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(4).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(4).getFights().get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(4).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(4)).get(0).getName(), "Team13");
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(4)).get(1).getName(), "Team14");

    }

    @Test(dependsOnMethods = {"solveFights"})
    public void populateLevel1() {
        treeTournamentHandler.generateNextFights(tournamentConverter.reverse(tournamentDTO), null);

        //Check level1 groups' teams.
        final List<Group> groups = groupController.getGroups(tournamentDTO, 1);
        Assert.assertEquals(groups.size(), 5);

        //Group 1 -> Team01 vs Team04
        Assert.assertEquals(groups.get(0).getTeams().get(0).getName(), "Team01");
        Assert.assertEquals(groups.get(0).getTeams().get(1).getName(), "Team14");

        //Group 2 -> Team09 vs Team02
        Assert.assertEquals(groups.get(1).getTeams().get(0).getName(), "Team06");
        Assert.assertEquals(groups.get(1).getTeams().get(1).getName(), "Team02");

        //Group 3 -> Team06 vs Team12
        Assert.assertEquals(groups.get(2).getTeams().get(0).getName(), "Team09");
        Assert.assertEquals(groups.get(2).getTeams().get(1).getName(), "Team04");

        //Group 4 -> Team013 vs Team08
        Assert.assertEquals(groups.get(3).getTeams().get(0).getName(), "Team10");
        Assert.assertEquals(groups.get(3).getTeams().get(1).getName(), "Team08");

        //Group 5 -> Team10 vs Team14
        Assert.assertEquals(groups.get(4).getTeams().get(0).getName(), "Team13");
        Assert.assertEquals(groups.get(4).getTeams().get(1).getName(), "Team12");
    }

    @Test(dependsOnMethods = {"populateLevel1"})
    public void solveLevel1() {
        final List<Group> groups = groupController.getGroups(tournamentDTO, 1);

        // First group has team1 as winner.
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(0).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(0)).get(0).getName(), "Team01");

        // Second group has team02 as winner.
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(1).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(1)).get(0).getName(), "Team02");

        // Third group has team04 as winner.
        groups.get(2).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(2).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(2).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(2)).get(0).getName(), "Team04");

        // Forth group has team10 as winner.
        groups.get(3).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(3).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(3).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(3)).get(0).getName(), "Team10");

        // Fifth group has team13 as winner.
        groups.get(4).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(4).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(4).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(4)).get(0).getName(), "Team13");
    }

    @Test(dependsOnMethods = {"solveLevel1"})
    public void populateLevel2() {
        treeTournamentHandler.generateNextFights(tournamentConverter.reverse(tournamentDTO), null);

        //Check level2 groups' teams.
        final List<Group> groups = groupController.getGroups(tournamentDTO, 2);
        Assert.assertEquals(groups.size(), 3);

        //Group 1 -> Team01
        Assert.assertEquals(groups.get(0).getTeams().get(0).getName(), "Team01");
        Assert.assertEquals(groups.get(0).getTeams().get(1).getName(), "Team02");

        //Group 2 -> Team01 vs Team16
        Assert.assertEquals(groups.get(1).getTeams().get(0).getName(), "Team04");
        Assert.assertEquals(groups.get(1).getTeams().get(1).getName(), "Team10");

        // Third group has only team 13.
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(2)).get(0).getName(), "Team13");
        Assert.assertEquals(groups.get(2).getTeams().size(), 1);
    }

    @Test(dependsOnMethods = {"populateLevel2"})
    public void solveLevel2() {
        final List<Group> groups = groupController.getGroups(tournamentDTO, 2);

        // First group has team02 as winner.
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(0).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(0)).get(0).getName(), "Team02");

        // Second group has team04 as winner.
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(1).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(1)).get(0).getName(), "Team04");

        // Third group has only Team10.
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(2)).get(0).getName(), "Team13");
    }

    @Test(dependsOnMethods = {"solveLevel2"})
    public void populateLevel3() {
        treeTournamentHandler.generateNextFights(tournamentConverter.reverse(tournamentDTO), null);

        //Check level2 groups' teams.
        final List<Group> groups = groupController.getGroups(tournamentDTO, 3);
        Assert.assertEquals(groups.size(), 2);

        //Group 1 -> Only Team02
        Assert.assertEquals(groups.get(0).getTeams().get(0).getName(), "Team02");
        Assert.assertEquals(groups.get(0).getTeams().size(), 1);

        //Group 2 -> Team04 vs Team13
        Assert.assertEquals(groups.get(1).getTeams().get(0).getName(), "Team04");
        Assert.assertEquals(groups.get(1).getTeams().get(1).getName(), "Team13");
    }

    @Test(dependsOnMethods = {"populateLevel3"})
    public void solveLevel3() {
        final List<Group> groups = groupController.getGroups(tournamentDTO, 3);

        // Second group has team13 as winner.
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(1).getFights().get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        groups.get(1).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(1)).get(0).getName(), "Team13");
    }

    @Test(dependsOnMethods = {"solveLevel3"})
    public void populateLevel4() {
        treeTournamentHandler.generateNextFights(tournamentConverter.reverse(tournamentDTO), null);

        //Check level2 groups' teams.
        final List<Group> groups = groupController.getGroups(tournamentDTO, 4);
        Assert.assertEquals(groups.size(), 1);

        //Group 1 -> Team04 vs Team13
        Assert.assertEquals(groups.get(0).getTeams().get(0).getName(), "Team02");
        Assert.assertEquals(groups.get(0).getTeams().get(1).getName(), "Team13");
    }

    @Test(dependsOnMethods = {"populateLevel4"})
    public void solveLevel4() {
        final List<Group> groups = groupController.getGroups(tournamentDTO, 4);

        // Final winner is Team02
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(0).getFights().get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        groups.get(0).getFights().forEach(fight -> {
            fight.getDuels().forEach(duel -> duel.setFinished(true));
            fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null);
        });
        Assert.assertEquals(rankingProvider.getTeamsRanking(groups.get(0)).get(0).getName(), "Team02");
    }

    @Test(dependsOnMethods = {"solveLevel4"})
    public void checkFinalRanking() {
        List<ScoreOfTeam> score = rankingProvider.getTeamsScoreRanking(tournamentConverter.reverse(tournamentDTO));
        Assert.assertEquals(score.get(0).getTeam().getName(), "Team02");
        Assert.assertEquals(score.get(1).getTeam().getName(), "Team13");
        Assert.assertEquals(score.get(2).getTeam().getName(), "Team04");
        //Same score but ordered by name.
        Assert.assertEquals(score.get(3).getTeam().getName(), "Team01");
        Assert.assertEquals(score.get(4).getTeam().getName(), "Team10");
        //Same score but ordered by name.
        Assert.assertEquals(score.get(5).getTeam().getName(), "Team06");
        Assert.assertEquals(score.get(6).getTeam().getName(), "Team09");
        //Same level, but less hits.
        Assert.assertEquals(score.get(7).getTeam().getName(), "Team08");
        Assert.assertEquals(score.get(8).getTeam().getName(), "Team14");
        //One hit
        Assert.assertEquals(score.get(9).getTeam().getName(), "Team12");
        //Same score but ordered by name.
        Assert.assertEquals(score.get(10).getTeam().getName(), "Team03");
        Assert.assertEquals(score.get(11).getTeam().getName(), "Team05");
        Assert.assertEquals(score.get(12).getTeam().getName(), "Team07");
        Assert.assertEquals(score.get(13).getTeam().getName(), "Team11");
        Assert.assertEquals(score.get(14).getTeam().getName(), "Team15");
        Assert.assertEquals(score.get(15).getTeam().getName(), "Team16");
    }

    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        groupController.delete(tournamentDTO);
        fightController.delete(tournamentDTO);
        duelController.delete(tournamentDTO);
        teamController.delete(tournamentDTO);
        roleController.delete(tournamentDTO);
        tournamentController.delete(tournamentDTO, null);
        participantController.deleteAll();
        clubController.delete(clubDTO, null);
        Assert.assertEquals(fightController.count(), 0);
        Assert.assertEquals(duelController.count(), 0);
    }
}
