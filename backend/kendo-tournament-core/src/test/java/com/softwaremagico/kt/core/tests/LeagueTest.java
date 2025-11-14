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

import com.softwaremagico.kt.core.TournamentTestUtils;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.persistence.values.LeagueFightsOrder;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

@SpringBootTest
@Test(groups = {"leagueTest"})
public class LeagueTest extends TournamentTestUtils {
    private static final int MEMBERS = 3;
    private static final int TEAMS = 4;
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
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    @BeforeClass
    public void prepareData() {
        addParticipants(MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament1() {
        //Create Tournament
        tournament1DTO = addTournament(TOURNAMENT1_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
        tournamentExtraPropertyController.create(new TournamentExtraPropertyDTO(tournament1DTO,
                TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION, LeagueFightsOrder.FIFO.name()), null, null);
    }

    @BeforeClass(dependsOnMethods = "prepareData")
    public void prepareTournament2() {
        //Create Tournament
        tournament2DTO = addTournament(TOURNAMENT2_NAME, MEMBERS, TEAMS, REFEREES, ORGANIZER, VOLUNTEER, PRESS, 0);
        tournamentExtraPropertyController.create(new TournamentExtraPropertyDTO(tournament2DTO,
                TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION, LeagueFightsOrder.LIFO.name()), null, null);
    }

    @Test
    public void checkFifoOrder() {
        List<FightDTO> fightDTOs = fightController.createFights(tournament1DTO.getId(), TeamsOrder.SORTED, 0, null, null);
        Assert.assertEquals(fightDTOs.get(0).getTeam1().getName(), "Team01");
        Assert.assertEquals(fightDTOs.get(0).getTeam2().getName(), "Team02");

        Assert.assertEquals(fightDTOs.get(1).getTeam1().getName(), "Team03");
        Assert.assertEquals(fightDTOs.get(1).getTeam2().getName(), "Team02");

        Assert.assertEquals(fightDTOs.get(2).getTeam1().getName(), "Team03");
        Assert.assertEquals(fightDTOs.get(2).getTeam2().getName(), "Team04");

        Assert.assertEquals(fightDTOs.get(3).getTeam1().getName(), "Team01");
        Assert.assertEquals(fightDTOs.get(3).getTeam2().getName(), "Team04");

        Assert.assertEquals(fightDTOs.get(4).getTeam1().getName(), "Team01");
        Assert.assertEquals(fightDTOs.get(4).getTeam2().getName(), "Team03");

        Assert.assertEquals(fightDTOs.get(5).getTeam1().getName(), "Team04");
        Assert.assertEquals(fightDTOs.get(5).getTeam2().getName(), "Team02");
    }

    @Test
    public void checkLifoOrder() {
        List<FightDTO> fightDTOs = fightController.createFights(tournament2DTO.getId(), TeamsOrder.SORTED, 0, null, null);
        Assert.assertEquals(fightDTOs.get(0).getTeam1().getName(), "Team01");
        Assert.assertEquals(fightDTOs.get(0).getTeam2().getName(), "Team02");

        Assert.assertEquals(fightDTOs.get(1).getTeam1().getName(), "Team01");
        Assert.assertEquals(fightDTOs.get(1).getTeam2().getName(), "Team03");

        Assert.assertEquals(fightDTOs.get(2).getTeam1().getName(), "Team04");
        Assert.assertEquals(fightDTOs.get(2).getTeam2().getName(), "Team03");

        Assert.assertEquals(fightDTOs.get(3).getTeam1().getName(), "Team04");
        Assert.assertEquals(fightDTOs.get(3).getTeam2().getName(), "Team02");

        Assert.assertEquals(fightDTOs.get(4).getTeam1().getName(), "Team03");
        Assert.assertEquals(fightDTOs.get(4).getTeam2().getName(), "Team02");

        Assert.assertEquals(fightDTOs.get(5).getTeam1().getName(), "Team04");
        Assert.assertEquals(fightDTOs.get(5).getTeam2().getName(), "Team01");
    }

    @AfterClass(alwaysRun = true)
    @Override
    public void wipeOut() {
        super.wipeOut();
    }

}
