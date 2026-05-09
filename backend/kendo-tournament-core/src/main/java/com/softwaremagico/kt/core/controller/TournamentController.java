package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.TournamentRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Business-logic controller for {@link Tournament} entities.
 * <p>
 * Extends {@link BasicInsertableController} with tournament-specific behaviour:
 * </p>
 * <ul>
 *   <li><b>create</b> — After persisting the tournament, automatically creates a
 *       default {@link Group} so that teams can be assigned immediately.</li>
 *   <li><b>update</b> — Manages the locked-state transition: sets
 *       {@code lockedAt} and {@code finishedAt} timestamps when the tournament is
 *       first locked; clears them when unlocked. Also propagates any change to
 *       {@code duelsDuration} to all existing {@link Duel}s in the tournament.</li>
 * </ul>
 * <p>
 * The {@code "tournaments-by-id"} cache is evicted on every update.
 * </p>
 */
@Controller
public class TournamentController extends BasicInsertableController<Tournament, TournamentDTO, TournamentRepository,
        TournamentProvider, TournamentConverterRequest, TournamentConverter> {

    private final GroupProvider groupProvider;
    private final DuelProvider duelProvider;


    @Autowired
    public TournamentController(TournamentProvider provider, TournamentConverter converter, GroupProvider groupProvider, DuelProvider duelProvider) {
        super(provider, converter);
        this.groupProvider = groupProvider;
        this.duelProvider = duelProvider;
    }

    @Override
    protected TournamentConverterRequest createConverterRequest(Tournament entity) {
        return new TournamentConverterRequest(entity);
    }

    /**
     * Creates a new tournament and immediately adds a default empty {@link Group}
     * so that teams can be assigned without a separate API call.
     *
     * @param tournamentDTO the tournament data to persist
     * @param username      the authenticated user performing the creation
     * @param session       the client session identifier for WebSocket notifications
     * @return the persisted tournament as a DTO
     */
    @Override
    public TournamentDTO create(TournamentDTO tournamentDTO, String username, String session) {
        final TournamentDTO createdTournamentDTO = super.create(tournamentDTO, username, session);
        final Group group = new Group();
        group.setCreatedBy(username);
        groupProvider.addGroup(reverse(createdTournamentDTO), group);
        return createdTournamentDTO;
    }

    /**
     * Updates an existing tournament.
     * <p>
     * In addition to the standard update, this method handles the locked-state
     * transition: if the tournament is being locked for the first time,
     * {@code lockedAt} and {@code finishedAt} timestamps are set to the current
     * time. If it is being unlocked, both timestamps are cleared.
     * </p>
     * <p>
     * If the {@code duelsDuration} has changed, all existing incomplete duels (and
     * duels whose duration is shorter than the new value) are updated accordingly.
     * </p>
     * <p>
     * The {@code "tournaments-by-id"} cache is evicted on every call.
     * </p>
     *
     * @param tournamentDTO the updated tournament data
     * @param username      the authenticated user performing the update
     * @param session       the client session identifier for WebSocket notifications
     * @return the updated tournament as a DTO
     */
    @CacheEvict(allEntries = true, value = {"tournaments-by-id"})
    @Override
    public TournamentDTO update(TournamentDTO tournamentDTO, String username, String session) {
        //If a tournament is locked we can define it as finished (maybe fights are not finished by time).
        if (tournamentDTO.isLocked() && tournamentDTO.getFinishedAt() == null) {
            tournamentDTO.setFinishedAt(LocalDateTime.now());
        }
        if (tournamentDTO.isLocked() && tournamentDTO.getLockedAt() == null) {
            tournamentDTO.setLockedAt(LocalDateTime.now());
        }
        final Optional<Tournament> previousData = getProvider().get(tournamentDTO.getId());
        try {
            return super.update(tournamentDTO, username, session);
        } finally {
            // We need to update all duels durations if already are defined, and duration is changed.
            if (previousData.isPresent() && tournamentDTO.getDuelsDuration() != null
                    && !Objects.equals(previousData.get().getDuelsDuration(), tournamentDTO.getDuelsDuration())) {
                //Update all duels
                final List<Duel> duels = duelProvider.get(previousData.get());
                duels.forEach(duel -> {
                    if (!duel.isOver() || (duel.getDuration() != null && duel.getDuration() < tournamentDTO.getDuelsDuration())) {
                        duel.setTotalDuration(tournamentDTO.getDuelsDuration());
                    }
                });
                if (!duels.isEmpty()) {
                    duelProvider.saveAll(duels);
                }
            }
        }
    }

    /**
     * Creates a new tournament from individual parameters rather than from a full DTO.
     *
     * @param name     the unique name of the tournament
     * @param shiaijos the number of simultaneous fighting areas
     * @param teamSize the number of members per team
     * @param type     the structural format of the tournament
     * @param username the authenticated user performing the creation
     * @return the persisted tournament as a DTO
     */
    public TournamentDTO create(String name, Integer shiaijos, Integer teamSize, TournamentType type, String username) {
        return convert(getProvider().create(name, shiaijos, teamSize, type, username));
    }

    /**
     * Creates a deep copy of the tournament identified by the given ID.
     *
     * @param tournamentId the ID of the tournament to clone
     * @param username     the authenticated user performing the operation
     * @return the cloned tournament as a DTO
     */
    public TournamentDTO clone(Integer tournamentId, String username) {
        return clone(get(tournamentId), username);
    }

    /**
     * Creates a deep copy of the given tournament DTO.
     *
     * @param tournamentDTO the tournament to clone
     * @param username      the authenticated user performing the operation
     * @return the cloned tournament as a DTO
     */
    public TournamentDTO clone(TournamentDTO tournamentDTO, String username) {
        return convert(getProvider().clone(reverse(tournamentDTO), username));
    }

    /**
     * Sets the number of winning teams per group for the given tournament.
     *
     * @param tournamentId    the ID of the tournament to update
     * @param numberOfWinners the number of teams that advance from each group
     * @param updatedBy       the username of the user performing the update
     */
    public void setNumberOfWinners(Integer tournamentId, Integer numberOfWinners, String updatedBy) {
        getProvider().setNumberOfWinners(tournamentId, numberOfWinners, updatedBy);
    }

    @Override
    public void deleteById(Integer id, String username, String session) {
        delete(get(id), username, session);
    }

    /**
     * Returns a list of tournaments that ended before the given tournament,
     * limited to the specified number of results.
     *
     * @param tournamentDTO      the reference tournament
     * @param elementsToRetrieve maximum number of previous tournaments to return
     * @return list of previous tournaments ordered by finish date descending
     */
    public List<TournamentDTO> getPreviousTo(TournamentDTO tournamentDTO, int elementsToRetrieve) {
        return convertAll(getProvider().getPreviousTo(reverse(tournamentDTO), elementsToRetrieve));
    }

    /**
     * Returns the most recently modified tournament that has not been locked.
     *
     * @return the latest unlocked tournament as a DTO, or {@code null} if none exists
     */
    public TournamentDTO getLatestUnlocked() {
        return convert(getProvider().findLastByUnlocked());
    }
}
