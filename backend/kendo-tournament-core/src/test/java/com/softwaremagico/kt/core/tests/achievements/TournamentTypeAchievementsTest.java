package com.softwaremagico.kt.core.tests.achievements;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 Softwaremagico
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
import com.softwaremagico.kt.core.tournaments.KingOfTheMountainHandler;
import com.softwaremagico.kt.persistence.values.AchievementType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Test(groups = {"tournamentTypeAchievementTests"})
public class TournamentTypeAchievementsTest extends TournamentTestUtils {
    private static final int MEMBERS = 3;
    private static final int TEAMS = 4;

    private static final int REFEREES = 0;

    private static final int ORGANIZER = 0;

    private static final int VOLUNTEER = 0;

    private static final int PRESS = 0;

    private static final String TOURNAMENT1_NAME = "Tournament 1";

    private static final String TOURNAMENT2_NAME = "Tournament 2";

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private FightController fightController;

    @Autowired
    private AchievementController achievementController;

    @Autowired
    private KingOfTheMountainHandler kingOfTheMountainHandler;

    private TournamentDTO tournament1DTO;
    private TournamentDTO tournament2DTO;

    private ParticipantDTO masterTheLoop;
    private ParticipantDTO theKing;


    @BeforeClass
    public void prepareData() {
        addParticipants(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament1() {
        //Create Tournament
        tournament1DTO = addTournament(TOURNAMENT1_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TournamentType.LOOP, 2);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //Winner is Participant2
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1ScoreTime(11);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));
        masterTheLoop = fightDTOs.get(0).getDuels().get(2).getCompetitor1();

        fightDTOs.get(1).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(1).getDuels().get(0).addCompetitor1ScoreTime(11);
        fightDTOs.get(1).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(1), null, null));

        fightDTOs.get(2).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(2).getDuels().get(0).addCompetitor1ScoreTime(11);
        fightDTOs.get(2).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(2), null, null));


        achievementController.generateAchievements(tournament1DTO);
    }

    @BeforeClass(dependsOnMethods = "prepareTournament1")
    public void prepareTournament2() {
        //Create Tournament
        tournament2DTO = addTournament(TOURNAMENT2_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, TournamentType.KING_OF_THE_MOUNTAIN, 1);
        List<FightDTO> fightDTOs = new ArrayList<>(fightController.createFights(tournament2DTO.getId(), TeamsOrder.SORTED, 0, null, null));

        //Winner is Participant2
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1ScoreTime(11);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.DO);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null, null));
        theKing = fightDTOs.get(0).getDuels().get(2).getCompetitor1();

        achievementController.generateAchievements(tournament2DTO);
    }

    @Test
    public void checkMasterTheLoopAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(AchievementType.MASTER_THE_LOOP);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), masterTheLoop);
    }

    @Test
    public void checkTheKingAchievement() {
        List<AchievementDTO> achievementsDTOs = achievementController.getAchievements(AchievementType.THE_KING);
        Assert.assertEquals(achievementsDTOs.size(), 1);
        Assert.assertEquals(achievementsDTOs.get(0).getParticipant(), theKing);
    }

    @Override
    @AfterClass(alwaysRun = true)
    public void wipeOut() {
        super.wipeOut();
    }
}
