package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.ParticipantDTO;
import com.softwaremagico.kt.core.controller.models.RoleDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.controller.models.TournamentFightStatisticsDTO;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.TournamentFightStatisticsConverter;
import com.softwaremagico.kt.core.providers.TournamentFightStatisticsProvider;
import com.softwaremagico.kt.core.statistics.TournamentFightStatistics;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;

@Test(groups = "scoreTests")
public class FightStatisticsControllerTest {

	@Mock
	private TournamentFightStatisticsProvider provider;
	@Mock
	private TournamentFightStatisticsConverter converter;
	@Mock
	private TournamentConverter tournamentConverter;
	@Mock
	private TeamConverter teamConverter;

	private FightStatisticsController controller;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new FightStatisticsController(provider, converter, tournamentConverter, teamConverter);
	}

	@Test
	public void shouldEstimateFromTournament() {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		final Tournament tournament = new Tournament("T", 1, 1, TournamentType.LEAGUE, "tester");
		final TournamentFightStatistics statistics = mock(TournamentFightStatistics.class);
		final TournamentFightStatisticsDTO dto = new TournamentFightStatisticsDTO();

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(provider.estimate(tournament)).thenReturn(statistics);
		when(converter.convert(any())).thenReturn(dto);

		assertSame(controller.estimate(tournamentDTO), dto);
	}

	@Test
	public void shouldEstimateByTeams() {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		final Tournament tournament = new Tournament("T", 1, 1, TournamentType.LEAGUE, "tester");
		final TournamentFightStatisticsDTO dto = new TournamentFightStatisticsDTO();

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(provider.estimateByTeams(tournament)).thenReturn(mock(TournamentFightStatistics.class));
		when(converter.convert(any())).thenReturn(dto);

		assertSame(controller.estimateByTeams(tournamentDTO), dto);
	}

	@Test
	public void shouldEstimateByMembers() {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		final Tournament tournament = new Tournament("T", 1, 1, TournamentType.LEAGUE, "tester");
		final TournamentFightStatisticsDTO dto = new TournamentFightStatisticsDTO();

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(provider.estimateByMembers(tournament)).thenReturn(mock(TournamentFightStatistics.class));
		when(converter.convert(any())).thenReturn(dto);

		assertSame(controller.estimateByMembers(tournamentDTO), dto);
	}

	@Test
	public void shouldReturnNullWhenNotEnoughTeams() {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		assertNull(controller.estimate(tournamentDTO, 3, List.of()));
		assertNull(controller.estimate(null, 3, List.of(new TeamDTO("A", tournamentDTO))));
	}

	@Test
	public void shouldReturnNullForCustomizedAndKingOfTheMountain() {
		final TournamentDTO customized = new TournamentDTO("T", 1, 1, TournamentType.CUSTOMIZED);
		final TournamentDTO king = new TournamentDTO("T", 1, 1, TournamentType.KING_OF_THE_MOUNTAIN);
		final List<TeamDTO> teams = List.of(new TeamDTO("A", customized), new TeamDTO("B", customized));

		assertNull(controller.estimate(customized, 2, teams));
		assertNull(controller.estimate(king, 2, teams));
	}

	@Test
	public void shouldEstimateLeagueStatisticsForTeams() {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		tournamentDTO.setTeamSize(2);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", new Tournament("T", 1, 1, TournamentType.LEAGUE, "tester"));
		final TournamentFightStatisticsDTO dto = new TournamentFightStatisticsDTO();

		when(teamConverter.reverseAll(any())).thenReturn(List.of(team));
		when(provider.estimateLeagueStatistics(anyInt(), any())).thenReturn(mock(TournamentFightStatistics.class));
		when(converter.convert(any())).thenReturn(dto);

		assertSame(controller.estimate(tournamentDTO, List.of(teamDTO, new TeamDTO("B", tournamentDTO))), dto);
	}

	@Test
	public void shouldEstimateLoopStatisticsForTeams() {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LOOP);
		tournamentDTO.setTeamSize(2);
		final Tournament tournament = new Tournament("T", 1, 1, TournamentType.LOOP, "tester");
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", tournament);
		final TournamentFightStatisticsDTO dto = new TournamentFightStatisticsDTO();

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(teamConverter.reverseAll(any())).thenReturn(List.of(team));
		when(provider.estimateLoopStatistics(any(), anyInt(), any())).thenReturn(mock(TournamentFightStatistics.class));
		when(converter.convert(any())).thenReturn(dto);

		assertSame(controller.estimate(tournamentDTO, List.of(teamDTO, new TeamDTO("B", tournamentDTO))), dto);
	}

	@Test
	public void shouldEstimateByRolesEmulatingTeams() {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		tournamentDTO.setTeamSize(2);
		final ParticipantDTO participant1 = new ParticipantDTO();
		participant1.setId(1);
		final ParticipantDTO participant2 = new ParticipantDTO();
		participant2.setId(2);
		final ParticipantDTO participant3 = new ParticipantDTO();
		participant3.setId(3);
		final RoleDTO role1 = new RoleDTO();
		role1.setParticipant(participant1);
		final RoleDTO role2 = new RoleDTO();
		role2.setParticipant(participant2);
		final RoleDTO role3 = new RoleDTO();
		role3.setParticipant(participant3);
		final TournamentFightStatisticsDTO dto = new TournamentFightStatisticsDTO();

		when(teamConverter.reverseAll(any())).thenReturn(List.of(mock(Team.class), mock(Team.class)));
		when(provider.estimateLeagueStatistics(anyInt(), any())).thenReturn(mock(TournamentFightStatistics.class));
		when(converter.convert(any())).thenReturn(dto);

		assertSame(controller.estimateByRoles(tournamentDTO, List.of(role1, role2, role3)), dto);
	}
}

