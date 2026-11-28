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
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Assigns competitors to teams after a public registration.
 * <p>
 * A requested team is filled first; extra competitors with the same preference
 * are placed in a numbered team. Competitors without a preference join the
 * fullest available team, preferring one with members from the same club.
 * Existing teams are never dismantled and {@code teamSize} is always respected.
 * Senbatsu ({@code teamSize == 1}) is left untouched.
 * </p>
 */
@Service
public class RegistrationTeamService {

    private static final String DEFAULT_TEAM_PREFIX = "Team ";
    private static final int FIRST_PART = 1;

    private final TeamProvider teamProvider;

    private final RoleProvider roleProvider;

    @Autowired
    public RegistrationTeamService(TeamProvider teamProvider, RoleProvider roleProvider) {
        this.teamProvider = teamProvider;
        this.roleProvider = roleProvider;
    }

    /**
     * Assigns every competitor that is not already in a team. Useful when an
     * organizer enables teams after competitors have registered individually.
     */
    @Transactional
    public void regenerateTeams(Tournament tournament, String createdBy) {
        for (final Role role : roleProvider.getAll(tournament, RoleType.COMPETITOR)) {
            final Participant participant = role.getParticipant();
            if (participant != null && teamProvider.get(tournament, participant).isEmpty()) {
                assignParticipant(tournament, participant, role.getTeamPreference(), createdBy);
            }
        }
    }

    /**
     * Adds one participant to a team and returns the assigned team name, or
     * {@code null} when teams are disabled for this tournament.
     */
    @Transactional
    public String assignParticipant(Tournament tournament, Participant participant, String teamPreference, String createdBy) {
        final Integer teamSize = tournament.getTeamSize();
        if (teamSize == null || teamSize <= 1 || participant == null) {
            return null;
        }
        if (teamProvider.get(tournament, participant).isPresent()) {
            return getTeamName(tournament, participant);
        }

        final String preference = normalize(teamPreference);
        final List<Team> teams = new ArrayList<>(teamProvider.getAll(tournament));
        final Team team = preference.isEmpty()
                ? findTeamForUnassigned(teams, participant, teamSize)
                : findTeamForPreference(teams, preference, teamSize);

        final Team selected = team == null
                ? createTeam(preference.isEmpty() ? nextDefaultName(teams) : nextPreferenceName(teams, preference),
                        tournament, createdBy)
                : team;
        if (!selected.isMember(participant)) {
            selected.addMember(participant);
        }
        teamProvider.save(selected);
        return selected.getName();
    }

    public String getTeamName(Tournament tournament, Participant participant) {
        return teamProvider.get(tournament, participant).map(Team::getName).orElse(null);
    }

    /**
     * Returns the teams a participant of {@code clubId} can join. Empty teams
     * are available to everyone; otherwise a team is offered when it already
     * has a member from the same club. Team names are public, member data is not.
     */
    @Transactional(readOnly = true)
    public List<Team> getAvailableTeams(Tournament tournament, Integer clubId) {
        final Integer teamSize = tournament == null ? null : tournament.getTeamSize();
        if (teamSize == null || teamSize <= 1) {
            return List.of();
        }
        return teamProvider.getAll(tournament).stream()
                .filter(team -> clubId == null || hasNoMembers(team) || belongsToClub(team, clubId))
                .sorted(Comparator.comparing(Team::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    private boolean hasNoMembers(Team team) {
        return team.getMembers() == null || team.getMembers().isEmpty();
    }

    private boolean belongsToClub(Team team, Integer clubId) {
        return team.getMembers() != null && team.getMembers().stream()
                .anyMatch(member -> member != null && clubId.equals(clubId(member)));
    }

    private Team findTeamForPreference(List<Team> teams, String preference, int teamSize) {
        return teams.stream()
                .filter(team -> isPreferredTeam(team, preference))
                .filter(team -> team.getMembers().size() < teamSize)
                .max(Comparator.comparingInt(team -> team.getMembers().size()))
                .orElse(null);
    }

    private boolean isPreferredTeam(Team team, String preference) {
        final String name = normalize(team.getName());
        return name.equalsIgnoreCase(preference) || name.startsWith(preference + " ");
    }

    private Team findTeamForUnassigned(List<Team> teams, Participant participant, int teamSize) {
        final Integer clubId = clubId(participant);
        return teams.stream()
                .filter(team -> team.getMembers().size() < teamSize)
                .max(Comparator.comparingInt((Team team) -> sameClubCount(team, clubId))
                        .thenComparingInt(team -> team.getMembers().size()))
                .orElse(null);
    }

    private int sameClubCount(Team team, Integer clubId) {
        if (clubId == null) {
            return 0;
        }
        return (int) team.getMembers().stream()
                .filter(member -> member != null && clubId.equals(clubId(member)))
                .count();
    }

    private Integer clubId(Participant participant) {
        final Club club = participant.getClub();
        return club == null ? null : club.getId();
    }

    private Team createTeam(String name, Tournament tournament, String createdBy) {
        final Team team = new Team(name, tournament);
        team.setCreatedBy(createdBy);
        return team;
    }

    private String nextDefaultName(List<Team> teams) {
        int index = FIRST_PART;
        while (true) {
            final String candidate = DEFAULT_TEAM_PREFIX + index;
            if (teams.stream().noneMatch(team -> candidate.equalsIgnoreCase(team.getName()))) {
                return candidate;
            }
            index++;
        }
    }

    private String nextPreferenceName(List<Team> teams, String preference) {
        int index = FIRST_PART;
        while (true) {
            final String candidate = index == FIRST_PART ? preference : preference + " " + index;
            if (teams.stream().noneMatch(team -> candidate.equalsIgnoreCase(team.getName()))) {
                return candidate;
            }
            index++;
        }
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim();
    }
}
