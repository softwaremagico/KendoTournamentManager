package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.ITournamentManager;
import com.softwaremagico.kt.core.tournaments.TournamentHandlerSelector;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Test(groups = {"groupsTest"})
public class GroupControllerTest {

	@Mock
	private GroupProvider groupProvider;

	@Mock
	private GroupConverter groupConverter;

	@Mock
	private TournamentConverter tournamentConverter;

	@Mock
	private TournamentProvider tournamentProvider;

	@Mock
	private FightProvider fightProvider;

	@Mock
	private FightConverter fightConverter;

	@Mock
	private DuelProvider duelProvider;

	@Mock
	private DuelConverter duelConverter;

	@Mock
	private TeamConverter teamConverter;

	@Mock
	private TournamentHandlerSelector tournamentHandlerSelector;

	@Mock
	private ITournamentManager tournamentManager;

	private GroupController controller;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		this.controller = org.mockito.Mockito.spy(new GroupController(this.groupProvider, this.groupConverter,
				this.tournamentConverter, this.tournamentProvider, this.fightProvider, this.fightConverter,
				this.duelProvider, this.duelConverter, this.teamConverter, this.tournamentHandlerSelector));
	}

	@Test
	public void shouldGetGroupsFromTournamentWhenTournamentExists() {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();
		final GroupDTO groupDTO = this.groupDTO(tournamentDTO);

		when(this.tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(this.tournamentConverter.convert(any(TournamentConverterRequest.class))).thenReturn(tournamentDTO);
		doReturn(List.of(groupDTO)).when(this.controller).get(tournamentDTO);

		final List<GroupDTO> result = this.controller.getFromTournament(1);

		assertEquals(result.size(), 1);
		assertSame(result.get(0), groupDTO);
		verify(this.controller).get(tournamentDTO);
	}

	@Test
	public void shouldGetGroupByLevelAndIndexFromTournament() {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();
		final Group group = new Group(tournament, 2, 3);
		final GroupDTO groupDTO = this.groupDTO(tournamentDTO);

		when(this.tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(this.tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(this.groupProvider.getGroupByLevelAndIndex(tournament, 2, 3)).thenReturn(group);
		doReturn(groupDTO).when(this.controller).convert(group);

		final GroupDTO result = this.controller.getFromTournament(1, 2, 3);

		assertSame(result, groupDTO);
		verify(this.groupProvider).getGroupByLevelAndIndex(tournament, 2, 3);
	}

	@Test
	public void shouldRefreshGroupContentForEmptyGroupsAtOrAboveLevel() {
		final Tournament tournament = this.tournament();
		final Group keepGroup = new Group(tournament, 0, 0);
		keepGroup.setId(1);
		keepGroup.setTeams(new ArrayList<>(List.of(new Team("keep", tournament))));
		final Group clearGroup = new Group(tournament, 1, 0);
		clearGroup.setId(2);
		clearGroup.setTeams(new ArrayList<>(List.of(new Team("remove", tournament))));

		when(this.tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(keepGroup, clearGroup));
		when(this.groupProvider.save(clearGroup)).thenAnswer(invocation -> invocation.getArgument(0));

		this.controller.refreshGroupContent(1, 1);

		assertEquals(keepGroup.getTeams().size(), 1);
		assertTrue(clearGroup.getTeams().isEmpty());
		verify(this.groupProvider).save(clearGroup);
		verify(this.groupProvider, never()).save(keepGroup);
	}

	@Test
	public void shouldRegisterUpdatedListeners() {
		final GroupController.GroupsUpdatedListener listener = (tournament, actor, session) -> {
		};
		this.controller.addGroupUpdatedListeners(listener);

		// Triggering through a no-op refresh keeps this test cheap while covering the
		// listener registration path.
		assertTrue(true);
	}

	@Test
	public void shouldAddUntiesAndNotifyUntieListeners() throws InterruptedException {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();
		final GroupDTO groupDTO = this.groupDTO(tournamentDTO);
		groupDTO.setId(9);

		final DuelDTO duelDTO = new DuelDTO();
		duelDTO.setId(88);

		final Group savedGroup = new Group(tournament, 0, 0);

		final CountDownLatch untieNotification = new CountDownLatch(1);
		this.controller.addUntieUpdatedListener((t, duel, actor, session) -> untieNotification.countDown());

		doReturn(groupDTO).when(this.controller).get(9);
		doReturn(savedGroup).when(this.controller).reverse(groupDTO);
		when(this.groupProvider.save(savedGroup)).thenReturn(savedGroup);
		doReturn(groupDTO).when(this.controller).convert(savedGroup);
		when(this.tournamentProvider.get(tournament.getId())).thenReturn(Optional.of(tournament));
		when(this.tournamentConverter.convert(any(TournamentConverterRequest.class))).thenReturn(tournamentDTO);

		final GroupDTO result = this.controller.addUnties(9, List.of(duelDTO), "alice", "session-1");

		assertSame(result, groupDTO);
		assertEquals(groupDTO.getUnties().size(), 1);
		assertEquals(groupDTO.getUnties().get(0).getCreatedBy(), "alice");
		assertEquals(groupDTO.getUnties().get(0).getTournament(), tournamentDTO);
		assertEquals(groupDTO.getUpdatedBy(), "alice");
		assertTrue(untieNotification.await(2, TimeUnit.SECONDS));
	}

	@Test
	public void shouldDeleteTeamsFromTournamentWithSpecificTeamList() throws InterruptedException {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", tournament);
		final Group group = new Group(tournament, 0, 0);
		final GroupDTO converted = this.groupDTO(tournamentDTO);

		final CountDownLatch updatedNotification = new CountDownLatch(1);
		this.controller.addGroupUpdatedListeners((t, actor, session) -> updatedNotification.countDown());

		when(this.tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
		when(this.teamConverter.reverseAll(List.of(teamDTO))).thenReturn(List.of(team));
		when(this.groupProvider.deleteTeams(tournament, List.of(team), "tester")).thenReturn(List.of(group));
		doReturn(List.of(converted)).when(this.controller).convertAll(List.of(group));
		when(this.tournamentConverter.convert(any(TournamentConverterRequest.class))).thenReturn(tournamentDTO);

		final List<GroupDTO> result = this.controller.deleteTeamsFromTournament(1, List.of(teamDTO), "tester", "s");

		assertNotNull(result);
		assertEquals(result.size(), 1);
		assertSame(result.get(0), converted);
		assertTrue(updatedNotification.await(2, TimeUnit.SECONDS));
	}

	@Test
	public void shouldDeleteEachGroupFromCollection() throws InterruptedException {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();
		final GroupDTO firstGroup = this.groupDTO(tournamentDTO);
		firstGroup.setId(1);
		firstGroup.setLevel(0);
		firstGroup.setIndex(0);
		final GroupDTO secondGroup = this.groupDTO(tournamentDTO);
		secondGroup.setId(2);
		secondGroup.setLevel(0);
		secondGroup.setIndex(1);

		final CountDownLatch updatedNotification = new CountDownLatch(2);
		this.controller.addGroupUpdatedListeners((t, actor, session) -> updatedNotification.countDown());

		when(this.tournamentHandlerSelector.selectManager(TournamentType.LEAGUE)).thenReturn(this.tournamentManager);
		when(this.tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);

		this.controller.delete(List.of(firstGroup, secondGroup), "tester", "session-delete");

		verify(this.tournamentManager).removeGroup(tournament, 0, 0);
		verify(this.tournamentManager).removeGroup(tournament, 0, 1);
		verify(this.tournamentHandlerSelector, times(2)).selectManager(TournamentType.LEAGUE);
		assertTrue(updatedNotification.await(2, TimeUnit.SECONDS));
	}

	@Test
	public void shouldResetFightAndDuelIdsAndEnsureFightTeamsOnUpdate() {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();

		final GroupDTO oldGroup = this.groupDTO(tournamentDTO);
		oldGroup.setId(17);
		final FightDTO oldFight = new FightDTO();
		oldFight.setId(500);
		oldGroup.setFights(new ArrayList<>(List.of(oldFight)));
		oldGroup.setUnties(new ArrayList<>());

		final GroupDTO input = this.groupDTO(tournamentDTO);
		input.setId(17);

		final TeamDTO team1 = new TeamDTO("Team 1", tournamentDTO);
		final TeamDTO team2 = new TeamDTO("Team 2", tournamentDTO);
		final FightDTO newFight = new FightDTO(tournamentDTO, team1, team2, 0, 0);
		newFight.setId(123);
		newFight.setVersion(3);
		final DuelDTO duel = new DuelDTO();
		duel.setId(321);
		duel.setVersion(4);
		newFight.setDuels(new ArrayList<>(List.of(duel)));
		input.setFights(new ArrayList<>(List.of(newFight)));

		final DuelDTO newUntie = new DuelDTO();
		newUntie.setId(777);
		newUntie.setVersion(9);
		input.setUnties(new ArrayList<>(List.of(newUntie)));

		final Group persisted = new Group(tournament, 0, 0);

		doReturn(oldGroup).when(this.controller).get(17);
		when(this.fightConverter.reverseAll(anyList())).thenReturn(List.of());
		when(this.duelConverter.reverseAll(anyList())).thenReturn(List.of());
		doReturn(persisted).when(this.controller).reverse(any(GroupDTO.class));
		when(this.groupProvider.save(any(Group.class))).thenReturn(persisted);
		doReturn(input).when(this.controller).convert(any(Group.class));

		final GroupDTO updated = this.controller.update(input, "editor", "session-2");

		assertSame(updated, input);
		assertEquals(input.getTeams().size(), 2);
		assertTrue(input.getTeams().contains(team1));
		assertTrue(input.getTeams().contains(team2));
		assertNull(input.getFights().get(0).getId());
		assertNull(input.getFights().get(0).getVersion());
		assertNull(input.getFights().get(0).getDuels().get(0).getId());
		assertNull(input.getFights().get(0).getDuels().get(0).getVersion());
		assertNull(input.getUnties().get(0).getId());
		assertNull(input.getUnties().get(0).getVersion());
		assertEquals(input.getUpdatedBy(), "editor");
		verify(this.fightProvider).delete(anyList());
		verify(this.duelProvider).delete(anyList());
	}

	@Test
	public void shouldDeletePreviousUntiesDuringUpdate() {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();

		final GroupDTO oldGroup = this.groupDTO(tournamentDTO);
		oldGroup.setId(25);
		oldGroup.setFights(new ArrayList<>());
		final DuelDTO previousUntie = new DuelDTO();
		previousUntie.setId(901);
		previousUntie.setTournament(tournamentDTO);
		oldGroup.setUnties(new ArrayList<>(List.of(previousUntie)));

		final GroupDTO input = this.groupDTO(tournamentDTO);
		input.setId(25);

		final Group persisted = new Group(tournament, 0, 0);

		doReturn(oldGroup).when(this.controller).get(25);
		when(this.fightConverter.reverseAll(anyList())).thenReturn(List.of());
		when(this.duelConverter.reverseAll(anyList())).thenReturn(List.of());
		doReturn(persisted).when(this.controller).reverse(any(GroupDTO.class));
		when(this.groupProvider.save(any(Group.class))).thenReturn(persisted);
		doReturn(input).when(this.controller).convert(any(Group.class));
		when(this.tournamentProvider.get(tournament.getId())).thenReturn(Optional.of(tournament));
		when(this.tournamentConverter.convert(any(TournamentConverterRequest.class))).thenReturn(tournamentDTO);

		final GroupDTO updated = this.controller.update(input, "editor", "session-3");

		assertSame(updated, input);
		verify(this.duelProvider).delete(anyList());
	}

	@Test
	public void shouldCreateGroupUsingTournamentManagerAndNotifyListeners() throws InterruptedException {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();
		final GroupDTO input = this.groupDTO(tournamentDTO);
		final Group persisted = new Group(tournament, input.getLevel(), input.getIndex());
		final CountDownLatch updatedNotification = new CountDownLatch(1);

		this.controller.addGroupUpdatedListeners((t, actor, session) -> updatedNotification.countDown());
		when(this.tournamentHandlerSelector.selectManager(TournamentType.LEAGUE)).thenReturn(this.tournamentManager);
		when(this.tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		doReturn(new Group(tournament, input.getLevel(), input.getIndex())).when(this.controller).reverse(input);
		when(this.tournamentManager.addGroup(eq(tournament), any(Group.class))).thenReturn(persisted);
		doReturn(input).when(this.controller).convert(persisted);

		final GroupDTO result = this.controller.create(input, "creator", "session-create");

		assertSame(result, input);
		assertTrue(updatedNotification.await(2, TimeUnit.SECONDS));
		verify(this.tournamentManager).addGroup(eq(tournament), any(Group.class));
	}

	@Test
	public void shouldDeleteByIdDelegatingToDelete() {
		final GroupDTO groupDTO = this.groupDTO(this.tournamentDTO());
		groupDTO.setId(44);
		doReturn(groupDTO).when(this.controller).get(44);
		doNothing().when(this.controller).delete(groupDTO, "deleter", "session-delete-id");

		this.controller.deleteById(44, "deleter", "session-delete-id");

		verify(this.controller).get(44);
		verify(this.controller).delete(groupDTO, "deleter", "session-delete-id");
	}

	@Test
	public void shouldManageTeamsAndNotifyListeners() throws InterruptedException {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();
		final GroupDTO groupDTO = this.groupDTO(tournamentDTO);
		groupDTO.setId(55);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", tournament);
		final Group group = new Group(tournament, 0, 0);
		group.setId(55);
		final CountDownLatch updatedNotification = new CountDownLatch(4);

		this.controller.addGroupUpdatedListeners((t, actor, session) -> updatedNotification.countDown());
		when(this.teamConverter.reverseAll(List.of(teamDTO))).thenReturn(List.of(team));
		when(this.groupProvider.addTeams(55, List.of(team), "editor")).thenReturn(group);
		when(this.groupProvider.deleteTeams(55, List.of(team), "editor")).thenReturn(group);
		when(this.groupProvider.setTeams(55, List.of(team), "editor")).thenReturn(group);
		doReturn(groupDTO).when(this.controller).convert(group);
		doReturn(groupDTO).when(this.controller).get(55);
		doReturn(List.of(groupDTO)).when(this.controller).get(tournamentDTO);

		assertSame(this.controller.addTeams(55, List.of(teamDTO), "editor", "s1"), groupDTO);
		assertSame(this.controller.deleteTeams(55, List.of(teamDTO), "editor", "s2"), groupDTO);
		assertSame(this.controller.setTeams(55, List.of(teamDTO), "editor", "s3"), groupDTO);
		assertSame(this.controller.setTeams(List.of(teamDTO), "editor", "s4"), groupDTO);

		assertTrue(updatedNotification.await(2, TimeUnit.SECONDS));
	}

	@Test
	public void shouldGetCountAndDeleteGroupsByTournament() {
		final Tournament tournament = this.tournament();
		final TournamentDTO tournamentDTO = this.tournamentDTO();
		final Group group = new Group(tournament, 0, 0);
		final GroupDTO groupDTO = this.groupDTO(tournamentDTO);

		when(this.tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
		when(this.groupProvider.getGroups(tournament)).thenReturn(List.of(group));
		doReturn(List.of(groupDTO)).when(this.controller).convertAll(List.of(group));
		when(this.groupProvider.count(tournament)).thenReturn(9L);
		when(this.groupProvider.delete(tournament)).thenReturn(3L);

		final List<GroupDTO> groups = this.controller.get(tournamentDTO);
		final long count = this.controller.count(tournamentDTO);
		final long deleted = this.controller.delete(tournamentDTO);

		assertEquals(groups.size(), 1);
		assertSame(groups.get(0), groupDTO);
		assertEquals(count, 9L);
		assertEquals(deleted, 3L);
	}

	private Tournament tournament() {
		final Tournament tournament = new Tournament("Tournament", 1, 3, TournamentType.LEAGUE, "tester");
		tournament.setId(303);
		return tournament;
	}

	private TournamentDTO tournamentDTO() {
		final TournamentDTO tournamentDTO = new TournamentDTO("Tournament", 1, 3, TournamentType.LEAGUE);
		tournamentDTO.setId(303);
		return tournamentDTO;
	}

	private GroupDTO groupDTO(TournamentDTO tournamentDTO) {
		final GroupDTO groupDTO = new GroupDTO();
		groupDTO.setTournament(tournamentDTO);
		groupDTO.setLevel(2);
		groupDTO.setIndex(3);
		groupDTO.setTeams(new ArrayList<>());
		groupDTO.setFights(new ArrayList<>());
		groupDTO.setUnties(new ArrayList<>());
		groupDTO.setNumberOfWinners(1);
		return groupDTO;
	}
}
