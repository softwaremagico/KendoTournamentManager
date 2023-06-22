package com.softwaremagico.kt.persistence.entities;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.persistence.encryption.TournamentExtraPropertyKeyTypeCryptoConverter;
import com.softwaremagico.kt.persistence.values.TournamentExtraPropertyKey;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;


@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "tournament_extra_properties", uniqueConstraints = {@UniqueConstraint(columnNames = {"tournament", "property_key"})},
        indexes = {
                @Index(name = "ind_tournament", columnList = "tournament"),
        })
public class TournamentExtraProperty extends Element {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament", nullable = false)
    private Tournament tournament;

    @Enumerated(EnumType.STRING)
    @Column(name = "property_key", nullable = false)
    @Convert(converter = TournamentExtraPropertyKeyTypeCryptoConverter.class)
    private TournamentExtraPropertyKey propertyKey;

    @Column(name = "property_value", nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String propertyValue;

    public TournamentExtraProperty() {
        super();
    }

    public TournamentExtraProperty(Tournament tournament, TournamentExtraPropertyKey propertyKey, String propertyValue) {
        this();
        this.tournament = tournament;
        this.propertyKey = propertyKey;
        this.propertyValue = propertyValue;
    }

    public Tournament getTournament() {
        return tournament;
    }

    public void setTournament(Tournament tournament) {
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
