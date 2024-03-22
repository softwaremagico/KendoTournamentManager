package com.softwaremagico.kt.core.controller;

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

import com.softwaremagico.kt.core.controller.models.DuelDTO;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.core.converters.FightConverter;
import com.softwaremagico.kt.core.converters.TournamentConverter;
import com.softwaremagico.kt.core.converters.models.FightConverterRequest;
import com.softwaremagico.kt.core.converters.models.TournamentConverterRequest;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.managers.TeamsOrder;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.ITournamentManager;
import com.softwaremagico.kt.core.tournaments.TournamentHandlerSelector;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
public class FightController extends BasicInsertableController<Fight, FightDTO, FightRepository,
        FightProvider, FightConverterRequest, FightConverter> {
    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;
    private final TournamentHandlerSelector tournamentHandlerSelector;

    private final Set<FightsAddedListener> fightsAddedListeners = new HashSet<>();

    public interface FightsAddedListener {
        void created(List<FightDTO> fights, String actor);
    }


    @Autowired
    public FightController(FightProvider provider, FightConverter converter, TournamentConverter tournamentConverter,
                           TournamentProvider tournamentProvider, TournamentHandlerSelector tournamentHandlerSelector) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.tournamentHandlerSelector = tournamentHandlerSelector;
    }

    public void addFightsAddedListeners(FightsAddedListener listener) {
        fightsAddedListeners.add(listener);
    }

    @Override
    protected FightConverterRequest createConverterRequest(Fight entity) {
        return new FightConverterRequest(entity);
    }

    public List<FightDTO> getByTournamentId(Integer tournamentId) {
        return get(tournamentConverter.convert(new TournamentConverterRequest(tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)))));
    }

    public List<FightDTO> get(TournamentDTO tournamentDTO) {
        return convertAll(getProvider().getFights(tournamentConverter.reverse(tournamentDTO)));
    }

    public void delete(TournamentDTO tournamentDTO) {
        getProvider().delete(tournamentConverter.reverse(tournamentDTO));
    }

    public FightDTO generateDuels(FightDTO fightDTO, String createdBy) {
        fightDTO.getDuels().clear();
        boolean added = false;
        if (fightDTO.getTeam1() != null && fightDTO.getTeam2() != null) {
            for (int i = 0; i < Math.max(fightDTO.getTeam1().getMembers().size(), fightDTO.getTeam2().getMembers().size()); i++) {
                final DuelDTO duelDTO = new DuelDTO(i < fightDTO.getTeam1().getMembers().size() ? fightDTO.getTeam1().getMembers().get(i) : null,
                        i < fightDTO.getTeam2().getMembers().size() ? fightDTO.getTeam2().getMembers().get(i) : null, fightDTO.getTournament(), createdBy);
                fightDTO.getDuels().add(duelDTO);
                added = true;
            }
        }
        if (added) {
            tournamentProvider.markAsFinished(tournamentConverter.reverse(fightDTO.getTournament()), false);
        }
        return fightDTO;
    }

    public boolean areOver(TournamentDTO tournament) {
        return getProvider().areOver(tournamentConverter.reverse(tournament));
    }

    public long count(TournamentDTO tournament) {
        return getProvider().count(tournamentConverter.reverse(tournament));
    }

    public FightDTO getCurrent(TournamentDTO tournament) {
        return convert(getProvider().getCurrent(tournamentConverter.reverse(tournament)));
    }

    public FightDTO getCurrent(Integer tournamentId) {
        return convert(getProvider().getCurrent((tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)))));
    }

    public List<FightDTO> createFights(Integer tournamentId, TeamsOrder teamsOrder, Integer level, String createdBy) {
        final Tournament tournament = (tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)));
        final ITournamentManager selectedManager = tournamentHandlerSelector.selectManager(tournament.getType());
        if (selectedManager != null) {
            final List<Fight> createdFights = getProvider().saveAll(selectedManager.createFights(tournament, teamsOrder, level, createdBy));
            tournamentProvider.markAsFinished(tournament, false);
            final List<FightDTO> fightDTOS = convertAll(createdFights);
            try {
                return fightDTOS;
            } finally {
                new Thread(() ->
                        fightsAddedListeners.forEach(fightsAddedListener -> fightsAddedListener.created(fightDTOS, createdBy))
                ).start();
            }
        }
        return new ArrayList<>();
    }

    public List<FightDTO> createNextFights(Integer tournamentId, String createdBy) {
        final Tournament tournament = (tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)));
        final ITournamentManager selectedManager = tournamentHandlerSelector.selectManager(tournament.getType());
        if (selectedManager != null) {
            final List<Fight> createdFights = getProvider().saveAll(selectedManager.generateNextFights(tournament, createdBy));
            if (!createdFights.isEmpty()) {
                tournamentProvider.markAsFinished(tournament, false);
            }
            final List<FightDTO> fightDTOS = convertAll(createdFights);
            try {
                return fightDTOS;
            } finally {
                new Thread(() ->
                        fightsAddedListeners.forEach(fightsAddedListener -> fightsAddedListener.created(fightDTOS, createdBy))
                ).start();
            }
        }
        return new ArrayList<>();
    }

}
