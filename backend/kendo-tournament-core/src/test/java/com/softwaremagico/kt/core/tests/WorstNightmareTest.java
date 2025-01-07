package com.softwaremagico.kt.core.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

@SpringBootTest
@Test(groups = {"worstNightmareTest"})
public class WorstNightmareTest extends TournamentTestUtils {
    private static final int MEMBERS = 3;
    private static final int TEAMS = 3;
    private static final int REFEREES = 0;
    private static final int ORGANIZER = 0;
    private static final int VOLUNTEER = 0;
    private static final int PRESS = 0;

    private static final String TOURNAMENT1_NAME = "leagueTest1";
    private static final String TOURNAMENT2_NAME = "leagueTest2";

    private TournamentDTO tournament1DTO = null;
    private TournamentDTO tournament2DTO = null;

    @Autowired
    private FightController fightController;

    @Autowired
    private ParticipantProvider participantProvider;

    @BeforeClass
    public void prepareData() {
        addParticipants(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void createTournaments() {
        //Create Tournament
        tournament1DTO = addTournament(TOURNAMENT1_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
        tournament2DTO = addTournament(TOURNAMENT2_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
    }

    @BeforeClass(dependsOnMethods = "createTournaments")
    public void prepareTournament1() {
        List<FightDTO> fightDTOs = fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null);

        //P0 vs P3
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(0).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));

        //P1 vs P4
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));

        //P2 vs P5
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));

        //P6 vs P3
        fightDTOs.get(1).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(0), null));

        //P7 vs P4
        fightDTOs.get(1).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(1).getDuels().get(1).addCompetitor1Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(0), null));

        //P8 vs P5
        fightDTOs.get(1).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(1).getDuels().get(2).addCompetitor1Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(0), null));

        //P6 vs P0
        fightDTOs.get(2).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(0).addCompetitor1Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(0), null));

        //P7 vs P1
        fightDTOs.get(2).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(1).addCompetitor1Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(0), null));

        //P8 vs P2
        fightDTOs.get(2).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(2).addCompetitor1Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(0), null));
    }

    @BeforeClass(dependsOnMethods = "createTournaments")
    public void prepareTournament2() {
        List<FightDTO> fightDTOs = fightController.createFights(tournament2DTO.getId(), TeamsOrder.SORTED, 0, null);

        //P0 vs P3
        fightDTOs.get(0).getDuels().get(0).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));

        //P1 vs P4
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(1).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(1).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));

        //P2 vs P5
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(0).getDuels().get(2).addCompetitor1Score(Score.MEN);
        fightDTOs.get(0).getDuels().get(2).setFinished(true);
        fightDTOs.set(0, fightController.update(fightDTOs.get(0), null));

        //P6 vs P3
        fightDTOs.get(1).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(1).getDuels().get(0).addCompetitor1Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(0).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(0), null));

        //P7 vs P4
        fightDTOs.get(1).getDuels().get(1).addCompetitor2Score(Score.KOTE);
        fightDTOs.get(1).getDuels().get(1).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(0), null));

        //P8 vs P5
        fightDTOs.get(1).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(1).getDuels().get(2).addCompetitor1Score(Score.MEN);
        fightDTOs.get(1).getDuels().get(2).setFinished(true);
        fightDTOs.set(1, fightController.update(fightDTOs.get(0), null));

        //P6 vs P0
        fightDTOs.get(2).getDuels().get(0).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(0).addCompetitor1Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(0).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(0), null));

        //P7 vs P1
        fightDTOs.get(2).getDuels().get(1).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(1).addCompetitor1Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(1).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(0), null));

        //P8 vs P2
        fightDTOs.get(2).getDuels().get(2).addCompetitor1Score(Score.KOTE);
        fightDTOs.get(2).getDuels().get(2).addCompetitor1Score(Score.MEN);
        fightDTOs.get(2).getDuels().get(2).setFinished(true);
        fightDTOs.set(2, fightController.update(fightDTOs.get(0), null));
    }

    @Test
    public void checkYourWorstNightmare() {
        final Participant participant1 = participantProvider.getByIdCard("0001");
        final Participant participant3 = participantProvider.getByIdCard("0003");
        final Participant participant4 = participantProvider.getByIdCard("0004");
        final Participant participant6 = participantProvider.getByIdCard("0006");
        Assert.assertEquals(participantProvider.getYourWorstNightmare(participant3), participant6);
        Assert.assertEquals(participantProvider.getYourWorstNightmare(participant4), participant1);
    }

    @Test
    public void checkWorstNightmareOf() {
        final Participant participant1 = participantProvider.getByIdCard("0001");
        final Participant participant3 = participantProvider.getByIdCard("0003");
        final Participant participant4 = participantProvider.getByIdCard("0004");
        final Participant participant6 = participantProvider.getByIdCard("0006");
        Assert.assertEquals(participantProvider.getYouAreTheWorstNightmareOf(participant6), participant3);
        Assert.assertEquals(participantProvider.getYouAreTheWorstNightmareOf(participant1), participant4);
    }

    @AfterClass(alwaysRun = true)
    public void wipeOut() {
        super.wipeOut();
    }

}
