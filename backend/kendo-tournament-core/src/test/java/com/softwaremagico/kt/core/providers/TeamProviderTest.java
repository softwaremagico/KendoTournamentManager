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

import com.softwaremagico.kt.core.exceptions.TeamNotFoundException;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = {"teamProviderTests"})
public class TeamProviderTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private RoleProvider roleProvider;

    private TeamProvider provider;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        provider = new TeamProvider(teamRepository, roleProvider);
    }

    @Test
    public void testUpdateWithTeamAndMembersSavesTeam() {
        final Team team = team("Team 1", tournament("Autumn Cup", 3));
        final List<Participant> members = List.of(participant("P1"), participant("P2"));

        when(teamRepository.save(team)).thenReturn(team);

        final Team result = provider.update(team, members);

        assertThat(result).isEqualTo(team);
        assertThat(result.getMembers()).containsExactlyElementsOf(members);
        verify(teamRepository).save(team);
    }

    @Test
    public void testUpdateWithNullTeamReturnsNull() {
        final Team result = provider.update(null, List.of(participant("P1")));

        assertThat(result).isNull();
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    public void testGetByTournamentAndNameSetsTournamentOnTeam() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        final Team team = team("Team 1", tournament("Different", 3));

        when(teamRepository.findByTournamentAndName(tournament, "Team 1")).thenReturn(Optional.of(team));

        final Optional<Team> result = provider.get(tournament, "Team 1");

        assertThat(result).contains(team);
        assertThat(result.get().getTournament()).isEqualTo(tournament);
    }

    @Test
    public void testGetByTournamentAndNameReturnsEmptyWhenNotFound() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        when(teamRepository.findByTournamentAndName(tournament, "Team X")).thenReturn(Optional.empty());

        final Optional<Team> result = provider.get(tournament, "Team X");

        assertThat(result).isEmpty();
    }

    @Test
    public void testCreateDefaultTeamsCreatesExpectedNumberOfTeams() {
        final Tournament tournament = tournament("Autumn Cup", 2);
        when(roleProvider.count(tournament, RoleType.COMPETITOR)).thenReturn(5L);
        when(teamRepository.saveAll(anyCollection())).thenAnswer(invocation -> new ArrayList<>(invocation.getArgument(0)));

        final List<Team> result = provider.createDefaultTeams(tournament, "coach");

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Team::getName).containsExactly("Team 1", "Team 2", "Team 3");
        assertThat(result).allSatisfy(team -> {
            assertThat(team.getTournament()).isEqualTo(tournament);
            assertThat(team.getCreatedBy()).isEqualTo("coach");
        });
        verify(teamRepository).saveAll(anyCollection());
    }

    @Test
    public void testCreateDefaultTeamsWithZeroTeamSizeReturnsEmptyList() {
        final Tournament tournament = tournament("Autumn Cup", 0);
        when(roleProvider.count(tournament, RoleType.COMPETITOR)).thenReturn(10L);
        when(teamRepository.saveAll(anyCollection())).thenAnswer(invocation -> new ArrayList<>(invocation.getArgument(0)));

        final List<Team> result = provider.createDefaultTeams(tournament, "coach");

        assertThat(result).isEmpty();
        verify(teamRepository).saveAll(anyCollection());
    }

    @Test
    public void testGetAllSetsTournamentOnReturnedTeams() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        final Team firstTeam = team("Team 1", tournament("Wrong 1", 3));
        final Team secondTeam = team("Team 2", tournament("Wrong 2", 3));

        when(teamRepository.findByTournament(tournament)).thenReturn(new ArrayList<>(List.of(firstTeam, secondTeam)));

        final List<Team> result = provider.getAll(tournament);

        assertThat(result).containsExactly(firstTeam, secondTeam);
        assertThat(result).allSatisfy(team -> assertThat(team.getTournament()).isEqualTo(tournament));
    }

    @Test
    public void testCountDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        when(teamRepository.countByTournament(tournament)).thenReturn(4L);

        final long result = provider.count(tournament);

        assertThat(result).isEqualTo(4L);
    }

    @Test
    public void testDeleteByTournamentDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        when(teamRepository.deleteByTournament(tournament)).thenReturn(6L);

        final long result = provider.delete(tournament);

        assertThat(result).isEqualTo(6L);
    }

    @Test
    public void testDeleteByExistingIdDeletesTeam() {
        when(teamRepository.existsById(7)).thenReturn(true);

        provider.delete(7);

        verify(teamRepository).deleteById(7);
    }

    @Test
    public void testDeleteByUnknownIdThrowsTeamNotFoundException() {
        when(teamRepository.existsById(7)).thenReturn(false);

        assertThatThrownBy(() -> provider.delete(7))
                .isInstanceOf(TeamNotFoundException.class)
                .hasMessageContaining("Team with id '7' not found");

        verify(teamRepository, never()).deleteById(7);
    }

    @Test
    public void testGetByTournamentAndParticipantDelegatesToRepository() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        final Participant member = participant("P1");
        final Team team = team("Team 1", tournament);

        when(teamRepository.findByTournamentAndMembers(tournament, member)).thenReturn(Optional.of(team));

        final Optional<Team> result = provider.get(tournament, member);

        assertThat(result).contains(team);
    }

    @Test
    public void testDeleteByTournamentAndParticipantUpdatesTeamWithNullMemberSlot() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        final Participant member = participant("P1");
        final Participant otherMember = participant("P2");
        final Team team = team("Team 1", tournament);
        team.setMembers(new ArrayList<>(List.of(member, otherMember)));

        when(teamRepository.findByTournamentAndMembers(tournament, member)).thenReturn(Optional.of(team));
        when(teamRepository.save(any(Team.class))).thenAnswer(invocation -> invocation.getArgument(0));

        final Optional<Team> result = provider.delete(tournament, member);

        assertThat(result).contains(team);
        assertThat(result.get().getTournament()).isEqualTo(tournament);
        assertThat(result.get().getMembers()).containsExactly(null, otherMember);
        verify(teamRepository).save(team);
    }

    @Test
    public void testDeleteByTournamentAndParticipantReturnsEmptyWhenNotFound() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        final Participant member = participant("P1");

        when(teamRepository.findByTournamentAndMembers(tournament, member)).thenReturn(Optional.empty());

        final Optional<Team> result = provider.delete(tournament, member);

        assertThat(result).isEmpty();
        verify(teamRepository, never()).save(any(Team.class));
    }

    @Test
    public void testGetNextDefaultNameSkipsExistingTeamNames() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        final Team team1 = team("Team 1", tournament);
        final Team team2 = team("Team 2", tournament);

        when(teamRepository.findByTournamentAndName(tournament, "Team 1")).thenReturn(Optional.of(team1));
        when(teamRepository.findByTournamentAndName(tournament, "Team 2")).thenReturn(Optional.of(team2));
        when(teamRepository.findByTournamentAndName(tournament, "Team 3")).thenReturn(Optional.empty());

        final String teamName = provider.getNextDefaultName(tournament);

        assertThat(teamName).isEqualTo("Team 3");
    }

    @Test
    public void testGetNextDefaultNameReturnsTeamOneWhenNoTeamsExist() {
        final Tournament tournament = tournament("Autumn Cup", 3);
        when(teamRepository.findByTournamentAndName(tournament, "Team 1")).thenReturn(Optional.empty());

        final String teamName = provider.getNextDefaultName(tournament);

        assertThat(teamName).isEqualTo("Team 1");
    }

    private Tournament tournament(String name, int teamSize) {
        final Tournament tournament = new Tournament(name, 3, teamSize, TournamentType.LEAGUE, "user");
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

    private Team team(String name, Tournament tournament) {
        final Team team = new Team(name, tournament);
        team.setId(Math.abs((name + tournament.getName()).hashCode()));
        team.setCreatedBy("user");
        return team;
    }
}

