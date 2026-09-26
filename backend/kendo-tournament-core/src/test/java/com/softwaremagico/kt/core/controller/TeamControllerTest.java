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
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "scoreTests")
public class TeamControllerTest {

	@Mock
	private TeamProvider teamProvider;
	@Mock
	private TeamConverter teamConverter;
	@Mock
	private TournamentProvider tournamentProvider;
	@Mock
	private TournamentConverter tournamentConverter;
	@Mock
	private ParticipantConverter participantConverter;
	@Mock
	private GroupRepository groupRepository;
	@Mock
	private SenbatsuTournamentHandler senbatsuTournamentHandler;

	private TeamController controller;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		controller = new TeamController(teamProvider, teamConverter, tournamentProvider, tournamentConverter,
				participantConverter, groupRepository, senbatsuTournamentHandler);
	}

	private Tournament tournament(int id, TournamentType type) {
		final Tournament tournament = new Tournament("T", 1, 1, type, "tester");
		tournament.setId(id);
		return tournament;
	}

	private TournamentDTO tournamentDTO(int id, TournamentType type) {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, type);
		tournamentDTO.setId(id);
		return tournamentDTO;
	}

	@Test
	public void shouldReturnExistingTeamsByTournamentDTO() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(teamProvider.getAll(tournament)).thenReturn(List.of(new Team("A", tournament)));
		when(teamConverter.convertAll(any())).thenReturn(List.of(teamDTO));

		final List<TeamDTO> result = controller.getAllByTournament(tournamentDTO, "user");

		assertEquals(result, List.of(teamDTO));
	}

	@Test
	public void shouldCreateDefaultTeamsWhenNoneExistByTournamentDTO() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("Default", tournamentDTO);

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(teamProvider.getAll(tournament)).thenReturn(List.of());
		when(teamProvider.createDefaultTeams(tournament, "user")).thenReturn(List.of(new Team("Default", tournament)));
		when(teamConverter.convertAll(any())).thenReturn(List.of(), List.of(teamDTO));

		final List<TeamDTO> result = controller.getAllByTournament(tournamentDTO, "user");

		assertEquals(result, List.of(teamDTO));
	}

	@Test
	public void shouldReturnExistingTeamsByTournamentId() {
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO(1, TournamentType.LEAGUE));

		when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(teamProvider.getAll(tournament)).thenReturn(List.of(new Team("A", tournament)));
		when(teamConverter.convertAll(any())).thenReturn(List.of(teamDTO));

		final List<TeamDTO> result = controller.getAllByTournament(1, "user");

		assertEquals(result, List.of(teamDTO));
	}

	@Test
	public void shouldCreateDefaultTeamsWhenNoneExistByTournamentId() {
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("Default", tournamentDTO(1, TournamentType.LEAGUE));

		when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(teamProvider.getAll(tournament)).thenReturn(List.of());
		when(teamProvider.createDefaultTeams(tournament, "user")).thenReturn(List.of(new Team("Default", tournament)));
		when(teamConverter.convertAll(any())).thenReturn(List.of());
		when(teamConverter.convertAllNotSorted(any())).thenReturn(List.of(teamDTO));

		final List<TeamDTO> result = controller.getAllByTournament(1, "user");

		assertEquals(result, List.of(teamDTO));
	}

	@Test(expectedExceptions = TournamentNotFoundException.class)
	public void shouldThrowWhenTournamentNotFoundByTournamentId() {
		when(tournamentProvider.get(99)).thenReturn(Optional.empty());
		controller.getAllByTournament(99, "user");
	}

	@Test
	public void shouldDelegateToGetAllByTournamentWhenNotSenbatsu() {
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO(1, TournamentType.LEAGUE));

		when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(teamProvider.getAll(tournament)).thenReturn(List.of(new Team("A", tournament)));
		when(teamConverter.convertAll(any())).thenReturn(List.of(teamDTO));

		final List<TeamDTO> result = controller.getAllRemainingByTournament(1, "user");

		assertEquals(result, List.of(teamDTO));
	}

	@Test
	public void shouldReturnNextTeamsWhenSenbatsu() {
		final Tournament tournament = tournament(1, TournamentType.SENBATSU);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO(1, TournamentType.SENBATSU));

		when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(senbatsuTournamentHandler.getNextTeamsOrderedByRanks(tournament, null)).thenReturn(List.of(new Team("A", tournament)));
		when(teamConverter.convertAllNotSorted(any())).thenReturn(List.of(teamDTO));

		final List<TeamDTO> result = controller.getAllRemainingByTournament(1, "user");

		assertEquals(result, List.of(teamDTO));
	}

	@Test(expectedExceptions = TournamentNotFoundException.class)
	public void shouldThrowWhenTournamentNotFoundForRemaining() {
		when(tournamentProvider.get(99)).thenReturn(Optional.empty());
		controller.getAllRemainingByTournament(99, "user");
	}

	@Test
	public void shouldCountByTournament() {
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(teamProvider.count(tournament)).thenReturn(5L);

		assertEquals(controller.countByTournament(1), 5L);
	}

	@Test(expectedExceptions = TournamentNotFoundException.class)
	public void shouldThrowWhenTournamentNotFoundForCount() {
		when(tournamentProvider.get(99)).thenReturn(Optional.empty());
		controller.countByTournament(99);
	}

	@Test
	public void shouldSetDefaultNameWhenCreatingTeamWithoutName() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO(null, tournamentDTO);
		final Team savedTeam = new Team("Auto", tournament);

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(teamProvider.getNextDefaultName(tournament)).thenReturn("Auto");
		when(teamConverter.reverse(any())).thenReturn(savedTeam);
		when(teamProvider.save(savedTeam)).thenReturn(savedTeam);
		when(teamConverter.convert(any())).thenReturn(new TeamDTO("Auto", tournamentDTO));

		final TeamDTO result = controller.create(teamDTO, "user", "session");

		assertEquals(result.getTournament(), tournamentDTO);
		verify(teamProvider, times(1)).getNextDefaultName(tournament);
	}

	@Test
	public void shouldCreateDefaultTeamsForTournament() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("Default", tournamentDTO);

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(teamProvider.createDefaultTeams(tournament, "user")).thenReturn(List.of(new Team("Default", tournament)));
		when(teamConverter.convertAll(any())).thenReturn(List.of(teamDTO));

		final List<TeamDTO> result = controller.create(tournamentDTO, "user");

		assertEquals(result, List.of(teamDTO));
	}

	@Test
	public void shouldSetDefaultNamesForCollectionCreate() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO(null, tournamentDTO);
		final Team savedTeam = new Team("Auto", tournament);

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(teamProvider.getNextDefaultName(tournament)).thenReturn("Auto");
		when(teamConverter.reverseAll(any())).thenReturn(List.of(savedTeam));
		when(teamProvider.save(any(List.class))).thenReturn(List.of(savedTeam));
		when(teamConverter.convertAll(any())).thenReturn(List.of(new TeamDTO("Auto", tournamentDTO)));

		final List<TeamDTO> result = controller.create(List.of(teamDTO), "user", "session");

		assertEquals(result.size(), 1);
	}

	@Test
	public void shouldDeleteTeamMemberAndReturnUpdatedTeam() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final Participant participant = new Participant();
		final Team team = new Team("A", tournament);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(participantConverter.reverse(participantDTO)).thenReturn(participant);
		when(teamProvider.delete(tournament, participant)).thenReturn(Optional.of(team));
		when(teamConverter.convert(any())).thenReturn(teamDTO);

		assertEquals(controller.delete(tournamentDTO, participantDTO), teamDTO);
	}

	@Test
	public void shouldReturnNullWhenNoTeamToDeleteMemberFrom() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final ParticipantDTO participantDTO = new ParticipantDTO();
		final Participant participant = new Participant();

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(participantConverter.reverse(participantDTO)).thenReturn(participant);
		when(teamProvider.delete(tournament, participant)).thenReturn(Optional.empty());

		assertNull(controller.delete(tournamentDTO, participantDTO));
	}

	@Test
	public void shouldDeleteAllTeamsAndClearGroupsForTournament() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final Group group = new Group();
		group.setTeams(new ArrayList<>(List.of(new Team("A", tournament))));

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament)).thenReturn(List.of(group));

		controller.delete(tournamentDTO);

		assertTrue(group.getTeams().isEmpty());
		verify(groupRepository, times(1)).saveAll(List.of(group));
		verify(teamProvider, times(1)).delete(tournament);
	}

	@Test
	public void shouldUpdateTeamPreservingTournament() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", tournament);

		when(teamConverter.reverse(teamDTO)).thenReturn(team);
		when(teamProvider.save(team)).thenReturn(team);
		when(teamConverter.convert(any())).thenReturn(new TeamDTO("A", null));

		final TeamDTO result = controller.update(teamDTO, "user", "session");

		assertEquals(result.getTournament(), tournamentDTO);
	}

	@Test
	public void shouldCountTeamsForTournament() {
		final TournamentDTO tournamentDTO = tournamentDTO(1, TournamentType.LEAGUE);
		final Tournament tournament = tournament(1, TournamentType.LEAGUE);

		when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(teamProvider.count(tournament)).thenReturn(3L);

		assertEquals(controller.count(tournamentDTO), 3L);
	}

	@Test(expectedExceptions = ValidateBadRequestException.class)
	public void shouldThrowWhenValidatingNullTeam() {
		controller.validate((TeamDTO) null);
	}

	@Test(expectedExceptions = ValidateBadRequestException.class)
	public void shouldThrowWhenValidatingTeamWithoutTournament() {
		controller.validate(new TeamDTO("A", null));
	}

	@Test
	public void shouldNotThrowWhenValidatingValidTeam() {
		controller.validate(new TeamDTO("A", tournamentDTO(1, TournamentType.LEAGUE)));
	}
}

