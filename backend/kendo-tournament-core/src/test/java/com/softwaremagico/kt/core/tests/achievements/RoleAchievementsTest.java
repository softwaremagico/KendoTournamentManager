package com.softwaremagico.kt.core.tests.achievements;

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

import com.softwaremagico.kt.core.TournamentTestUtils;
import com.softwaremagico.kt.core.controller.AchievementController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

@SpringBootTest
@Test(groups = {"roleAchievementTests"})
public class RoleAchievementsTest extends TournamentTestUtils {
    private static final int MEMBERS = 3;
    private static final int TEAMS = 4;

    private static final int REFEREES = 3;

    private static final int ORGANIZER = 2;

    private static final int VOLUNTEER = 2;

    private static final int PRESS = 1;

    private static final int BAMBOO_ACHIEVEMENT_PARTICIPANTS = 1;
    private static final int I_LOVE_THE_FLAGS_PARTICIPANTS_SOMETIMES = 1;
    private static final int LOOKS_GOOD_FROM_FAR_AWAY_SOMETIMES = 1;
    private static final int LOVE_SHARING_SOMETIMES = 1;

    private static final String TOURNAMENT1_NAME = "Tournament 1";
    private static final String TOURNAMENT2_NAME = "Tournament 2";
    private static final String TOURNAMENT3_NAME = "Tournament 3";
    private static final String TOURNAMENT4_NAME = "Tournament 4";
    private static final String TOURNAMENT5_NAME = "Tournament 5";
    private static final String TOURNAMENT6_NAME = "Tournament 6";
    private static final String TOURNAMENT7_NAME = "Tournament 7";
    private static final String TOURNAMENT8_NAME = "Tournament 8";
    private static final String TOURNAMENT9_NAME = "Tournament 9";
    private static final String TOURNAMENT10_NAME = "Tournament 10";

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private FightController fightController;

    @Autowired
    private AchievementController achievementController;

    private TournamentDTO tournament1DTO;
    private TournamentDTO tournament2DTO;
    private TournamentDTO tournament3DTO;
    private TournamentDTO tournament4DTO;
    private TournamentDTO tournament5DTO;
    private TournamentDTO tournament6DTO;
    private TournamentDTO tournament7DTO;
    private TournamentDTO tournament8DTO;
    private TournamentDTO tournament9DTO;
    private TournamentDTO tournament10DTO;

    private ParticipantDTO bambooAchievementParticipant;
    private ParticipantDTO iLoveTheFlagsParticipant;
    private ParticipantDTO iLoveTheFlagsParticipantSometimes;
    private ParticipantDTO looksGoodFromFarAway;
    private ParticipantDTO looksGoodFromFarAwaySometimes;
    private ParticipantDTO loveSharer;
    private ParticipantDTO loveSharerSometimes;


    @BeforeClass
    public void prepareData() {
        List<ParticipantDTO> extras = addParticipants(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS,
                BAMBOO_ACHIEVEMENT_PARTICIPANTS + I_LOVE_THE_FLAGS_PARTICIPANTS_SOMETIMES + LOOKS_GOOD_FROM_FAR_AWAY_SOMETIMES
                        + LOVE_SHARING_SOMETIMES);
        bambooAchievementParticipant = extras.get(0);
        iLoveTheFlagsParticipant = getReferees(MEMBERS, TEAMS, REFEREES).get(0);
        iLoveTheFlagsParticipantSometimes = extras.get(1);
        looksGoodFromFarAway = getOrganizers(MEMBERS, TEAMS, REFEREES, ORGANIZER).get(0);
        looksGoodFromFarAwaySometimes = extras.get(2);
        loveSharer = getVolunteers(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER).get(0);
        loveSharerSometimes = extras.get(3);
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament1() {
        //Create Tournament
        tournament1DTO = addTournament(TOURNAMENT1_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 10);
        roleController.create(new RoleDTO(tournament1DTO, bambooAchievementParticipant, RoleType.REFEREE), null);
        roleController.create(new RoleDTO(tournament1DTO, iLoveTheFlagsParticipantSometimes, RoleType.REFEREE), null);
        roleController.create(new RoleDTO(tournament1DTO, loveSharerSometimes, RoleType.VOLUNTEER), null);
        achievementController.generateAchievements(tournament1DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament1")
    public void prepareTournament2() {
        //Create Tournament
        tournament2DTO = addTournament(TOURNAMENT2_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 9);
        roleController.create(new RoleDTO(tournament2DTO, bambooAchievementParticipant, RoleType.COMPETITOR), null);
        roleController.create(new RoleDTO(tournament2DTO, iLoveTheFlagsParticipantSometimes, RoleType.REFEREE), null);
        achievementController.generateAchievements(tournament2DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament2")
    public void prepareTournament3() {
        tournament3DTO = addTournament(TOURNAMENT3_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 8);
        //Create Tournament
        roleController.create(new RoleDTO(tournament3DTO, bambooAchievementParticipant, RoleType.ORGANIZER), null);
        roleController.create(new RoleDTO(tournament3DTO, loveSharerSometimes, RoleType.VOLUNTEER), null);
        achievementController.generateAchievements(tournament3DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament3")
    public void prepareTournament4() {
        tournament4DTO = addTournament(TOURNAMENT4_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 7);
        //Create Tournament
        roleController.create(new RoleDTO(tournament4DTO, bambooAchievementParticipant, RoleType.VOLUNTEER), null);
        roleController.create(new RoleDTO(tournament4DTO, iLoveTheFlagsParticipantSometimes, RoleType.REFEREE), null);
        achievementController.generateAchievements(tournament4DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament4")
    public void prepareTournament5() {
        tournament5DTO = addTournament(TOURNAMENT5_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 6);
        //Create Tournament
        roleController.create(new RoleDTO(tournament5DTO, bambooAchievementParticipant, RoleType.PRESS), null);
        roleController.create(new RoleDTO(tournament5DTO, iLoveTheFlagsParticipantSometimes, RoleType.REFEREE), null);
        roleController.create(new RoleDTO(tournament5DTO, looksGoodFromFarAwaySometimes, RoleType.ORGANIZER), null);
        roleController.create(new RoleDTO(tournament5DTO, loveSharerSometimes, RoleType.VOLUNTEER), null);
        achievementController.generateAchievements(tournament5DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament5")
    public void prepareTournament6() {
        tournament6DTO = addTournament(TOURNAMENT6_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 5);
        //Create Tournament
        roleController.create(new RoleDTO(tournament6DTO, bambooAchievementParticipant, RoleType.COMPETITOR), null);
        roleController.create(new RoleDTO(tournament6DTO, iLoveTheFlagsParticipantSometimes, RoleType.REFEREE), null);
        roleController.create(new RoleDTO(tournament6DTO, looksGoodFromFarAwaySometimes, RoleType.ORGANIZER), null);
        achievementController.generateAchievements(tournament6DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament6")
    public void prepareTournament7() {
        tournament7DTO = addTournament(TOURNAMENT7_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 4);
        //Create Tournament
        roleController.create(new RoleDTO(tournament7DTO, bambooAchievementParticipant, RoleType.REFEREE), null);
        roleController.create(new RoleDTO(tournament7DTO, looksGoodFromFarAwaySometimes, RoleType.ORGANIZER), null);
        roleController.create(new RoleDTO(tournament7DTO, loveSharerSometimes, RoleType.VOLUNTEER), null);
        achievementController.generateAchievements(tournament7DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament7")
    public void prepareTournament8() {
        tournament8DTO = addTournament(TOURNAMENT8_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 3);
        //Create Tournament
        roleController.create(new RoleDTO(tournament8DTO, bambooAchievementParticipant, RoleType.ORGANIZER), null);
        roleController.create(new RoleDTO(tournament8DTO, iLoveTheFlagsParticipantSometimes, RoleType.REFEREE), null);
        roleController.create(new RoleDTO(tournament8DTO, looksGoodFromFarAwaySometimes, RoleType.ORGANIZER), null);
        achievementController.generateAchievements(tournament8DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament8")
    public void prepareTournament9() {
        tournament9DTO = addTournament(TOURNAMENT9_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 2);
        //Create Tournament
        roleController.create(new RoleDTO(tournament9DTO, bambooAchievementParticipant, RoleType.VOLUNTEER), null);
        roleController.create(new RoleDTO(tournament9DTO, iLoveTheFlagsParticipantSometimes, RoleType.REFEREE), null);
        roleController.create(new RoleDTO(tournament9DTO, loveSharerSometimes, RoleType.VOLUNTEER), null);
        achievementController.generateAchievements(tournament9DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament9")
    public void prepareTournament10() {
        tournament10DTO = addTournament(TOURNAMENT10_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 1);
        //Create Tournament
        roleController.create(new RoleDTO(tournament10DTO, bambooAchievementParticipant, RoleType.PRESS), null);
        roleController.create(new RoleDTO(tournament10DTO, iLoveTheFlagsParticipantSometimes, RoleType.REFEREE), null);
        roleController.create(new RoleDTO(tournament10DTO, looksGoodFromFarAwaySometimes, RoleType.ORGANIZER), null);
        achievementController.generateAchievements(tournament10DTO);
    }

    @Test
    public void checkBambooAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(bambooAchievementParticipant);
        List<AchievementDTO> flexibleAsBambooAchievements =
                achievementsDTOs.stream().filter(achievementDTO -> achievementDTO.getAchievementType() == AchievementType.FLEXIBLE_AS_BAMBOO)
                        .toList();
        Assert.assertEquals(flexibleAsBambooAchievements.size(), 4);
        flexibleAsBambooAchievements.forEach(achievementDTO -> Assert.assertEquals(achievementDTO.getParticipant(), bambooAchievementParticipant));
        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.NORMAL).size(), 1);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.BRONZE).size(), 1);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.SILVER).size(), 1);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.GOLD).size(), 1);

        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.NORMAL).get(0).getTournament(), tournament2DTO);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.BRONZE).get(0).getTournament(), tournament3DTO);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.SILVER).get(0).getTournament(), tournament4DTO);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.FLEXIBLE_AS_BAMBOO, AchievementGrade.GOLD).get(0).getTournament(), tournament5DTO);
    }

    @Test
    public void checkILoveTheFlagsAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(iLoveTheFlagsParticipant);
        List<AchievementDTO> iLoveTheFlagsAchievements =
                achievementsDTOs.stream().filter(achievementDTO -> achievementDTO.getAchievementType() == AchievementType.I_LOVE_THE_FLAGS)
                        .toList();
        iLoveTheFlagsAchievements.forEach(achievementDTO -> Assert.assertEquals(achievementDTO.getParticipant(), iLoveTheFlagsParticipant));
        Assert.assertEquals(iLoveTheFlagsAchievements.size(), 14);
        Assert.assertEquals(iLoveTheFlagsAchievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.NORMAL)
                .toList().size(), 10);
        Assert.assertEquals(iLoveTheFlagsAchievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.BRONZE)
                .toList().size(), 2);
        Assert.assertEquals(iLoveTheFlagsAchievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.SILVER)
                .toList().size(), 1);
        Assert.assertEquals(iLoveTheFlagsAchievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.GOLD)
                .toList().size(), 1);
        Assert.assertEquals(iLoveTheFlagsAchievements.get(0).getParticipant(), iLoveTheFlagsParticipant);
    }

    @Test
    public void checkILoveTheFlagsSometimesAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(iLoveTheFlagsParticipantSometimes);
        List<AchievementDTO> iLoveTheFlagsAchievements =
                achievementsDTOs.stream().filter(achievementDTO -> achievementDTO.getAchievementType() == AchievementType.I_LOVE_THE_FLAGS)
                        .toList();
        iLoveTheFlagsAchievements.forEach(achievementDTO -> Assert.assertEquals(achievementDTO.getParticipant(), iLoveTheFlagsParticipantSometimes));
        Assert.assertEquals(iLoveTheFlagsAchievements.size(), 10);
        Assert.assertEquals(iLoveTheFlagsAchievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.NORMAL)
                .toList().size(), 8);
        Assert.assertEquals(iLoveTheFlagsAchievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.BRONZE)
                .toList().size(), 2);
        Assert.assertEquals(iLoveTheFlagsAchievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.SILVER)
                .toList().size(), 0);
        Assert.assertEquals(iLoveTheFlagsAchievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.GOLD)
                .toList().size(), 0);
        Assert.assertEquals(iLoveTheFlagsAchievements.get(0).getParticipant(), iLoveTheFlagsParticipantSometimes);
    }

    @Test
    public void checkLooksGoodFromFarAwayAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(looksGoodFromFarAway);
        List<AchievementDTO> achievements =
                achievementsDTOs.stream().filter(achievementDTO -> achievementDTO.getAchievementType() == AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT)
                        .toList();
        achievements.forEach(achievementDTO -> Assert.assertEquals(achievementDTO.getParticipant(), looksGoodFromFarAway));
        Assert.assertEquals(achievements.size(), 14);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.NORMAL)
                .toList().size(), 10);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.BRONZE)
                .toList().size(), 2);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.SILVER)
                .toList().size(), 1);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.GOLD)
                .toList().size(), 1);
        Assert.assertEquals(achievements.get(0).getParticipant(), looksGoodFromFarAway);
    }

    public void checkLooksGoodFromFarAwaySometimesAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(looksGoodFromFarAwaySometimes);
        List<AchievementDTO> achievements =
                achievementsDTOs.stream().filter(achievementDTO -> achievementDTO.getAchievementType() == AchievementType.LOOKS_GOOD_FROM_FAR_AWAY_BUT)
                        .toList();
        achievements.forEach(achievementDTO -> Assert.assertEquals(achievementDTO.getParticipant(), looksGoodFromFarAwaySometimes));
        Assert.assertEquals(achievements.size(), 6);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.NORMAL)
                .toList().size(), 5);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.BRONZE)
                .toList().size(), 1);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.SILVER)
                .toList().size(), 0);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.GOLD)
                .toList().size(), 0);
        Assert.assertEquals(achievements.get(0).getParticipant(), looksGoodFromFarAwaySometimes);
    }

    @Test
    public void checksLoveSharingAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(loveSharer);
        List<AchievementDTO> achievements =
                achievementsDTOs.stream().filter(achievementDTO -> achievementDTO.getAchievementType() == AchievementType.LOVE_SHARING)
                        .toList();
        achievements.forEach(achievementDTO -> Assert.assertEquals(achievementDTO.getParticipant(), loveSharer));
        Assert.assertEquals(achievements.size(), 14);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.NORMAL)
                .toList().size(), 10);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.BRONZE)
                .toList().size(), 2);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.SILVER)
                .toList().size(), 1);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.GOLD)
                .toList().size(), 1);
        Assert.assertEquals(achievements.get(0).getParticipant(), loveSharer);
    }

    @Test
    public void checksLoveSharingSometimesAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(loveSharerSometimes);
        List<AchievementDTO> achievements =
                achievementsDTOs.stream().filter(achievementDTO -> achievementDTO.getAchievementType() == AchievementType.LOVE_SHARING)
                        .toList();
        achievements.forEach(achievementDTO -> Assert.assertEquals(achievementDTO.getParticipant(), loveSharerSometimes));
        Assert.assertEquals(achievements.size(), 5);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.NORMAL)
                .toList().size(), 5);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.BRONZE)
                .toList().size(), 0);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.SILVER)
                .toList().size(), 0);
        Assert.assertEquals(achievements.stream().filter(achievementDTO -> achievementDTO.getAchievementGrade() == AchievementGrade.GOLD)
                .toList().size(), 0);
        Assert.assertEquals(achievements.get(0).getParticipant(), loveSharerSometimes);
    }

    @Test
    public void checkSweatyTenuguiAchievement() {
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.SWEATY_TENUGUI).size(), MEMBERS * TEAMS);
        Assert.assertEquals(achievementController.getAchievements(tournament2DTO, AchievementType.SWEATY_TENUGUI).size(), 1);
        Assert.assertEquals(achievementController.getAchievements(tournament3DTO, AchievementType.SWEATY_TENUGUI).size(), 0);
    }

    @Test
    public void searchLastTournaments() {
        List<TournamentDTO> tournamentDTOS = tournamentController.getPreviousTo(tournament3DTO, 1);
        Assert.assertEquals(tournamentDTOS.size(), 1);
        Assert.assertTrue(tournamentDTOS.stream().map(TournamentDTO::getId).toList().contains(tournament2DTO.getId()));

        tournamentDTOS = tournamentController.getPreviousTo(tournament3DTO, 2);
        Assert.assertEquals(tournamentDTOS.size(), 2);
        Assert.assertTrue(tournamentDTOS.stream().map(TournamentDTO::getId).toList().contains(tournament2DTO.getId()));
        Assert.assertTrue(tournamentDTOS.stream().map(TournamentDTO::getId).toList().contains(tournament1DTO.getId()));

        tournamentDTOS = tournamentController.getPreviousTo(tournament3DTO, 3);
        Assert.assertEquals(tournamentDTOS.size(), 2);
        Assert.assertTrue(tournamentDTOS.stream().map(TournamentDTO::getId).toList().contains(tournament2DTO.getId()));
        Assert.assertTrue(tournamentDTOS.stream().map(TournamentDTO::getId).toList().contains(tournament1DTO.getId()));
    }

    @AfterClass
    public void wipeOut() {
        super.wipeOut();
    }
}
