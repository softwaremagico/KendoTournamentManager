package com.softwaremagico.kt.core.converters.models;

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

import com.softwaremagico.kt.core.controller.models.TournamentDTO;
import com.softwaremagico.kt.persistence.entities.Duel;
import com.softwaremagico.kt.persistence.entities.Tournament;

public class DuelConverterRequest extends ConverterRequest<Duel> {
    private final Tournament tournament;
    private final TournamentDTO tournamentDTO;

    public DuelConverterRequest(Duel entity) {
        super(entity);
        this.tournament = null;
        this.tournamentDTO = null;
    }

    public DuelConverterRequest(Duel entity, Tournament tournament) {
        super(entity);
        this.tournament = tournament;
        this.tournamentDTO = null;
    }

    public DuelConverterRequest(Duel entity, TournamentDTO tournamentDTO) {
        super(entity);
        this.tournamentDTO = tournamentDTO;
        this.tournament = null;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public TournamentDTO getTournamentDTO() {
        return tournamentDTO;
    }
}
