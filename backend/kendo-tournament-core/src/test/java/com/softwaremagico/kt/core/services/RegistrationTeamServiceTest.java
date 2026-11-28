package com.softwaremagico.kt.core.services;

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

import com.softwaremagico.kt.core.providers.RoleProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.persistence.entities.Club;
import com.softwaremagico.kt.persistence.entities.Participant;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"registrationTeamServiceTests"})
public class RegistrationTeamServiceTest {

    @Mock
    private TeamProvider teamProvider;
    @Mock
    private RoleProvider roleProvider;

    private RegistrationTeamService service;
    private List<Team> storedTeams;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new RegistrationTeamService(teamProvider, roleProvider);
        storedTeams = new ArrayList<>();
        when(teamProvider.getAll(any(Tournament.class))).thenAnswer(invocation -> new ArrayList<>(storedTeams));
        when(teamProvider.get(any(Tournament.class), any(Participant.class))).thenReturn(Optional.empty());
        when(teamProvider.save(any(Team.class))).thenAnswer(invocation -> {
            final Team team = invocation.getArgument(0);
            if (!storedTeams.contains(team)) {
                storedTeams.add(team);
            }
            return team;
        });
    }

    @Test
    public void shouldRespectTeamSizeWhenFillingPreferredTeams() {
        final Tournament tournament = tournament(2);

        assertThat(service.assignParticipant(tournament, participant("A"), "Team A", "public-registration"))
                .isEqualTo("Team A");
        assertThat(service.assignParticipant(tournament, participant("B"), "Team A", "public-registration"))
                .isEqualTo("Team A");
        assertThat(service.assignParticipant(tournament, participant("C"), "Team A", "public-registration"))
                .isEqualTo("Team A 2");

        assertThat(storedTeams).hasSize(2);
        assertThat(storedTeams.get(0).getMembers()).hasSize(2);
        assertThat(storedTeams.get(1).getMembers()).hasSize(1);
    }

    @Test
    public void shouldNotCreateTeamsForSenbatsu() {
        assertThat(service.assignParticipant(tournament(1), participant("A"), "Team A", "public-registration"))
                .isNull();
        verify(teamProvider, never()).getAll(any(Tournament.class));
        verify(teamProvider, never()).save(any(Team.class));
    }

    @Test
    public void shouldReuseAvailableTeamWithoutPreference() {
        final Tournament tournament = tournament(3);
        final Team existing = new Team("Team 1", tournament);
        existing.addMember(participant("Z"));
        storedTeams.add(existing);

        assertThat(service.assignParticipant(tournament, participant("A"), null, "public-registration"))
                .isEqualTo("Team 1");
        assertThat(existing.getMembers()).hasSize(2);
        assertThat(storedTeams).hasSize(1);
    }

    @Test
    public void shouldOnlyOfferEmptyAndSameClubTeams() {
        final Tournament tournament = tournament(3);
        final Team empty = new Team("Team 1", tournament);
        final Team sameClub = new Team("Alpha", tournament);
        sameClub.addMember(participant("Neighbor", 7));
        final Team otherClub = new Team("Beta", tournament);
        otherClub.addMember(participant("Foreigner", 8));
        storedTeams.add(otherClub);
        storedTeams.add(sameClub);
        storedTeams.add(empty);

        assertThat(service.getAvailableTeams(tournament, 7)).containsExactly(sameClub, empty);
    }

    @Test
    public void shouldNotOfferTeamsForSenbatsu() {
        assertThat(service.getAvailableTeams(tournament(1), 7)).isEmpty();
    }

    private Tournament tournament(int teamSize) {
        final Tournament tournament = new Tournament("Spring Cup", 1, teamSize, TournamentType.LEAGUE, "organizer");
        tournament.setId(1);
        return tournament;
    }

    private Participant participant(String name) {
        return participant(name, null);
    }

    private Participant participant(String name, Integer clubId) {
        final Participant participant = new Participant();
        participant.setName(name);
        participant.setLastname("Lastname");
        participant.setIdCard(name);
        if (clubId != null) {
            final Club club = new Club("Club", "Spain", "Barcelona");
            club.setId(clubId);
            participant.setClub(club);
        }
        return participant;
    }
}
