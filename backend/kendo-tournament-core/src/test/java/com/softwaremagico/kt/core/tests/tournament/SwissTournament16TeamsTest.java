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

import java.util.HashMap;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Collectors;

@SpringBootTest
@Test(groups = {"swissTournament16Test"})
public class SwissTournament16TeamsTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "Swiss16Club";
    private static final String CLUB_CITY = "Swiss16City";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 16;
    private static final int ROUNDS = 4;
    private static final int FIGHTS_PER_ROUND = 8;
    private static final String TOURNAMENT_NAME = "SwissTournament16TeamsTest";

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
            participantController.create(new ParticipantDTO(String.format("S16-%04d", i),
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
    public void createAndAdvanceSwissRoundsWithoutByes() {
        final Set<String> playedPairs = new HashSet<>();
        for (int level = 0; level < ROUNDS; level++) {
            final int roundLevel = level;
            Assert.assertEquals(getAllFights().stream().filter(Fight::isOver).count(),
                    (long) roundLevel * FIGHTS_PER_ROUND);

            final List<FightDTO> createdFights = fightController.createNextFights(tournamentDTO.getId(), null, null);
            Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

            final List<Group> roundGroups = groupController.getGroups(tournamentDTO, roundLevel);
            Assert.assertFalse(roundGroups.isEmpty());
            Assert.assertTrue(roundGroups.size() <= roundLevel + 1,
                    "Swiss score brackets at level " + roundLevel + " cannot exceed " + (roundLevel + 1));
            Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
                    IntStream.range(0, roundGroups.size()).boxed().toList());
            final List<Fight> fightsInRound = roundGroups.stream().flatMap(group -> group.getFights().stream()).toList();
            Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);

            final Map<String, Integer> winsBeforeRound = this.getSwissWinsBeforeRoundWithoutByes(roundLevel);
            SwissTestAssertions.assertAdjacentBracketFloatsOnly(fightsInRound, winsBeforeRound, roundLevel);
            final int minimumCrossBracketPairings = SwissTestAssertions.getMinimumCrossBracketPairings(
                    winsBeforeRound,
                    null,
                    "without byes at level " + roundLevel);
            final int actualCrossBracketPairings = SwissTestAssertions.countCrossBracketPairings(fightsInRound, winsBeforeRound);
            Assert.assertTrue(actualCrossBracketPairings >= minimumCrossBracketPairings,
                    "Cross-bracket pairings at level " + roundLevel + " cannot be below theoretical minimum. "
                            + "actual=" + actualCrossBracketPairings + ", min=" + minimumCrossBracketPairings);
            Assert.assertTrue(actualCrossBracketPairings <= minimumCrossBracketPairings + 2,
                    "Cross-bracket pairings at level " + roundLevel + " should stay near minimum. "
                            + "actual=" + actualCrossBracketPairings + ", min=" + minimumCrossBracketPairings);

            SwissTestAssertions.assertNoRematchAndSingleAppearance(fightsInRound, playedPairs, roundLevel);

            for (Fight fight : fightsInRound) {
                Assert.assertNotNull(fight.getTeam1());
                Assert.assertNotNull(fight.getTeam2());
                fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
                fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
                fight.getDuels().forEach(duel -> duel.setFinished(true));
                fightController.update(fightConverter.convert(new FightConverterRequest(fight)), null, null);
            }

            Assert.assertEquals(groupController.getGroups(tournamentDTO, roundLevel).stream()
                            .mapToLong(group -> group.getFights().size()).sum(),
                    FIGHTS_PER_ROUND);

            // Next round is generated only after finishing all fights from current round.
            if (roundLevel < ROUNDS - 1) {
                Assert.assertEquals(groupController.getGroups(tournamentDTO, roundLevel + 1).stream()
                        .mapToLong(group -> group.getFights().size()).sum(), 0);
            }
        }
    }

    @Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithoutByes")
    public void checkGroupsPerSwissRound() {
        for (int level = 0; level < ROUNDS; level++) {
            final List<Group> roundGroups = groupController.getGroups(tournamentDTO, level);
            Assert.assertFalse(roundGroups.isEmpty());
            Assert.assertTrue(roundGroups.size() <= level + 1,
                    "Swiss score brackets at level " + level + " cannot exceed " + (level + 1));
            Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
                    IntStream.range(0, roundGroups.size()).boxed().toList());

            final long assignedTeams = roundGroups.stream().mapToLong(group -> group.getTeams().size()).sum();
            Assert.assertEquals(assignedTeams, TEAMS,
                    "All teams must be assigned to a score bracket at level " + level);

            final Set<String> uniqueTeams = roundGroups.stream().flatMap(group -> group.getTeams().stream())
                    .map(team -> team.getName()).collect(Collectors.toSet());
            Assert.assertEquals(uniqueTeams.size(), TEAMS,
                    "No team can appear in multiple groups at level " + level);

            Assert.assertEquals(roundGroups.stream().mapToLong(group -> group.getFights().size()).sum(), FIGHTS_PER_ROUND);
        }
    }

    @Test(dependsOnMethods = "checkGroupsPerSwissRound")
    public void checkFinalRanking() {
        final List<ScoreOfTeam> ranking = rankingProvider.getTeamsScoreRanking(tournamentConverter.reverse(tournamentDTO));
        Assert.assertEquals(ranking.size(), TEAMS);
        Assert.assertNotNull(ranking.getFirst().getTeam());
        Assert.assertNotNull(ranking.getFirst().getTeam().getName());

        final List<Fight> allFights = getAllFights();
        Assert.assertEquals(allFights.size(), ROUNDS * FIGHTS_PER_ROUND);
        Assert.assertTrue(allFights.stream().allMatch(fight -> fight.getTeam1() != null && fight.getTeam2() != null));

        final Map<String, Integer> fightsByTeamName = new HashMap<>();
        allFights.forEach(fight -> {
            fightsByTeamName.merge(fight.getTeam1().getName(), 1, Integer::sum);
            fightsByTeamName.merge(fight.getTeam2().getName(), 1, Integer::sum);
        });

        Assert.assertEquals(fightsByTeamName.size(), TEAMS);
        ranking.forEach(score -> Assert.assertEquals((int) fightsByTeamName.get(score.getTeam().getName()), ROUNDS));

        Assert.assertTrue(ranking.stream().allMatch(score -> score.getWonFights() >= 0 && score.getWonFights() <= ROUNDS),
                "Wins must be in range [0, " + ROUNDS + "]");

        final int totalWins = ranking.stream().mapToInt(ScoreOfTeam::getWonFights).sum();
        Assert.assertEquals(totalWins, ROUNDS * FIGHTS_PER_ROUND,
                "Total wins must equal winners-per-round without byes");

        final long unbeatenTeams = ranking.stream().filter(score -> score.getWonFights() == ROUNDS).count();
        Assert.assertEquals(unbeatenTeams, 1L,
                "In this deterministic 16-team / 4-round Swiss scenario, exactly one team should finish unbeaten");
    }

    private Map<String, Integer> getSwissWinsBeforeRoundWithoutByes(int roundLevel) {
        final Map<String, Integer> winsByTeam = new HashMap<>();
        for (int level = 0; level < roundLevel; level++) {
            final List<Fight> fightsAtLevel = groupController.getGroups(tournamentDTO, level).stream()
                    .flatMap(group -> group.getFights().stream()).toList();
            for (final Fight fight : fightsAtLevel) {
                winsByTeam.putIfAbsent(fight.getTeam1().getName(), 0);
                winsByTeam.putIfAbsent(fight.getTeam2().getName(), 0);
                if (fight.getWinner() != null) {
                    winsByTeam.computeIfPresent(fight.getWinner().getName(), (ignored, value) -> value + 1);
                }
            }
        }
        return winsByTeam;
    }


    private List<Fight> getAllFights() {
        final List<Fight> fights = new ArrayList<>();
        for (int level = 0; level < ROUNDS; level++) {
            fights.addAll(groupController.getGroups(tournamentDTO, level).stream().flatMap(group -> group.getFights().stream()).toList());
        }
        return fights;
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
