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

import com.softwaremagico.kt.core.controller.models.RegistrationRequestDTO;
import com.softwaremagico.kt.core.controller.models.RegistrationResponseDTO;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.ClubProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.services.RegistrationTeamService;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"registrationControllerTests"})
public class RegistrationControllerTest {

    @Mock
    private TournamentProvider tournamentProvider;
    @Mock
    private ClubProvider clubProvider;
    @Mock
    private ParticipantProvider participantProvider;
    @Mock
    private RoleProvider roleProvider;
    @Mock
    private RegistrationTeamService registrationTeamService;

    private RegistrationController controller;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new RegistrationController(tournamentProvider, clubProvider, participantProvider,
                roleProvider, registrationTeamService);
    }

    @Test
    public void shouldExposeTeamsAvailableForEachClubInConfiguration() {
        final Tournament tournament = tournament(3);
        final Club club = club(4, "Existing club");
        final Team team = new Team("Club Team", tournament);
        team.addMember(participant(11, "999"));
        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
        when(clubProvider.getAll()).thenReturn(List.of(club));
        when(registrationTeamService.getAvailableTeams(tournament, 4)).thenReturn(List.of(team));

        final var configuration = controller.getConfiguration(1);

        assertThat(configuration.isTeamSelectionEnabled()).isTrue();
        assertThat(configuration.getClubs()).hasSize(1);
        assertThat(configuration.getClubs().get(0).getTeams()).hasSize(1);
        assertThat(configuration.getClubs().get(0).getTeams().get(0).getName()).isEqualTo("Club Team");
        assertThat(configuration.getClubs().get(0).getTeams().get(0).getFreeSlots()).isEqualTo(2);
    }

    @Test
    public void shouldReuseExistingParticipantAndAddBothPublicRoles() {
        final Tournament tournament = tournament(1);
        final Club club = club(4, "Existing club");
        final Participant existing = participant(9, "1234");
        final Role competitorRole = new Role(tournament, existing, RoleType.COMPETITOR);
        competitorRole.setPendingReview(true);
        final Role refereeRole = new Role(tournament, existing, RoleType.REFEREE);
        refereeRole.setPendingReview(true);
        final Role organizerRole = new Role(tournament, existing, RoleType.ORGANIZER);
        final List<Role> roles = List.of(competitorRole, refereeRole, organizerRole);
        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
        when(clubProvider.get(4)).thenReturn(Optional.of(club));
        when(participantProvider.findByIdCard("1234")).thenReturn(Optional.of(existing));
        when(roleProvider.createOrUpdateRoles(eq(tournament), eq(existing), any(), isNull(), eq(true)))
                .thenReturn(roles);

        final RegistrationRequestDTO request = request();
        request.setClubId(4);
        request.setRoles(List.of(RoleType.COMPETITOR, RoleType.REFEREE));
        request.setTeamPreference("Ignored");

        final RegistrationResponseDTO response = controller.register(1, request, "public-registration");

        assertThat(response.getId()).isEqualTo(9);
        assertThat(response.getName()).isEqualTo("Ada");
        assertThat(response.getLastname()).isEqualTo("Yukimura");
        assertThat(response.getIdCard()).isNull();
        assertThat(response.getRegistrationRoles()).containsExactly(RoleType.COMPETITOR, RoleType.REFEREE);
        assertThat(response.isExistingParticipant()).isTrue();
        assertThat(response.isPendingReview()).isTrue();
        verify(participantProvider, never()).save(any(Participant.class));
        verify(roleProvider).createOrUpdateRoles(eq(tournament), eq(existing), any(), isNull(), eq(true));
        verify(registrationTeamService, never()).assignParticipant(any(), any(), any(), any());
    }

    @Test
    public void shouldCreateParticipantAndClubAndAssignPreferredTeam() {
        final Tournament tournament = tournament(3);
        final Club savedClub = club(5, "New club");
        final List<Role> roles = List.of(new Role(tournament, null, RoleType.COMPETITOR));
        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));
        when(clubProvider.findBy("New club", "Barcelona")).thenReturn(Optional.empty());

        when(clubProvider.save(any(Club.class))).thenReturn(savedClub);
        when(participantProvider.findByIdCard("1234")).thenReturn(Optional.empty());
        when(participantProvider.save(any(Participant.class))).thenAnswer(invocation -> {
            final Participant participant = invocation.getArgument(0);
            participant.setId(10);
            return participant;
        });
        when(roleProvider.createOrUpdateRoles(eq(tournament), any(Participant.class), any(), eq("Team A"), eq(true)))
                .thenReturn(roles);
        when(registrationTeamService.assignParticipant(eq(tournament), any(Participant.class), eq("Team A"), eq("public-registration")))
                .thenReturn("Team A");

        final RegistrationRequestDTO request = request();
        request.setNewClubName("New club");
        request.setNewClubCity("Barcelona");
        request.setNewClubCountry("Spain");
        request.setRoles(List.of(RoleType.COMPETITOR));
        request.setTeamPreference("  Team A  ");

        final RegistrationResponseDTO response = controller.register(1, request, "public-registration");

        assertThat(response.getId()).isEqualTo(10);
        assertThat(response.isExistingParticipant()).isFalse();
        assertThat(response.getTeamName()).isEqualTo("Team A");
        final ArgumentCaptor<Club> clubCaptor = ArgumentCaptor.forClass(Club.class);
        verify(clubProvider).save(clubCaptor.capture());
        assertThat(clubCaptor.getValue().getName()).isEqualTo("New Club");
        assertThat(clubCaptor.getValue().getCity()).isEqualTo("Barcelona");
        final ArgumentCaptor<Participant> participantCaptor = ArgumentCaptor.forClass(Participant.class);
        verify(participantProvider).save(participantCaptor.capture());
        assertThat(participantCaptor.getValue().getIdCard()).isEqualTo("1234");
        verify(registrationTeamService).assignParticipant(eq(tournament), any(Participant.class), eq("Team A"),
                eq("public-registration"));
    }

    @Test
    public void shouldRejectPlatformRoles() {
        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament(1)));
        final RegistrationRequestDTO request = request();
        request.setClubId(4);
        request.setRoles(List.of(RoleType.ORGANIZER));

        assertThatThrownBy(() -> controller.register(1, request, "public-registration"))
                .isInstanceOf(ValidateBadRequestException.class);
    }

    @Test
    public void shouldRejectRegistrationForLockedTournament() {
        final Tournament tournament = tournament(1);
        tournament.setLocked(true);
        when(tournamentProvider.get(1)).thenReturn(Optional.of(tournament));

        assertThatThrownBy(() -> controller.register(1, request(), "public-registration"))
                .isInstanceOf(ValidateBadRequestException.class);
    }

    private RegistrationRequestDTO request() {
        final RegistrationRequestDTO request = new RegistrationRequestDTO();
        request.setIdCard("123-4");
        request.setName("Ada");
        request.setLastname("Yukimura");
        return request;
    }

    private Tournament tournament(int teamSize) {
        final Tournament tournament = new Tournament("Spring Cup", 1, teamSize, TournamentType.LEAGUE, "organizer");
        tournament.setId(1);
        return tournament;
    }

    private Club club(int id, String name) {
        final Club club = new Club(name, "Spain", "Barcelona");
        club.setId(id);
        return club;
    }

    private Participant participant(int id, String idCard) {
        final Participant participant = new Participant(idCard, "Ada", "Yukimura", club(4, "Existing club"));
        participant.setId(id);
        return participant;
    }
}
