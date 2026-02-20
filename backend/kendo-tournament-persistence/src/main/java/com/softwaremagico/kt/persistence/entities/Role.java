package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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


import com.softwaremagico.kt.persistence.values.RoleType;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "roles", indexes = {
        @Index(name = "ind_tournament", columnList = "tournament"),
        @Index(name = "ind_type", columnList = "role_type"),
})
public class Role extends Element {

    @ManyToOne(fetch = FetchType.LAZY)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.EAGER)
    @Fetch(FetchMode.JOIN)
    @JoinColumn(name = "participant", nullable = false)
    private Participant participant;

    @Column(name = "role_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private RoleType roleType;

    @Column(name = "diploma_printed", nullable = false)
    private boolean diplomaPrinted = false;

    @Column(name = "accreditation_printed", nullable = false)
    private boolean accreditationPrinted = false;

    public Role() {
        super();
    }

    public Role(Tournament tournament, Participant participant, RoleType roleType) {
        this();
        setTournament(tournament);
        setParticipant(participant);
        setRoleType(roleType);
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
        this.tournament = tournament;
    }

    public Participant getParticipant() {
        return participant;
    }

    public void setParticipant(Participant participant) {
        this.participant = participant;
    }

    public RoleType getRoleType() {
        return roleType;
    }

    public void setRoleType(RoleType roleType) {
        this.roleType = roleType;
    }

    public boolean isDiplomaPrinted() {
        return diplomaPrinted;
    }

    public void setDiplomaPrinted(boolean diplomaPrinted) {
        this.diplomaPrinted = diplomaPrinted;
    }

    public boolean isAccreditationPrinted() {
        return accreditationPrinted;
    }

    public void setAccreditationPrinted(boolean accreditationPrinted) {
        this.accreditationPrinted = accreditationPrinted;
    }

    @Override
    public String toString() {
        if (getTournament() != null) {
            return String.format("ROLE{%s %s %s}", getTournament().getName(), getParticipant().getName(), getRoleType());
        }
        return super.toString();
    }
}
