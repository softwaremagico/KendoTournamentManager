package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TeamProvider extends CrudProvider<Team, Integer, TeamRepository> {
    private final RoleProvider roleProvider;

    private final GroupRepository groupRepository;

    @Autowired
    public TeamProvider(TeamRepository repository, RoleProvider roleProvider, GroupRepository groupRepository) {
        super(repository);
        this.roleProvider = roleProvider;
        this.groupRepository = groupRepository;
    }

    public Team update(Team team, List<Participant> members) {
        if (team != null) {
            team.setMembers(members);
            return getRepository().save(team);
        }
        return null;
    }

    public Optional<Team> get(Tournament tournament, String name) {
        final Optional<Team> team = getRepository().findByTournamentAndName(tournament, name);
        team.ifPresent(value -> value.setTournament(tournament));
        return team;
    }

    public List<Team> createDefaultTeams(Tournament tournament, String createdBy) {
        final List<Team> newTeams = new ArrayList<>();
        final long competitors = roleProvider.count(tournament, RoleType.COMPETITOR);
        if (tournament.getTeamSize() > 0) {
            for (int i = 1; i <= (competitors + tournament.getTeamSize() - 1) / tournament.getTeamSize(); i++) {
                final Team team = new Team();
                team.setName(String.format("Team %d", i));
                team.setTournament(tournament);
                team.setCreatedBy(createdBy);
                newTeams.add(team);
            }
        }
        return new ArrayList<>(saveAll(newTeams));
    }

    public synchronized List<Team> getAll(Tournament tournament) {
        final List<Team> teams = getRepository().findByTournament(tournament);
        teams.forEach(team -> team.setTournament(tournament));
        return teams;
    }

    public long count(Tournament tournament) {
        return getRepository().countByTournament(tournament);
    }

    public long delete(Tournament tournament) {
        return getRepository().deleteByTournament(tournament);
    }

    public void delete(Integer id) {
        if (getRepository().existsById(id)) {
            getRepository().deleteById(id);
        } else {
            throw new TeamNotFoundException(getClass(), "Team with id '" + id + "' not found");
        }
    }

    public Optional<Team> get(Tournament tournament, Participant participant) {
        return getRepository().findByTournamentAndMembers(tournament, participant);
    }

    public Optional<Team> delete(Tournament tournament, Participant member) {
        Optional<Team> optionalTeam = get(tournament, member);
        if (optionalTeam.isPresent()) {
            //Setting tournament for updating.
            optionalTeam.get().setTournament(tournament);
            optionalTeam.get().getMembers().set(optionalTeam.get().getMembers().indexOf(member), null);
            optionalTeam = Optional.of(update(optionalTeam.get()));
            //setting tournament for returning element.
            optionalTeam.get().setTournament(tournament);
        }
        return optionalTeam;
    }

    public String getNextDefaultName(Tournament tournament) {
        long i = 0;
        String teamName;
        do {
            i++;
            teamName = String.format("Team %d", i);
        } while (get(tournament, teamName).isPresent());
        return teamName;
    }

}
