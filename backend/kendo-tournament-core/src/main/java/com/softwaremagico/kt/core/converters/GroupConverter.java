package com.softwaremagico.kt.core.converters;

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

import com.softwaremagico.kt.core.controller.models.GroupDTO;
import com.softwaremagico.kt.core.converters.models.*;
import com.softwaremagico.kt.persistence.entities.Group;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class GroupConverter extends ElementConverter<Group, GroupDTO, GroupConverterRequest> {
    private final TournamentConverter tournamentConverter;
    private final FightConverter fightConverter;
    private final DuelConverter duelConverter;
    private final TeamConverter teamConverter;

    @Autowired
    public GroupConverter(TournamentConverter tournamentConverter, FightConverter fightConverter, DuelConverter duelConverter, TeamConverter teamConverter) {
        this.tournamentConverter = tournamentConverter;
        this.fightConverter = fightConverter;
        this.duelConverter = duelConverter;
        this.teamConverter = teamConverter;
    }


    @Override
    public GroupDTO convert(GroupConverterRequest from) {
        final GroupDTO groupDTO = new GroupDTO();
        BeanUtils.copyProperties(from.getEntity(), groupDTO, ConverterUtils.getNullPropertyNames(from.getEntity()));
        groupDTO.setTournament(tournamentConverter.convert(
                new TournamentConverterRequest(from.getEntity().getTournament())));
        groupDTO.setFights(new ArrayList<>());
        from.getEntity().getFights().forEach(fight ->
                groupDTO.getFights().add(fightConverter.convert(new FightConverterRequest(fight))));
        groupDTO.setUnties(new ArrayList<>());
        from.getEntity().getUnties().forEach(duel ->
                groupDTO.getUnties().add(duelConverter.convert(new DuelConverterRequest(duel))));
        groupDTO.setTeams(new ArrayList<>());
        from.getEntity().getTeams().forEach(team ->
                groupDTO.getTeams().add(teamConverter.convert(new TeamConverterRequest(team))));
        return groupDTO;
    }

    @Override
    public Group reverse(GroupDTO to) {
        if (to == null) {
            return null;
        }
        final Group group = new Group();
        BeanUtils.copyProperties(to, group, ConverterUtils.getNullPropertyNames(to));
        group.setTournament(tournamentConverter.reverse(to.getTournament()));
        group.setFights(new ArrayList<>());
        if (to.getFights() != null) {
            to.getFights().forEach(fight -> group.getFights().add(fightConverter.reverse(fight)));
        }
        group.setUnties(new ArrayList<>());
        if (to.getUnties() != null) {
            to.getUnties().forEach(duel -> group.getUnties().add(duelConverter.reverse(duel)));
        }
        group.setTeams(new ArrayList<>());
        if (to.getTeams() != null) {
            to.getTeams().forEach(team -> group.getTeams().add(teamConverter.reverse(team)));
        }
        return group;
    }
}
