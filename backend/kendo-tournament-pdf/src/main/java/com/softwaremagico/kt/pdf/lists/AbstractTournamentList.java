package com.softwaremagico.kt.pdf.lists;

/*-
 * #%L
 * Kendo Tournament Manager (PDF)
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
import com.softwaremagico.kt.pdf.ParentList;

/**
 * Base class for PDF lists whose header and column layout only depend on the
 * tournament. Keeps the common tournament field and the default three-column
 * widths in a single place.
 */
public abstract class AbstractTournamentList extends ParentList {

    protected static final float[] TOURNAMENT_TABLE_WIDTH = {0.46f, 0.08f, 0.46f};

    private final TournamentDTO tournament;

    protected AbstractTournamentList(TournamentDTO tournament) {
        this.tournament = tournament;
    }

    protected TournamentDTO getTournament() {
        return tournament;
    }

    @Override
    protected String getHeaderTitle() {
        return tournament.getName();
    }

    @Override
    public float[] getTableWidths() {
        return TOURNAMENT_TABLE_WIDTH;
    }
}
