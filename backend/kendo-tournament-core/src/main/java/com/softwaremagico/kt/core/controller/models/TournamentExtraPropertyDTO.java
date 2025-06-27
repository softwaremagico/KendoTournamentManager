package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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

import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import jakarta.validation.constraints.NotNull;

import java.io.Serial;

public class TournamentExtraPropertyDTO extends ElementDTO {

    @Serial
    private static final long serialVersionUID = -4825047701201067422L;

    @NotNull
    private TournamentDTO tournament;

    @NotNull
    private TournamentExtraPropertyKey propertyKey;

    @NotNull
    private String propertyValue;

    public TournamentExtraPropertyDTO() {
        super();
    }

    public TournamentExtraPropertyDTO(TournamentDTO tournamentDTO, TournamentExtraPropertyKey propertyKey, String propertyValue) {
        this();
        setTournament(tournamentDTO);
        setPropertyKey(propertyKey);
        setPropertyValue(propertyValue);
    }

    public TournamentDTO getTournament() {
        return tournament;
    }

    public void setTournament(TournamentDTO tournament) {
        this.tournament = tournament;
    }

    public TournamentExtraPropertyKey getPropertyKey() {
        return propertyKey;
    }

    public void setPropertyKey(TournamentExtraPropertyKey propertyKey) {
        this.propertyKey = propertyKey;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }
}
