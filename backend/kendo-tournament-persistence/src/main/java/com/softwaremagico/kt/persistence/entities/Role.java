package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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


import com.softwaremagico.kt.persistence.encryption.BooleanCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.RoleTypeCryptoConverter;
import com.softwaremagico.kt.persistence.values.RoleType;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;

@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "roles")
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
    @Convert(converter = RoleTypeCryptoConverter.class)
    private RoleType roleType;

    @Column(name = "diploma_printed", nullable = false)
    @Convert(converter = BooleanCryptoConverter.class)
    private boolean diplomaPrinted = false;

    @Column(name = "accreditation_printed", nullable = false)
    @Convert(converter = BooleanCryptoConverter.class)
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
