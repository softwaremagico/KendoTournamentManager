package com.softwaremagico.kt.core.providers;

import com.softwaremagico.kt.core.exceptions.NameAlreadyInUseException;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamProvider {
    private final TeamRepository teamRepository;

    @Autowired
    public TeamProvider(TeamRepository teamRepository) {
        this.teamRepository = teamRepository;
    }

    public Team save(Team team) {
        if (team.getId() == null && get(team.getTournament(), team.getName()) != null) {
            throw new NameAlreadyInUseException(TeamProvider.class, "Already exists a team with name '" + team.getName() + "'.");
        }
        return teamRepository.save(team);
    }

    public Team get(Tournament tournament, String name) {
        return teamRepository.findByTournamentAndName(tournament, name);
    }

    public List<Team> getAll() {
        return teamRepository.findAll();
    }

    public long count() {
        return teamRepository.count();
    }

    public List<Team> getAll(Tournament tournament) {
        return teamRepository.findByTournament(tournament);
    }

    public long count(Tournament tournament) {
        return teamRepository.countByTournament(tournament);
    }
}
