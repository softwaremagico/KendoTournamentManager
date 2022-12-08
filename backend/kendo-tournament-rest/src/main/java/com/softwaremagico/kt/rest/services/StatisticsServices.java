package com.softwaremagico.kt.rest.services;

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

import com.softwaremagico.kt.core.controller.TournamentController;
import com.softwaremagico.kt.core.statistics.FightStatisticsProvider;
import com.softwaremagico.kt.core.statistics.models.FightStatisticsDTO;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/statistics")
public class StatisticsServices {

    private final TournamentController tournamentController;
    private final FightStatisticsProvider fightStatisticsProvider;

    public StatisticsServices(TournamentController tournamentController, FightStatisticsProvider fightStatisticsProvider) {
        this.tournamentController = tournamentController;
        this.fightStatisticsProvider = fightStatisticsProvider;
    }

    @PreAuthorize("hasAnyRole('ROLE_VIEWER', 'ROLE_EDITOR', 'ROLE_ADMIN')")
    @Operation(summary = "Gets fight statistics.", security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping(value = "/tournament/{tournamentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public FightStatisticsDTO getStatisticsFromTournament(@Parameter(description = "Id of an existing tournament", required = true)
                                                          @PathVariable("tournamentId") Integer tournamentId,
                                                          @RequestParam(name = "calculateByTeams") Optional<Boolean> checkByTeams,
                                                          @RequestParam(name = "calculateByMembers") Optional<Boolean> checkByMembers,
                                                          HttpServletRequest request) {
        if (checkByMembers.isPresent() && checkByMembers.get()) {
            KendoTournamentLogger.debug(this.getClass(), "Forcing statistics by members.");
            return fightStatisticsProvider.calculateByMembers(tournamentController.get(tournamentId));
        }
        if (checkByTeams.isPresent() && checkByTeams.get()) {
            KendoTournamentLogger.debug(this.getClass(), "Forcing statistics by teams.");
            return fightStatisticsProvider.calculateByTeams(tournamentController.get(tournamentId));
        }
        return fightStatisticsProvider.calculate(tournamentController.get(tournamentId));
    }
}
