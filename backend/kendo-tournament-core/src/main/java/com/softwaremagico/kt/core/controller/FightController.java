package com.softwaremagico.kt.core.controller;

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
import com.softwaremagico.kt.core.providers.GroupProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.core.tournaments.CustomLeagueHandler;
import com.softwaremagico.kt.core.tournaments.ITournamentManager;
import com.softwaremagico.kt.core.tournaments.SimpleLeagueHandler;
import com.softwaremagico.kt.logger.ExceptionType;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Group;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.persistence.repositories.FightRepository;
import com.softwaremagico.kt.persistence.values.TournamentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FightController extends BasicInsertableController<Fight, FightDTO, FightRepository,
        FightProvider, FightConverterRequest, FightConverter> {
    private final TournamentConverter tournamentConverter;
    private final TournamentProvider tournamentProvider;
    private final GroupProvider groupProvider;
    private final SimpleLeagueHandler simpleLeagueHandler;

    private final CustomLeagueHandler customTournamentHandler;

    @Autowired
    public FightController(FightProvider provider, FightConverter converter, TournamentConverter tournamentConverter, TournamentProvider tournamentProvider,
                           GroupProvider groupProvider, SimpleLeagueHandler simpleLeagueHandler, CustomLeagueHandler customTournamentHandler) {
        super(provider, converter);
        this.tournamentConverter = tournamentConverter;
        this.tournamentProvider = tournamentProvider;
        this.groupProvider = groupProvider;
        this.simpleLeagueHandler = simpleLeagueHandler;
        this.customTournamentHandler = customTournamentHandler;
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
        return converter.convertAll(provider.getFights(tournamentConverter.reverse(tournamentDTO)).stream()
                .map(this::createConverterRequest).collect(Collectors.toList()));
    }

    @Override
    public void delete(FightDTO entity) {
        final Fight fight = converter.reverse(entity);
        final Group group = groupProvider.getGroup(fight);
        group.getFights().remove(fight);
        groupProvider.save(group);
        provider.delete(fight);
    }

    public void delete(TournamentDTO tournamentDTO) {
        groupProvider.getGroups(tournamentConverter.reverse(tournamentDTO)).forEach(group -> {
            group.setFights(new ArrayList<>());
            groupProvider.save(group);
        });
        provider.delete(tournamentConverter.reverse(tournamentDTO));
    }

    @Override
    public void delete(Collection<FightDTO> entities) {
        final List<Fight> fights = converter.reverseAll(entities);
        groupProvider.getGroups(fights).forEach(group -> {
            group.getFights().removeAll(fights);
            groupProvider.save(group);
        });
        provider.delete(fights);
    }

    public FightDTO generateDuels(FightDTO fightDTO, String createdBy) {
        fightDTO.getDuels().clear();
        if (fightDTO.getTeam1() != null && fightDTO.getTeam2() != null) {
            for (int i = 0; i < Math.max(fightDTO.getTeam1().getMembers().size(), fightDTO.getTeam2().getMembers().size()); i++) {
                final DuelDTO duelDTO = new DuelDTO(i < fightDTO.getTeam1().getMembers().size() ? fightDTO.getTeam1().getMembers().get(i) : null,
                        i < fightDTO.getTeam2().getMembers().size() ? fightDTO.getTeam2().getMembers().get(i) : null, fightDTO.getTournament(), createdBy);
                fightDTO.getDuels().add(duelDTO);
            }
        }
        return fightDTO;
    }

    public boolean areOver(TournamentDTO tournament) {
        return provider.areOver(tournamentConverter.reverse(tournament));
    }

    public long count(TournamentDTO tournament) {
        return provider.count(tournamentConverter.reverse(tournament));
    }

    public FightDTO getCurrent(TournamentDTO tournament) {
        return converter.convert(new FightConverterRequest(provider.getCurrent(tournamentConverter.reverse(tournament))));
    }

    public FightDTO getCurrent(Integer tournamentId) {
        return converter.convert(new FightConverterRequest(provider.getCurrent((tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO))))));
    }

    public List<FightDTO> createFights(Integer tournamentId, TeamsOrder teamsOrder, boolean maximizeFights, Integer level, String createdBy) {
        final Tournament tournament = (tournamentProvider.get(tournamentId)
                .orElseThrow(() -> new TournamentNotFoundException(getClass(), "No tournament found with id '" + tournamentId + "',",
                        ExceptionType.INFO)));
        final ITournamentManager selectedManager = selectManager(tournament.getType());
        if (selectedManager != null) {
            final List<Fight> createdFights = selectedManager.createFights(tournament, teamsOrder, maximizeFights, level, createdBy);
            provider.saveAll(createdFights);
            return converter.convertAll(createdFights.stream().map(this::createConverterRequest).collect(Collectors.toList()));

        }
        return new ArrayList<>();
    }

    private ITournamentManager selectManager(TournamentType type) {
        switch (type) {
            case LOOP:
                //manager = new LoopTournamentManager(tournament);
                break;
            case TREE:
            case CHAMPIONSHIP:
                //manager = new Championship(tournament);
                //manager.fillGroups();
                break;
            case CUSTOM_CHAMPIONSHIP:
                //manager = new CustomChampionship(tournament);
                //manager.fillGroups();
                break;
            case CUSTOMIZED:
                return customTournamentHandler;
            case KING_OF_THE_MOUNTAIN:
                //manager = new KingOfTheMountainTournament(tournament);
                break;
            case LEAGUE:
                return simpleLeagueHandler;
        }
        return null;
    }

}
