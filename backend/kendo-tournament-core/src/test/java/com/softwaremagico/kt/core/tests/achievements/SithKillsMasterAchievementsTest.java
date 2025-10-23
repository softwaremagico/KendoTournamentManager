package com.softwaremagico.kt.core.tests.achievements;

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

import com.softwaremagico.kt.core.TournamentTestUtils;
import com.softwaremagico.kt.core.controller.AchievementController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.models.AchievementDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.values.AchievementGrade;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Test(groups = {"sithKillsMasterAchievementsTest"})
public class SithKillsMasterAchievementsTest extends TournamentTestUtils {
    private static final int MEMBERS = 3;
    private static final int TEAMS = 3;
    private static final int REFEREES = 0;
    private static final int ORGANIZER = 0;
    private static final int VOLUNTEER = 0;
    private static final int PRESS = 0;

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

    private static final int TOURNAMENT1_DELAY = 20;
    private static final int TOURNAMENT2_DELAY = 19;
    private static final int TOURNAMENT3_DELAY = 18;
    private static final int TOURNAMENT4_DELAY = 17;
    private static final int TOURNAMENT5_DELAY = 16;
    private static final int TOURNAMENT6_DELAY = 15;
    private static final int TOURNAMENT7_DELAY = 14;
    private static final int TOURNAMENT8_DELAY = 13;
    private static final int TOURNAMENT9_DELAY = 12;
    private static final int TOURNAMENT10_DELAY = 11;
    private static final int TOURNAMENT11_DELAY = 10;

    @Autowired
    private DuelController duelController;

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
    private TournamentDTO tournament11DTO;


    @BeforeClass
    public void prepareData() {
        addParticipants(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament1() {
        //Create Tournament
        tournament1DTO = addTournament(TOURNAMENT1_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT1_DELAY);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //P1 vs P4 Win P1
        //P2 vs P5 Win P2
        //P3 vs P6 Win P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));


        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT1_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });


        achievementController.generateAchievements(tournament1DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament1")
    public void prepareTournament2() {
        //Create Tournament
        tournament2DTO = addTournament(TOURNAMENT2_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT2_DELAY);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament2DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //P1 vs P4 Win P1
        //P2 vs P5 Win P2
        //P3 vs P6 Win P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT2_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });


        achievementController.generateAchievements(tournament2DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament2")
    public void prepareTournament3() {
        //Create Tournament
        tournament3DTO = addTournament(TOURNAMENT3_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT3_DELAY);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament3DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //P1 vs P4 Win P1
        //P2 vs P5 Win P2
        //P3 vs P6 Win P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT3_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });


        achievementController.generateAchievements(tournament3DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament3")
    public void prepareTournament4() {
        tournament4DTO = addTournament(TOURNAMENT4_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT4_DELAY);
        //Create Tournament
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament4DTO.getId(), TeamsOrder.SORTED, 0, null, null));
        //P1 vs P4 Win P4  <---
        //P2 vs P5 Win P2
        //P3 vs P6 Win P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor2Score(Score.TSUKI);
        fightDTOs.get(0).getDuels().get(0).addCompetitor2Score(Score.TSUKI);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT4_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });

        achievementController.generateAchievements(tournament4DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament4")
    public void prepareTournament5() {
        //Create Tournament
        tournament5DTO = addTournament(TOURNAMENT5_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT5_DELAY);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament5DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //P1 vs P4 Win P1
        //P2 vs P5 Win P2
        //P3 vs P6 Win P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT5_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });


        achievementController.generateAchievements(tournament5DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament5")
    public void prepareTournament6() {
        tournament6DTO = addTournament(TOURNAMENT6_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT6_DELAY);
        //Create Tournament
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament6DTO.getId(), TeamsOrder.SORTED, 0, null, null));
        //P1 vs P4 Win P1
        //P2 vs P5 Win P5 <---
        //P3 vs P6 Win P6
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor2Score(Score.TSUKI);
        fightDTOs.get(0).getDuels().get(1).addCompetitor2Score(Score.TSUKI);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT6_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });

        achievementController.generateAchievements(tournament6DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament6")
    public void prepareTournament7() {
        //Create Tournament
        tournament7DTO = addTournament(TOURNAMENT7_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT7_DELAY);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament7DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //P1 vs P4 Win P1
        //P2 vs P5 Win P2
        //P3 vs P6 Win P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT7_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });


        achievementController.generateAchievements(tournament7DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament7")
    public void prepareTournament8() {
        tournament8DTO = addTournament(TOURNAMENT8_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT8_DELAY);
        //Create Tournament
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament8DTO.getId(), TeamsOrder.SORTED, 0, null, null));
        //P1 vs P4 Win P4
        //P2 vs P5 Win P2
        //P3 vs P6 Win P6 <--
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor2Score(Score.TSUKI);
        fightDTOs.get(0).getDuels().get(2).addCompetitor2Score(Score.TSUKI);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT8_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });

        achievementController.generateAchievements(tournament8DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament8")
    public void prepareTournament9() {
        //Create Tournament
        tournament9DTO = addTournament(TOURNAMENT9_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT9_DELAY);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament9DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //P1 vs P4 Win P1
        //P2 vs P5 Win P2
        //P3 vs P6 Win P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT9_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });


        achievementController.generateAchievements(tournament9DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament9")
    public void prepareTournament10() {
        //Create Tournament
        tournament10DTO = addTournament(TOURNAMENT10_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT10_DELAY);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament10DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //P1 vs P4 Win P1
        //P2 vs P5 Win P2
        //P3 vs P6 Win P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P4
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT10_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });


        achievementController.generateAchievements(tournament10DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament10")
    public void prepareTournament11() {
        tournament11DTO = addTournament(TOURNAMENT11_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TOURNAMENT11_DELAY);
        //Create Tournament
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament11DTO.getId(), TeamsOrder.SORTED, 0, null, null));
        //P1 vs P4 Win P4 <-- (Again!)
        //P2 vs P5 Win P5
        //P3 vs P6 Win P6
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.TSUKI);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.TSUKI);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));

        //P7 vs P4 Win P7 <--
        //P8 vs P5 Win P5
        //P9 vs P6 Win P6
        fightDTOs.get(1).getDuels().get(0).addCompetitor1Score(Score.TSUKI);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.get(1).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        //P7 vs P1 Win P1
        //P8 vs P2 Win P2
        //P9 vs P3 Win P3
        fightDTOs.get(2).getDuels().get(0).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.get(2).getDuels().get(1).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.get(2).getDuels().get(2).addCompetitor2Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));

        //Set Time
        fightDTOs.forEach(fightDTO -> {
            fightDTO.getDuels().forEach(duelDTO -> duelDTO.setCreatedAt(LocalDateTime.now().minusMinutes(TOURNAMENT11_DELAY)));
            duelController.updateAll(fightDTO.getDuels(), null, null);
        });

        achievementController.generateAchievements(tournament11DTO);
    }

    @Test
    public void checkSithKillsMasterAchievementsTest() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(tournament1DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 0);

        achievementsDTOs = achievementController.getAchievements(tournament2DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 0);

        achievementsDTOs = achievementController.getAchievements(tournament3DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 0);

        achievementsDTOs = achievementController.getAchievements(tournament4DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant().getLastname(), "Lastname3"); //P4 -> Lastname 3
        Assert.assertEquals(achievementsDTOs.get(0).getAchievementGrade(), AchievementGrade.NORMAL);

        achievementsDTOs = achievementController.getAchievements(tournament5DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 0);

        achievementsDTOs = achievementController.getAchievements(tournament6DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant().getLastname(), "Lastname4"); //P5 -> Lastname 4
        Assert.assertEquals(achievementsDTOs.get(0).getAchievementGrade(), AchievementGrade.BRONZE);

        achievementsDTOs = achievementController.getAchievements(tournament7DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 0);

        achievementsDTOs = achievementController.getAchievements(tournament8DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant().getLastname(), "Lastname5"); //P6 -> Lastname 5
        Assert.assertEquals(achievementsDTOs.get(0).getAchievementGrade(), AchievementGrade.SILVER);

        achievementsDTOs = achievementController.getAchievements(tournament9DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 0);

        achievementsDTOs = achievementController.getAchievements(tournament10DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        Assert.assertEquals(achievementsDTOs.size(), 0);

        achievementsDTOs = achievementController.getAchievements(tournament11DTO, AchievementType.SITH_APPRENTICES_ALWAYS_KILL_THEIR_MASTER);
        //P6 has defeated the master, P4 has lost, lost, win, lost, lost win. On Tournament6 win again, but is not the master as has been already defeated.
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant().getLastname(), "Lastname6"); //P7 -> Lastname 6
        Assert.assertEquals(achievementsDTOs.get(0).getAchievementGrade(), AchievementGrade.GOLD);
    }


    @AfterClass(alwaysRun = true)
    @Override
    public void wipeOut() {
        super.wipeOut();
    }
}
