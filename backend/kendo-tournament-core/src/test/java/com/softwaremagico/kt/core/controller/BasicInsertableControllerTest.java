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

import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.ParticipantConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.exceptions.NotFoundException;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.SenbatsuTournamentHandler;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Exercises the generic CRUD behaviour of {@link BasicInsertableController}
 * through the concrete {@link TeamController} subclass.
 */
@Test(groups = "scoreTests")
public class BasicInsertableControllerTest {

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
        this.controller = new TeamController(this.teamProvider, this.teamConverter, this.tournamentProvider, this.tournamentConverter,
            this.participantConverter, this.groupRepository, this.senbatsuTournamentHandler);
	}

	private Tournament tournament() {
		final Tournament tournament = new Tournament("T", 1, 1, TournamentType.LEAGUE, "tester");
		tournament.setId(1);
		return tournament;
	}

	@Test
	public void shouldGetEntityById() {
		final Team team = new Team("A", this.tournament());
		final TeamDTO teamDTO = new TeamDTO("A", new TournamentDTO("T", 1, 1, TournamentType.LEAGUE));

		when(this.teamProvider.get(5)).thenReturn(Optional.of(team));
		when(this.teamConverter.convert(any())).thenReturn(teamDTO);

		assertEquals(this.controller.get(5), teamDTO);
	}

	@Test(expectedExceptions = NotFoundException.class)
	public void shouldThrowWhenEntityByIdNotFound() {
		when(this.teamProvider.get(99)).thenReturn(Optional.empty());
        this.controller.get(99);
	}

	@Test
	public void shouldGetAllEntities() {
		final Team team = new Team("A", this.tournament());
		final TeamDTO teamDTO = new TeamDTO("A", new TournamentDTO("T", 1, 1, TournamentType.LEAGUE));

		when(this.teamProvider.getAll()).thenReturn(List.of(team));
		when(this.teamConverter.convertAll(any())).thenReturn(List.of(teamDTO));

		assertEquals(this.controller.get(), List.of(teamDTO));
	}

	@Test
	public void shouldGetEntitiesByIds() {
		final Team team = new Team("A", this.tournament());
		final TeamDTO teamDTO = new TeamDTO("A", new TournamentDTO("T", 1, 1, TournamentType.LEAGUE));

		when(this.teamProvider.get(List.of(1, 2))).thenReturn(List.of(team));
		when(this.teamConverter.convertAll(any())).thenReturn(List.of(teamDTO));

		assertEquals(this.controller.get(List.of(1, 2)), List.of(teamDTO));
	}

	@Test
	public void shouldUpdateAllEntitiesAndNotifyListeners() throws InterruptedException {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", this.tournament());
		final AtomicBoolean notified = new AtomicBoolean(false);

		when(this.teamConverter.reverse(teamDTO)).thenReturn(team);
		when(this.teamProvider.save(team)).thenReturn(team);
		when(this.teamConverter.convert(any())).thenReturn(teamDTO);

        this.controller.addElementUpdatedListeners((element, actor, session) -> notified.set(true));

		final List<TeamDTO> result = this.controller.updateAll(List.of(teamDTO), "user", "session");

		assertEquals(result, List.of(teamDTO));
		Thread.sleep(200);
		assertTrue(notified.get());
	}

	@Test
	public void shouldNotifyElementCreatedListeners() throws InterruptedException {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", this.tournament());
		final AtomicBoolean notified = new AtomicBoolean(false);

		when(this.teamConverter.reverse(teamDTO)).thenReturn(team);
		when(this.teamProvider.save(team)).thenReturn(team);
		when(this.teamConverter.convert(any())).thenReturn(teamDTO);

        this.controller.addElementCreatedListeners((element, actor, session) -> notified.set(true));

        this.controller.create(teamDTO, "user", "session");

		Thread.sleep(200);
		assertTrue(notified.get());
	}

	@Test
	public void shouldDeleteSingleEntityAndNotifyListeners() throws InterruptedException {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", this.tournament());
		final AtomicBoolean notified = new AtomicBoolean(false);

		when(this.teamConverter.reverse(teamDTO)).thenReturn(team);

        this.controller.addElementDeletedListeners((element, actor, session) -> notified.set(true));

        this.controller.delete(teamDTO, "user", "session");

		Thread.sleep(200);
		assertTrue(notified.get());
	}

	@Test
	public void shouldDeleteCollectionOfEntitiesAndNotifyListeners() throws InterruptedException {
		final TournamentDTO tournamentDTO = new TournamentDTO("T", 1, 1, TournamentType.LEAGUE);
		final TeamDTO teamDTO = new TeamDTO("A", tournamentDTO);
		final Team team = new Team("A", this.tournament());
		final AtomicBoolean notified = new AtomicBoolean(false);

		when(this.teamConverter.reverseAll(any())).thenReturn(List.of(team));

        this.controller.addElementDeletedListeners((element, actor, session) -> notified.set(true));

        this.controller.delete(List.of(teamDTO), "user", "session");

		Thread.sleep(200);
		assertTrue(notified.get());
	}

	@Test
	public void shouldDeleteAllEntities() {
        this.controller.deleteAll();
	}

	@Test
	public void shouldCreateConverterRequestsForCollection() {
		final Team team1 = new Team("A", this.tournament());
		final Team team2 = new Team("B", this.tournament());

		final List<?> requests = this.controller.createConverterRequest(List.of(team1, team2));

		assertEquals(requests.size(), 2);
	}
}
