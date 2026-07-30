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
import com.softwaremagico.kt.persistence.values.SwissTieBreakRule;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
@Test(groups = {"swissTournament16TieBreakLevelByLevelTest"})
public class SwissTournament16TeamsTieBreakLevelByLevelTest extends AbstractTestNGSpringContextTests {

    private static final String CLUB_NAME = "Swiss16LevelByLevelClub";
    private static final String CLUB_CITY = "Swiss16LevelByLevelCity";
    private static final int MEMBERS = 3;
    private static final int TEAMS = 16;
    private static final int ROUNDS = 4;
    private static final int FIGHTS_PER_ROUND = 8;
    private static final String TOURNAMENT_NAME = "SwissTournament16TeamsTieBreakLevelByLevelTest";

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

    @Test
    public void addClub() {
        this.clubDTO = this.clubController.create(new ClubDTO(CLUB_NAME, CLUB_CITY), null, null);
    }

    @Test(dependsOnMethods = "addClub")
    public void addParticipants() {
        for (int i = 0; i < MEMBERS * TEAMS; i++) {
            this.participantController.create(new ParticipantDTO(String.format("S16LL-%04d", i),
                    String.format("name%s", i), String.format("lastname%s", i), this.clubDTO), null, null);
        }
    }

    @Test(dependsOnMethods = "addParticipants")
    public void addTournament() {
        this.tournamentDTO = this.tournamentController
                .create(new TournamentDTO(TOURNAMENT_NAME, 1, MEMBERS, TournamentType.SWISS), null, null);
    }

    @Test(dependsOnMethods = "addTournament")
    public void configureSwissProperties() {
        this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
                TournamentExtraPropertyKey.SWISS_ROUNDS, String.valueOf(ROUNDS)), null, null);
        this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
                TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, Boolean.FALSE.toString()), null, null);
        this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
                TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()), null, null);
    }

    @Test(dependsOnMethods = "configureSwissProperties")
    public void addRoles() {
        for (final ParticipantDTO competitor : this.participantController.get()) {
            this.roleController.create(new RoleDTO(this.tournamentDTO, competitor, RoleType.COMPETITOR), null, null);
        }
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
    }

    @Test(dependsOnMethods = "addTeams")
    public void shouldResolveTiesLevelByLevelUsingConfiguredFallbackChain() {
        final Map<Integer, List<String>> expectedPairingsByRound = this.getExpectedPairingsByRound();
        final Map<String, int[]> scoresByFight = this.getScoresByFight();

        for (int level = 0; level < ROUNDS; level++) {
            final List<FightDTO> createdFights = this.fightController.createNextFights(this.tournamentDTO.getId(), null,
                    null);
            Assert.assertEquals(createdFights.size(), FIGHTS_PER_ROUND);

            final List<Fight> fightsInRound = this.groupController.getGroups(this.tournamentDTO, level).stream()
                    .flatMap(group -> group.getFights().stream()).toList();
            Assert.assertEquals(fightsInRound.stream().map(this::fightKey).toList(), expectedPairingsByRound.get(level));

            for (final Fight fight : fightsInRound) {
                final int[] configuredScore = scoresByFight.get(level + ":" + this.fightKey(fight));
                Assert.assertNotNull(configuredScore,
                        "Missing configured score for fight " + level + ":" + this.fightKey(fight));
                this.applyResult(fight, configuredScore[0], configuredScore[1]);
            }

            this.assertTieBreakResolutionForLevel(level);
        }
    }

    private void assertTieBreakResolutionForLevel(int level) {
        final List<Fight> allFightsUntilLevel = SwissTestAssertions
                .getAllRoundFightsWithoutDuplicates(this.groupController, this.tournamentDTO, level + 1);
        final Map<String, Integer> pointsByTeam = SwissTestAssertions.getSwissPointsByTeam(allFightsUntilLevel);
        final List<String> teamNames = this.groupController.getGroups(this.tournamentDTO, 0).getFirst().getTeams().stream()
                .map(com.softwaremagico.kt.persistence.entities.Team::getName).toList();

        // The scenario below documents, round by round, which fallback depth should
        // be reached to resolve at least one tie for the selected primary rule.
        final SwissTieBreakRule selectedRule;
        final int expectedDepth;
        if (level == 0) {
            selectedRule = SwissTieBreakRule.BUCHHOLZ;
            expectedDepth = 5;
        } else if (level == 1) {
            selectedRule = SwissTieBreakRule.MEDIAN_BUCHHOLZ;
            expectedDepth = 5;
        } else if (level == 2) {
            selectedRule = SwissTieBreakRule.SONNEBORN_BERGER;
            expectedDepth = 5;
        } else {
            selectedRule = SwissTieBreakRule.DIRECT_ENCOUNTER;
            expectedDepth = 4;
        }

        final SwissTestAssertions.TieBreakExpectation expectation = SwissTestAssertions
                .findTieBreakExpectationAtDepth(teamNames, allFightsUntilLevel, pointsByTeam, selectedRule,
                        expectedDepth);
        Assert.assertNotNull(expectation,
                "Expected a tie resolved at depth " + expectedDepth + " for rule " + selectedRule
                        + " at Swiss level " + level);

        this.tournamentExtraPropertyController.update(new TournamentExtraPropertyDTO(this.tournamentDTO,
                TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, selectedRule.name()), null, null);

        final List<ScoreOfTeam> ranking = this.rankingProvider
                .getTeamsScoreRanking(this.tournamentConverter.reverse(this.tournamentDTO));
        final int higherPosition = SwissTestAssertions.getTeamPosition(ranking, expectation.getExpectedHigherTeam());
        final int lowerPosition = SwissTestAssertions.getTeamPosition(ranking, expectation.getExpectedLowerTeam());
        Assert.assertTrue(higherPosition < lowerPosition,
                "At Swiss level " + level + ", tie " + expectation.describePair() + " should be resolved by "
                        + expectation.getDecidingRule() + " (depth " + expectation.getDecidingDepth() + ") for "
                        + "selected rule " + selectedRule + ".");
    }

    private Map<Integer, List<String>> getExpectedPairingsByRound() {
        final Map<Integer, List<String>> pairings = new LinkedHashMap<>();
        pairings.put(0, List.of("Team01-Team02", "Team03-Team04", "Team05-Team06", "Team07-Team08", "Team09-Team10",
                "Team11-Team12", "Team13-Team14", "Team15-Team16"));
        pairings.put(1, List.of("Team01-Team03", "Team05-Team07", "Team09-Team11", "Team13-Team15", "Team02-Team04",
                "Team06-Team08", "Team10-Team12", "Team14-Team16"));
        pairings.put(2, List.of("Team01-Team05", "Team09-Team15", "Team02-Team03", "Team06-Team07", "Team10-Team11",
                "Team13-Team14", "Team04-Team08", "Team12-Team16"));
        pairings.put(3, List.of("Team01-Team15", "Team02-Team05", "Team07-Team09", "Team10-Team14", "Team03-Team04",
                "Team06-Team11", "Team12-Team13", "Team08-Team16"));
        return pairings;
    }

    private Map<String, int[]> getScoresByFight() {
        final Map<String, int[]> scores = new LinkedHashMap<>();
        scores.put("0:Team01-Team02", new int[]{2, 1});
        scores.put("0:Team03-Team04", new int[]{2, 0});
        scores.put("0:Team05-Team06", new int[]{2, 0});
        scores.put("0:Team07-Team08", new int[]{2, 1});
        scores.put("0:Team09-Team10", new int[]{2, 1});
        scores.put("0:Team11-Team12", new int[]{2, 1});
        scores.put("0:Team13-Team14", new int[]{2, 1});
        scores.put("0:Team15-Team16", new int[]{2, 1});

        scores.put("1:Team01-Team03", new int[]{2, 0});
        scores.put("1:Team05-Team07", new int[]{2, 0});
        scores.put("1:Team09-Team11", new int[]{2, 1});
        scores.put("1:Team13-Team15", new int[]{1, 2});
        scores.put("1:Team02-Team04", new int[]{2, 0});
        scores.put("1:Team06-Team08", new int[]{2, 0});
        scores.put("1:Team10-Team12", new int[]{2, 0});
        scores.put("1:Team14-Team16", new int[]{2, 0});

        scores.put("2:Team01-Team05", new int[]{2, 1});
        scores.put("2:Team09-Team15", new int[]{1, 2});
        scores.put("2:Team02-Team03", new int[]{2, 0});
        scores.put("2:Team06-Team07", new int[]{1, 2});
        scores.put("2:Team10-Team11", new int[]{2, 1});
        scores.put("2:Team13-Team14", new int[]{0, 2});
        scores.put("2:Team04-Team08", new int[]{2, 1});
        scores.put("2:Team12-Team16", new int[]{2, 0});

        scores.put("3:Team01-Team15", new int[]{2, 1});
        scores.put("3:Team02-Team05", new int[]{1, 2});
        scores.put("3:Team07-Team09", new int[]{1, 2});
        scores.put("3:Team10-Team14", new int[]{0, 2});
        scores.put("3:Team03-Team04", new int[]{2, 1});
        scores.put("3:Team06-Team11", new int[]{0, 2});
        scores.put("3:Team12-Team13", new int[]{2, 1});
        scores.put("3:Team08-Team16", new int[]{2, 0});
        return scores;
    }

    private String fightKey(Fight fight) {
        return fight.getTeam1().getName() + "-" + fight.getTeam2().getName();
    }

    private void applyResult(Fight fight, int team1Score, int team2Score) {
        for (int i = 0; i < team1Score; i++) {
            fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
        }
        for (int i = 0; i < team2Score; i++) {
            fight.getDuels().getFirst().addCompetitor2Score(Score.MEN);
        }
        fight.getDuels().forEach(duel -> duel.setFinished(true));
        this.fightController.update(this.fightConverter.convert(new FightConverterRequest(fight)), null, null);
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
    }
}

