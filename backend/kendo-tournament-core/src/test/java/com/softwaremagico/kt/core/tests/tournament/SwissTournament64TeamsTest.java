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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@SpringBootTest
@Test(groups = {"swissTournament64Test"})
public class SwissTournament64TeamsTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "Swiss64Club";
    private static final String CLUB_CITY = "Swiss64City";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 64;
    private static final int ROUNDS = 6;
    private static final int FIGHTS_PER_ROUND = 32;
    private static final String TOURNAMENT_NAME = "SwissTournament64TeamsTest";

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
        this.clubDTO = this.clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            this.participantController.create(new ParticipantDTO(String.format("S64-%04d", i),
                    String.format("name%s", i), String.format("lastname%s", i), this.clubDTO), null, null);
        }
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() {
        Assert.assertEquals(this.tournamentController.count(), 0);
        final TournamentDTO newTournament = new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.SWISS);
        this.tournamentDTO = this.tournamentController.create(newTournament, null, null);
        Assert.assertEquals(this.tournamentController.count(), 1);
    }

    @Test(dependsOnMethods = "addTournament")
    public void addRoles() {
        for (final ParticipantDTO competitor : this.participantController.get()) {
            this.roleController.create(new RoleDTO(this.tournamentDTO, competitor, RoleType.COMPETITOR), null, null);
        }
        Assert.assertEquals(this.roleController.count(this.tournamentDTO), this.participantController.count());
    }

    @Test(dependsOnMethods = "addRoles")
    public void addTeams() {
        int teamIndex = 0;
        TeamDTO team = null;
        int teamMember = 0;

        final List<Group> groups = this.groupController.getGroups(this.tournamentDTO, 0);
        Assert.assertEquals(groups.size(), 1);

        for (final ParticipantDTO competitor : this.participantController.get()) {
            if (team == null) {
                teamIndex++;
                team = new TeamDTO("Team" + String.format("%02d", teamIndex), this.tournamentDTO);
                teamMember = 0;
            }

            team.addMember(competitor);
            team = this.teamController.create(team, null, null);

            if (teamMember == 0) {
                this.groupController.addTeams(groups.getFirst().getId(), Collections.singletonList(team), null, null);
            }

            teamMember++;
            if (teamMember >= MEMBERS) {
                team = null;
            }
        }

        Assert.assertEquals(this.teamController.count(this.tournamentDTO), TEAMS);
        Assert.assertEquals(this.groupController.getGroups(this.tournamentDTO, 0).getFirst().getTeams().size(), TEAMS);
    }

    @Test(dependsOnMethods = "addTeams")
    public void createAndAdvanceSwissRoundsWithoutByes() {
        final List<Integer> expectedGroupsByLevel = List.of(1, 2, 3, 4, 5, 6);
        // Swiss 64 teams / 6 rounds theoretical score-bracket sizes by losses:
        // R1: 0L=32, 1L=32
        // R2: 0L=16, 1L=32, 2L=16
        // R3: 0L=8, 1L=24, 2L=24, 3L=8
        // R4: 0L=4, 1L=16, 2L=24, 3L=16, 4L=4
        // R5: 0L=2, 1L=10, 2L=20, 3L=20, 4L=10, 5L=2
        // R6 final: 0L=1, 1L=6, 2L=15, 3L=20, 4L=15, 5L=6, 6L=1
        for (int level = 0; level < ROUNDS; level++) {
            final int roundLevel = level;
            Assert.assertEquals(this.getAllFights().stream().filter(Fight::isOver).count(),
                    (long) roundLevel * FIGHTS_PER_ROUND);

            final List<FightDTO> createdFights = this.fightController.createNextFights(this.tournamentDTO.getId(), null,
                    null);
            Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

            final List<Group> roundGroups = this.groupController.getGroups(this.tournamentDTO, roundLevel);
            Assert.assertFalse(roundGroups.isEmpty());
            Assert.assertEquals(roundGroups.size(), (int) expectedGroupsByLevel.get(roundLevel));
            Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
                    IntStream.range(0, expectedGroupsByLevel.get(roundLevel)).boxed().toList());
            final List<Fight> fightsInRound = roundGroups.stream().flatMap(group -> group.getFights().stream()).toList();
            Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);

            for (final Fight fight : fightsInRound) {
                fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
                fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
                fight.getDuels().forEach(duel -> duel.setFinished(true));
                this.fightController.update(this.fightConverter.convert(new FightConverterRequest(fight)), null, null);
            }

            Assert.assertEquals(this.groupController.getGroups(this.tournamentDTO, roundLevel).stream()
                    .mapToLong(group -> group.getFights().size())
                    .sum(), FIGHTS_PER_ROUND);

            if (roundLevel < ROUNDS - 1) {
                Assert.assertEquals(this.groupController.getGroups(this.tournamentDTO, roundLevel + 1).stream()
                        .mapToLong(group -> group.getFights().size())
                        .sum(), 0);
            }
        }
    }

    @Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithoutByes")
    public void checkGroupsPerSwissRound() {
        final List<Integer> expectedGroupsByLevel = List.of(1, 2, 3, 4, 5, 6);
        final List<List<Integer>> expectedTeamSizesByLevel = List.of(
                List.of(64),
                List.of(32, 32),
                List.of(16, 32, 16),
                List.of(8, 24, 24, 8),
                List.of(4, 4, 17, 17, 22),
                List.of(2, 2, 11, 11, 19, 19)
        );

        for (int level = 0; level < ROUNDS; level++) {
            final List<Group> roundGroups = this.groupController.getGroups(this.tournamentDTO, level);
            final List<Integer> actualTeamSizes = roundGroups.stream().map(group -> group.getTeams().size()).sorted().toList();
            Assert.assertEquals(roundGroups.size(), (int) expectedGroupsByLevel.get(level));
            Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
                    IntStream.range(0, expectedGroupsByLevel.get(level)).boxed().toList());
            Assert.assertEquals(actualTeamSizes, expectedTeamSizesByLevel.get(level).stream().sorted().toList(),
                    "Level " + level + " actual sizes: " + actualTeamSizes);
            Assert.assertEquals(roundGroups.stream().mapToLong(group -> group.getFights().size()).sum(), FIGHTS_PER_ROUND);
        }
    }

    @Test(dependsOnMethods = "checkGroupsPerSwissRound")
    public void checkFinalRanking() {
        final List<ScoreOfTeam> ranking = this.rankingProvider
                .getTeamsScoreRanking(this.tournamentConverter.reverse(this.tournamentDTO));
        Assert.assertEquals(ranking.size(), TEAMS);

        final List<Fight> allFights = this.getAllFights();
        Assert.assertEquals(allFights.size(), ROUNDS * FIGHTS_PER_ROUND);

        final Map<String, Integer> fightsByTeamName = new HashMap<>();
        allFights.forEach(fight -> {
            fightsByTeamName.merge(fight.getTeam1().getName(), 1, Integer::sum);
            fightsByTeamName.merge(fight.getTeam2().getName(), 1, Integer::sum);
        });

        Assert.assertEquals(fightsByTeamName.size(), TEAMS);
        ranking.forEach(score -> Assert.assertEquals((int) fightsByTeamName.get(score.getTeam().getName()), ROUNDS));

        assertTeamsWithWinsCount(ranking, 6, 1);
        assertTeamsWithWinsCount(ranking, 5, 7);
        assertTeamsWithWinsCount(ranking, 4, 14);
        assertTeamsWithWinsCount(ranking, 3, 20);
        assertTeamsWithWinsCount(ranking, 2, 14);
        assertTeamsWithWinsCount(ranking, 1, 7);
        assertTeamsWithWinsCount(ranking, 0, 1);
    }

    private void assertTeamsWithWinsCount(List<ScoreOfTeam> ranking, int wins, int expectedCount) {
        final long teamsWithWins = ranking.stream().filter(score -> score.getWonFights() == wins).count();
        Assert.assertEquals(teamsWithWins, expectedCount);
    }

    private List<Fight> getAllFights() {
        final List<Fight> fights = new ArrayList<>();
        for (int level = 0; level < ROUNDS; level++) {
            fights.addAll(this.groupController.getGroups(this.tournamentDTO, level).stream()
                    .flatMap(group -> group.getFights().stream()).toList());
        }
        return fights;
    }

    @AfterClass(alwaysRun = true)
    public void deleteTournament() {
        if (this.tournamentDTO != null) {
            this.groupController.delete(this.tournamentDTO);
            this.fightController.delete(this.tournamentDTO);
            this.duelController.delete(this.tournamentDTO);
            this.teamController.delete(this.tournamentDTO);
            this.roleController.delete(this.tournamentDTO);
            this.tournamentController.delete(this.tournamentDTO, null, null);
        }
        this.participantController.deleteAll();
        if (this.clubDTO != null) {
            this.clubController.delete(this.clubDTO, null, null);
        }
        Assert.assertEquals(this.fightController.count(), 0);
        Assert.assertEquals(this.duelController.count(), 0);
    }
}





