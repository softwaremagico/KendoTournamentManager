package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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

import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serial;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "groups_links", indexes = {
        @Index(name = "ind_tournament", columnList = "tournament"),
})
public class GroupLink extends Element {

    @Serial
    private static final long serialVersionUID = -482003310040506662L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "source", nullable = false)
    private Group source;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "destination", nullable = false)
    private Group destination;

    @Column(name = "winner")
    private Integer winner = 0;

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

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
