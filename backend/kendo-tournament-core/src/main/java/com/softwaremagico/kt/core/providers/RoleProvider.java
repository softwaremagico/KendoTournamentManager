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

import com.softwaremagico.kt.core.exceptions.RoleNotFoundException;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class RoleProvider {
    private final RoleRepository roleRepository;

    @Autowired
    public RoleProvider(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Role save(Role role) {
        return roleRepository.save(role);
    }

    public Role update(Role role) {
        if (role.getId() == null) {
            throw new RoleNotFoundException(getClass(), "Role with null id does not exists.");
        }
        return roleRepository.save(role);
    }

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    public Role get(int id) {
        return roleRepository.findById(id)
                .orElseThrow(() -> new RoleNotFoundException(getClass(), "Role with id '" + id + "' not found"));
    }

    public long count() {
        return roleRepository.count();
    }

    public List<Role> getAll(Tournament tournament) {
        return roleRepository.findByTournament(tournament);
    }

    public List<Role> getAll(Tournament tournament, RoleType roleType) {
        return roleRepository.findByTournamentAndType(tournament, roleType);
    }

    public List<Role> getAll(Tournament tournament, Collection<RoleType> roleTypes) {
        return roleRepository.findByTournamentAndTypeIn(tournament, roleTypes);
    }

    public long count(Tournament tournament) {
        return roleRepository.countByTournament(tournament);
    }

    public void delete(Role role) {
        roleRepository.delete(role);
    }

    public void delete(Integer id) {
        if (roleRepository.existsById(id)) {
            roleRepository.deleteById(id);
        } else {
            throw new RoleNotFoundException(getClass(), "Role with id '" + id + "' not found");
        }
    }
}
