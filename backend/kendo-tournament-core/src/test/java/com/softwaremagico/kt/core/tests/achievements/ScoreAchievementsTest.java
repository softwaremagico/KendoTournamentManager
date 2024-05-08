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
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.TournamentExtraPropertyProvider;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Test(groups = {"scoreAchievementTests"})
public class ScoreAchievementsTest extends TournamentTestUtils {
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

    private ParticipantDTO woodCutter;

    private ParticipantDTO noWoodCutter1;

    private ParticipantDTO noWoodCutter2;

    private ParticipantDTO boneBreaker;

    private ParticipantDTO billyTheKid;

    private ParticipantDTO terminator;

    private ParticipantDTO juggernaut;

    private ParticipantDTO theCastle;

    private ParticipantDTO entrenched;


    @BeforeClass
    public void prepareData() {
        addParticipants(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament1() {
        //Create Tournament
        tournament1DTO = addTournament(TOURNAMENT1_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 3);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null));

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
        fightDTOs.get(1).getDuels().get(0).addCompetitor1ScoreTime(4);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null));
        billyTheKid = fightDTOs.get(1).getDuels().get(0).getCompetitor1();

        //Woodcutter
        fightDTOs.get(3).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(3).getDuels().get(0).addCompetitor1ScoreTime(11);
        fightDTOs.get(3).getDuels().get(0).setFinished(true);
        fightDTOs.set(3, fightController.update(fightDTOs.get(3), null));

        //No Woodcutter has a 'Men' here!
        fightDTOs.get(3).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(3).getDuels().get(1).addCompetitor1Score(Score.MEN);
        fightDTOs.get(3).getDuels().get(1).setFinished(true);
        fightDTOs.set(3, fightController.update(fightDTOs.get(3), null));


        //Woodcutter
        fightDTOs.get(4).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(4).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(4).getDuels().get(0).setFinished(true);
        fightDTOs.set(4, fightController.update(fightDTOs.get(4), null));

        //No Woodcutter and now Terminator!
        fightDTOs.get(4).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(4).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(4).getDuels().get(1).setFinished(true);
        fightDTOs.set(4, fightController.update(fightDTOs.get(4), null));
        terminator = fightDTOs.get(4).getDuels().get(1).getCompetitor1();

        achievementController.generateAchievements(tournament1DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament1")
    public void prepareTournament2() {
        //Create Tournament
        tournament2DTO = addTournament(TOURNAMENT2_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 2);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament2DTO.getId(), TeamsOrder.SORTED, 0, null));

        //Juggernaut
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1ScoreTime(11);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));
        juggernaut = fightDTOs.get(0).getDuels().get(2).getCompetitor1();


        //Juggernaut
        fightDTOs.get(3).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(3).getDuels().get(2).addCompetitor1Score(Score.MEN);
        fightDTOs.get(3).getDuels().get(2).addCompetitor1ScoreTime(11);
        fightDTOs.get(3).getDuels().get(2).setFinished(true);
        fightDTOs.set(3, fightController.update(fightDTOs.get(3), null));


        //Juggernaut
        fightDTOs.get(4).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(4).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(4).getDuels().get(2).setFinished(true);
        fightDTOs.set(4, fightController.update(fightDTOs.get(4), null));

        achievementController.generateAchievements(tournament2DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament2")
    public void prepareTournament3() {
        tournament3DTO = addTournament(TOURNAMENT3_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 1);
        //Create Tournament
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament3DTO.getId(), TeamsOrder.SORTED, 0, null));

        //The Castle
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1ScoreTime(35);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        theCastle = fightDTOs.get(0).getDuels().get(0).getCompetitor1();

        //Entrenched
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        entrenched = fightDTOs.get(0).getDuels().get(1).getCompetitor1();

        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1ScoreTime(35);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));

        fightDTOs.get(2).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).addCompetitor1ScoreTime(35);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        theCastle = fightDTOs.get(0).getDuels().get(0).getCompetitor1();

        fightDTOs.get(2).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).addCompetitor1ScoreTime(35);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);

        fightDTOs.get(2).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).addCompetitor1ScoreTime(35);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null));

        //I only want one entrenched.
        fightDTOs.get(5).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(5).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(5).getDuels().get(1).addCompetitor1ScoreTime(35);
        fightDTOs.get(5).getDuels().get(1).setFinished(true);
        fightDTOs.set(5, fightController.update(fightDTOs.get(5), null));

        achievementController.generateAchievements(tournament3DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament3")
    public void prepareTournament4() {
        tournament4DTO = addTournament(TOURNAMENT4_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 1);
        //Create Tournament
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament4DTO.getId(), TeamsOrder.SORTED, 0, null));

        //Team1 and Team2 no scores. Team1 has no scores neither against itself.

        //Team1 vs Team2
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));

        //Team3 vs Team2
        fightDTOs.get(1).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(1).getDuels().get(0).addCompetitor1ScoreTime(68);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null));

        //Team3 vs Team4
        fightDTOs.get(2).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(2).getDuels().get(0).addCompetitor1ScoreTime(68);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2ScoreTime(68);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null));

        fightDTOs.get(3).getDuels().get(0).setFinished(true);
        fightDTOs.get(3).getDuels().get(1).setFinished(true);
        fightDTOs.get(3).getDuels().get(2).setFinished(true);
        fightDTOs.set(3, fightController.update(fightDTOs.get(3), null));

        fightDTOs.get(4).getDuels().get(0).setFinished(true);
        fightDTOs.get(4).getDuels().get(1).setFinished(true);
        fightDTOs.get(4).getDuels().get(2).setFinished(true);
        fightDTOs.set(4, fightController.update(fightDTOs.get(4), null));

        fightDTOs.get(5).getDuels().get(0).setFinished(true);
        fightDTOs.get(5).getDuels().get(1).setFinished(true);
        fightDTOs.get(5).getDuels().get(2).setFinished(true);
        fightDTOs.set(5, fightController.update(fightDTOs.get(5), null));

        achievementController.generateAchievements(tournament4DTO);
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
    }

    @Test
    public void checkLethalWeaponAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.LETHAL_WEAPON);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), billyTheKid);
    }

    @Test
    public void checkTerminatorAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.TERMINATOR);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), terminator);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.TERMINATOR).size(), 1);
    }

    @Test
    public void checkJuggernautAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament2DTO, AchievementType.JUGGERNAUT);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), juggernaut);
        Assert.assertEquals(achievementController.getAchievements(AchievementType.JUGGERNAUT).size(), 1);
    }

    @Test
    public void checkJuggernautNotTerminatorAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(juggernaut);
        achievementsDTOs.forEach(achievementDTO -> Assert.assertNotSame(achievementDTO.getAchievementType(), AchievementType.TERMINATOR));
    }

    @Test
    public void checkJuggernautNotTheCastleAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(tournament2DTO, juggernaut);
        achievementsDTOs.forEach(achievementDTO -> Assert.assertNotSame(achievementDTO.getAchievementType(), AchievementType.THE_CASTLE));
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
    public void checkTheCastleAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament3DTO, AchievementType.THE_CASTLE, AchievementGrade.NORMAL);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), theCastle);
    }

    @Test
    public void checkEntrenchedAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament3DTO, AchievementType.ENTRENCHED, AchievementGrade.NORMAL);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), entrenched);
    }

    @Test
    public void checkEntrenchedNotTheCastleAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getParticipantAchievements(entrenched);
        achievementsDTOs.forEach(achievementDTO -> Assert.assertNotSame(achievementDTO.getAchievementType(), AchievementType.THE_CASTLE));
    }

    @Test
    public void tisButAScratchAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.NORMAL);
        //Only Participant2, Participant8 and Participant11 has not this one.
        Assert.assertEquals(achievementsDTOs.size(), MEMBERS * TEAMS - 3);

        //Participant2, Participant8
        achievementsDTOs = achievementController.getAchievements(tournament2DTO, AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.NORMAL);
        Assert.assertEquals(achievementsDTOs.size(), 2);

        //Participant11
        achievementsDTOs = achievementController.getAchievements(tournament3DTO, AchievementType.TIS_BUT_A_SCRATCH, AchievementGrade.NORMAL);
        Assert.assertEquals(achievementsDTOs.size(), 1);
    }

    @Test
    public void firstBloodAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.FIRST_BLOOD, AchievementGrade.NORMAL);
        //Only Participant0, Participant1, Participant2, Participant3, Participant4, Participant6
        Assert.assertEquals(achievementsDTOs.size(), 6);

        //Nobody new
        achievementsDTOs = achievementController.getAchievements(tournament2DTO, AchievementType.FIRST_BLOOD, AchievementGrade.NORMAL);
        Assert.assertEquals(achievementsDTOs.size(), 0);

        //Only Participant5, Participant7, Participant8, Participant9, Participant10, Participant11
        achievementsDTOs = achievementController.getAchievements(tournament3DTO, AchievementType.FIRST_BLOOD, AchievementGrade.NORMAL);
        Assert.assertEquals(achievementsDTOs.size(), 6);
    }

    @Test
    public void stormtrooperAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament4DTO, AchievementType.STORMTROOPER_SYNDROME, AchievementGrade.NORMAL);
        //Only Participant0, Participant1, Participant2, Participant3, Participant4, Participant5
        Assert.assertEquals(achievementsDTOs.size(), 6);
    }

    @AfterClass(alwaysRun = true)
    public void wipeOut() {
        super.wipeOut();
    }
}
