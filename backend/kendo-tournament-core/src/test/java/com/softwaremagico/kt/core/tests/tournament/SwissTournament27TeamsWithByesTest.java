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
import com.softwaremagico.kt.persistence.entities.Team;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Integration test for a Swiss tournament with 27 teams (odd count → one bye per round).
 *
 * <p><b>Tournament parameters:</b>
 * <ul>
 *   <li>27 teams, 3 members each.</li>
 *   <li>Default rounds = ceil(log2(27)) = 5 (since 2^4=16 &lt; 27 &le; 2^5=32).</li>
 *   <li>13 fights per round (27 odd → 13 fights + 1 bye).</li>
 * </ul>
 *
 * <p><b>Deterministic scenario:</b> competitor1 always wins two MEN points in the first duel.
 * Teams sorted alphabetically within the same score bracket: Team01 &gt; Team02 &gt; ... &gt; Team27.
 * The algorithm pairs teams top-down within score brackets, crossing bracket boundaries only when needed.
 *
 * <p><b>Bye rotation (lowest-ranked team not yet having received a bye):</b>
 * <pre>
 * R0: Team27 (all 0pts, last alphabetically)
 * R1: Team26 (0pts group last with no prior bye)
 * R2: Team24 (0pts group, Team26 already used its bye in R1)
 * R3: Team20 (0pts group, Team24 already used its bye in R2)
 * R4: Team12 (0pts group last with no prior bye)
 * </pre>
 *
 * <p><b>Score brackets per round (groups created based on Swiss points before each round):</b>
 * <pre>
 * R0: 1 group  → {0pts: all 27}
 * R1: 2 groups → {3pts: 14 [13 winners + Team27 bye], 0pts: 13}
 * R2: 3 groups → {6pts: 7, 3pts: 14, 0pts: 6}
 * R3: 4 groups → {9pts: 4, 6pts: 10, 3pts: 10, 0pts: 3}
 * R4: 5 groups → {12pts: 2, 9pts: 7, 6pts: 10, 3pts: 7, 0pts: 1}
 * </pre>
 *
 * <p><b>Final ranking — note: {@code RankingProvider} adds bye count to {@code getWonFights()} for Swiss
 * tournaments, so a bye counts as 1 won fight in the ranking.</b>
 * <pre>
 * 5 wins : Team01                                              (5 fight-wins, 0 byes)
 * 4 wins : Team03, Team09, Team13, Team17, Team25              (4 fight-wins, 0 byes)
 * 3 wins : Team02, Team07, Team11, Team14, Team15, Team21,     (2 or 3 fight-wins)
 *          Team23, Team27                                      Team27: 2 fight-wins + 1 bye
 * 2 wins : Team04, Team05, Team08, Team16, Team18, Team19,     (1 or 2 fight-wins)
 *          Team20, Team24                                      Team20,Team24: 1 fight-win + 1 bye
 * 1 win  : Team06, Team10, Team12, Team22, Team26              Team12,Team26: 0 fight-wins + 1 bye
 * 0 wins : (none)
 * </pre>
 */
@SpringBootTest
@Test(groups = {"swissTournament27ByesTest"})
public class SwissTournament27TeamsWithByesTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "Swiss27Club";
    private static final String CLUB_CITY = "Swiss27City";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 27;
    private static final int ROUNDS = 5;
    // 27 teams is odd: 13 fights + 1 bye per round
    private static final int FIGHTS_PER_ROUND = 13;
    private static final String TOURNAMENT_NAME = "SwissTournament27TeamsWithByesTest";

    /**
     * Expected bye recipient per round (rotating, lowest-ranked team without a previous bye).
     * R0→Team27 (last alphabetically, all 0pts), R1→Team26, R2→Team24, R3→Team20, R4→Team12.
     */
    private static final List<String> EXPECTED_BYES_PER_ROUND =
            List.of("Team27", "Team26", "Team24", "Team20", "Team12");

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
            this.participantController.create(new ParticipantDTO(String.format("S27-%04d", i),
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

    /**
     * Advances through all 5 Swiss rounds using generateNextFights() for every round (including R0).
     *
     * <p>Each round:
     * <ol>
     *   <li>Call createNextFights() — for R0 it detects no existing fights and generates level 0;
     *       for subsequent rounds it detects the finished level and advances.</li>
     *   <li>Assert fight count and group structure for the current level.</li>
     *   <li>Record which team received the bye (the one absent from all fights in the round).</li>
     *   <li>Make competitor1 win every fight (2×MEN, all duels finished).</li>
     *   <li>Assert next-level groups have 0 fights before the round is advanced.</li>
     * </ol>
     *
     * <p>Expected groups per level (score brackets, groups created before each round based on Swiss pts):
     * <pre>
     * R0: 1 group  — all 27 teams at 0 pts
     * R1: 2 groups — 14 teams at 3pts (13 winners + Team27 bye) / 13 teams at 0pts
     * R2: 3 groups — 7 at 6pts / 14 at 3pts / 6 at 0pts
     * R3: 4 groups — 4 at 9pts / 10 at 6pts / 10 at 3pts / 3 at 0pts
     * R4: 5 groups — 2 at 12pts / 7 at 9pts / 10 at 6pts / 7 at 3pts / 1 at 0pts
     * </pre>
     */
    @Test(dependsOnMethods = "addTeams")
    public void createAndAdvanceSwissRoundsWithByes() {
        // Number of score-bracket groups created per level (grows as teams diverge in points).
        final List<Integer> expectedGroupsByLevel = List.of(1, 2, 3, 4, 5);

        final List<String> allTeamNames = this.groupController.getGroups(this.tournamentDTO, 0).getFirst().getTeams()
                .stream().map(Team::getName).sorted().toList();
        final List<String> byeTeamsByRound = new ArrayList<>();

        for (int level = 0; level < ROUNDS; level++) {
            final int roundLevel = level;
            Assert.assertEquals(this.getAllFights().stream().filter(Fight::isOver).count(),
                    (long) roundLevel * FIGHTS_PER_ROUND,
                    "Completed fights count before round " + roundLevel);

            // createNextFights() handles both the initial round (no existing fights) and subsequent ones.
            final List<FightDTO> createdFights =
                    this.fightController.createNextFights(this.tournamentDTO.getId(), null, null);
            Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND,
                    "Expected " + FIGHTS_PER_ROUND + " fights in round " + roundLevel);

            // Verify group structure for the current level.
            final List<Group> roundGroups = this.groupController.getGroups(this.tournamentDTO, roundLevel);
            Assert.assertFalse(roundGroups.isEmpty(), "Round " + roundLevel + " must have at least one group");
            Assert.assertEquals(roundGroups.size(), (int) expectedGroupsByLevel.get(roundLevel),
                    "Wrong number of score-bracket groups at level " + roundLevel);
            Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
                    IntStream.range(0, expectedGroupsByLevel.get(roundLevel)).boxed().toList(),
                    "Group indices must be consecutive starting from 0 at level " + roundLevel);

            final List<Fight> fightsInRound = roundGroups.stream()
                    .flatMap(group -> group.getFights().stream()).toList();
            Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND,
                    "Fights stored in groups must match expected count at level " + roundLevel);

            // Identify and record the bye team (the only team absent from all fights this round).
            final String byeTeamName = this.getByeTeamName(allTeamNames, fightsInRound);
            byeTeamsByRound.add(byeTeamName);

            // Make competitor1 win every fight in this round.
            for (final Fight fight : fightsInRound) {
                Assert.assertNotNull(fight.getTeam1(), "fight.team1 must not be null");
                Assert.assertNotNull(fight.getTeam2(), "fight.team2 must not be null");
                fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
                fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
                fight.getDuels().forEach(duel -> duel.setFinished(true));
                this.fightController.update(
                        this.fightConverter.convert(new FightConverterRequest(fight)), null, null);
            }

            Assert.assertEquals(
                    this.groupController.getGroups(this.tournamentDTO, roundLevel).stream()
                            .flatMap(group -> group.getFights().stream()).count(),
                    FIGHTS_PER_ROUND,
                    "Persisted fights count must remain " + FIGHTS_PER_ROUND + " after update at level " + roundLevel);

            // Next-level groups must not have any fights yet (generated only after this round finishes).
            if (roundLevel < ROUNDS - 1) {
                Assert.assertEquals(
                        this.groupController.getGroups(this.tournamentDTO, roundLevel + 1).stream()
                                .flatMap(group -> group.getFights().stream()).count(),
                        0L,
                        "Level " + (roundLevel + 1) + " must have 0 fights before current round is marked done");
            }
        }

        // All byes must have gone to different teams (no repeated bye until strictly necessary).
        Assert.assertEquals(new HashSet<>(byeTeamsByRound).size(), ROUNDS,
                "All " + ROUNDS + " bye slots must go to different teams; actual=" + byeTeamsByRound);
        Assert.assertEquals(byeTeamsByRound, EXPECTED_BYES_PER_ROUND,
                "Bye rotation must follow the lowest-ranked no-bye-yet policy; actual=" + byeTeamsByRound);
    }

    /**
     * Verifies the exact group composition (count, consecutive indices, team sizes, fight counts)
     * for every level after all rounds have been played.
     *
     * <p>Expected sorted team sizes per level:
     * <pre>
     * R0: [27]
     * R1: [13, 14]
     * R2: [6, 7, 14]
     * R3: [3, 4, 10, 10]
     * R4: [1, 2, 7, 7, 10]
     * </pre>
     */
    @Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithByes")
    public void checkGroupsPerSwissRound() {
        final List<Integer> expectedGroupsByLevel = List.of(1, 2, 3, 4, 5);

        /*
         * Team sizes per score-bracket group, sorted ascending within each level.
         *
         * R0: 1 group  → all 27 teams (0pts)
         * R1: 2 groups → 0pts=13 / 3pts=14 (13 fight-winners + Team27 bye)
         * R2: 3 groups → 0pts=6 / 6pts=7 / 3pts=14
         * R3: 4 groups → 0pts=3 / 9pts=4 / 6pts=10 / 3pts=10
         * R4: 5 groups → 0pts=1 / 12pts=2 / 9pts=7 / 3pts=7 / 6pts=10
         */
        final List<List<Integer>> expectedTeamSizesByLevel = List.of(
                List.of(27),
                List.of(13, 14),
                List.of(6, 7, 14),
                List.of(3, 4, 10, 10),
                List.of(1, 2, 7, 7, 10)
        );

        for (int level = 0; level < ROUNDS; level++) {
            final List<Group> roundGroups = this.groupController.getGroups(this.tournamentDTO, level);
            final List<Integer> actualTeamSizes =
                    roundGroups.stream().map(group -> group.getTeams().size()).sorted().toList();

            Assert.assertEquals(roundGroups.size(), (int) expectedGroupsByLevel.get(level),
                    "Group count at level " + level);
            Assert.assertEquals(roundGroups.stream().map(Group::getIndex).sorted().toList(),
                    IntStream.range(0, expectedGroupsByLevel.get(level)).boxed().toList(),
                    "Group indices must be consecutive at level " + level);
            Assert.assertEquals(actualTeamSizes,
                    expectedTeamSizesByLevel.get(level).stream().sorted().toList(),
                    "Team distribution across groups at level " + level
                            + " — actual sizes: " + actualTeamSizes);
            Assert.assertEquals(
                    roundGroups.stream().mapToLong(group -> group.getFights().size()).sum(),
                    FIGHTS_PER_ROUND,
                    "Total fights stored at level " + level);
        }
    }

    /**
     * Verifies the final ranking win distribution and bye bookkeeping after all 5 rounds.
     *
     * <p>{@link com.softwaremagico.kt.core.providers.RankingProvider} adds the bye count to
     * {@code getWonFights()} for Swiss tournaments, so a bye counts as 1 additional won fight in the ranking.
     * This means teams that received byes have their fight-win count incremented accordingly:
     * <pre>
     * 5 wins : Team01                                              (5 fight-wins, 0 byes)
     * 4 wins : Team03, Team09, Team13, Team17, Team25              (4 fight-wins, 0 byes)
     * 3 wins : Team02, Team07, Team11, Team14, Team15, Team21,     (2-3 fight-wins)
     *          Team23, Team27                                      Team27 = 2 fight-wins + 1 bye
     * 2 wins : Team04, Team05, Team08, Team16, Team18, Team19,     (1-2 fight-wins)
     *          Team20, Team24                                      Team20,Team24 = 1 fight-win + 1 bye
     * 1 win  : Team06, Team10, Team12, Team22, Team26              Team12,Team26 = 0 fight-wins + 1 bye
     * 0 wins : (none — all teams received at least 1 win or 1 bye)
     * </pre>
     *
     * <p>Each team must appear in exactly ROUNDS participations (fights + byes).
     */
    @Test(dependsOnMethods = "checkGroupsPerSwissRound")
    public void checkFinalRanking() {
        final List<ScoreOfTeam> ranking = this.rankingProvider
                .getTeamsScoreRanking(this.tournamentConverter.reverse(this.tournamentDTO));
        Assert.assertEquals(ranking.size(), TEAMS, "Ranking must contain all " + TEAMS + " teams");

        final List<Fight> allFights = this.getAllFights();
        Assert.assertEquals(allFights.size(), ROUNDS * FIGHTS_PER_ROUND,
                "Total fights across all rounds");

        // Verify every team participated in exactly ROUNDS rounds (fights + byes).
        final Map<String, Integer> fightsByTeamName = new HashMap<>();
        ranking.forEach(score -> fightsByTeamName.put(score.getTeam().getName(), 0));
        allFights.forEach(fight -> {
            fightsByTeamName.merge(fight.getTeam1().getName(), 1, Integer::sum);
            fightsByTeamName.merge(fight.getTeam2().getName(), 1, Integer::sum);
        });

        final Map<String, Integer> byeCountByTeam = this.getByeCountByTeam(allFights);
        ranking.forEach(score ->
                Assert.assertEquals(
                        (int) fightsByTeamName.get(score.getTeam().getName())
                                + byeCountByTeam.getOrDefault(score.getTeam().getName(), 0),
                        ROUNDS,
                        "Team " + score.getTeam().getName() + " must have participated in exactly " + ROUNDS + " rounds"));

        /*
         * Win distribution explanation:
         *
         * 5 wins (1 team) — Team01 wins every round as the highest-ranked team throughout.
         *
         * 4 wins (5 teams) — Teams that won rounds 0, 1 and 2 but eventually lost once to a stronger opponent:
         *   Team03: W-L-W-W-W, Team09: W-W-W-L-W, Team13: W-W-L-W-W,
         *   Team17: W-W-W-W-L, Team25: W-W-W-L-W.
         *
         * 3 wins (7 teams) — Mid-bracket teams that ended 3-2 across 5 rounds:
         *   Team02: L-W-L-W-W, Team07: W-L-W-W-L, Team11: W-L-W-L-W,
         *   Team14: L-W-L-W-W, Team15: W-L-W-W-L, Team21: W-W-L-L-W, Team23: W-L-W-W-L.
         *
         * 2 wins (7 teams) — Lower-bracket teams finishing 2-3; Team27 also had 1 bye:
         *   Team04: L-L-L-W-W, Team05: W-W-L-L-L, Team08: L-L-W-W-L,
         *   Team16: L-L-W-W-L, Team18: L-W-L-L-W, Team19: W-L-W-L-L,
         *   Team27: (bye R0)-L-W-L-W = 2 fight-wins + 1 bye → 3 total wins.
         *
         * 2 wins (8 teams) — Fight-wins + byes sum to 2:
         *   Team04: L-L-L-W-W (2 fight-wins), Team05: W-W-L-L-L (2 fight-wins),
         *   Team08: L-L-W-W-L (2 fight-wins), Team16: L-L-W-W-L (2 fight-wins),
         *   Team18: L-W-L-L-W (2 fight-wins), Team19: W-L-W-L-L (2 fight-wins),
         *   Team20: L-L-L-(bye R3)-W = 1 fight-win + 1 bye → 2 total wins,
         *   Team24: L-L-(bye R2)-W-L = 1 fight-win + 1 bye → 2 total wins.
         *
         * 1 win (5 teams) — Fight-wins + byes sum to 1:
         *   Team06: L-W-L-L-L (1 fight-win), Team10: L-W-L-L-L (1 fight-win),
         *   Team22: L-W-L-L-L (1 fight-win),
         *   Team12: L-L-L-L-(bye R4) = 0 fight-wins + 1 bye → 1 total win,
         *   Team26: L-(bye R1)-L-L-L = 0 fight-wins + 1 bye → 1 total win.
         *
         * 0 wins (0 teams) — Every team received at least 1 fight-win or 1 bye, so no team ends at 0.
         */
        this.assertTeamsWithWins(ranking, 5, List.of("Team01"));
        this.assertTeamsWithWins(ranking, 4, List.of("Team03", "Team09", "Team13", "Team17", "Team25"));
        this.assertTeamsWithWins(ranking, 3, List.of("Team02", "Team07", "Team11", "Team14", "Team15", "Team21", "Team23", "Team27"));
        this.assertTeamsWithWins(ranking, 2, List.of("Team04", "Team05", "Team08", "Team16", "Team18", "Team19", "Team20", "Team24"));
        this.assertTeamsWithWins(ranking, 1, List.of("Team06", "Team10", "Team12", "Team22", "Team26"));
        this.assertTeamsWithWins(ranking, 0, List.of());

        // Verify that exactly the five expected teams received one bye each.
        final Map<String, Integer> teamsWithBye = byeCountByTeam.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        Assert.assertEquals(teamsWithBye,
                Map.of("Team12", 1, "Team20", 1, "Team24", 1, "Team26", 1, "Team27", 1),
                "Exactly 5 teams should have received 1 bye each");
    }

    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------

    /**
     * Returns the name of the single team absent from all fights in the given round (the bye team).
     */
    private String getByeTeamName(List<String> allTeamNames, List<Fight> fightsInRound) {
        final Set<String> teamsInRound = new HashSet<>();
        fightsInRound.forEach(fight -> {
            teamsInRound.add(fight.getTeam1().getName());
            teamsInRound.add(fight.getTeam2().getName());
        });
        final List<String> byeTeams = allTeamNames.stream()
                .filter(teamName -> !teamsInRound.contains(teamName)).toList();
        Assert.assertEquals(byeTeams.size(), 1,
                "Exactly one team should have a bye per round; found: " + byeTeams);
        return byeTeams.getFirst();
    }

    /**
     * Computes how many byes each team received across all rounds.
     * A team received a bye in a given round if it appears in no fight for that round level.
     */
    private Map<String, Integer> getByeCountByTeam(List<Fight> allFights) {
        final Map<Integer, Set<String>> teamsByRound = allFights.stream()
                .collect(Collectors.groupingBy(Fight::getLevel, Collectors.flatMapping(
                        fight -> java.util.stream.Stream.of(
                                fight.getTeam1().getName(), fight.getTeam2().getName()),
                        Collectors.toSet())));
        final Set<String> allTeams = this.groupController.getGroups(this.tournamentDTO, 0).stream()
                .flatMap(group -> group.getTeams().stream())
                .map(Team::getName)
                .collect(Collectors.toSet());

        final Map<String, Integer> byeCountByTeam = new HashMap<>();
        allTeams.forEach(team -> byeCountByTeam.put(team, 0));

        for (final Set<String> teamsInRound : teamsByRound.values()) {
            final Set<String> byeTeams = new HashSet<>(allTeams);
            byeTeams.removeAll(teamsInRound);
            Assert.assertEquals(byeTeams.size(), 1,
                    "Exactly one team should have been absent per round; found: " + byeTeams);
            final String byeTeam = byeTeams.iterator().next();
            byeCountByTeam.computeIfPresent(byeTeam, (ignoredTeam, value) -> value + 1);
        }
        return byeCountByTeam;
    }

    /**
     * Asserts that exactly the given team names appear in the ranking with the specified win count.
     * Note: for Swiss tournaments, {@code RankingProvider} adds the bye count to {@code getWonFights()},
     * so a bye counts as 1 additional won fight in the ranking.
     */
    private void assertTeamsWithWins(List<ScoreOfTeam> ranking, int wins, List<String> expectedTeamNames) {
        final List<String> actualTeamNames = ranking.stream()
                .filter(score -> score.getWonFights() == wins)
                .map(score -> score.getTeam().getName())
                .sorted()
                .toList();
        final List<String> expectedSorted = expectedTeamNames.stream().sorted().toList();
        Assert.assertEquals(actualTeamNames, expectedSorted,
                "Teams with " + wins + " win(s) mismatch");
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






