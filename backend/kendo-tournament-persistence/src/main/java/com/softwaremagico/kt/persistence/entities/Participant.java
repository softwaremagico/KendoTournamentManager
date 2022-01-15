package com.softwaremagico.kt.persistence.entities;

/*
 * #%L
 * KendoTournamentManager
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

import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.utils.NameUtils;
import com.softwaremagico.kt.utils.StringUtils;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.text.Collator;
import java.util.Locale;

/**
 * A registered person is any person that is in a tournament. Can be a
 * competitor, referee, public, etc.
 */
@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "participant")
public class Participant implements Comparable<Participant> {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "id_card")
    @Convert(converter = StringCryptoConverter.class)
    private String idCard;

    @Column(name = "name")
    @Convert(converter = StringCryptoConverter.class)
    private String name = "";

    @Column(name = "lastname")
    @Convert(converter = StringCryptoConverter.class)
    private String lastname = "";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "club")
    private Club club;

    public Participant() {

    }


    public Participant(String idCard, String name, String lastname) {
        setName(name);
        setLastname(lastname);
        setIdCard(idCard);
    }

    public Integer getId() {
        return id;
    }

    public final void setIdCard(String value) {
        idCard = value.replaceAll("-", "").replaceAll(" ", "").trim().toUpperCase();
    }

    public String getIdCard() {
        return idCard;
    }

    public boolean isValid() {
        return getName().length() > 0 && getIdCard().length() > 0;
    }

    protected final void setName(String value) {
        name = StringUtils.setCase(value);
    }


    protected final void setLastname(String value) {
        lastname = StringUtils.setCase(value);
    }

    public String getName() {
        return name;
    }

    public String getLastname() {
        return lastname;
    }


    public void setClub(Club club) {
        this.club = club;
    }

    public Club getClub() {
        return club;
    }


    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Participant)) {
            return false;
        }
        final Participant otherParticipant = (Participant) object;
        return this.idCard.equals(otherParticipant.idCard);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 31 * hash + (this.idCard != null ? this.idCard.hashCode() : 0);
        return hash;
    }

    /**
     * Compare participants avoiding accent problems.
     *
     * @param otherParticipant
     * @return
     */
    @Override
    public int compareTo(Participant otherParticipant) {
        final String string1 = this.lastname + " " + this.name;
        final String string2 = otherParticipant.lastname + " " + otherParticipant.name;

        // Ignore accents
        final Collator collator = Collator.getInstance(new Locale("es"));
        collator.setStrength(Collator.SECONDARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);

        return collator.compare(string1, string2);
    }

    @Override
    public String toString() {
        return NameUtils.getLastnameName(getLastname(), getName());
    }
}
