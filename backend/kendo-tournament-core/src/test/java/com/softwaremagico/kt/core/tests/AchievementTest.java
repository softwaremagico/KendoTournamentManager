package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.controller.*;
import com.softwaremagico.kt.core.controller.models.*;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SpringBootTest
@Test(groups = {"achievementTests"})
public class AchievementTest extends AbstractTransactionalTestNGSpringContextTests {

    private static final String CLUB_NAME = "ClubName";
    private static final String CLUB_COUNTRY = "ClubCountry";
    private static final String CLUB_CITY = "ClubCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 4;

    private static final int REFEREES = 3;

    private static final int ORGANIZER = 2;

    private static final int VOLUNTEER = 2;

    private static final int BAMBOO_ACHIEVEMENT_PARTICIPANTS = 1;

    private static final String TOURNAMENT1_NAME = "Tournament 1";

    private static final String TOURNAMENT2_NAME = "Tournament 2";

    private static final String TOURNAMENT3_NAME = "Tournament 3";

    @Autowired
    private ClubController clubController;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private GroupController groupController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private FightController fightController;

    @Autowired
    private AchievementController achievementController;

    private List<ParticipantDTO> participantsDTOs;

    private TournamentDTO tournament1DTO;
    private TournamentDTO tournament2DTO;
    private TournamentDTO tournament3DTO;

    private ParticipantDTO bambooAchievementParticipant;

    private ParticipantDTO woodCutter;

    private ParticipantDTO noWoodCutter1;

    private ParticipantDTO noWoodCutter2;

    private ParticipantDTO boneBreaker;

    private ParticipantDTO billyTheKid;

    private void generateRoles(TournamentDTO tournamentDTO) {
        //Add Competitors Roles
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(i), RoleType.COMPETITOR), null);
        }

        //Add Referee Roles
        for (int i = 0; i < REFEREES; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + i), RoleType.REFEREE), null);
        }

        //Add Organizer Roles
        for (int i = 0; i < ORGANIZER; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + REFEREES + i), RoleType.ORGANIZER), null);
        }

        //Add Volunteer Roles
        for (int i = 0; i < VOLUNTEER; i++) {
            roleController.create(new RoleDTO(tournamentDTO, participantsDTOs.get(MEMBERS * TEAMS + REFEREES + ORGANIZER + i), RoleType.VOLUNTEER), null);
        }
    }

    private void addTeams(TournamentDTO tournamentDTO) {
        List<RoleDTO> competitorsRolesDTO = roleController.get(tournamentDTO, RoleType.COMPETITOR);

        int teamIndex = 0;
        TeamDTO teamDTO = null;
        int teamMember = 0;

        final GroupDTO groupDTO = groupController.get(tournamentDTO).get(0);

        for (RoleDTO competitorRoleDTO : competitorsRolesDTO) {
            // Create a new team.
            if (teamDTO == null) {
                teamIndex++;
                teamDTO = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
                teamMember = 0;
            }

            // Add member.
            teamDTO.addMember(competitorRoleDTO.getParticipant());
            teamDTO = teamController.update(teamDTO, null);

            if (teamMember == 0) {
                groupController.addTeams(groupDTO.getId(), Collections.singletonList(teamDTO), null);
            }

            teamMember++;

            // Team filled up, create a new team.
            if (teamMember >= MEMBERS) {
                teamDTO = null;
            }
        }
    }

    @BeforeClass
    public void prepareData() {
        //Add club
        final ClubDTO clubDTO = clubController.create(CLUB_NAME, CLUB_COUNTRY, CLUB_CITY, null);

        //Add participants
        participantsDTOs = new ArrayList<>();
        for (int i = 0; i < MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER + BAMBOO_ACHIEVEMENT_PARTICIPANTS; i++) {
            participantsDTOs.add(participantController.create(new ParticipantDTO(String.format("0000%s", i), String.format("name%s", i),
                    String.format("lastname%s", i), clubDTO), null));
            if (i == MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER + BAMBOO_ACHIEVEMENT_PARTICIPANTS - 1) {
                bambooAchievementParticipant = participantsDTOs.get(i);
            }
        }
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament1() {
        //Create Tournament
        tournament1DTO = tournamentController.create(new TournamentDTO(TOURNAMENT1_NAME, 1, MEMBERS, TournamentType.LEAGUE), null);
        tournament1DTO.setCreatedAt(LocalDateTime.now().minusMinutes(2));
        tournamentController.update(tournament1DTO, null);
        generateRoles(tournament1DTO);
        roleController.create(new RoleDTO(tournament1DTO, bambooAchievementParticipant, RoleType.REFEREE), null);
        addTeams(tournament1DTO);
        List<FightDTO> fightDTOs = fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null);

        //Woodcutter
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1ScoreTime(11);
        fightDTOs.get(0).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));
        woodCutter = fightDTOs.get(0).getDuels().get(0).getCompetitor1();

        //No Woodcutter
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor2Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));
        noWoodCutter1 = fightDTOs.get(0).getDuels().get(1).getCompetitor1();
        noWoodCutter2 = fightDTOs.get(0).getDuels().get(1).getCompetitor2();

        //BoneBreaker
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.HANSOKU);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.HANSOKU);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));
        boneBreaker = fightDTOs.get(0).getDuels().get(2).getCompetitor2();

        //BillyTheKid
        fightDTOs.get(1).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(1).getDuels().get(0).addCompetitor1ScoreTime(6);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null));
        billyTheKid = fightDTOs.get(1).getDuels().get(0).getCompetitor1();

        achievementController.generateAchievements(tournament1DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament1")
    public void prepareTournament2() {
        //Create Tournament
        tournament2DTO = tournamentController.create(new TournamentDTO(TOURNAMENT2_NAME, 1, MEMBERS, TournamentType.LEAGUE), null);
        tournament2DTO.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        tournamentController.update(tournament2DTO, null);
        generateRoles(tournament2DTO);
        roleController.create(new RoleDTO(tournament2DTO, bambooAchievementParticipant, RoleType.COMPETITOR), null);
        addTeams(tournament2DTO);
        fightController.createFights(tournament2DTO.getId(), TeamsOrder.SORTED, 0, null);
        achievementController.generateAchievements(tournament2DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament2")
    public void prepareTournament3() {
        //Create Tournament
        tournament3DTO = tournamentController.create(new TournamentDTO(TOURNAMENT3_NAME, 1, MEMBERS, TournamentType.LEAGUE), null);
        tournament3DTO.setCreatedAt(LocalDateTime.now());
        tournamentController.update(tournament3DTO, null);
        generateRoles(tournament3DTO);
        roleController.create(new RoleDTO(tournament3DTO, bambooAchievementParticipant, RoleType.ORGANIZER), null);
        addTeams(tournament3DTO);
        fightController.createFights(tournament3DTO.getId(), TeamsOrder.SORTED, 0, null);
        achievementController.generateAchievements(tournament3DTO);
    }

    @Test
    public void checkBambooAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(bambooAchievementParticipant);
        //Flexible  + Sweaty Tenugui
        Assert.assertEquals(achievementsDTOs.size(), 2);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), bambooAchievementParticipant);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO).size(), 1);
    }

    @Test
    public void checkWoodCutterAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.WOODCUTTER);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), woodCutter);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.WOODCUTTER).size(), 1);
    }

    @Test
    public void checkNoWoodCutter() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.WOODCUTTER);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        // noWoodCutter1 has 'D' and 'M'.
        Assert.assertNotEquals(achievementsDTOs.get(0).getParticipant(), noWoodCutter1);
        // noWoodCutter2 has only one 'D'.
        Assert.assertNotEquals(achievementsDTOs.get(0).getParticipant(), noWoodCutter2);
    }

    @Test
    public void checkBoneBreakerAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.BONE_BREAKER);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), boneBreaker);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.BONE_BREAKER).size(), 1);
    }

    @Test
    public void checkBillyTheKidAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.BILLY_THE_KID);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), billyTheKid);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.BILLY_THE_KID).size(), 1);
    }

    @Test
    public void checkSweatyTenuguiAchievement() {
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.SWEATY_TENUGUI).size(), MEMBERS * TEAMS);
        Assert.assertEquals(achievementController.getAchievements(tournament2DTO, AchievementType.SWEATY_TENUGUI).size(), 1);
    }

    @Test
    public void checkTheWinnerAchievement() {
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.THE_WINNER).size(), 1);
        Assert.assertEquals(achievementController.getAchievements(tournament2DTO, AchievementType.THE_WINNER).size(), 1);
    }

    @Test
    public void checkTheWinnerTeamAchievement() {
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.THE_WINNER_TEAM).size(), MEMBERS);
        Assert.assertEquals(achievementController.getAchievements(tournament2DTO, AchievementType.THE_WINNER_TEAM).size(), MEMBERS);
    }

    @Test
    public void searchLastTournaments() {
        List<TournamentDTO> tournamentDTOS = tournamentController.getPreviousTo(tournament3DTO, 1);
        Assert.assertEquals(tournamentDTOS.size(), 1);
        Assert.assertTrue(tournamentDTOS.contains(tournament2DTO));

        tournamentDTOS = tournamentController.getPreviousTo(tournament3DTO, 2);
        Assert.assertEquals(tournamentDTOS.size(), 2);
        Assert.assertTrue(tournamentDTOS.contains(tournament2DTO));
        Assert.assertTrue(tournamentDTOS.contains(tournament1DTO));

        tournamentDTOS = tournamentController.getPreviousTo(tournament3DTO, 3);
        Assert.assertEquals(tournamentDTOS.size(), 2);
        Assert.assertTrue(tournamentDTOS.contains(tournament2DTO));
        Assert.assertTrue(tournamentDTOS.contains(tournament1DTO));
    }

    @AfterClass
    public void deleteTournament() {
        deleteFromTables("competitor_1_score", "competitor_2_score", "competitor_1_score_time", "competitor_2_score_time",
                "achievements", "duels_by_fight");
        deleteFromTables("duels", "fights_by_group");
        deleteFromTables("fights", "members_of_team", "teams_by_group");
        deleteFromTables("teams");
        deleteFromTables("tournament_groups", "roles");
        deleteFromTables("tournaments");
        deleteFromTables("participant_image");
        deleteFromTables("participants");
        deleteFromTables("clubs");
    }
}
