package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import com.softwaremagico.kt.core.exceptions.GroupNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.score.CompetitorRanking;
import com.softwaremagico.kt.core.score.ScoreOfCompetitor;
import com.softwaremagico.kt.core.score.ScoreOfTeam;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@DisplayName("RankingProvider Tests")
public class RankingProviderTest {

     @Mock
     private FightProvider fightProvider;
     @Mock
     private DuelProvider duelProvider;
     @Mock
     private ParticipantProvider participantProvider;
     @Mock
     private TournamentRepository tournamentRepository;
     @Mock
     private GroupProvider groupProvider;
     @Mock
     private RoleProvider roleProvider;
     @Mock
     private TeamProvider teamProvider;

     private RankingProvider provider;

     @BeforeMethod(alwaysRun = true)
     public void setUp() {
         MockitoAnnotations.openMocks(this);
         provider = new RankingProvider(fightProvider, duelProvider, participantProvider, tournamentRepository,
                 groupProvider, roleProvider, teamProvider);
     }

     @Nested
     @DisplayName("Basic Competitors Ranking")
     class CompetitorsRankingTests {

         @Test
         @DisplayName("should_return_empty_list_when_no_competitors")
         void when_getCompetitorsScoreRanking_with_empty_expect_emptyList() {
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final List<ScoreOfCompetitor> ranking = provider.getCompetitorsScoreRanking(List.of(), List.of(), List.of(), tournament);

             assertThat(ranking).isEmpty();
         }

         @Test
         @DisplayName("should_return_all_competitors_with_scores")
         void when_getCompetitorsScoreRanking_with_multipleFighters_expect_allIncluded() {
             final Participant p1 = participant(1, "Ken", "Do");
             final Participant p2 = participant(2, "Ryu", "Gi");
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Fight fight = fight(List.of(p1), List.of(p2), LocalDateTime.now());

             final List<ScoreOfCompetitor> ranking = provider.getCompetitorsScoreRanking(List.of(p1, p2), List.of(fight), List.of(), tournament);

             assertThat(ranking).hasSize(2).extracting(ScoreOfCompetitor::getCompetitor).containsExactlyInAnyOrder(p1, p2);
         }

         @Test
         @DisplayName("should_filter_competitors_without_recent_fights_when_no_list_provided")
         void when_getCompetitorsGlobalScoreRanking_with_nullList_expect_onlyRecentFighters() {
             final Participant p1 = participant(1, "A", "One");
             final Participant p2 = participant(2, "B", "Two");
             final Participant p3 = participant(3, "C", "Three");

             final Fight recentFight = fight(List.of(p1), List.of(p2), LocalDateTime.now().minusDays(2));
             final Fight oldFight = fight(List.of(p2), List.of(p3), LocalDateTime.now().minusDays(90));

             when(participantProvider.getAll()).thenReturn(new ArrayList<>(List.of(p1, p2, p3)));
             when(fightProvider.getBy(any(Collection.class))).thenReturn(List.of(recentFight, oldFight));
             when(duelProvider.getUnties(any(Collection.class))).thenReturn(List.of());

             final List<ScoreOfCompetitor> ranking = provider.getCompetitorsGlobalScoreRanking(null, ScoreType.DEFAULT, 30);

             assertThat(ranking).hasSize(2).extracting(ScoreOfCompetitor::getCompetitor).containsExactlyInAnyOrder(p1, p2);
         }

         @Test
         @DisplayName("should_show_all_competitors_when_explicit_list_provided")
         void when_getCompetitorsGlobalScoreRanking_with_providedList_expect_allProvided() {
             final Participant p1 = participant(1, "A", "One");
             final Participant p2 = participant(2, "B", "Two");
             final Participant p3 = participant(3, "C", "Three");

             final Fight fight = fight(List.of(p1), List.of(p2), LocalDateTime.now());

             when(fightProvider.getBy(any(Collection.class))).thenReturn(List.of(fight));
             when(duelProvider.getUnties(any(Collection.class))).thenReturn(List.of());

             final List<ScoreOfCompetitor> ranking = provider.getCompetitorsGlobalScoreRanking(List.of(p1, p2, p3), ScoreType.DEFAULT, 30);

             assertThat(ranking).hasSize(3);
         }

         @Test
         @DisplayName("should_return_global_ranking_sorted")
         void when_getCompetitorGlobalRanking_expect_sortedRanking() {
             final Participant p1 = participant(1, "A", "One");
             final Participant p2 = participant(2, "B", "Two");
             final Tournament tournament = tournament(TournamentType.LEAGUE);

             final Role role1 = new Role(tournament, p1, RoleType.COMPETITOR);
             final Role role2 = new Role(tournament, p2, RoleType.COMPETITOR);

             when(roleProvider.getAll()).thenReturn(List.of(role1, role2));
             when(fightProvider.getAll()).thenReturn(List.of());
             when(duelProvider.getUnties()).thenReturn(List.of());

             final List<ScoreOfCompetitor> ranking = provider.getCompetitorGlobalRanking(ScoreType.DEFAULT);

             assertThat(ranking).hasSize(2);
         }
     }

     @Nested
     @DisplayName("Teams Ranking Tests")
     class TeamsRankingTests {

          @Test
          @DisplayName("should_return_empty_list_when_group_null")
          void when_getTeamsScoreRanking_with_nullGroup_expect_empty() {
              final Group nullGroup = null;
              final List<ScoreOfTeam> ranking = provider.getTeamsScoreRanking(nullGroup);

             assertThat(ranking).isEmpty();
         }

         @Test
         @DisplayName("should_return_teams_ranked_by_group")
         void when_getTeamsScoreRanking_with_validGroup_expect_rankedTeams() {
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Group group = group(tournament);
             final Team team1 = team(1, "Team A", tournament);
             final Team team2 = team(2, "Team B", tournament);
             group.setTeams(List.of(team1, team2));
             group.setFights(List.of());
             group.setUnties(List.of());

             final List<ScoreOfTeam> ranking = provider.getTeamsScoreRanking(group);

             assertThat(ranking).hasSize(2).extracting(ScoreOfTeam::getTeam).containsExactlyInAnyOrder(team1, team2);
         }

         @Test
         @DisplayName("should_set_sorting_indices_for_all_scores")
         void when_getTeamsScoreRanking_with_multipleTeams_expect_sortingIndices() {
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Team team1 = team(1, "Team A", tournament);
             final Team team2 = team(2, "Team B", tournament);
             final Team team3 = team(3, "Team C", tournament);

             final List<ScoreOfTeam> ranking = provider.getTeamsScoreRanking(ScoreType.CLASSIC, List.of(team1, team2, team3), List.of(), List.of(), true);

             assertThat(ranking).allMatch(score -> score.getSortingIndex() != null);
         }

         @Test
         @DisplayName("should_group_teams_by_position_with_ties")
         void when_getTeamsByPosition_with_tiedTeams_expect_grouped() {
             final RankingProvider spyProvider = Mockito.spy(provider);
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Group group = group(tournament);

             final Team teamA = team(1, "A", tournament);
             final Team teamB = team(2, "B", tournament);
             final Team teamC = team(3, "C", tournament);
             final Team teamD = team(4, "D", tournament);

             final ScoreOfTeam scoreA = score(teamA, 3, 0, 6, 12, 0, 0);
             final ScoreOfTeam scoreB = score(teamB, 2, 0, 4, 8, 0, 0);
             final ScoreOfTeam scoreC = score(teamC, 2, 0, 4, 8, 0, 0);
             final ScoreOfTeam scoreD = score(teamD, 1, 0, 2, 4, 0, 0);

             doReturn(List.of(scoreA, scoreB, scoreC, scoreD)).when(spyProvider).getTeamsScoreRanking(group);

             final Map<Integer, List<Team>> teamsByPosition = spyProvider.getTeamsByPosition(group);

             assertThat(teamsByPosition).hasSize(3);
             assertThat(teamsByPosition.get(0)).containsExactly(teamA);
             assertThat(teamsByPosition.get(1)).containsExactlyInAnyOrder(teamB, teamC);
             assertThat(teamsByPosition.get(2)).containsExactly(teamD);
         }

         @Test
         @DisplayName("should_return_teams_with_draw_scores")
         void when_getFirstTeamsWithDrawScore_expect_drawTeams() {
             final RankingProvider spyProvider = Mockito.spy(provider);
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Group group = group(tournament);

             final Team teamA = team(1, "A", tournament);
             final Team teamB = team(2, "B", tournament);
             final Team teamC = team(3, "C", tournament);

             final ScoreOfTeam scoreA = score(teamA, 3, 0, 6, 12, 0, 0);
             final ScoreOfTeam scoreB = score(teamB, 2, 0, 4, 8, 0, 0);
             final ScoreOfTeam scoreC = score(teamC, 2, 0, 4, 8, 0, 0);

             doReturn(List.of(scoreA, scoreB, scoreC)).when(spyProvider).getTeamsScoreRanking(group);

             final List<Team> drawTeams = spyProvider.getFirstTeamsWithDrawScore(group, 2);

             assertThat(drawTeams).containsExactlyInAnyOrder(teamB, teamC);
         }

         @Test
         @DisplayName("should_return_empty_when_no_draw_teams_within_positions")
         void when_getFirstTeamsWithDrawScore_without_draws_expect_empty() {
             final RankingProvider spyProvider = Mockito.spy(provider);
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Group group = group(tournament);

             final Team teamA = team(1, "A", tournament);
             final Team teamB = team(2, "B", tournament);

             final ScoreOfTeam scoreA = score(teamA, 3, 0, 6, 12, 0, 0);
             final ScoreOfTeam scoreB = score(teamB, 2, 0, 4, 8, 0, 0);

             doReturn(List.of(scoreA, scoreB)).when(spyProvider).getTeamsScoreRanking(group);

             final List<Team> drawTeams = spyProvider.getFirstTeamsWithDrawScore(group, 2);

             assertThat(drawTeams).isEmpty();
         }
     }

     @Nested
     @DisplayName("Tournament Type Specific Tests")
     class TournamentTypeTests {

         @Test
         @DisplayName("should_count_not_over_fights_only_for_king_of_mountain")
         void when_countNotOver_with_kingOfMountain_expect_true() {
             final Participant p1 = participant(11, "Ken", "Do");
             final Participant p2 = participant(12, "Ryu", "Gi");

             final Fight notFinishedFight = fight(List.of(p1), List.of(p2), LocalDateTime.now().minusDays(1));
             notFinishedFight.getDuels().get(0).addCompetitor1Score(Score.MEN);

             final Tournament leagueTournament = tournament(TournamentType.LEAGUE);
             final Tournament kingTournament = tournament(TournamentType.KING_OF_THE_MOUNTAIN);

             final List<ScoreOfCompetitor> leagueRanking = provider.getCompetitorsScoreRanking(List.of(p1, p2),
                     List.of(notFinishedFight), List.of(), leagueTournament);
             final List<ScoreOfCompetitor> kingRanking = provider.getCompetitorsScoreRanking(List.of(p1, p2),
                     List.of(notFinishedFight), List.of(), kingTournament);

             final ScoreOfCompetitor leagueP1 = leagueRanking.stream()
                     .filter(score -> score.getCompetitor().equals(p1)).findFirst().orElseThrow();
             final ScoreOfCompetitor kingP1 = kingRanking.stream()
                     .filter(score -> score.getCompetitor().equals(p1)).findFirst().orElseThrow();

             assertThat(leagueP1.getWonDuels()).isZero();
             assertThat(kingP1.getWonDuels()).isOne();
         }
     }

     @Nested
     @DisplayName("Individual Lookup Tests")
     class IndividualLookupTests {

         @Test
         @DisplayName("should_get_score_of_competitor_by_order")
         void when_getScoreRanking_expect_score() {
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Group group = group(tournament);
             final Participant p1 = participant(1, "Ken", "Do");
             final Team team = team(1, "Team", tournament);
             team.setMembers(List.of(p1));
             group.setTeams(List.of(team));
             group.setFights(List.of());
             group.setUnties(List.of());

             final ScoreOfCompetitor score = provider.getScoreRanking(group, p1);

             assertThat(score).isNotNull().extracting(ScoreOfCompetitor::getCompetitor).isEqualTo(p1);
         }

         @Test
         @DisplayName("should_return_null_when_competitor_not_in_group")
         void when_getScoreRanking_with_missingCompetitor_expect_null() {
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Group group = group(tournament);
             final Participant p1 = participant(1, "Ken", "Do");
             final Participant p2 = participant(2, "Ryu", "Gi");
             final Team team = team(1, "Team", tournament);
             team.setMembers(List.of(p1));
             group.setTeams(List.of(team));
             group.setFights(List.of());
             group.setUnties(List.of());

             final ScoreOfCompetitor score = provider.getScoreRanking(group, p2);

             assertThat(score).isNull();
         }

         @Test
         @DisplayName("should_return_last_position_when_competitor_not_found")
         void when_getCompetitorRanking_with_missing_expect_lastPosition() {
             final RankingProvider spyProvider = Mockito.spy(provider);
             final Participant p1 = participant(1, "Ken", "Do");
             final Participant p2 = participant(2, "Ryu", "Gi");
             final Participant missing = participant(3, "C", "Three");

             final ScoreOfCompetitor score1 = new ScoreOfCompetitor();
             score1.setCompetitor(p1);
             final ScoreOfCompetitor score2 = new ScoreOfCompetitor();
             score2.setCompetitor(p2);

             doReturn(List.of(score1, score2)).when(spyProvider).getCompetitorGlobalRanking(ScoreType.DEFAULT);

             final CompetitorRanking ranking = spyProvider.getCompetitorRanking(missing);

             assertThat(ranking.getRanking()).isEqualTo(1);
             assertThat(ranking.getTotal()).isEqualTo(2);
         }

         @Test
         @DisplayName("should_get_order_from_ranking_list")
         void when_getOrderFromRanking_with_validTeam_expect_order() {
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             final Team team1 = team(1, "Team A", tournament);
             final Team team2 = team(2, "Team B", tournament);

             final ScoreOfTeam score1 = score(team1, 3, 0, 6, 12, 0, 0);
             final ScoreOfTeam score2 = score(team2, 2, 0, 4, 8, 0, 0);

             final Integer order = provider.getOrderFromRanking(List.of(score1, score2), team1);

             assertThat(order).isZero();
         }
     }

     @Nested
     @DisplayName("Exception Handling Tests")
     class ExceptionHandlingTests {

         @Test
         @DisplayName("should_throw_TournamentNotFoundException_when_not_found")
         void when_getCompetitorsScoreRankingFromTournament_with_invalidId_expect_exception() {
             when(tournamentRepository.findById(999)).thenReturn(Optional.empty());

             assertThatThrownBy(() -> provider.getCompetitorsScoreRankingFromTournament(999))
                     .isInstanceOf(TournamentNotFoundException.class);
         }

         @Test
         @DisplayName("should_throw_GroupNotFoundException_when_not_found")
         void when_getTeamsRanking_with_invalidGroupId_expect_exception() {
             when(groupProvider.getGroup(999)).thenReturn(null);

             assertThatThrownBy(() -> provider.getTeamsRanking(999))
                     .isInstanceOf(GroupNotFoundException.class);
         }

         @Test
         @DisplayName("should_return_ranking_when_tournament_exists")
         void when_getTeamsScoreRankingFromTournament_with_validId_expect_ranking() {
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             tournament.setId(100);
             when(tournamentRepository.findById(100)).thenReturn(Optional.of(tournament));
             when(teamProvider.getAll(tournament)).thenReturn(List.of());
             when(fightProvider.getFights(tournament)).thenReturn(List.of());
             when(groupProvider.getGroups(tournament)).thenReturn(List.of());

             final List<ScoreOfTeam> ranking = provider.getTeamsScoreRankingFromTournament(100);

             assertThat(ranking).isNotNull();
         }

         @Test
         @DisplayName("should_return_competitors_ranking_from_tournament_id")
         void when_getCompetitorsScoreRankingFromTournament_with_validId_expect_ranking() {
             final Tournament tournament = tournament(TournamentType.LEAGUE);
             tournament.setId(100);
             when(tournamentRepository.findById(100)).thenReturn(Optional.of(tournament));
             when(groupProvider.getGroups(tournament)).thenReturn(List.of());

             final List<ScoreOfCompetitor> ranking = provider.getCompetitorsScoreRankingFromTournament(100);

             assertThat(ranking).isNotNull();
         }
     }

     private Tournament tournament(TournamentType type) {
         final Tournament tournament = new Tournament("T", 1, 1, type, "tester", ScoreType.INTERNATIONAL);
         tournament.setId(100);
         return tournament;
     }

     private Group group(Tournament tournament) {
         final Group group = new Group(tournament, 0, 0);
         group.setTeams(new ArrayList<>());
         group.setFights(new ArrayList<>());
         group.setUnties(new ArrayList<>());
         return group;
     }

     private Participant participant(int id, String name, String lastname) {
         final Club club = new Club("Club " + id, "ES", "City");
         club.setId(id);
         final Participant participant = new Participant("ID" + id, name, lastname, club);
         participant.setId(id);
         return participant;
     }

     private Team team(int id, String name, Tournament tournament) {
         final Team team = new Team(name, tournament);
         team.setId(id);
         team.setMembers(new ArrayList<>());
         return team;
     }

     private Fight fight(List<Participant> members1, List<Participant> members2, LocalDateTime createdAt) {
         final Tournament tournament = tournament(TournamentType.LEAGUE);
         final Team team1 = team(200 + members1.get(0).getId(), "T1-" + members1.get(0).getId(), tournament);
         final Team team2 = team(300 + members2.get(0).getId(), "T2-" + members2.get(0).getId(), tournament);
         team1.setMembers(new ArrayList<>(members1));
         team2.setMembers(new ArrayList<>(members2));
         final Fight fight = new Fight(tournament, team1, team2, 0, 0, "tester");
         fight.setCreatedAt(createdAt);
         return fight;
     }

     private ScoreOfTeam score(Team team, int wonFights, int drawFights, int wonDuels, int hits, int unties, int level) {
         final ScoreOfTeam score = new ScoreOfTeam();
         score.setTeam(team);
         score.setWonFights(wonFights);
         score.setDrawFights(drawFights);
         score.setWonDuels(wonDuels);
         score.setHits(hits);
         score.setUntieDuels(unties);
         score.setLevel(level);
         return score;
     }
 }

