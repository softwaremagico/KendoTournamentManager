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

import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class RoleProvider extends CrudProvider<Role, Integer, RoleRepository> {

    @Autowired
    public RoleProvider(RoleRepository repository) {
        super(repository);
    }

    public List<Role> getAll(Tournament tournament) {
        final List<Role> roles = repository.findByTournament(tournament);
        roles.forEach(role -> role.setTournament(tournament));
        return roles;
    }

    public List<Role> getAll(Tournament tournament, RoleType roleType) {
        final List<Role> roles = repository.findByTournamentAndRoleType(tournament, roleType);
        roles.forEach(role -> role.setTournament(tournament));
        return roles;
    }

    public List<Role> getAll(RoleType roleType) {
        return repository.findByRoleType(roleType);
    }

    public List<Role> getAll(Tournament tournament, Collection<RoleType> roleTypes) {
        final List<Role> roles = repository.findByTournamentAndRoleTypeIn(tournament, roleTypes);
        roles.forEach(role -> role.setTournament(tournament));
        return roles;
    }

    public List<Role> getAllForDiplomas(Tournament tournament, Boolean onlyNewDiplomas, Collection<RoleType> roleTypes) {
        final List<Role> roles;
        if (onlyNewDiplomas != null && onlyNewDiplomas) {
            if (!roleTypes.isEmpty()) {
                roles = repository.findByTournamentAndDiplomaPrintedAndRoleTypeIn(tournament, false, roleTypes);
            } else {
                roles = repository.findByTournamentAndDiplomaPrinted(tournament, false);
            }
        } else {
            if (!roleTypes.isEmpty()) {
                roles = repository.findByTournamentAndRoleTypeIn(tournament, roleTypes);
            } else {
                roles = repository.findByTournament(tournament);
            }
        }
        roles.forEach(role -> role.setTournament(tournament));
        return roles;
    }

    public List<Role> getAllForAccreditations(Tournament tournament, Boolean onlyNewAccreditations, Collection<RoleType> roleTypes) {
        final List<Role> roles;
        if (onlyNewAccreditations != null && onlyNewAccreditations) {
            if (!roleTypes.isEmpty()) {
                roles = repository.findByTournamentAndAccreditationPrintedAndRoleTypeIn(tournament, false, roleTypes);
            } else {
                roles = repository.findByTournamentAndAccreditationPrinted(tournament, false);
            }
        } else {
            if (!roleTypes.isEmpty()) {
                roles = repository.findByTournamentAndRoleTypeIn(tournament, roleTypes);
            } else {
                roles = repository.findByTournament(tournament);
            }
        }
        roles.forEach(role -> role.setTournament(tournament));
        return roles;
    }

    public List<Role> get(Tournament tournament, List<Participant> participants) {
        return repository.findByTournamentAndParticipantIn(tournament, participants);
    }

    public Role get(Tournament tournament, Participant participant) {
        return repository.findByTournamentAndParticipant(tournament, participant);
    }

    public long count(Tournament tournament) {
        return repository.countByTournament(tournament);
    }

    public long count(Tournament tournament, RoleType roleType) {
        return repository.countByTournamentAndRoleType(tournament, roleType);
    }

    public long count(Participant participant, RoleType roleType) {
        return repository.countByParticipantAndRoleType(participant, roleType);
    }

    public void delete(Participant participant, Tournament tournament) {
        repository.deleteByParticipantAndTournament(participant, tournament);
    }

    public long delete(Tournament tournament) {
        return repository.deleteByTournament(tournament);
    }
}
