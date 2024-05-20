package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.DuelConverter;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.DuelConverterRequest;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.exceptions.FightNotFoundException;
import com.softwaremagico.kt.core.exceptions.ParticipantNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.exceptions.ValidateBadRequestException;
import com.softwaremagico.kt.core.providers.DuelProvider;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.DuelRepository;
import com.softwaremagico.kt.persistence.values.Score;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class DuelController extends BasicInsertableController<Duel, DuelDTO, DuelRepository,
        DuelProvider, DuelConverterRequest, DuelConverter> {

    private final TournamentConverter tournamentConverter;

    private final FightProvider fightProvider;

    private final FightConverter fightConverter;

    private final TournamentProvider tournamentProvider;

    private final ParticipantProvider participantProvider;

    private final Set<ShiaijoFinishedListener> shiaijoFinishedListeners = new HashSet<>();
    private final Set<FightUpdatedListener> fightsUpdatedListeners = new HashSet<>();

    public interface ShiaijoFinishedListener {
        void finished(TournamentDTO tournament, Integer shiaijo);
    }

    public interface FightUpdatedListener {
        void finished(TournamentDTO tournament, FightDTO fight, DuelDTO duel, String actor);
    }

    @Autowired
    public DuelController(DuelProvider provider,
                          DuelConverter converter,
                          TournamentConverter tournamentConverter,
                          FightProvider fightProvider, FightConverter fightConverter,
                          TournamentProvider tournamentProvider,
                          ParticipantProvider participantProvider) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.fightProvider = fightProvider;
        this.fightConverter = fightConverter;
        this.tournamentProvider = tournamentProvider;
        this.participantProvider = participantProvider;
    }

    public void addShiaijoFinishedListener(ShiaijoFinishedListener listener) {
        shiaijoFinishedListeners.add(listener);
    }

    public void addFightUpdatedListener(FightUpdatedListener listener) {
        fightsUpdatedListeners.add(listener);
    }

    @Override
    protected DuelConverterRequest createConverterRequest(Duel entity) {
        return new DuelConverterRequest(entity);
    }

    @Override
    public void validate(DuelDTO dto) throws ValidateBadRequestException {
        if (dto.getCompetitor1Score().contains(Score.EMPTY)
                || dto.getCompetitor1Score().contains(Score.DRAW)
                || dto.getCompetitor1Score().contains(Score.FAULT)) {
            throw new ValidateBadRequestException(this.getClass(), "Invalid score on duel '" + dto + "'");
        }
        if (dto.getCompetitor2Score().contains(Score.EMPTY)
                || dto.getCompetitor2Score().contains(Score.DRAW)
                || dto.getCompetitor2Score().contains(Score.FAULT)) {
            throw new ValidateBadRequestException(this.getClass(), "Invalid score on duel '" + dto + "'");
        }
    }

    @Override
    @CacheEvict(allEntries = true, value = {"ranking", "competitors-ranking"})
    public DuelDTO update(DuelDTO duel, String username) {
        try {
            return super.update(duel, username);
        } finally {
            new Thread(() -> {
                //If a shiaijo has finished, send a message to all computers.
                final Fight fight = fightProvider.findByDuels(reverse(duel)).orElseThrow(() ->
                        new FightNotFoundException(this.getClass(), "No fight found for duel '" + duel + "'"));

                final FightDTO fightDTO = fightConverter.convert(new FightConverterRequest(fight));

                final Tournament tournament = tournamentProvider.get(fight.getTournament().getId()).orElseThrow(()
                        -> new TournamentNotFoundException(this.getClass(), "No tournament found for duel '" + duel + "'."));

                final TournamentDTO tournamentDTO = tournamentConverter.convert(new TournamentConverterRequest(tournament));

                //Fight is updated, refresh screens.
                fightsUpdatedListeners.forEach(fightUpdatedListener -> fightUpdatedListener.finished(tournamentDTO, fightDTO, duel, username));

                if (tournament.getShiaijos() > 1) {
                    final List<Fight> fightsOfShiaijo = fightProvider.findByTournamentAndShiaijo(tournament, fight.getShiaijo());
                    final long fightsNotOver = fightsOfShiaijo.stream().filter(fightOfShiaijo -> !fightOfShiaijo.isOver()).count();
                    if (fightsNotOver == 0) {
                        shiaijoFinishedListeners.forEach(shiaijoFinishedListener
                                -> shiaijoFinishedListener.finished(tournamentDTO, fight.getShiaijo()));
                    }
                }
            }).start();
        }
    }

    public List<DuelDTO> getUntiesFromGroup(Integer groupId) {
        return convertAll(getProvider().getUntiesFromGroup(groupId));
    }

    public List<DuelDTO> getUntiesFromTournament(Integer tournamentId) {
        return convertAll(getProvider().getUntiesFromTournament(tournamentId));
    }

    public List<DuelDTO> getUntiesFromParticipant(Integer participantId) {
        final Participant participant = participantProvider.get(participantId).orElseThrow(() ->
                new ParticipantNotFoundException(this.getClass(), "No participant found with id '" + participantId + "'."));

        return convertAll(getProvider().getUntiesFromParticipant(participant));
    }

    public long count(TournamentDTO tournament) {
        return getProvider().count(tournamentConverter.reverse(tournament));
    }

    public void delete(TournamentDTO tournamentDTO) {
        getProvider().delete(tournamentConverter.reverse(tournamentDTO));
    }

    public List<DuelDTO> getBy(Integer participantId) {
        final Participant participant = participantProvider.get(participantId).orElseThrow(() ->
                new ParticipantNotFoundException(this.getClass(), "No participant found with id '" + participantId + "'."));

        return getBy(participant);
    }

    public List<DuelDTO> getBy(Participant participant) {
        return convertAll(getProvider().get(participant));
    }

}
