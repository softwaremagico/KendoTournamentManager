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
        return repository.findByTournament(tournament);
    }

    public List<Role> getAll(Tournament tournament, RoleType roleType) {
        return repository.findByTournamentAndRoleType(tournament, roleType);
    }

    public List<Role> getAll(Tournament tournament, Collection<RoleType> roleTypes) {
        return repository.findByTournamentAndRoleTypeIn(tournament, roleTypes);
    }

    public long count(Tournament tournament) {
        return repository.countByTournament(tournament);
    }

    public void delete(Participant participant, Tournament tournament) {
        repository.deleteByParticipantAndTournament(participant, tournament);
    }
}
