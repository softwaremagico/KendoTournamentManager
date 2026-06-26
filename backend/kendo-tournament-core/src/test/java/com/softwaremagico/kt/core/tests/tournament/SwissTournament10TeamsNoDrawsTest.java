package com.softwaremagico.kt.core.tests.tournament;

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

import com.softwaremagico.kt.core.controller.ClubController;
import com.softwaremagico.kt.core.controller.DuelController;
import com.softwaremagico.kt.core.controller.FightController;
import com.softwaremagico.kt.core.controller.GroupController;
import com.softwaremagico.kt.core.controller.ParticipantController;
import com.softwaremagico.kt.core.controller.RoleController;
import com.softwaremagico.kt.core.controller.TeamController;
import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
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

@SpringBootTest
@Test(groups = {"swissTournament10NoDrawsTest"})
public class SwissTournament10TeamsNoDrawsTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "Swiss10Club";
    private static final String CLUB_CITY = "Swiss10City";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 10;
    private static final int ROUNDS = 4;
    private static final int FIGHTS_PER_ROUND = 5;
    private static final String TOURNAMENT_NAME = "SwissTournament10TeamsNoDrawsTest";

    @Autowired
    private TournamentController tournamentController;

    @Autowired
    private TournamentConverter tournamentConverter;

    @Autowired
    private ParticipantController participantController;

    @Autowired
    private RoleController roleController;

    @Autowired
    private TeamController teamController;

    @Autowired
    private ClubController clubController;

    @Autowired
    private RankingProvider rankingProvider;

    @Autowired
    private GroupController groupController;

    @Autowired
    private FightController fightController;

    @Autowired
    private DuelController duelController;

    @Autowired
    private FightConverter fightConverter;

    private ClubDTO clubDTO;
    private TournamentDTO tournamentDTO;

    @Test
    public void addClub() {
        clubDTO = clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            participantController.create(new ParticipantDTO(String.format("S10-%04d", i),
                    String.format("name%s", i), String.format("lastname%s", i), clubDTO), null, null);
        }
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() {
        Assert.assertEquals(tournamentController.count(), 0);
        final TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.SWISS);
        tournamentDTO = tournamentController.create(newTournament, null, null);
        Assert.assertEquals(tournamentController.count(), 1);
    }

    @Test(dependsOnMethods = "addTournament")
    public void addRoles() {
        for (ParticipantDTO competitor : participantController.get()) {
            roleController.create(new RoleDTO(tournamentDTO, competitor, RoleType.COMPETITOR), null, null);
        }
        Assert.assertEquals(roleController.count(tournamentDTO), participantController.count());
    }

    @Test(dependsOnMethods = "addRoles")
    public void addTeams() {
        int teamIndex = 0;
        TeamDTO team = null;
        int teamMember = 0;

        final List<Group> groups = groupController.getGroups(tournamentDTO, 0);
        Assert.assertEquals(groups.size(), 1);

        for (ParticipantDTO competitor : participantController.get()) {
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), tournamentDTO);
                teamMember = 0;
            }

            team.addMember(competitor);
            team = teamController.create(team, null, null);

            if (teamMember == 0) {
                groupController.addTeams(groups.getFirst().getId(), Collections.singletonList(team), null, null);
            }

            teamMember++;
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        Assert.assertEquals(teamController.count(tournamentDTO), TEAMS);
        Assert.assertEquals(groupController.getGroups(tournamentDTO, 0).getFirst().getTeams().size(), TEAMS);
    }

    @Test(dependsOnMethods = "addTeams")
    public void createAndSolveSwissRoundsWithoutDrawFights() {
        for (int level = 0; level < ROUNDS; level++) {
            final int roundLevel = level;
            final List<FightDTO> createdFights = fightController.createFights(tournamentDTO.getId(), TeamsOrder.NONE, level, null, null);
            Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

            final Group group = groupController.getGroups(tournamentDTO, 0).getFirst();
            final List<Fight> fightsInRound = group.getFights().stream().filter(fight -> fight.getLevel() == roundLevel).toList();
            Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);

            for (Fight fight : fightsInRound) {
                fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
                fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
                fight.getDuels().forEach(duel -> duel.setFinished(true));
                fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null, null);
            }

            final Group updatedGroup = groupController.getGroups(tournamentDTO, 0).getFirst();
            final List<Fight> updatedFightsInRound = updatedGroup.getFights().stream().filter(fight -> fight.getLevel() == roundLevel).toList();
            Assert.assertTrue(updatedFightsInRound.stream().allMatch(Fight::isOver));
            Assert.assertTrue(updatedFightsInRound.stream().noneMatch(Fight::isDrawFight));
        }
    }

    @Test(dependsOnMethods = "createAndSolveSwissRoundsWithoutDrawFights")
    public void checkFinalRanking() {
        final List<ScoreOfTeam> ranking = rankingProvider.getTeamsScoreRanking(tournamentConverter.reverse(tournamentDTO));
        Assert.assertEquals(ranking.size(), TEAMS);
        Assert.assertNotNull(ranking.getFirst().getTeam());

        final Group group = groupController.getGroups(tournamentDTO, 0).getFirst();
        Assert.assertEquals(group.getFights().size(), ROUNDS * FIGHTS_PER_ROUND);
        Assert.assertTrue(group.getFights().stream().allMatch(Fight::isOver));
        Assert.assertTrue(group.getFights().stream().noneMatch(Fight::isDrawFight));
    }

    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        if (tournamentDTO != null) {
            groupController.delete(tournamentDTO);
            fightController.delete(tournamentDTO);
            duelController.delete(tournamentDTO);
            teamController.delete(tournamentDTO);
            roleController.delete(tournamentDTO);
            tournamentController.delete(tournamentDTO, null, null);
        }
        participantController.deleteAll();
        if (clubDTO != null) {
            clubController.delete(clubDTO, null, null);
        }
        Assert.assertEquals(fightController.count(), 0);
        Assert.assertEquals(duelController.count(), 0);
    }
}


