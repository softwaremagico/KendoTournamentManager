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
import com.softwaremagico.kt.core.controller.TournamentExtraPropertyController;
import com.softwaremagico.kt.core.controller.models.ClubDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentExtraPropertyDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.providers.RankingProvider;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Verifies a Swiss tournament configured with MORE rounds than the theoretical optimum.
 *
 * <h2>Goal of this test</h2>
 * For 16 teams, the "optimal"/default number of Swiss rounds is {@code ceil(log2(16)) = 4}
 * (see {@link com.softwaremagico.kt.core.tournaments.SwissTournamentHandler#getDefaultRounds}).
 * This test explicitly overrides that value via the {@code SWISS_ROUNDS} extra property and
 * plays <b>8 rounds instead of 4</b>, i.e. double the optimum. The purpose is to check how two
 * specific teams cross brackets when there are more rounds than strictly needed to rank 16
 * teams, and to make sure the final ranking still correctly reflects every team's results.
 *
 * <h2>The two teams being tracked</h2>
 * <ul>
 *     <li><b>{@link #STREAK_THEN_COLLAPSE_TEAM} ("streak-then-collapse")</b>: wins its first
 *     {@value #STREAK_WINS_UNTIL_ROUND_EXCLUSIVE} fights in a row (rounds 0-3), climbing to the
 *     top score bracket, and then loses every remaining fight (rounds 4-7), falling back down
 *     through the brackets. It ends the tournament with exactly
 *     {@value #STREAK_WINS_UNTIL_ROUND_EXCLUSIVE} wins.</li>
 *     <li><b>{@link #COMEBACK_TEAM} ("comeback")</b>: loses its first
 *     {@value #COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE} fights (rounds 0-1), starting in the lower
 *     brackets, and then wins every remaining fight (rounds 2-7), climbing back up through the
 *     brackets round after round. It ends the tournament with {@code ROUNDS -
 *     COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE = 6} wins.</li>
 * </ul>
 * Both patterns are enforced fight-by-fight in {@link #decideWinner(String, String, int)} and
 * are additionally tracked round-by-round in {@link #resultsByRoundForTrackedTeams} so that
 * {@link #checkTrackedTeamsRoundByRoundPattern()} can assert, round by round (not only on the
 * final total), that each team actually won/lost exactly when the scenario says it should.
 *
 * <h2>Every other team</h2>
 * The remaining 14 "other" teams are not scripted to any exact result, but this test still
 * decides every one of their fights (see {@link #decideWinner(String, String, int)}) so that
 * none of them ever wins more than {@link #OTHER_TEAMS_MAX_WINS} of its {@link #ROUNDS} fights.
 *
 * <h2>What is asserted</h2>
 * <ol>
 *     <li>{@link #createAndAdvanceSwissRoundsWithCustomWinPattern()}: plays all 8 rounds,
 *     applying the scripted/bounded results above, and checks the round-by-round shape of the
 *     tournament (fight counts, no premature round generation).</li>
 *     <li>{@link #checkTrackedTeamsRoundByRoundPattern()}: replays the recorded per-round log of
 *     the two tracked teams and asserts every single round matches the scripted win/loss
 *     pattern, which is what makes their bracket crossings verifiable and easy to follow.</li>
 *     <li>{@link #checkFinalRanking()}: asserts the persisted ranking matches this test's own
 *     bookkeeping, that no team exceeds its allowed wins, and that the ranking is correctly
 *     sorted (comeback team, with more wins, ranks above the streak-then-collapse team).</li>
 * </ol>
 */
@SpringBootTest
@Test(groups = {"swissTournament16ExtraRoundsTest"})
public class SwissTournament16TeamsExtraRoundsTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "Swiss16ExtraRoundsClub";
    private static final String CLUB_CITY = "Swiss16ExtraRoundsCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 16;
    // Optimal/default rounds for 16 teams would be ceil(log2(16)) = 4. This test doubles it.
    private static final int ROUNDS = 8;
    private static final int FIGHTS_PER_ROUND = TEAMS / 2;
    private static final String TOURNAMENT_NAME = "SwissTournament16TeamsExtraRoundsTest";

    // Wins its first 4 rounds (rounds 0-3), climbing to the top bracket, then loses its
    // last 4 rounds (rounds 4-7), falling back down through the brackets. 4 total wins.
    private static final String STREAK_THEN_COLLAPSE_TEAM = "Team01";
    private static final int STREAK_WINS_UNTIL_ROUND_EXCLUSIVE = 4;

    // Loses its first 2 rounds (rounds 0-1), starting in the lower brackets, then wins the
    // remaining 6 rounds (rounds 2-7), climbing back up through the brackets. 6 total wins.
    private static final String COMEBACK_TEAM = "Team02";
    private static final int COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE = 2;

    // No other team may win more than 5 of its 8 fights.
    private static final int OTHER_TEAMS_MAX_WINS = 5;

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

    @Autowired
    private TournamentExtraPropertyController tournamentExtraPropertyController;

    private ClubDTO clubDTO;
    private TournamentDTO tournamentDTO;

    // Bookkeeping of wins decided by this test, kept in sync with the persisted fights.
    private final Map<String, Integer> winsByTeam = new HashMap<>();
    // Round-by-round (index = Swiss level, 0-based) win("true")/loss("false") log for the
    // two tracked teams only. This is what makes it easy to verify/understand exactly when
    // each of them was winning or losing, instead of only checking the final total.
    // Populated while playing the rounds in createAndAdvanceSwissRoundsWithCustomWinPattern()
    // and asserted round-by-round in checkTrackedTeamsRoundByRoundPattern().
    private final Map<String, List<Boolean>> resultsByRoundForTrackedTeams = new LinkedHashMap<>(
            Map.of(STREAK_THEN_COLLAPSE_TEAM, new ArrayList<>(), COMEBACK_TEAM, new ArrayList<>()));
    // Used to alternate winners when balancing "other" teams, avoiding always favouring
    // the same team whenever two of them are tied on wins.
    private int tieBreakCounter;

    @Test
    public void addClub() {
        this.clubDTO = this.clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            this.participantController.create(new ParticipantDTO(String.format("S16ER-%04d", i),
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
    public void configureSwissProperties() {
        // Doubles the default optimal rounds (4) for 16 teams.
        this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
                TournamentExtraPropertyKey.SWISS_ROUNDS, String.valueOf(ROUNDS)), null, null);
        // Keeps rematch avoidance enabled (the default): with 8 rounds a team can face at
        // most 8 of its 15 possible opponents, so no team can accumulate an unbounded number
        // of forced "windfall" results just from repeatedly facing the two scripted teams.
        this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
                TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, Boolean.TRUE.toString()), null, null);
    }

    @Test(dependsOnMethods = "configureSwissProperties")
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
     * Plays all 8 Swiss rounds and, for every single fight, decides who wins it (see
     * {@link #decideWinner(String, String, int)} for the full decision rules):
     * <ul>
     *     <li>{@link #STREAK_THEN_COLLAPSE_TEAM} wins rounds 0-3 and loses rounds 4-7.</li>
     *     <li>{@link #COMEBACK_TEAM} loses rounds 0-1 and wins rounds 2-7.</li>
     *     <li>Every other team's winner is chosen to keep every team at or below
     *     {@link #OTHER_TEAMS_MAX_WINS} total wins, favouring whichever of the two
     *     opponents currently has fewer wins.</li>
     * </ul>
     * Since 16 teams is always an even number, this Swiss implementation never needs to
     * assign a bye, so every round has exactly {@link #FIGHTS_PER_ROUND} decisive fights.
     * <p>
     * Every time {@link #STREAK_THEN_COLLAPSE_TEAM} or {@link #COMEBACK_TEAM} fights, the
     * win/loss is additionally appended to {@link #resultsByRoundForTrackedTeams} so that
     * {@link #checkTrackedTeamsRoundByRoundPattern()} can later confirm, round by round,
     * that the scripted pattern was actually followed (and not just correct in total).
     */
    @Test(dependsOnMethods = "addTeams")
    public void createAndAdvanceSwissRoundsWithCustomWinPattern() {
        for (int level = 0; level < ROUNDS; level++) {
            final List<FightDTO> createdFights = this.fightController.createNextFights(this.tournamentDTO.getId(), null,
                    null);
            Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND,
                    "Round " + level + " must generate " + FIGHTS_PER_ROUND + " fights (16 teams is always even, no byes)");

            final List<Fight> fightsInRound = this.groupController.getGroups(this.tournamentDTO, level).stream()
                    .flatMap(group -> group.getFights().stream()).toList();
            Assert.assertEquals(fightsInRound.size(), FIGHTS_PER_ROUND);

            for (final Fight fight : fightsInRound) {
                Assert.assertNotNull(fight.getTeam1());
                Assert.assertNotNull(fight.getTeam2());
                final String team1Name = fight.getTeam1().getName();
                final String team2Name = fight.getTeam2().getName();
                final boolean team1Wins = this.decideWinner(team1Name, team2Name, level);
                this.applyResult(fight, team1Wins);
                this.winsByTeam.merge(team1Wins ? team1Name : team2Name, 1, Integer::sum);
                this.winsByTeam.putIfAbsent(team1Wins ? team2Name : team1Name, 0);
                this.recordTrackedTeamResult(team1Name, team2Name, team1Wins);
            }

            // Next round is generated only after finishing all fights from current round.
            if (level < ROUNDS - 1) {
                Assert.assertEquals(this.groupController.getGroups(this.tournamentDTO, level + 1).stream()
                        .mapToLong(group -> group.getFights().size()).sum(), 0);
            }
        }

        Assert.assertEquals((int) this.winsByTeam.get(STREAK_THEN_COLLAPSE_TEAM), STREAK_WINS_UNTIL_ROUND_EXCLUSIVE);
        Assert.assertEquals((int) this.winsByTeam.get(COMEBACK_TEAM), ROUNDS - COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE);
        this.winsByTeam.forEach((teamName, wins) -> {
            if (!teamName.equals(STREAK_THEN_COLLAPSE_TEAM) && !teamName.equals(COMEBACK_TEAM)) {
                Assert.assertTrue(wins <= OTHER_TEAMS_MAX_WINS,
                        teamName + " must not win more than " + OTHER_TEAMS_MAX_WINS + " fights, but won " + wins);
            }
        });
    }

    /**
     * If {@code team1Name} or {@code team2Name} is one of the two tracked teams
     * ({@link #STREAK_THEN_COLLAPSE_TEAM} or {@link #COMEBACK_TEAM}), appends whether that
     * team won ({@code true}) or lost ({@code false}) this fight to
     * {@link #resultsByRoundForTrackedTeams}. Rounds are processed strictly in order (0, 1,
     * 2, ...), so the position of each entry in the list is exactly the Swiss round it
     * belongs to, e.g. {@code resultsByRoundForTrackedTeams.get(STREAK_THEN_COLLAPSE_TEAM)
     * .get(2)} is that team's result in round 2. Fights between two "other" teams are
     * ignored here since only the two tracked teams need this fine-grained log.
     *
     * @param team1Name  name of the first team of the fight.
     * @param team2Name  name of the second team of the fight.
     * @param team1Wins  {@code true} if team1 won this fight.
     */
    private void recordTrackedTeamResult(String team1Name, String team2Name, boolean team1Wins) {
        if (this.resultsByRoundForTrackedTeams.containsKey(team1Name)) {
            this.resultsByRoundForTrackedTeams.get(team1Name).add(team1Wins);
        }
        if (this.resultsByRoundForTrackedTeams.containsKey(team2Name)) {
            this.resultsByRoundForTrackedTeams.get(team2Name).add(!team1Wins);
        }
    }

    /**
     * Documents and verifies, round by round, the exact win/loss pattern of the two tracked
     * teams, using the log built while playing the tournament
     * ({@link #resultsByRoundForTrackedTeams}). This is what makes the scenario easy to
     * follow: instead of only trusting the final win totals, every single round of both
     * teams is checked against the scripted expectation:
     * <pre>
     * Round:                 0     1     2     3     4     5     6     7
     * Team01 (streak-then-collapse): WIN   WIN   WIN   WIN   LOSE  LOSE  LOSE  LOSE
     * Team02 (comeback):             LOSE  LOSE  WIN   WIN   WIN   WIN   WIN   WIN
     * </pre>
     */
    @Test(dependsOnMethods = "createAndAdvanceSwissRoundsWithCustomWinPattern")
    public void checkTrackedTeamsRoundByRoundPattern() {
        final List<Boolean> streakTeamResults = this.resultsByRoundForTrackedTeams.get(STREAK_THEN_COLLAPSE_TEAM);
        final List<Boolean> comebackTeamResults = this.resultsByRoundForTrackedTeams.get(COMEBACK_TEAM);

        // Both tracked teams must have fought exactly once per round.
        Assert.assertEquals(streakTeamResults.size(), ROUNDS,
                STREAK_THEN_COLLAPSE_TEAM + " must have exactly one recorded result per round");
        Assert.assertEquals(comebackTeamResults.size(), ROUNDS,
                COMEBACK_TEAM + " must have exactly one recorded result per round");

        for (int level = 0; level < ROUNDS; level++) {
            final boolean expectedStreakTeamWin = level < STREAK_WINS_UNTIL_ROUND_EXCLUSIVE;
            Assert.assertEquals(streakTeamResults.get(level), expectedStreakTeamWin,
                    "Round " + level + ": " + STREAK_THEN_COLLAPSE_TEAM + " was expected to "
                            + (expectedStreakTeamWin ? "WIN" : "LOSE") + " but it "
                            + (streakTeamResults.get(level) ? "WON" : "LOST"));

            final boolean expectedComebackTeamWin = level >= COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE;
            Assert.assertEquals(comebackTeamResults.get(level), expectedComebackTeamWin,
                    "Round " + level + ": " + COMEBACK_TEAM + " was expected to "
                            + (expectedComebackTeamWin ? "WIN" : "LOSE") + " but it "
                            + (comebackTeamResults.get(level) ? "WON" : "LOST"));
        }

        // Cross-check: the win streak/comeback boundaries themselves must be exactly where
        // the scenario says they are (the round where each team's pattern switches).
        Assert.assertTrue(streakTeamResults.subList(0, STREAK_WINS_UNTIL_ROUND_EXCLUSIVE).stream().allMatch(won -> won),
                STREAK_THEN_COLLAPSE_TEAM + " must win ALL of its first " + STREAK_WINS_UNTIL_ROUND_EXCLUSIVE + " rounds");
        Assert.assertTrue(streakTeamResults.subList(STREAK_WINS_UNTIL_ROUND_EXCLUSIVE, ROUNDS).stream()
                        .noneMatch(won -> won),
                STREAK_THEN_COLLAPSE_TEAM + " must lose ALL of its last " + (ROUNDS - STREAK_WINS_UNTIL_ROUND_EXCLUSIVE)
                        + " rounds");
        Assert.assertTrue(comebackTeamResults.subList(0, COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE).stream()
                        .noneMatch(won -> won),
                COMEBACK_TEAM + " must lose ALL of its first " + COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE + " rounds");
        Assert.assertTrue(comebackTeamResults.subList(COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE, ROUNDS).stream()
                        .allMatch(won -> won),
                COMEBACK_TEAM + " must win ALL of its remaining "
                        + (ROUNDS - COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE) + " rounds");
    }

    /**
     * Decides, for a single fight, whether team1 wins.
     *
     * @param team1Name name of the first team of the fight.
     * @param team2Name name of the second team of the fight.
     * @param level     the Swiss round (0-based) being played.
     * @return {@code true} if team1 must win this fight, {@code false} if team2 must win.
     */
    private boolean decideWinner(String team1Name, String team2Name, int level) {
        if (STREAK_THEN_COLLAPSE_TEAM.equals(team1Name) || STREAK_THEN_COLLAPSE_TEAM.equals(team2Name)) {
            final boolean streakTeamIsTeam1 = STREAK_THEN_COLLAPSE_TEAM.equals(team1Name);
            final boolean streakTeamWins = level < STREAK_WINS_UNTIL_ROUND_EXCLUSIVE;
            return streakTeamIsTeam1 == streakTeamWins;
        }
        if (COMEBACK_TEAM.equals(team1Name) || COMEBACK_TEAM.equals(team2Name)) {
            final boolean comebackTeamIsTeam1 = COMEBACK_TEAM.equals(team1Name);
            final boolean comebackTeamWins = level >= COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE;
            return comebackTeamIsTeam1 == comebackTeamWins;
        }

        final int wins1 = this.winsByTeam.getOrDefault(team1Name, 0);
        final int wins2 = this.winsByTeam.getOrDefault(team2Name, 0);

        // Swiss pairing sorts ALL teams by score and pairs them sequentially, so as soon
        // as two "other" teams are simultaneously sitting at the same high win count they
        // are likely to be paired against each other, and whichever wins would go one win
        // above that shared count. If that shared count is already the maximum allowed,
        // one of them would end up exceeding it. To avoid this, wins are paced: an "other"
        // team may only reach OTHER_TEAMS_MAX_WINS on the very last round; on every earlier
        // round its wins are bounded well below the cap (roughly proportionally to how many
        // rounds have been played), which keeps teams spread out and avoids clustering right
        // at the limit while rounds still remain.
        final int cap = this.maxOtherTeamWinsAfterRound(level);

        final boolean canWin1 = wins1 < cap;
        final boolean canWin2 = wins2 < cap;
        if (canWin1 && !canWin2) {
            return true;
        }
        if (canWin2 && !canWin1) {
            return false;
        }
        if (wins1 != wins2) {
            // Favour whichever team currently has fewer wins, keeping the distribution balanced.
            return wins1 < wins2;
        }
        // Tied on wins: alternate deterministically so the same team is not always favoured.
        this.tieBreakCounter++;
        return this.tieBreakCounter % 2 == 0;
    }

    /**
     * Computes the maximum wins an "other" team may have accumulated once round {@code level}
     * (0-based) is finished. The bound only reaches {@link #OTHER_TEAMS_MAX_WINS} on the last
     * round; before that it grows slower than proportionally to the rounds played, so that
     * "other" teams stay spread out and rarely reach the same win count exactly when there are
     * still rounds left to pair them together.
     *
     * @param level the Swiss round (0-based) being played.
     * @return the maximum number of wins allowed for an "other" team after this round.
     */
    private int maxOtherTeamWinsAfterRound(int level) {
        if (level == ROUNDS - 1) {
            return OTHER_TEAMS_MAX_WINS;
        }
        final int roundsPlayed = level + 1;
        // Ceiling division: proportional pace towards the cap, rounded up so round 0
        // already allows the single win a just-decided fight produces.
        final int paced = (roundsPlayed * OTHER_TEAMS_MAX_WINS + ROUNDS - 1) / ROUNDS;
        return Math.min(OTHER_TEAMS_MAX_WINS - 1, paced);
    }

    private void applyResult(Fight fight, boolean team1Wins) {
        if (team1Wins) {
            fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
            fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
        } else {
            fight.getDuels().getFirst().addCompetitor2Score(Score.MEN);
            fight.getDuels().getFirst().addCompetitor2Score(Score.MEN);
        }
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        this.fightController.update(this.fightConverter.convert(new FightConverterRequest(fight)), null, null);
    }

    @Test(dependsOnMethods = "checkTrackedTeamsRoundByRoundPattern")
    public void checkFinalRanking() {
        final List<ScoreOfTeam> ranking = this.rankingProvider
                .getTeamsScoreRanking(this.tournamentConverter.reverse(this.tournamentDTO));
        Assert.assertEquals(ranking.size(), TEAMS);

        final Map<String, Integer> wonFightsByTeam = new HashMap<>();
        ranking.forEach(score -> wonFightsByTeam.put(score.getTeam().getName(), score.getWonFights()));

        // The ranking (persisted, computed independently by RankingProvider) must match
        // exactly the wins this test decided while playing every round.
        this.winsByTeam.forEach((teamName, expectedWins) -> Assert.assertEquals(wonFightsByTeam.get(teamName),
                expectedWins, "Persisted wins for " + teamName + " must match the scripted result"));

        Assert.assertEquals((int) wonFightsByTeam.get(STREAK_THEN_COLLAPSE_TEAM), STREAK_WINS_UNTIL_ROUND_EXCLUSIVE,
                STREAK_THEN_COLLAPSE_TEAM + " must win exactly its first " + STREAK_WINS_UNTIL_ROUND_EXCLUSIVE
                        + " rounds and lose the rest");
        Assert.assertEquals((int) wonFightsByTeam.get(COMEBACK_TEAM), ROUNDS - COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE,
                COMEBACK_TEAM + " must lose exactly its first " + COMEBACK_LOSSES_UNTIL_ROUND_EXCLUSIVE
                        + " rounds and win the rest");

        wonFightsByTeam.forEach((teamName, wins) -> {
            Assert.assertTrue(wins >= 0 && wins <= ROUNDS, "Wins for " + teamName + " must be in range [0, " + ROUNDS + "]");
            if (!teamName.equals(STREAK_THEN_COLLAPSE_TEAM) && !teamName.equals(COMEBACK_TEAM)) {
                Assert.assertTrue(wins <= OTHER_TEAMS_MAX_WINS,
                        teamName + " must not win more than " + OTHER_TEAMS_MAX_WINS + " fights, but won " + wins);
            }
        });

        final int totalWins = wonFightsByTeam.values().stream().mapToInt(Integer::intValue).sum();
        Assert.assertEquals(totalWins, ROUNDS * FIGHTS_PER_ROUND, "Total wins must equal winners-per-round (no draws, no byes)");

        // Ranking must be sorted by won fights descending (primary Swiss ranking criterion).
        for (int i = 0; i < ranking.size() - 1; i++) {
            Assert.assertTrue(ranking.get(i).getWonFights() >= ranking.get(i + 1).getWonFights(),
                    "Ranking must be sorted by won fights descending: position " + i + " ("
                            + ranking.get(i).getTeam().getName() + ", " + ranking.get(i).getWonFights()
                            + ") vs position " + (i + 1) + " (" + ranking.get(i + 1).getTeam().getName() + ", "
                            + ranking.get(i + 1).getWonFights() + ")");
        }

        // The comeback team (6 wins) must rank strictly above the streak-then-collapse
        // team (4 wins), since the ranking is primarily sorted by won fights.
        final int comebackPosition = this.indexOfTeam(ranking, COMEBACK_TEAM);
        final int streakPosition = this.indexOfTeam(ranking, STREAK_THEN_COLLAPSE_TEAM);
        Assert.assertTrue(comebackPosition < streakPosition,
                COMEBACK_TEAM + " (6 wins) must be ranked above " + STREAK_THEN_COLLAPSE_TEAM + " (4 wins)");
    }

    private int indexOfTeam(List<ScoreOfTeam> ranking, String teamName) {
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getTeam().getName().equals(teamName)) {
                return i;
            }
        }
        throw new AssertionError("Team " + teamName + " not found in ranking");
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


