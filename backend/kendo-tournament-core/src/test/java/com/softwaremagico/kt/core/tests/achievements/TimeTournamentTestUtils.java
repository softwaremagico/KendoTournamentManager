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
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest
@Test(groups = {"darumaAchievementTests"})
public class TimeTournamentTestUtils extends TournamentTestUtils {
    private static final int MEMBERS = 3;
    private static final int TEAMS = 4;

    private static final int REFEREES = 3;

    private static final int ORGANIZER = 2;

    private static final int VOLUNTEER = 2;

    private static final int PRESS = 1;

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
    private static final String TOURNAMENT11_NAME = "Tournament 11";
    private static final String TOURNAMENT12_NAME = "Tournament 12";
    private static final String TOURNAMENT13_NAME = "Tournament 13";
    private static final String TOURNAMENT14_NAME = "Tournament 14";
    private static final String TOURNAMENT15_NAME = "Tournament 15";
    private static final String TOURNAMENT16_NAME = "Tournament 16";
    private static final String TOURNAMENT17_NAME = "Tournament 17";
    private static final String TOURNAMENT18_NAME = "Tournament 18";
    private static final String TOURNAMENT19_NAME = "Tournament 19";
    private static final String TOURNAMENT20_NAME = "Tournament 20";

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private FightController fightController;

    @Autowired
    private AchievementController achievementController;

    @Autowired
    private ParticipantController participantController;

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
    private TournamentDTO tournament11DTO;
    private TournamentDTO tournament20DTO;


    @BeforeClass
    public void prepareData() {
        addParticipants(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
        List<ParticipantDTO> participants = participantController.get();
        int years = participants.size() - 1;
        for (final ParticipantDTO participant : participants) {
            participant.setCreatedAt(LocalDateTime.now().minusYears(years).minusHours(1));
            years--;
        }
        participantController.updateAll(participants, null);
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament1() {
        //Create Tournament
        tournament1DTO = addTournament(TOURNAMENT1_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 20);
        achievementController.generateAchievements(tournament1DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament1")
    public void prepareTournament2() {
        //Create Tournament
        tournament2DTO = addTournament(TOURNAMENT2_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 19);
        achievementController.generateAchievements(tournament2DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament2")
    public void prepareTournament3() {
        tournament3DTO = addTournament(TOURNAMENT3_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 18);
        //Create Tournament
        achievementController.generateAchievements(tournament3DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament3")
    public void prepareTournament4() {
        tournament4DTO = addTournament(TOURNAMENT4_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 17);
        //Create Tournament
        achievementController.generateAchievements(tournament4DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament4")
    public void prepareTournament5() {
        tournament5DTO = addTournament(TOURNAMENT5_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 16);
        //Create Tournament
        achievementController.generateAchievements(tournament5DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament5")
    public void prepareTournament6() {
        tournament6DTO = addTournament(TOURNAMENT6_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 15);
        //Create Tournament
        achievementController.generateAchievements(tournament6DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament6")
    public void prepareTournament7() {
        tournament7DTO = addTournament(TOURNAMENT7_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 14);
        //Create Tournament
        achievementController.generateAchievements(tournament7DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament7")
    public void prepareTournament8() {
        tournament8DTO = addTournament(TOURNAMENT8_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 13);
        //Create Tournament
        achievementController.generateAchievements(tournament8DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament8")
    public void prepareTournament9() {
        tournament9DTO = addTournament(TOURNAMENT9_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 12);
        //Create Tournament
        achievementController.generateAchievements(tournament9DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament9")
    public void prepareTournament10() {
        tournament10DTO = addTournament(TOURNAMENT10_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 11);
        //Create Tournament
        achievementController.generateAchievements(tournament10DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament10")
    public void prepareTournament11() {
        tournament11DTO = addTournament(TOURNAMENT11_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 10);
        achievementController.generateAchievements(tournament11DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament11")
    public void prepareTournament12() {
        achievementController.generateAchievements(addTournament(TOURNAMENT12_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 9));
    }

    @BeforeClass(dependsOnMethods = "prepareTournament12")
    public void prepareTournament13() {
        achievementController.generateAchievements(addTournament(TOURNAMENT13_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 8));
    }

    @BeforeClass(dependsOnMethods = "prepareTournament13")
    public void prepareTournament14() {
        achievementController.generateAchievements(addTournament(TOURNAMENT14_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 7));
    }

    @BeforeClass(dependsOnMethods = "prepareTournament14")
    public void prepareTournament15() {
        achievementController.generateAchievements(addTournament(TOURNAMENT15_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 6));
    }

    @BeforeClass(dependsOnMethods = "prepareTournament15")
    public void prepareTournament16() {
        achievementController.generateAchievements(addTournament(TOURNAMENT16_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 5));
    }

    @BeforeClass(dependsOnMethods = "prepareTournament16")
    public void prepareTournament17() {
        achievementController.generateAchievements(addTournament(TOURNAMENT17_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 4));
    }

    @BeforeClass(dependsOnMethods = "prepareTournament17")
    public void prepareTournament18() {
        achievementController.generateAchievements(addTournament(TOURNAMENT18_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 3));
    }

    @BeforeClass(dependsOnMethods = "prepareTournament18")
    public void prepareTournament19() {
        achievementController.generateAchievements(addTournament(TOURNAMENT19_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 2));
    }

    @BeforeClass(dependsOnMethods = "prepareTournament19")
    public void prepareTournament20() {
        tournament20DTO = addTournament(TOURNAMENT20_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 1);
        achievementController.generateAchievements(tournament20DTO);
    }

    @Test
    public void checkDarumaAchievement() {
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.DARUMA).size(), 0);
        Assert.assertEquals(achievementController.getAchievements(tournament9DTO, AchievementType.DARUMA).size(), 0);
        Assert.assertEquals(achievementController.getAchievements(tournament10DTO, AchievementType.DARUMA).size(),
                MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER + PRESS);
        Assert.assertEquals(achievementController.getAchievements(tournament11DTO, AchievementType.DARUMA).size(), 0);
        Assert.assertEquals(achievementController.getAchievements(tournament20DTO, AchievementType.DARUMA, AchievementGrade.NORMAL).size(), 0);
        Assert.assertEquals(achievementController.getAchievements(tournament20DTO, AchievementType.DARUMA, AchievementGrade.BRONZE).size(),
                MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER + PRESS);
    }

    @Test
    public void checkNeverEndingStoryAchievement() {
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.NORMAL).size(),
                MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER + PRESS - 5);
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.BRONZE).size(),
                MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER + PRESS - 10);
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.SILVER).size(),
                MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER + PRESS - 15);
        Assert.assertEquals(achievementController.getAchievements(tournament1DTO, AchievementType.THE_NEVER_ENDING_STORY, AchievementGrade.GOLD).size(),
                MEMBERS * TEAMS + REFEREES + ORGANIZER + VOLUNTEER + PRESS - 20);
    }

    @AfterClass(alwaysRun = true)
    public void wipeOut() {
        super.wipeOut();
    }
}
