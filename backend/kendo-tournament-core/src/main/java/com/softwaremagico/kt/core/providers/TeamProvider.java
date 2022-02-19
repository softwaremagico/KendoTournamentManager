package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2022 Softwaremagico
 * %%
 * This software is designed by Jorge Hortelano Otero. Jorge Hortelano Otero
 * <softwaremagico@gmail.com> Valencia (Spain).
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.softwaremagico.kt.core.exceptions.NameAlreadyInUseException;
import com.softwaremagico.kt.core.exceptions.TeamNotFoundException;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
public class TeamProvider {
    private final TeamRepository teamRepository;
    private final RoleProvider roleProvider;

    @Autowired
    public TeamProvider(TeamRepository teamRepository, RoleProvider roleProvider) {
        this.teamRepository = teamRepository;
        this.roleProvider = roleProvider;
    }

    public Team save(Team team) {
        if (team.getId() == null && get(team.getTournament(), team.getName()) != null) {
            throw new NameAlreadyInUseException(TeamProvider.class, "Already exists a team with name '" + team.getName() + "'.");
        }
        return teamRepository.save(team);
    }

    public List<Team> save(Collection<Team> teams) {
        return teamRepository.saveAll(teams);
    }

    public Team update(Team team) {
        if (team.getId() == null) {
            throw new TeamNotFoundException(getClass(), "Team with null id does not exists.");
        }
        return teamRepository.save(team);
    }

    public Team update(Team team, List<Participant> members) {
        if (team != null) {
            //Remove old members
            team.getMembers().clear();
            team = update(team);
            team.setMembers(members);
            return teamRepository.save(team);
        }
        return null;
    }

    public Team get(Tournament tournament, String name) {
        final Team team = teamRepository.findByTournamentAndName(tournament, name);
        team.setTournament(tournament);
        return team;
    }

    public Team get(int id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new TeamNotFoundException(getClass(), "Team with id '" + id + "' not found"));
    }


    public List<Team> getAll() {
        return teamRepository.findAll();
    }

    public long count() {
        return teamRepository.count();
    }

    public List<Team> getAll(Tournament tournament) {
        final List<Team> teams = teamRepository.findByTournament(tournament);
        if (teams.isEmpty() && tournament.getType() == TournamentType.LEAGUE) {
            final List<Team> newTeams = new ArrayList<>();
            final long competitors = roleProvider.count(tournament);
            for (int i = 1; i <= (competitors + tournament.getTeamSize() - 1) / tournament.getTeamSize(); i++) {
                final Team team = new Team();
                team.setName(String.format("Team %d", i));
                team.setTournament(tournament);
                team.setGroup(0);
                newTeams.add(team);
            }
            teams.addAll(save(newTeams));
        } else {
            for (final Team team : teams) {
                team.setTournament(tournament);
            }
        }
        return teams;
    }

    public long count(Tournament tournament) {
        return teamRepository.countByTournament(tournament);
    }

    public void delete(Team team) {
        teamRepository.delete(team);
    }

    public void delete(Tournament tournament) {
        teamRepository.deleteByTournament(tournament);
    }

    public void delete(Integer id) {
        if (teamRepository.existsById(id)) {
            teamRepository.deleteById(id);
        } else {
            throw new TeamNotFoundException(getClass(), "Team with id '" + id + "' not found");
        }
    }

    public Team get(Tournament tournament, Participant participant) {
        return teamRepository.findByTournamentAndMembers(tournament, participant);
    }

    public Team delete(Tournament tournament, Participant member) {
        Team team = get(tournament, member);
        if (team != null) {
            //Setting tournament for updating.
            team.setTournament(tournament);
            team.getMembers().remove(member);
            team = update(team);
            //setting tournament for returning element.
            team.setTournament(tournament);
        }
        return team;
    }

}
