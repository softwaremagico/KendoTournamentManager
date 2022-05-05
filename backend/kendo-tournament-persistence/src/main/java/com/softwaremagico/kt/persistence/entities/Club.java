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

import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.utils.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.text.Collator;
import java.util.Locale;

/**
 * Defines a club. A club is an organization where competitors came from. This
 * discrimination is not important now, but in future can be used to generate
 * statistics
 */
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "clubs")
public class Club extends Element implements Comparable<Club> {

    @Column(name = "name")
    @Convert(converter = StringCryptoConverter.class)
    private String name = "";

    @Column(name = "country")
    @Convert(converter = StringCryptoConverter.class)
    private String country = "";

    @Column(name = "city")
    @Convert(converter = StringCryptoConverter.class)
    private String city = "";

    @Column(name = "address")
    @Convert(converter = StringCryptoConverter.class)
    private String address = "";

    @Column(name = "representative")
    @Convert(converter = StringCryptoConverter.class)
    private String representativeId = "";

    @Column(name = "email")
    @Convert(converter = StringCryptoConverter.class)
    private String email = "";

    @Column(name = "phone")
    @Convert(converter = StringCryptoConverter.class)
    private String phone = null;

    @Column(name = "web")
    @Convert(converter = StringCryptoConverter.class)
    private String web = "";

    public Club() {
        super();
    }

    public Club(String name, String country, String city) {
        setName(name);
        setCountry(country);
        setCity(city);
    }

    public void setRepresentativeId(String representativeId) {
        this.representativeId = representativeId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    /**
     * Representative is a registered person of the club that will be use to
     * establish contact in future tournaments.
     *
     * @param representativeId Identification number of the registered person.
     * @param email
     * @param phone
     */
    public void setRepresentative(String representativeId, String email, String phone) {
        this.representativeId = representativeId;
        this.email = email;
        this.phone = phone;
    }

    public void setName(String value) {
        this.name = StringUtils.setCase(value);
    }

    public String getName() {
        return name;
    }

    /**
     * City of the club. Only is an information value.
     *
     * @param value
     */
    public void setCountry(String value) {
        country = StringUtils.setCase(value);
    }

    /**
     * City of the club. Only is an information value.
     *
     * @param value
     */
    public void setCity(String value) {
        city = StringUtils.setCase(value);
    }

    /**
     * Address of the club. Only is an information value.
     *
     * @param value
     */
    public void setAddress(String value) {
        address = StringUtils.setCase(value);
    }

    /**
     * Sets the email of the representative. For future contacts reference.
     *
     * @param email
     */
    public void setMail(String email) {
        this.email = email;
    }

    /**
     * Sets the phone of the representative. For future contacts reference.
     *
     * @param phone
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setRepresentative(String representative) {
        this.representativeId = representative;
    }

    public void storeWeb(String value) {
        web = value.trim();
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getAddress() {
        return address;
    }

    public String getWeb() {
        return web;
    }

    public String getMail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getRepresentativeId() {
        return representativeId;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof Club)) {
            return false;
        }
        final Club otherClub = (Club) object;
        return this.name.equals(otherClub.name) && this.city.equals(otherClub.city);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + (this.name != null ? this.name.hashCode() : 0);
        hash = 67 * hash + (this.city != null ? this.city.hashCode() : 0);
        return hash;
    }

    @Override
    public String toString() {
        return this.getName();
    }

    /**
     * A club is compared only using the name and the city.
     */
    @Override
    public int compareTo(Club c) {
        // Ignore accents
        final Collator collator = Collator.getInstance(new Locale("es"));
        collator.setStrength(Collator.SECONDARY);
        collator.setDecomposition(Collator.FULL_DECOMPOSITION);

        return collator.compare(getName() + getCity(), c.getName() + c.getCity());
    }


}
