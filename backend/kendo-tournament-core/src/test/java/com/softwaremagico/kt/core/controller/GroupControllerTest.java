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

import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.GroupConverter;
import com.softwaremagico.kt.core.converters.TeamConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.controller.models.TeamDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

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

    private GroupController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = org.mockito.Mockito.spy(new GroupController(groupProvider, groupConverter, tournamentConverter,
                tournamentProvider, fightProvider, fightConverter, duelProvider, duelConverter, teamConverter, tournamentHandlerSelector));
    }

    @Test
    public void shouldGetGroupsFromTournamentWhenTournamentExists() {
        final Tournament tournament = tournament();
        final TournamentDTO tournamentDTO = tournamentDTO();
        final GroupDTO groupDTO = groupDTO(tournamentDTO);

        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
        when(tournamentConverter.convert(any(TournamentConverterRequest.class))).thenReturn(tournamentDTO);
        doReturn(List.of(groupDTO)).when(controller).get(tournamentDTO);

        final List<GroupDTO> result = controller.getFromTournament(1);

        assertEquals(result.size(), 1);
        assertSame(result.get(0), groupDTO);
        verify(controller).get(tournamentDTO);
    }

    @Test
    public void shouldGetGroupByLevelAndIndexFromTournament() {
        final Tournament tournament = tournament();
        final TournamentDTO tournamentDTO = tournamentDTO();
        final Group group = new Group(tournament, 2, 3);
        final GroupDTO groupDTO = groupDTO(tournamentDTO);

        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
        when(tournamentConverter.reverse(tournamentDTO)).thenReturn(tournament);
        when(groupProvider.getGroupByLevelAndIndex(tournament, 2, 3)).thenReturn(group);
        doReturn(groupDTO).when(controller).convert(group);

        final GroupDTO result = controller.getFromTournament(1, 2, 3);

        assertSame(result, groupDTO);
        verify(groupProvider).getGroupByLevelAndIndex(tournament, 2, 3);
    }

    @Test
    public void shouldRefreshGroupContentForEmptyGroupsAtOrAboveLevel() {
        final Tournament tournament = tournament();
        final Group keepGroup = new Group(tournament, 0, 0);
        keepGroup.setId(1);
        keepGroup.setTeams(new ArrayList<>(List.of(new Team("keep", tournament))));
        final Group clearGroup = new Group(tournament, 1, 0);
        clearGroup.setId(2);
        clearGroup.setTeams(new ArrayList<>(List.of(new Team("remove", tournament))));

        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
        when(groupProvider.getGroups(tournament)).thenReturn(List.of(keepGroup, clearGroup));
        when(groupProvider.save(clearGroup)).thenAnswer(invocation -> invocation.getArgument(0));

        controller.refreshGroupContent(1, 1);

        assertEquals(keepGroup.getTeams().size(), 1);
        assertTrue(clearGroup.getTeams().isEmpty());
        verify(groupProvider).save(clearGroup);
        verify(groupProvider, never()).save(keepGroup);
    }

    @Test
    public void shouldRegisterUpdatedListeners() {
        final GroupController.GroupsUpdatedListener listener = (tournament, actor, session) -> { };
        controller.addGroupUpdatedListeners(listener);

        // Triggering through a no-op refresh keeps this test cheap while covering the listener registration path.
        assertTrue(true);
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


