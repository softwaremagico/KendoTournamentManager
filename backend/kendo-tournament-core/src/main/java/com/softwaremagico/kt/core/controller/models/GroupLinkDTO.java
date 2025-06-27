package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import jakarta.validation.constraints.NotNull;

public class GroupLinkDTO extends ElementDTO {

    @NotNull
    private GroupDTO source;

    @NotNull
    private GroupDTO destination;

    private int winner = 0;

    public GroupDTO getSource() {
        return source;
    }

    public void setSource(GroupDTO source) {
        this.source = source;
    }

    public GroupDTO getDestination() {
        return destination;
    }

    public void setDestination(GroupDTO destination) {
        this.destination = destination;
    }

    public int getWinner() {
        return winner;
    }

    public void setWinner(int winner) {
        this.winner = winner;
    }
}
