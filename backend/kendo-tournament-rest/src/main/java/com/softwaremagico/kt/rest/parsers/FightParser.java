package com.softwaremagico.kt.rest.parsers;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.exceptions.TeamNotFoundException;
import com.softwaremagico.kt.core.exceptions.TournamentNotFoundException;
import com.softwaremagico.kt.core.providers.FightProvider;
import com.softwaremagico.kt.core.providers.TeamProvider;
import com.softwaremagico.kt.core.providers.TournamentProvider;
import com.softwaremagico.kt.persistence.entities.Fight;
import com.softwaremagico.kt.persistence.entities.Team;
import com.softwaremagico.kt.persistence.entities.Tournament;
import com.softwaremagico.kt.rest.exceptions.BadRequestException;
import com.softwaremagico.kt.core.controller.models.FightDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FightParser {

    private final TournamentProvider tournamentProvider;
    private final TeamProvider teamProvider;
    private final FightProvider fightProvider;
    private final DuelParser duelParser;

    @Autowired
    public FightParser(TournamentProvider tournamentProvider, TeamProvider teamProvider, FightProvider fightProvider,
                       DuelParser duelParser) {
        this.tournamentProvider = tournamentProvider;
        this.teamProvider = teamProvider;
        this.fightProvider = fightProvider;
        this.duelParser = duelParser;
    }

    public Fight parse(FightDTO fightDto) {
        if (fightDto.getId() == null) {
            return parse(fightDto, new Fight());
        } else {
            return parse(fightDto, fightProvider.getFight(fightDto.getId()));
        }
    }

    public Fight parse(FightDTO fightDto, Fight fight) {
        if (fightDto.getTournament() == null) {
            throw new BadRequestException(getClass(), "Fight data is missing");
        }

        final Tournament tournament = tournamentProvider.get(fightDto.getTournament().getId());
        if (tournament == null) {
            throw new TournamentNotFoundException(getClass(), "Tournament not found with id '" + fightDto.getTournament().getId() + "'.");
        }
        fight.setTournament(tournament);

        if (fightDto.getTeam1().getId() != null) {
            final Team team1 = teamProvider.get(fightDto.getTeam1().getId());
            if (team1 == null) {
                throw new TeamNotFoundException(getClass(), "Team1 not found with id '" + fightDto.getTeam1().getId() + "'.");
            }
            fight.setTeam1(team1);
        }

        if (fightDto.getTeam2().getId() != null) {
            final Team team2 = teamProvider.get(fightDto.getTeam2().getId());
            if (team2 == null) {
                throw new TeamNotFoundException(getClass(), "Team2 not found with id '" + fightDto.getTeam2().getId() + "'.");
            }
            fight.setTeam2(team2);
        }

        fightDto.getDuels().forEach(duelParser::parse);
        return fight;
    }
}
