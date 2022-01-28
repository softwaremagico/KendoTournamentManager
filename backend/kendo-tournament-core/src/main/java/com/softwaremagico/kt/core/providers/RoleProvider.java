package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<Role> getAll() {
        return roleRepository.findAll();
    }

    public long count() {
        return roleRepository.count();
    }

    public List<Role> getAll(Tournament tournament) {
        return roleRepository.findByTournament(tournament);
    }

    public long count(Tournament tournament) {
        return roleRepository.countByTournament(tournament);
    }
}
