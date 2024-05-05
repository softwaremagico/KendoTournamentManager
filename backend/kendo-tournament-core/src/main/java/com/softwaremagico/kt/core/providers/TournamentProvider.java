package com.softwaremagico.kt.core.providers;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.tournaments.ITournamentManager;
import com.softwaremagico.kt.core.tournaments.TournamentHandlerSelector;
import com.softwaremagico.kt.core.tournaments.TreeTournamentHandler;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Role;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.entities.TournamentExtraProperty;
import com.softwaremagico.kt.persistence.entities.TournamentImage;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.repositories.GroupRepository;
import com.softwaremagico.kt.persistence.repositories.RoleRepository;
import com.softwaremagico.kt.persistence.repositories.TeamRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentExtraPropertyRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentImageRepository;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import com.softwaremagico.kt.persistence.values.TournamentType;
import com.softwaremagico.kt.utils.GroupUtils;
import com.softwaremagico.kt.utils.NameUtils;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TournamentProvider extends CrudProvider<Tournament, Integer, TournamentRepository> {
    private static final int CACHE_EXPIRATION_TIME = 20 * 1000;
    public static final int DEFAULT_TEAM_SIZE = 3;
    private final TournamentExtraPropertyRepository tournamentExtraPropertyRepository;
    private final GroupRepository groupRepository;
    private final FightRepository fightRepository;
    private final DuelRepository duelRepository;
    private final TeamRepository teamRepository;
    private final RoleRepository roleRepository;
    private final TournamentHandlerSelector tournamentHandlerSelector;

    private final TournamentImageRepository tournamentImageRepository;

    @Autowired
    public TournamentProvider(TournamentRepository tournamentRepository, TournamentExtraPropertyRepository tournamentExtraPropertyRepository,
                              GroupRepository groupRepository, FightRepository fightRepository, DuelRepository duelRepository,
                              TeamRepository teamRepository, RoleRepository roleRepository, TournamentHandlerSelector tournamentHandlerSelector,
                              TournamentImageRepository tournamentImageRepository) {
        super(tournamentRepository);
        this.tournamentExtraPropertyRepository = tournamentExtraPropertyRepository;
        this.groupRepository = groupRepository;
        this.fightRepository = fightRepository;
        this.duelRepository = duelRepository;
        this.teamRepository = teamRepository;
        this.roleRepository = roleRepository;
        this.tournamentHandlerSelector = tournamentHandlerSelector;
        this.tournamentImageRepository = tournamentImageRepository;
    }

    @Transactional
    @Override
    public Tournament save(Tournament entity) {
        final boolean newEntity = entity.getId() == null;
        final Tournament tournament = super.save(entity);
        //Only for new tournaments.
        if (newEntity) {
            setDefaultProperties(tournament, tournament.getCreatedBy());
        }
        return tournament;
    }


    public Tournament save(String name, Integer shiaijos, Integer teamSize, TournamentType type, String createdBy) {
        final Tournament tournament = getRepository().save(new Tournament(name, shiaijos != null ? shiaijos : 1,
                teamSize != null ? teamSize : DEFAULT_TEAM_SIZE,
                type != null ? type : TournamentType.LEAGUE, createdBy));
        setDefaultProperties(tournament, createdBy);
        return tournament;
    }

    private void setDefaultProperties(Tournament tournament, String username) {
        final List<TournamentExtraProperty> properties = tournamentExtraPropertyRepository.findDistinctPropertyKeyByCreatedByOrderByCreatedAtDesc(username);
        properties.removeIf(tournamentExtraProperty -> Objects.equals(tournamentExtraProperty.getTournament().getId(), tournament.getId()));
        final List<TournamentExtraProperty> newProperties = new ArrayList<>();
        properties.forEach(tournamentExtraProperty -> {
            newProperties.add(TournamentExtraProperty.copy(tournamentExtraProperty));
            newProperties.get(newProperties.size() - 1).setTournament(tournament);
            newProperties.get(newProperties.size() - 1).setCreatedBy(username);
        });
        if (!newProperties.isEmpty()) {
            tournamentExtraPropertyRepository.saveAll(newProperties);
        }
    }

    @CacheEvict(allEntries = true, value = {"tournaments-by-id"})
    @Override
    public void delete(Tournament tournament) {
        if (tournament != null) {
            tournamentExtraPropertyRepository.deleteByTournament(tournament);
            groupRepository.deleteByTournament(tournament);
            fightRepository.deleteByTournament(tournament);
            duelRepository.deleteByTournament(tournament);
            teamRepository.deleteByTournament(tournament);
            roleRepository.deleteByTournament(tournament);
            getRepository().delete(tournament);
        }
    }

    @CacheEvict(allEntries = true, value = {"tournaments-by-id"})
    @Override
    public Tournament update(Tournament tournament) {
        if (tournament.isLocked() && tournament.getLockedAt() == null) {
            tournament.setLockedAt(LocalDateTime.now());
        } else if (!tournament.isLocked()) {
            tournament.setLockedAt(null);
        }
        return super.update(tournament);
    }

    public List<Tournament> getPreviousTo(Tournament tournament, int elementsToRetrieve) {
        if (tournament == null || tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        // Due to LocalDateTime encryption countByGreaterThan is not working very well.
        // final Pageable pageable = PageRequest.of(0, elementsToRetrieve, Sort.Direction.DESC, "createdAt");
        // return getRepository().findByCreatedAtLessThan(tournament.getCreatedAt(), pageable);
        final List<Tournament> tournaments = getRepository().findAll();
        tournaments.sort(Comparator.comparing(Tournament::getCreatedAt).reversed());
        return tournaments.subList(tournaments.indexOf(tournament) + 1, Math.min(tournaments.indexOf(tournament) + 1 + elementsToRetrieve, tournaments.size()));
    }

    public List<Tournament> getPreviousTo(Tournament tournament) {
        if (tournament == null || tournament.getCreatedAt() == null) {
            return new ArrayList<>();
        }
        final List<Tournament> tournaments = getRepository().findAll();
        tournaments.sort(Comparator.comparing(Tournament::getCreatedAt).reversed());
        return tournaments.subList(tournaments.indexOf(tournament) + 1, tournaments.size());
    }

    public long countTournamentsAfter(LocalDateTime createdAfter) {
        if (createdAfter == null) {
            return getRepository().findAll().stream().filter(tournament -> tournament.getCreatedAt() != null
                    && tournament.getCreatedAt().isAfter(LocalDateTime.now().minusYears(1)
                    .with(LocalTime.MIN))).count();

        }
        //Due to LocalDateTime encryption countByGreaterThan is not working very well.
        return getRepository().findAll().stream().filter(tournament ->
                tournament.getCreatedAt() != null && tournament.getCreatedAt().isAfter(createdAfter.with(LocalTime.MIN))).count();
    }

    @CacheEvict(allEntries = true, value = {"tournaments-by-id"})
    public void markAsFinished(Tournament tournament, boolean finish) {
        if (finish && tournament.getFinishedAt() == null) {
            tournament.updateFinishedAt(LocalDateTime.now());
            getRepository().save(tournament);
        } else if (!finish && tournament.getFinishedAt() != null) {
            tournament.updateFinishedAt(null);
            getRepository().save(tournament);
        }
    }

    public void setNumberOfWinners(Integer tournamentId, Integer numberOfWinners, String updatedBy) {
        final Tournament tournament = get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "'."));
        final ITournamentManager tournamentManager = tournamentHandlerSelector.selectManager(tournament.getType());
        if (tournamentManager instanceof TreeTournamentHandler) {
            tournamentExtraPropertyRepository.deleteByTournamentAndPropertyKey(tournament, TournamentExtraPropertyKey.NUMBER_OF_WINNERS);
            tournamentExtraPropertyRepository.save(new TournamentExtraProperty(tournament,
                    TournamentExtraPropertyKey.NUMBER_OF_WINNERS, String.valueOf(numberOfWinners)));

            //Update winners in group
            final List<Group> groups = groupRepository.findByTournamentOrderByLevelAscIndexAsc(tournament);
            final Map<Integer, List<Group>> groupsByLevel = GroupUtils.orderByLevel(groups);
            if (groupsByLevel.get(0) != null) {
                groupsByLevel.get(0).forEach(group -> group.setNumberOfWinners(numberOfWinners));
                groupRepository.saveAll(groupsByLevel.get(0));
            }

            //Resize tournament
            ((TreeTournamentHandler) tournamentManager).recreateGroupSize(tournament, numberOfWinners);
            KendoTournamentLogger.info(this.getClass(), "Updated tournament '{}' with number of winners '{}' by '{}'", tournament, numberOfWinners, updatedBy);
        } else {
            KendoTournamentLogger.warning(this.getClass(), "Cannot change the number of winners as tournament is of type '{}'.", tournament.getType());
        }
    }

    public Tournament clone(Tournament sourceTournament, String username) {

        final List<Role> sourceRoles = roleRepository.findByTournament(sourceTournament);

        final List<Team> sourceTeams = teamRepository.findByTournament(sourceTournament);

        final List<TournamentExtraProperty> sourceProperties = tournamentExtraPropertyRepository.findByTournament(sourceTournament);

        final List<TournamentImage> images = tournamentImageRepository.findByTournament(sourceTournament);

        //Update tournament
        sourceTournament.setName(NameUtils.getNameCopy(sourceTournament));
        sourceTournament.setId(null);
        sourceTournament.setCreatedBy(username);
        if (sourceTournament.getTournamentScore() != null) {
            sourceTournament.getTournamentScore().setId(null);
        }

        final Tournament clonedTournament = save(sourceTournament);

        sourceRoles.forEach(role -> {
            role.setTournament(clonedTournament);
            role.setId(null);
        });
        roleRepository.saveAll(sourceRoles);

        sourceTeams.forEach(team -> {
            team.setTournament(clonedTournament);
            team.setId(null);
            team.setMembers(new ArrayList<>(team.getMembers()));
        });
        teamRepository.saveAll(sourceTeams);

        sourceProperties.forEach(property -> {
            property.setTournament(clonedTournament);
            property.setId(null);
        });
        tournamentExtraPropertyRepository.deleteByTournament(sourceTournament);
        tournamentExtraPropertyRepository.saveAll(sourceProperties);

        images.forEach(image -> {
            image.setTournament(clonedTournament);
            image.setId(null);
        });
        tournamentImageRepository.saveAll(images);

        //Add default group:
        final Group group = new Group();
        group.setCreatedBy(username);
        group.setTournament(clonedTournament);
        groupRepository.save(group);

        return clonedTournament;
    }

    public Tournament create(String name, Integer shiaijos, Integer teamSize, TournamentType type, String username) {
        final Tournament tournament = save(new Tournament(name, shiaijos != null ? shiaijos : 1,
                teamSize != null ? teamSize : TournamentProvider.DEFAULT_TEAM_SIZE, type != null ? type : TournamentType.LEAGUE, username));
        //Add default group:
        final Group group = new Group();
        group.setCreatedBy(username);
        group.setTournament(tournament);
        groupRepository.save(group);
        return tournament;
    }

    public Tournament findLastByUnlocked() {
        final List<Tournament> tournaments = getRepository().findByLocked(false);
        if (!tournaments.isEmpty()) {
            tournaments.sort(Comparator.comparing(Tournament::getCreatedAt).reversed());
            return tournaments.get(0);
        }
        return null;
    }
}
