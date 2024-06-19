package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

public class GroupLink extends Element {

    private Group source;

    private Group destination;

    private Integer winner = 0;

    public Group getSource() {
        return source;
    }

    public void setSource(Group source) {
        this.source = source;
    }

    public Group getDestination() {
        return destination;
    }

    public void setDestination(Group destination) {
        this.destination = destination;
    }

    public Integer getWinner() {
        return winner;
    }

    public void setWinner(Integer winner) {
        this.winner = winner;
    }

    @Override
    public String toString() {
        return "GroupLink{"
                + "source=" + (source != null ? source.getLevel() : "null") + "-" + (source != null ? source.getIndex() : "null")
                + ", destination=" + (destination != null ? destination.getLevel() : "null") + "-" + (destination != null ? destination.getIndex() : "null")
                + ", winner=" + winner
                + '}';
    }
}
