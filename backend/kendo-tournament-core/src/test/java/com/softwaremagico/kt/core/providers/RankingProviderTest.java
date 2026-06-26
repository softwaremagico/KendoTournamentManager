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
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.Score;
import com.softwaremagico.kt.persistence.values.ScoreType;
import com.softwaremagico.kt.persistence.values.SwissTieBreakRule;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@Test(groups = {"rankingProviderTests"})
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
	@Mock
	private TournamentExtraPropertyProvider tournamentExtraPropertyProvider;

	private RankingProvider provider;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		this.provider = new RankingProvider(this.fightProvider, this.duelProvider, this.participantProvider,
				this.tournamentRepository, this.groupProvider, this.roleProvider, this.teamProvider,
				this.tournamentExtraPropertyProvider);
	}

	// ========== Competitors Ranking Tests ==========

	@Test
	public void testGetCompetitorsScoreRankingWithEmptyList() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final List<ScoreOfCompetitor> ranking = this.provider.getCompetitorsScoreRanking(List.of(), List.of(),
				List.of(), tournament);
		assertThat(ranking).isEmpty();
	}

	@Test
	public void testGetCompetitorsScoreRankingWithMultipleFighters() {
		final Participant p1 = this.participant(1, "Ken", "Do");
		final Participant p2 = this.participant(2, "Ryu", "Gi");
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Fight fight = this.fight(List.of(p1), List.of(p2), LocalDateTime.now());

		final List<ScoreOfCompetitor> ranking = this.provider.getCompetitorsScoreRanking(List.of(p1, p2),
				List.of(fight), List.of(), tournament);

		assertThat(ranking).hasSize(2).extracting(ScoreOfCompetitor::getCompetitor).containsExactlyInAnyOrder(p1, p2);
	}

	@Test
	public void testGetCompetitorsGlobalScoreRankingFiltersOldFights() {
		final Participant p1 = this.participant(1, "A", "One");
		final Participant p2 = this.participant(2, "B", "Two");
		final Participant p3 = this.participant(3, "C", "Three");

		final Fight recentFight = this.fight(List.of(p1), List.of(p2), LocalDateTime.now().minusDays(2));
		final Fight oldFight = this.fight(List.of(p2), List.of(p3), LocalDateTime.now().minusDays(90));

		when(this.participantProvider.getAll()).thenReturn(new ArrayList<>(List.of(p1, p2, p3)));
		when(this.fightProvider.getBy(any(Collection.class))).thenReturn(List.of(recentFight, oldFight));
		when(this.duelProvider.getUnties(any(Collection.class))).thenReturn(List.of());

		final List<ScoreOfCompetitor> ranking = this.provider.getCompetitorsGlobalScoreRanking(null, ScoreType.DEFAULT,
				30);

		assertThat(ranking).hasSize(2).extracting(ScoreOfCompetitor::getCompetitor).containsExactlyInAnyOrder(p1, p2);
	}

	@Test
	public void testGetCompetitorsGlobalScoreRankingWithProvidedList() {
		final Participant p1 = this.participant(1, "A", "One");
		final Participant p2 = this.participant(2, "B", "Two");
		final Participant p3 = this.participant(3, "C", "Three");

		final Fight fight = this.fight(List.of(p1), List.of(p2), LocalDateTime.now());

		when(this.fightProvider.getBy(any(Collection.class))).thenReturn(List.of(fight));
		when(this.duelProvider.getUnties(any(Collection.class))).thenReturn(List.of());

		final List<ScoreOfCompetitor> ranking = this.provider.getCompetitorsGlobalScoreRanking(List.of(p1, p2, p3),
				ScoreType.DEFAULT, 30);

		assertThat(ranking).hasSize(3);
	}

	@Test
	public void testGetCompetitorsGlobalScoreRankingWithZeroDaysUsesNoDateFilter() {
		final Participant p1 = this.participant(4, "A", "One");
		final Participant p2 = this.participant(5, "B", "Two");
		final Participant p3 = this.participant(6, "C", "Three");

		final Fight recentFight = this.fight(List.of(p1), List.of(p2), LocalDateTime.of(2026, 6, 20, 12, 0));
		final Fight oldFight = this.fight(List.of(p2), List.of(p3), LocalDateTime.of(2026, 1, 1, 12, 0));

		when(this.participantProvider.getAll()).thenReturn(new ArrayList<>(List.of(p1, p2, p3)));
		when(this.fightProvider.getBy(any(Collection.class))).thenReturn(List.of(recentFight, oldFight));
		when(this.duelProvider.getUnties(any(Collection.class))).thenReturn(List.of());

		final List<ScoreOfCompetitor> ranking = this.provider.getCompetitorsGlobalScoreRanking(null, ScoreType.DEFAULT,
				0);

		assertThat(ranking).hasSize(3);
	}

	@Test
	public void testGetCompetitorsGlobalScoreRankingFiltersUntiesByDateRange() {
		final Participant p1 = this.participant(7, "A", "One");
		final Participant p2 = this.participant(8, "B", "Two");
		final Participant p3 = this.participant(9, "C", "Three");
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);

		final Fight fight = this.fight(List.of(p1), List.of(p2), LocalDateTime.of(2026, 6, 20, 12, 0));
		final Duel recentUntie = new Duel(p1, p2, tournament, "tester");
		recentUntie.setCreatedAt(LocalDateTime.of(2026, 6, 22, 12, 0));
		final Duel oldUntie = new Duel(p2, p3, tournament, "tester");
		oldUntie.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));

		when(this.participantProvider.getAll()).thenReturn(new ArrayList<>(List.of(p1, p2, p3)));
		when(this.fightProvider.getBy(any(Collection.class))).thenReturn(List.of(fight));
		when(this.duelProvider.getUnties(any(Collection.class))).thenReturn(List.of(recentUntie, oldUntie));

		final List<ScoreOfCompetitor> ranking = this.provider
				.getCompetitorsGlobalScoreRanking(new ArrayList<>(List.of(p1, p2, p3)), ScoreType.DEFAULT, 30);

		assertThat(ranking).hasSize(3);
	}

	@Test
	public void testGetCompetitorsGlobalScoreRankingWithUntiesAndNullDaysUsesNoDateFilter() {
		final Participant p1 = this.participant(10, "A", "One");
		final Participant p2 = this.participant(11, "B", "Two");
		final Participant p3 = this.participant(12, "C", "Three");
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);

		final Fight fight = this.fight(List.of(p1), List.of(p2), LocalDateTime.of(2026, 6, 20, 12, 0));
		final Duel recentUntie = new Duel(p1, p2, tournament, "tester");
		recentUntie.setCreatedAt(LocalDateTime.of(2026, 6, 22, 12, 0));
		final Duel oldUntie = new Duel(p2, p3, tournament, "tester");
		oldUntie.setCreatedAt(LocalDateTime.of(2026, 1, 1, 12, 0));

		when(this.participantProvider.getAll()).thenReturn(new ArrayList<>(List.of(p1, p2, p3)));
		when(this.fightProvider.getBy(any(Collection.class))).thenReturn(List.of(fight));
		when(this.duelProvider.getUnties(any(Collection.class))).thenReturn(List.of(recentUntie, oldUntie));

		final List<ScoreOfCompetitor> ranking = this.provider
				.getCompetitorsGlobalScoreRanking(new ArrayList<>(List.of(p1, p2, p3)), ScoreType.DEFAULT, null);

		assertThat(ranking).hasSize(3);
	}

	@Test
	public void testGetCompetitorGlobalRanking() {
		final Participant p1 = this.participant(1, "A", "One");
		final Participant p2 = this.participant(2, "B", "Two");
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);

		final Role role1 = new Role(tournament, p1, RoleType.COMPETITOR);
		final Role role2 = new Role(tournament, p2, RoleType.COMPETITOR);

		when(this.roleProvider.getAll()).thenReturn(List.of(role1, role2));
		when(this.fightProvider.getAll()).thenReturn(List.of());
		when(this.duelProvider.getUnties()).thenReturn(List.of());

		final List<ScoreOfCompetitor> ranking = this.provider.getCompetitorGlobalRanking(ScoreType.DEFAULT);

		assertThat(ranking).hasSize(2);
	}

	@Test
	public void testGetCompetitorsScoreRankingFromTournament() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		tournament.setId(100);
		when(this.tournamentRepository.findById(100)).thenReturn(Optional.of(tournament));
		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of());

		final List<ScoreOfCompetitor> ranking = this.provider.getCompetitorsScoreRankingFromTournament(100);

		assertThat(ranking).isNotNull();
	}

	// ========== Teams Ranking Tests ==========

	@Test
	public void testGetTeamsScoreRankingWithNullGroup() {
		final Group nullGroup = null;
		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(nullGroup);
		assertThat(ranking).isEmpty();
	}

	@Test
	public void testGetTeamsScoreRankingWithValidGroup() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);
		group.setTeams(List.of(team1, team2));
		group.setFights(List.of());
		group.setUnties(List.of());

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(group);

		assertThat(ranking).hasSize(2).extracting(ScoreOfTeam::getTeam).containsExactlyInAnyOrder(team1, team2);
	}

	@Test
	public void testGetTeamsScoreRankingWithSortingIndices() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);
		final Team team3 = this.team(3, "Team C", tournament);

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(ScoreType.CLASSIC,
				List.of(team1, team2, team3), List.of(), List.of(), true);

		assertThat(ranking).isNotEmpty().allMatch(score -> score.getSortingIndex() != null);
	}

	@Test
	public void testGetTeamsByPositionWithTiedTeams() {
		final RankingProvider spyProvider = Mockito.spy(this.provider);
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);

		final Team teamA = this.team(1, "A", tournament);
		final Team teamB = this.team(2, "B", tournament);
		final Team teamC = this.team(3, "C", tournament);
		final Team teamD = this.team(4, "D", tournament);

		final ScoreOfTeam scoreA = this.score(teamA, 3, 0, 6, 12, 0, 0);
		final ScoreOfTeam scoreB = this.score(teamB, 2, 0, 4, 8, 0, 0);
		final ScoreOfTeam scoreC = this.score(teamC, 2, 0, 4, 8, 0, 0);
		final ScoreOfTeam scoreD = this.score(teamD, 1, 0, 2, 4, 0, 0);

		doReturn(List.of(scoreA, scoreB, scoreC, scoreD)).when(spyProvider).getTeamsScoreRanking(group);

		final Map<Integer, List<Team>> teamsByPosition = spyProvider.getTeamsByPosition(group);

		assertThat(teamsByPosition).hasSize(3);
		assertThat(teamsByPosition.get(0)).containsExactly(teamA);
		assertThat(teamsByPosition.get(1)).containsExactlyInAnyOrder(teamB, teamC);
		assertThat(teamsByPosition.get(2)).containsExactly(teamD);
	}

	@Test
	public void testGetFirstTeamsWithDrawScore() {
		final RankingProvider spyProvider = Mockito.spy(this.provider);
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);

		final Team teamA = this.team(1, "A", tournament);
		final Team teamB = this.team(2, "B", tournament);
		final Team teamC = this.team(3, "C", tournament);

		final ScoreOfTeam scoreA = this.score(teamA, 3, 0, 6, 12, 0, 0);
		final ScoreOfTeam scoreB = this.score(teamB, 2, 0, 4, 8, 0, 0);
		final ScoreOfTeam scoreC = this.score(teamC, 2, 0, 4, 8, 0, 0);

		doReturn(List.of(scoreA, scoreB, scoreC)).when(spyProvider).getTeamsScoreRanking(group);

		final List<Team> drawTeams = spyProvider.getFirstTeamsWithDrawScore(group, 2);

		assertThat(drawTeams).containsExactlyInAnyOrder(teamB, teamC);
	}

	@Test
	public void testGetFirstTeamsWithDrawScoreWhenNoDraws() {
		final RankingProvider spyProvider = Mockito.spy(this.provider);
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);

		final Team teamA = this.team(1, "A", tournament);
		final Team teamB = this.team(2, "B", tournament);

		final ScoreOfTeam scoreA = this.score(teamA, 3, 0, 6, 12, 0, 0);
		final ScoreOfTeam scoreB = this.score(teamB, 2, 0, 4, 8, 0, 0);

		doReturn(List.of(scoreA, scoreB)).when(spyProvider).getTeamsScoreRanking(group);

		final List<Team> drawTeams = spyProvider.getFirstTeamsWithDrawScore(group, 2);

		assertThat(drawTeams).isEmpty();
	}

	@Test
	public void testGetTeamsScoreRankingFromTournament() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		tournament.setId(100);
		when(this.tournamentRepository.findById(100)).thenReturn(Optional.of(tournament));
		when(this.teamProvider.getAll(tournament)).thenReturn(List.of());
		when(this.fightProvider.getFights(tournament)).thenReturn(List.of());
		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of());

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRankingFromTournament(100);

		assertThat(ranking).isNotNull();
	}

	// ========== Tournament Type Tests ==========

	@Test
	public void testCountNotOverFightsForKingOfMountain() {
		final Participant p1 = this.participant(11, "Ken", "Do");
		final Participant p2 = this.participant(12, "Ryu", "Gi");

		final Fight notFinishedFight = this.fight(List.of(p1), List.of(p2), LocalDateTime.now().minusDays(1));
		notFinishedFight.getDuels().get(0).addCompetitor1Score(Score.MEN);

		final Tournament leagueTournament = this.tournament(TournamentType.LEAGUE);
		final Tournament kingTournament = this.tournament(TournamentType.KING_OF_THE_MOUNTAIN);

		final List<ScoreOfCompetitor> leagueRanking = this.provider.getCompetitorsScoreRanking(List.of(p1, p2),
				List.of(notFinishedFight), List.of(), leagueTournament);
		final List<ScoreOfCompetitor> kingRanking = this.provider.getCompetitorsScoreRanking(List.of(p1, p2),
				List.of(notFinishedFight), List.of(), kingTournament);

		final ScoreOfCompetitor leagueP1 = leagueRanking.stream().filter(score -> score.getCompetitor().equals(p1))
				.findFirst().orElseThrow();
		final ScoreOfCompetitor kingP1 = kingRanking.stream().filter(score -> score.getCompetitor().equals(p1))
				.findFirst().orElseThrow();

		assertThat(leagueP1.getWonDuels()).isZero();
		assertThat(kingP1.getWonDuels()).isOne();
	}

	// ========== Individual Lookup Tests ==========

	@Test
	public void testGetScoreRanking() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Participant p1 = this.participant(1, "Ken", "Do");
		final Team team = this.team(1, "Team", tournament);
		team.setMembers(List.of(p1));
		group.setTeams(List.of(team));
		group.setFights(List.of());
		group.setUnties(List.of());

		final ScoreOfCompetitor score = this.provider.getScoreRanking(group, p1);

		assertThat(score).isNotNull().extracting(ScoreOfCompetitor::getCompetitor).isEqualTo(p1);
	}

	@Test
	public void testGetScoreRankingWithMissingCompetitor() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Participant p1 = this.participant(1, "Ken", "Do");
		final Participant p2 = this.participant(2, "Ryu", "Gi");
		final Team team = this.team(1, "Team", tournament);
		team.setMembers(List.of(p1));
		group.setTeams(List.of(team));
		group.setFights(List.of());
		group.setUnties(List.of());

		final ScoreOfCompetitor score = this.provider.getScoreRanking(group, p2);

		assertThat(score).isNull();
	}

	@Test
	public void testGetCompetitorRankingWithMissing() {
		final RankingProvider spyProvider = Mockito.spy(this.provider);
		final Participant p1 = this.participant(1, "Ken", "Do");
		final Participant p2 = this.participant(2, "Ryu", "Gi");
		final Participant missing = this.participant(3, "C", "Three");

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
	public void testGetCompetitorRankingWhenFound() {
		final RankingProvider spyProvider = Mockito.spy(this.provider);
		final Participant p1 = this.participant(1, "Ken", "Do");
		final Participant p2 = this.participant(2, "Ryu", "Gi");

		final ScoreOfCompetitor score1 = new ScoreOfCompetitor();
		score1.setCompetitor(p1);
		final ScoreOfCompetitor score2 = new ScoreOfCompetitor();
		score2.setCompetitor(p2);

		doReturn(List.of(score1, score2)).when(spyProvider).getCompetitorGlobalRanking(ScoreType.DEFAULT);

		final CompetitorRanking ranking = spyProvider.getCompetitorRanking(p1);

		assertThat(ranking.getRanking()).isZero();
		assertThat(ranking.getTotal()).isEqualTo(2);
	}

	@Test
	public void testGetOrderFromRanking() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);

		final ScoreOfTeam score1 = this.score(team1, 3, 0, 6, 12, 0, 0);
		final ScoreOfTeam score2 = this.score(team2, 2, 0, 4, 8, 0, 0);

		final Integer order = this.provider.getOrderFromRanking(List.of(score1, score2), team1);

		assertThat(order).isZero();
	}

	@Test
	public void testGetOrderFromRankingWithMissingTeam() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team missingTeam = this.team(3, "Team C", tournament);

		final ScoreOfTeam score1 = this.score(team1, 3, 0, 6, 12, 0, 0);

		final Integer order = this.provider.getOrderFromRanking(List.of(score1), missingTeam);

		assertThat(order).isNull();
	}

	@Test
	public void testGetTeamsRanking() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);
		group.setTeams(List.of(team1, team2));
		group.setFights(List.of());
		group.setUnties(List.of());

		final List<Team> ranking = this.provider.getTeamsRanking(group);

		assertThat(ranking).hasSize(2);
	}

	@Test
	public void testGetTeamsRankingById() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		group.setId(1);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);
		group.setTeams(List.of(team1, team2));
		group.setFights(List.of());
		group.setUnties(List.of());

		when(this.groupProvider.getGroup(1)).thenReturn(group);

		final List<Team> ranking = this.provider.getTeamsRanking(1);

		assertThat(ranking).hasSize(2);
	}

	@Test
	public void testGetTeamsRankingByIdNotFound() {
		when(this.groupProvider.getGroup(999)).thenReturn(null);

		assertThatThrownBy(() -> this.provider.getTeamsRanking(999)).isInstanceOf(GroupNotFoundException.class);
	}

	@Test
	public void testGetCompetitor() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Participant p1 = this.participant(1, "Ken", "Do");
		final Participant p2 = this.participant(2, "Ryu", "Gi");
		final Team team = this.team(1, "Team", tournament);
		team.setMembers(List.of(p1, p2));
		group.setTeams(List.of(team));
		group.setFights(List.of());
		group.setUnties(List.of());

		final Participant competitor = this.provider.getCompetitor(group, 0);

		assertThat(competitor).isNotNull();
	}

	@Test
	public void testGetCompetitorWithInvalidOrder() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		group.setTeams(List.of());

		final Participant competitor = this.provider.getCompetitor(group, 5);

		assertThat(competitor).isNull();
	}

	@Test
	public void testGetScoreOfCompetitor() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Participant p1 = this.participant(1, "Ken", "Do");
		final Team team = this.team(1, "Team", tournament);
		team.setMembers(List.of(p1));
		group.setTeams(List.of(team));
		group.setFights(List.of());
		group.setUnties(List.of());

		final ScoreOfCompetitor score = this.provider.getScoreOfCompetitor(group, 0);

		assertThat(score).isNotNull().extracting(ScoreOfCompetitor::getCompetitor).isEqualTo(p1);
	}

	@Test
	public void testGetScoreOfCompetitorWithInvalidOrder() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		group.setTeams(List.of());

		final ScoreOfCompetitor score = this.provider.getScoreOfCompetitor(group, 10);

		assertThat(score).isNull();
	}

	@Test
	public void testGetParticipants() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Participant p1 = this.participant(1, "Ken", "Do");
		final Participant p2 = this.participant(2, "Ryu", "Gi");
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);
		team1.setMembers(List.of(p1));
		team2.setMembers(List.of(p2));
		group.setTeams(List.of(team1, team2));
		group.setFights(List.of());
		group.setUnties(List.of());

		final List<Participant> participants = this.provider.getParticipants(group);

		assertThat(participants).hasSize(2);
	}

	@Test
	public void testGetOrder() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);
		group.setTeams(List.of(team1, team2));
		group.setFights(List.of());
		group.setUnties(List.of());

		final Integer order = this.provider.getOrder(group, team1);

		assertThat(order).isNotNull().isGreaterThanOrEqualTo(0);
	}

	@Test
	public void testGetOrderWithMissingTeam() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);
		final Team missingTeam = this.team(3, "Team C", tournament);
		group.setTeams(List.of(team1, team2));

		final Integer order = this.provider.getOrder(group, missingTeam);

		assertThat(order).isNull();
	}

	// ========== Exception Handling Tests ==========

	@Test
	public void testGetCompetitorsScoreRankingFromTournamentNotFound() {
		when(this.tournamentRepository.findById(999)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> this.provider.getCompetitorsScoreRankingFromTournament(999))
				.isInstanceOf(TournamentNotFoundException.class);
	}

	@Test
	public void testGetTeamsScoreRankingFromTournamentNotFound() {
		when(this.tournamentRepository.findById(999)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> this.provider.getTeamsScoreRankingFromTournament(999))
				.isInstanceOf(TournamentNotFoundException.class);
	}

	// ========== Score Type Tests ==========

	@Test
	public void testGetTeamsScoreRankingWithEuropeanScoreType() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(ScoreType.EUROPEAN, List.of(team1, team2),
				List.of(), List.of(), true);

		assertThat(ranking).hasSize(2);
	}

	@Test
	public void testGetTeamsScoreRankingWithInternationalScoreType() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(ScoreType.INTERNATIONAL,
				List.of(team1, team2), List.of(), List.of(), true);

		assertThat(ranking).hasSize(2);
	}

	@Test
	public void testGetTeamsScoreRankingWithCustomScoreType() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(ScoreType.CUSTOM, List.of(team1, team2),
				List.of(), List.of(), true);

		assertThat(ranking).hasSize(2);
	}

	@Test
	public void testGetTeamsScoreRankingWithWinOverDrawsScoreType() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Team team1 = this.team(1, "Team A", tournament);
		final Team team2 = this.team(2, "Team B", tournament);

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(ScoreType.WIN_OVER_DRAWS,
				List.of(team1, team2), List.of(), List.of(), true);

		assertThat(ranking).hasSize(2);
	}

	@Test
	public void testGetCompetitorsGlobalScoreRankingWithEmptyCompetitorsAndNoDateFilter() {
		final Participant p1 = this.participant(21, "A", "One");
		final Participant p2 = this.participant(22, "B", "Two");
		final Fight oldFight = this.fight(List.of(p1), List.of(p2), LocalDateTime.now().minusDays(200));

		when(this.participantProvider.getAll()).thenReturn(new ArrayList<>(List.of(p1, p2)));
		when(this.fightProvider.getBy(any(Collection.class))).thenReturn(List.of(oldFight));
		when(this.duelProvider.getUnties(any(Collection.class))).thenReturn(List.of());

		final List<ScoreOfCompetitor> ranking = this.provider.getCompetitorsGlobalScoreRanking(new ArrayList<>(),
				ScoreType.DEFAULT, null);

		assertThat(ranking).hasSize(2);
	}

	@Test
	public void testGetCompetitorAndScoreWithNegativeOrderReturnNull() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Group group = this.group(tournament);
		final Participant p1 = this.participant(31, "Ken", "Do");
		final Team team = this.team(31, "Team", tournament);
		team.setMembers(List.of(p1));
		group.setTeams(List.of(team));

		assertThat(this.provider.getCompetitor(group, -1)).isNull();
		assertThat(this.provider.getScoreOfCompetitor(group, -1)).isNull();
	}

	@Test
	public void testGetCompetitorGlobalRankingUsesAllComparatorTypesAndFiltersNonCompetitors() {
		final Participant competitor = this.participant(41, "C", "One");
		final Participant referee = this.participant(42, "R", "Two");
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Role competitorRole = new Role(tournament, competitor, RoleType.COMPETITOR);
		final Role refereeRole = new Role(tournament, referee, RoleType.REFEREE);

		when(this.roleProvider.getAll()).thenReturn(List.of(competitorRole, refereeRole));
		when(this.fightProvider.getAll()).thenReturn(List.of());
		when(this.duelProvider.getUnties()).thenReturn(List.of());

		assertThat(this.provider.getCompetitorGlobalRanking(ScoreType.CUSTOM)).hasSize(1)
				.extracting(ScoreOfCompetitor::getCompetitor).containsExactly(competitor);
		assertThat(this.provider.getCompetitorGlobalRanking(ScoreType.EUROPEAN)).hasSize(1);
		assertThat(this.provider.getCompetitorGlobalRanking(ScoreType.INTERNATIONAL)).hasSize(1);
		assertThat(this.provider.getCompetitorGlobalRanking(ScoreType.WIN_OVER_DRAWS)).hasSize(1);
	}

	@Test
	public void testPrivateSortHelpersAndCheckLevelBranchesViaReflection() throws Exception {
		final Method sortTeamsScores = RankingProvider.class.getDeclaredMethod("sortTeamsScores", ScoreType.class,
				List.class, boolean.class);
		sortTeamsScores.setAccessible(true);
		sortTeamsScores.invoke(null, ScoreType.DEFAULT, null, true);

		final Method sortCompetitorsScores = RankingProvider.class.getDeclaredMethod("sortCompetitorsScores",
				ScoreType.class, List.class);
		sortCompetitorsScores.setAccessible(true);
		sortCompetitorsScores.invoke(null, ScoreType.DEFAULT, null);

		final Method checkLevel = RankingProvider.class.getDeclaredMethod("checkLevel", Tournament.class);
		checkLevel.setAccessible(true);

		assertThat((Boolean) checkLevel.invoke(this.provider, new Object[]{null})).isTrue();
		assertThat((Boolean) checkLevel.invoke(this.provider, this.tournament(TournamentType.KING_OF_THE_MOUNTAIN)))
				.isFalse();
	}

	@Test
	public void testGetTeamsScoreRankingAssignsDifferentSortingIndexWhenTeamsDiffer() {
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Participant p1 = this.participant(51, "A", "One");
		final Participant p2 = this.participant(52, "B", "Two");
		final Team team1 = this.team(51, "Team A", tournament);
		final Team team2 = this.team(52, "Team B", tournament);
		team1.setMembers(new ArrayList<>(List.of(p1)));
		team2.setMembers(new ArrayList<>(List.of(p2)));

		final Fight fight = new Fight(tournament, team1, team2, 0, 0, "tester");
		fight.setCreatedAt(LocalDateTime.now());
		fight.getDuels().get(0).addCompetitor1Score(Score.MEN);
		fight.getDuels().get(0).addCompetitor1Score(Score.MEN);

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(ScoreType.CLASSIC, List.of(team1, team2),
				List.of(fight), List.of(), true);

		assertThat(ranking).hasSize(2);
		assertThat(ranking.get(0).getSortingIndex()).isEqualTo(0);
		assertThat(ranking.get(1).getSortingIndex()).isEqualTo(1);
	}

	@Test
	public void testSwissRankingUsesBuchholzByDefault() {
		final Tournament tournament = this.tournament(TournamentType.SWISS);
		final Group group = this.group(tournament);
		final Team team0 = this.team(101, "Team 0", tournament);
		final Team team1 = this.team(102, "Team 1", tournament);
		final Team team2 = this.team(103, "Team 2", tournament);
		final Team team3 = this.team(104, "Team 3", tournament);
		group.setTeams(List.of(team0, team1, team2, team3));
		group.setFights(List.of(this.fightWithScores(tournament, team0, team1, 2, 1),
				this.fightWithScores(tournament, team2, team3, 2, 0),
				this.fightWithScores(tournament, team0, team2, 0, 2),
				this.fightWithScores(tournament, team1, team3, 2, 0)));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE,
						SwissTieBreakRule.BUCHHOLZ.name()));

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(group);

		assertThat(ranking).extracting(score -> score.getTeam().getName()).containsExactly("Team 2", "Team 0", "Team 1",
				"Team 3");
	}

	@Test
	public void testSwissRankingCanUsePointDifferentialRule() {
		final Tournament tournament = this.tournament(TournamentType.SWISS);
		final Group group = this.group(tournament);
		final Team team0 = this.team(201, "Team 0", tournament);
		final Team team1 = this.team(202, "Team 1", tournament);
		final Team team2 = this.team(203, "Team 2", tournament);
		final Team team3 = this.team(204, "Team 3", tournament);
		group.setTeams(List.of(team0, team1, team2, team3));
		group.setFights(List.of(this.fightWithScores(tournament, team0, team1, 2, 1),
				this.fightWithScores(tournament, team2, team3, 2, 0),
				this.fightWithScores(tournament, team0, team2, 0, 2),
				this.fightWithScores(tournament, team1, team3, 2, 0)));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE,
						SwissTieBreakRule.POINT_DIFFERENTIAL.name()));

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(group);

		assertThat(ranking).extracting(score -> score.getTeam().getName()).containsExactly("Team 2", "Team 1", "Team 0",
				"Team 3");
	}

	@Test
	public void testSwissRankingCanUseDirectEncounterRule() {
		final Tournament tournament = this.tournament(TournamentType.SWISS);
		final Group group = this.group(tournament);
		final Team team0 = this.team(301, "Team 0", tournament);
		final Team team1 = this.team(302, "Team 1", tournament);
		final Team team2 = this.team(303, "Team 2", tournament);
		final Team team3 = this.team(304, "Team 3", tournament);
		group.setTeams(List.of(team0, team1, team2, team3));
		group.setFights(List.of(this.fightWithScores(tournament, team1, team0, 2, 0),
				this.fightWithScores(tournament, team2, team3, 2, 0),
				this.fightWithScores(tournament, team0, team3, 2, 0),
				this.fightWithScores(tournament, team2, team1, 2, 0)));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE,
						SwissTieBreakRule.DIRECT_ENCOUNTER.name()));

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(group);

		assertThat(ranking).extracting(score -> score.getTeam().getName()).containsExactly("Team 2", "Team 1", "Team 0",
				"Team 3");
	}

	@Test
	public void testSwissRankingCanUseMedianBuchholzRule() {
		final Tournament tournament = this.tournament(TournamentType.SWISS);
		final Group group = this.group(tournament);
		final Team team0 = this.team(311, "Team 0", tournament);
		final Team team1 = this.team(312, "Team 1", tournament);
		final Team team2 = this.team(313, "Team 2", tournament);
		final Team team3 = this.team(314, "Team 3", tournament);
		group.setTeams(List.of(team0, team1, team2, team3));
		group.setFights(List.of(this.fightWithScores(tournament, team0, team1, 2, 1),
				this.fightWithScores(tournament, team2, team3, 2, 0),
				this.fightWithScores(tournament, team0, team2, 0, 2),
				this.fightWithScores(tournament, team1, team3, 2, 0)));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE,
						SwissTieBreakRule.MEDIAN_BUCHHOLZ.name()));

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(group);

		assertThat(ranking).extracting(score -> score.getTeam().getName()).containsExactly("Team 2", "Team 0", "Team 1",
				"Team 3");
	}

	@Test
	public void testSwissRankingCanUseSonnebornBergerRule() {
		final Tournament tournament = this.tournament(TournamentType.SWISS);
		final Group group = this.group(tournament);
		final Team team0 = this.team(321, "Team 0", tournament);
		final Team team1 = this.team(322, "Team 1", tournament);
		final Team team2 = this.team(323, "Team 2", tournament);
		final Team team3 = this.team(324, "Team 3", tournament);
		group.setTeams(List.of(team0, team1, team2, team3));
		group.setFights(List.of(this.fightWithScores(tournament, team0, team1, 2, 1),
				this.fightWithScores(tournament, team2, team3, 2, 0),
				this.fightWithScores(tournament, team0, team2, 0, 2),
				this.fightWithScores(tournament, team1, team3, 2, 0)));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE,
						SwissTieBreakRule.SONNEBORN_BERGER.name()));

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(group);

		assertThat(ranking).extracting(score -> score.getTeam().getName()).containsExactly("Team 2", "Team 0", "Team 1",
				"Team 3");
	}

	@Test
	public void testSwissRankingFallsBackToStableNameOrderingWhenTieBreakersMatch() {
		final Tournament tournament = this.tournament(TournamentType.SWISS);
		final Group group = this.group(tournament);
		final Team alpha = this.team(401, "Alpha", tournament);
		final Team beta = this.team(402, "Beta", tournament);
		group.setTeams(List.of(beta, alpha));
		when(this.tournamentExtraPropertyProvider.getByTournamentAndProperty(tournament,
				TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, SwissTieBreakRule.BUCHHOLZ.name()))
				.thenReturn(new TournamentExtraProperty(tournament, TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE,
						SwissTieBreakRule.SONNEBORN_BERGER.name()));

		final List<ScoreOfTeam> ranking = this.provider.getTeamsScoreRanking(group);

		assertThat(ranking).extracting(score -> score.getTeam().getName()).containsExactly("Alpha", "Beta");
	}

	// ========== Helper Methods ==========

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
		final Tournament tournament = this.tournament(TournamentType.LEAGUE);
		final Team team1 = this.team(200 + members1.get(0).getId(), "T1-" + members1.get(0).getId(), tournament);
		final Team team2 = this.team(300 + members2.get(0).getId(), "T2-" + members2.get(0).getId(), tournament);
		team1.setMembers(new ArrayList<>(members1));
		team2.setMembers(new ArrayList<>(members2));
		final Fight fight = new Fight(tournament, team1, team2, 0, 0, "tester");
		fight.setCreatedAt(createdAt);
		return fight;
	}

	private Fight fightWithScores(Tournament tournament, Team team1, Team team2, int team1Score, int team2Score) {
		final Fight fight = new Fight(tournament, team1, team2, 0, 0, "tester");
		for (int i = 0; i < team1Score; i++) {
			fight.getDuels().getFirst().addCompetitor1Score(Score.MEN);
		}
		for (int i = 0; i < team2Score; i++) {
			fight.getDuels().getFirst().addCompetitor2Score(Score.MEN);
		}
		fight.getDuels().forEach(duel -> duel.setFinished(true));
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
