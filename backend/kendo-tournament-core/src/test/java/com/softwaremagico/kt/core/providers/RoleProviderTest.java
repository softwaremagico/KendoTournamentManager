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

import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"roleProviderTests"})
public class RoleProviderTest {

    @Mock
    private RoleRepository roleRepository;

    private RoleProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new RoleProvider(roleRepository);
    }

    @Test
    public void testGetAllByTournamentSetsTournamentOnReturnedRoles() {
        final Tournament tournament = tournament("Autumn Cup");
        final Role firstRole = role(tournament("Wrong A"), RoleType.COMPETITOR);
        final Role secondRole = role(tournament("Wrong B"), RoleType.REFEREE);

        when(roleRepository.findByTournament(tournament)).thenReturn(new ArrayList<>(List.of(firstRole, secondRole)));

        final List<Role> result = provider.getAll(tournament);

        assertThat(result).containsExactly(firstRole, secondRole);
        assertThat(result).allSatisfy(role -> assertThat(role.getTournament()).isEqualTo(tournament));
    }

    @Test
    public void testGetAllByTournamentAndRoleTypeSetsTournamentOnReturnedRoles() {
        final Tournament tournament = tournament("Autumn Cup");
        final Role role = role(tournament("Different"), RoleType.COMPETITOR);

        when(roleRepository.findByTournamentAndRoleType(tournament, RoleType.COMPETITOR)).thenReturn(List.of(role));

        final List<Role> result = provider.getAll(tournament, RoleType.COMPETITOR);

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
    }

    @Test
    public void testGetAllByRoleTypeDelegatesToRepository() {
        final Role role = role(tournament("Autumn Cup"), RoleType.PRESS);
        when(roleRepository.findByRoleType(RoleType.PRESS)).thenReturn(List.of(role));

        final List<Role> result = provider.getAll(RoleType.PRESS);

        assertThat(result).containsExactly(role);
    }

    @Test
    public void testGetAllByTournamentAndRoleTypesSetsTournamentOnReturnedRoles() {
        final Tournament tournament = tournament("Autumn Cup");
        final Collection<RoleType> roleTypes = List.of(RoleType.COMPETITOR, RoleType.REFEREE);
        final Role role = role(tournament("Different"), RoleType.REFEREE);

        when(roleRepository.findByTournamentAndRoleTypeIn(tournament, roleTypes)).thenReturn(List.of(role));

        final List<Role> result = provider.getAll(tournament, roleTypes);

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
    }

    @Test
    public void testGetAllForDiplomasWithOnlyNewAndRoleTypesUsesFilteredQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Collection<RoleType> roleTypes = List.of(RoleType.COMPETITOR);
        final Role role = role(tournament("Different"), RoleType.COMPETITOR);

        when(roleRepository.findByTournamentAndDiplomaPrintedAndRoleTypeIn(tournament, false, roleTypes)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForDiplomas(tournament, true, roleTypes);

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournamentAndDiplomaPrintedAndRoleTypeIn(tournament, false, roleTypes);
        verify(roleRepository, never()).findByTournamentAndDiplomaPrinted(tournament, false);
        verify(roleRepository, never()).findByTournamentAndRoleTypeIn(tournament, roleTypes);
        verify(roleRepository, never()).findByTournament(tournament);
    }

    @Test
    public void testGetAllForDiplomasWithOnlyNewAndEmptyRoleTypesUsesDiplomaQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Role role = role(tournament("Different"), RoleType.COMPETITOR);

        when(roleRepository.findByTournamentAndDiplomaPrinted(tournament, false)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForDiplomas(tournament, true, List.of());

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournamentAndDiplomaPrinted(tournament, false);
        verify(roleRepository, never()).findByTournamentAndDiplomaPrintedAndRoleTypeIn(tournament, false, List.of());
    }

    @Test
    public void testGetAllForDiplomasWithFalseFlagAndRoleTypesUsesTournamentRoleQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Collection<RoleType> roleTypes = List.of(RoleType.REFEREE, RoleType.PRESS);
        final Role role = role(tournament("Different"), RoleType.REFEREE);

        when(roleRepository.findByTournamentAndRoleTypeIn(tournament, roleTypes)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForDiplomas(tournament, false, roleTypes);

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournamentAndRoleTypeIn(tournament, roleTypes);
        verify(roleRepository, never()).findByTournament(tournament);
    }

    @Test
    public void testGetAllForDiplomasWithFalseFlagAndEmptyRoleTypesUsesTournamentQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Role role = role(tournament("Different"), RoleType.ORGANIZER);

        when(roleRepository.findByTournament(tournament)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForDiplomas(tournament, false, List.of());

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournament(tournament);
        verify(roleRepository, never()).findByTournamentAndRoleTypeIn(tournament, List.of());
    }

    @Test
    public void testGetAllForDiplomasWithNullFlagAndEmptyRoleTypesUsesTournamentQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Role role = role(tournament("Different"), RoleType.ORGANIZER);

        when(roleRepository.findByTournament(tournament)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForDiplomas(tournament, null, List.of());

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournament(tournament);
        verify(roleRepository, never()).findByTournamentAndRoleTypeIn(tournament, List.of());
    }

    @Test
    public void testGetAllForDiplomasWithNullFlagAndRoleTypesUsesTournamentRoleQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Collection<RoleType> roleTypes = List.of(RoleType.REFEREE, RoleType.ORGANIZER);
        final Role role = role(tournament("Different"), RoleType.REFEREE);

        when(roleRepository.findByTournamentAndRoleTypeIn(tournament, roleTypes)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForDiplomas(tournament, null, roleTypes);

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournamentAndRoleTypeIn(tournament, roleTypes);
    }

    @Test
    public void testGetAllForAccreditationsWithOnlyNewAndRoleTypesUsesFilteredQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Collection<RoleType> roleTypes = List.of(RoleType.COMPETITOR, RoleType.VOLUNTEER);
        final Role role = role(tournament("Different"), RoleType.VOLUNTEER);

        when(roleRepository.findByTournamentAndAccreditationPrintedAndRoleTypeIn(tournament, false, roleTypes)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForAccreditations(tournament, true, roleTypes);

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournamentAndAccreditationPrintedAndRoleTypeIn(tournament, false, roleTypes);
        verify(roleRepository, never()).findByTournamentAndAccreditationPrinted(tournament, false);
    }

    @Test
    public void testGetAllForAccreditationsWithOnlyNewAndEmptyRoleTypesUsesAccreditationQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Role role = role(tournament("Different"), RoleType.PRESS);

        when(roleRepository.findByTournamentAndAccreditationPrinted(tournament, false)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForAccreditations(tournament, true, List.of());

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournamentAndAccreditationPrinted(tournament, false);
    }

    @Test
    public void testGetAllForAccreditationsWithFalseFlagAndRoleTypesUsesTournamentRoleQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Collection<RoleType> roleTypes = List.of(RoleType.ORGANIZER);
        final Role role = role(tournament("Different"), RoleType.ORGANIZER);

        when(roleRepository.findByTournamentAndRoleTypeIn(tournament, roleTypes)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForAccreditations(tournament, false, roleTypes);

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournamentAndRoleTypeIn(tournament, roleTypes);
    }

    @Test
    public void testGetAllForAccreditationsWithFalseFlagAndEmptyRoleTypesUsesTournamentQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Role role = role(tournament("Different"), RoleType.PRESS);

        when(roleRepository.findByTournament(tournament)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForAccreditations(tournament, false, List.of());

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournament(tournament);
    }

    @Test
    public void testGetAllForAccreditationsWithNullFlagAndEmptyRoleTypesUsesTournamentQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Role role = role(tournament("Different"), RoleType.COMPETITOR);

        when(roleRepository.findByTournament(tournament)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForAccreditations(tournament, null, List.of());

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournament(tournament);
    }

    @Test
    public void testGetAllForAccreditationsWithNullFlagAndRoleTypesUsesTournamentRoleQuery() {
        final Tournament tournament = tournament("Autumn Cup");
        final Collection<RoleType> roleTypes = List.of(RoleType.VOLUNTEER);
        final Role role = role(tournament("Different"), RoleType.VOLUNTEER);

        when(roleRepository.findByTournamentAndRoleTypeIn(tournament, roleTypes)).thenReturn(List.of(role));

        final List<Role> result = provider.getAllForAccreditations(tournament, null, roleTypes);

        assertThat(result).containsExactly(role);
        assertThat(role.getTournament()).isEqualTo(tournament);
        verify(roleRepository).findByTournamentAndRoleTypeIn(tournament, roleTypes);
    }

    @Test
    public void testGetByTournamentAndParticipantsDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup");
        final List<Participant> participants = List.of(participant("P1"), participant("P2"));
        final Role role = role(tournament, RoleType.COMPETITOR);

        when(roleRepository.findByTournamentAndParticipantIn(tournament, participants)).thenReturn(List.of(role));

        final List<Role> result = provider.get(tournament, participants);

        assertThat(result).containsExactly(role);
    }

    @Test
    public void testGetByParticipantsDelegatesToRepository() {
        final List<Participant> participants = List.of(participant("P1"));
        final Role role = role(tournament("Autumn Cup"), RoleType.COMPETITOR);

        when(roleRepository.findByParticipantIn(participants)).thenReturn(List.of(role));

        final List<Role> result = provider.getBy(participants);

        assertThat(result).containsExactly(role);
    }

    @Test
    public void testGetByParticipantsAndRoleTypeDelegatesToRepository() {
        final List<Participant> participants = List.of(participant("P1"));
        final Role role = role(tournament("Autumn Cup"), RoleType.REFEREE);

        when(roleRepository.findByParticipantInAndRoleType(participants, RoleType.REFEREE)).thenReturn(List.of(role));

        final List<Role> result = provider.get(participants, RoleType.REFEREE);

        assertThat(result).containsExactly(role);
    }

    @Test
    public void testGetByTournamentAndParticipantDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup");
        final Participant participant = participant("P1");
        final Role role = role(tournament, RoleType.COMPETITOR);

        when(roleRepository.findByTournamentAndParticipant(tournament, participant)).thenReturn(role);

        final Role result = provider.get(tournament, participant);

        assertThat(result).isEqualTo(role);
    }

    @Test
    public void testCountByTournamentDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup");
        when(roleRepository.countByTournament(tournament)).thenReturn(10L);

        final long result = provider.count(tournament);

        assertThat(result).isEqualTo(10L);
    }

    @Test
    public void testCountByTournamentAndRoleTypeDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup");
        when(roleRepository.countByTournamentAndRoleType(tournament, RoleType.COMPETITOR)).thenReturn(8L);

        final long result = provider.count(tournament, RoleType.COMPETITOR);

        assertThat(result).isEqualTo(8L);
    }

    @Test
    public void testCountByParticipantAndRoleTypeDelegatesToRepository() {
        final Participant participant = participant("P1");
        when(roleRepository.countByParticipantAndRoleType(participant, RoleType.COMPETITOR)).thenReturn(2L);

        final long result = provider.count(participant, RoleType.COMPETITOR);

        assertThat(result).isEqualTo(2L);
    }

    @Test
    public void testDeleteByParticipantAndTournamentDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup");
        final Participant participant = participant("P1");

        provider.delete(participant, tournament);

        verify(roleRepository).deleteByParticipantAndTournament(participant, tournament);
    }

    @Test
    public void testDeleteByTournamentDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup");
        when(roleRepository.deleteByTournament(tournament)).thenReturn(4L);

        final long result = provider.delete(tournament);

        assertThat(result).isEqualTo(4L);
    }

    private Tournament tournament(String name) {
        final Tournament tournament = new Tournament(name, 3, 3, TournamentType.LEAGUE, "user");
        tournament.setId(Math.abs(name.hashCode()));
        return tournament;
    }

    private Participant participant(String name) {
        final Participant participant = new Participant();
        participant.setId(Math.abs(name.hashCode()));
        participant.setName(name);
        participant.setLastname("Lastname");
        participant.setCreatedBy("user");
        return participant;
    }

    private Role role(Tournament tournament, RoleType roleType) {
        final Role role = new Role();
        role.setTournament(tournament);
        role.setRoleType(roleType);
        role.setParticipant(participant("participant-" + roleType.name() + "-" + tournament.getId()));
        role.setCreatedBy("user");
        return role;
    }
}

