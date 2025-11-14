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

import com.softwaremagico.kt.core.controller.AchievementController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TeamConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.exceptions.InvalidChallengeDistanceException;
import com.softwaremagico.kt.core.exceptions.InvalidFightException;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
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
import java.util.Objects;

@SpringBootTest
@Test(groups = {"senbatsuTest"})
public class SenbatsuTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 1;
    private static final int TEAMS = 6;
    private static final String TOURNAMENT_NAME = "senbatsuTest";
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
    private FightController fightController;

    @Autowired
    private GroupProvider groupProvider;

    @Autowired
    private ClubProvider clubProvider;

    @Autowired
    private DuelProvider duelProvider;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private SenbatsuTournamentHandler senbatsuTournamentHandler;

    @Autowired
    private TeamConverter teamConverter;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private AchievementController achievementController;

    @Autowired
    private FightConverter fightConverter;

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
        Tournament newTournament = new Tournament(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.SENBATSU, null);
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

    @Test(dependsOnMethods = {"addTeams"}, expectedExceptions = InvalidChallengeDistanceException.class)
    public void createWrongFight() {
        List<Team> teams = senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null);

        //First team challenge fourth one.
        FightDTO fight = new FightDTO(tournamentConverter.convert(new TournamentConverterRequest(tournament)),
                teamConverter.convert(new TeamConverterRequest(teams.get(0))),
                teamConverter.convert(new TeamConverterRequest(teams.get(4))), 0, 0);
        Assert.assertEquals(fight.getTeam1().getName(), "Team01");
        Assert.assertEquals(fight.getTeam2().getName(), "Team05");
        fightController.create(fight, null, null);
    }

    @Test(dependsOnMethods = {"addTeams"}, expectedExceptions = InvalidFightException.class)
    public void createWrongChallengerFight() {
        List<Team> teams = senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null);

        //Second team challenge fourth one.
        FightDTO fight = new FightDTO(tournamentConverter.convert(new TournamentConverterRequest(tournament)),
                teamConverter.convert(new TeamConverterRequest(teams.get(1))),
                teamConverter.convert(new TeamConverterRequest(teams.get(4))), 0, 0);
        Assert.assertEquals(fight.getTeam1().getName(), "Team02");
        Assert.assertEquals(fight.getTeam2().getName(), "Team05");
        fightController.create(fight, null, null);
    }


    @Test(dependsOnMethods = {"createWrongFight", "createWrongChallengerFight"})
    public void createFights() {
        List<Team> teams = senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null);

        //First team challenge third one.
        FightDTO fight = new FightDTO(tournamentConverter.convert(new TournamentConverterRequest(tournament)),
                teamConverter.convert(new TeamConverterRequest(teams.get(0))),
                teamConverter.convert(new TeamConverterRequest(teams.get(2))), 0, 0);
        Assert.assertEquals(fight.getTeam1().getName(), "Team01");
        Assert.assertEquals(fight.getTeam2().getName(), "Team03");
        fight = fightController.create(fight, null, null);
        fightController.generateDuels(fight, null);

        //Wins Team01.
        fight.getDuels().get(0).addCompetitor1Score(Score.DO);
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        fightController.update(fight, null, null);

        //Save the fight at group.
        Group group = senbatsuTournamentHandler.getGroups(tournament, 0).get(0);
        group.getFights().add(fightConverter.reverse(fight));
        groupProvider.save(group);

        //Ensure team03 is eliminated
        teams = senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null);
        Assert.assertEquals(teams.size(), TEAMS - 1);
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team03")));
        Assert.assertTrue(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team01")));

        //Now Team02 vs Team01
        fight = new FightDTO(tournamentConverter.convert(new TournamentConverterRequest(tournament)),
                teamConverter.convert(new TeamConverterRequest(teams.get(0))),
                teamConverter.convert(new TeamConverterRequest(teams.get(1))), 0, 0);
        Assert.assertEquals(fight.getTeam1().getName(), "Team02");
        Assert.assertEquals(fight.getTeam2().getName(), "Team01");
        fight = fightController.create(fight, null, null);
        fightController.generateDuels(fight, null);

        //Wins Team01.
        fight.getDuels().get(0).addCompetitor2Score(Score.DO);
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        fightController.update(fight, null, null);

        //Save the fight at group.
        group = senbatsuTournamentHandler.getGroups(tournament, 0).get(0);
        group.getFights().add(fightConverter.reverse(fight));
        groupProvider.save(group);

        //Ensure Team02 and Team03 are eliminated
        teams = senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null);
        Assert.assertEquals(teams.size(), TEAMS - 2);
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team03")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team02")));
        Assert.assertTrue(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team01")));

        //Now Team01 again. Agains Team04
        fight = new FightDTO(tournamentConverter.convert(new TournamentConverterRequest(tournament)),
                teamConverter.convert(new TeamConverterRequest(teams.get(0))),
                teamConverter.convert(new TeamConverterRequest(teams.get(1))), 0, 0);
        Assert.assertEquals(fight.getTeam1().getName(), "Team01");
        Assert.assertEquals(fight.getTeam2().getName(), "Team04");
        fight = fightController.create(fight, null, null);
        fightController.generateDuels(fight, null);

        //Wins Team01.
        fight.getDuels().get(0).addCompetitor1Score(Score.DO);
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        fightController.update(fight, null, null);

        //Save the fight at group.
        group = senbatsuTournamentHandler.getGroups(tournament, 0).get(0);
        group.getFights().add(fightConverter.reverse(fight));
        groupProvider.save(group);

        //Ensure Team04, Team02 and Team03 are eliminated
        teams = senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null);
        Assert.assertEquals(teams.size(), TEAMS - 3);
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team03")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team02")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team04")));
        Assert.assertTrue(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team01")));

        //Now Team01 again, but vs. Team06.
        fight = new FightDTO(tournamentConverter.convert(new TournamentConverterRequest(tournament)),
                teamConverter.convert(new TeamConverterRequest(teams.get(0))),
                teamConverter.convert(new TeamConverterRequest(teams.get(2))), 0, 0);
        Assert.assertEquals(fight.getTeam1().getName(), "Team01");
        Assert.assertEquals(fight.getTeam2().getName(), "Team06");
        fight = fightController.create(fight, null, null);
        fightController.generateDuels(fight, null);

        //Wins Team01.
        fight.getDuels().get(0).addCompetitor1Score(Score.DO);
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        fightController.update(fight, null, null);

        //Save the fight at group.
        group = senbatsuTournamentHandler.getGroups(tournament, 0).get(0);
        group.getFights().add(fightConverter.reverse(fight));
        groupProvider.save(group);

        //Ensure Team06, Team04, Team02 and Team03 are eliminated
        teams = senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null);
        Assert.assertEquals(teams.size(), TEAMS - 4);
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team03")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team02")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team04")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team06")));
        Assert.assertTrue(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team01")));

        //Now Team05 vs Team01
        fight = new FightDTO(tournamentConverter.convert(new TournamentConverterRequest(tournament)),
                teamConverter.convert(new TeamConverterRequest(teams.get(0))),
                teamConverter.convert(new TeamConverterRequest(teams.get(1))), 0, 0);
        Assert.assertEquals(fight.getTeam1().getName(), "Team05");
        Assert.assertEquals(fight.getTeam2().getName(), "Team01");
        fight = fightController.create(fight, null, null);
        fightController.generateDuels(fight, null);

        //Wins Team01.
        fight.getDuels().get(0).addCompetitor2Score(Score.DO);
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        fightController.update(fight, null, null);

        //Save the fight at group.
        group = senbatsuTournamentHandler.getGroups(tournament, 0).get(0);
        group.getFights().add(fightConverter.reverse(fight));
        groupProvider.save(group);

        //Ensure Team05, Team06, Team04, Team02 and Team03 are eliminated
        teams = senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null);
        Assert.assertEquals(teams.size(), TEAMS - 5);
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team03")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team02")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team04")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team06")));
        Assert.assertFalse(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team05")));
        Assert.assertTrue(teams.stream().anyMatch(team -> Objects.equals(team.getName(), "Team01")));
    }


    @Test(dependsOnMethods = {"createFights"})
    public void achievementsAreGranted() {
        final TournamentDTO tournamentDTO = tournamentConverter.convert(new TournamentConverterRequest(tournament));
        achievementController.generateAchievements(tournamentDTO);

        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournamentDTO, AchievementType.CLIMB_THE_LADDER);
        Assert.assertEquals(achievementsDTOs.size(), MEMBERS);

        Assert.assertEquals(achievementsDTOs.get(0).getParticipant().getLastname(), "Lastname0"); //P4 -> Lastname 3
        Assert.assertEquals(achievementsDTOs.get(0).getAchievementGrade(), AchievementGrade.SILVER);
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
