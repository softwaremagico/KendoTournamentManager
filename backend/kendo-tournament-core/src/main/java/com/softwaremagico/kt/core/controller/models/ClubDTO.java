package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import java.util.Objects;

public class ClubDTO extends ElementDTO {

    private String name = "";

    private String country = "";

    private String city = "";

    private String address = "";

    private String representativeId = "";

    private String email = "";

    private String phone = null;

    private String web = "";

    public ClubDTO() {
        super();
    }

    public ClubDTO(String name) {
        this();
        setName(name);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRepresentativeId() {
        return representativeId;
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

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWeb() {
        return web;
    }

    public void setWeb(String web) {
        this.web = web;
    }

    @Override
    public String toString() {
        if (getName() != null) {
            return getName();
        }
        return super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClubDTO)) {
            return false;
        }
        final ClubDTO clubDTO = (ClubDTO) o;
        return getName().equals(clubDTO.getName()) && getCountry().equals(clubDTO.getCountry()) && getCity().equals(clubDTO.getCity())
                && Objects.equals(getAddress(), clubDTO.getAddress()) && Objects.equals(getRepresentativeId(), clubDTO.getRepresentativeId())
                && Objects.equals(getEmail(), clubDTO.getEmail()) && Objects.equals(getPhone(), clubDTO.getPhone()) &&
                Objects.equals(getWeb(), clubDTO.getWeb());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCountry(), getCity(), getAddress(), getRepresentativeId(), getEmail(), getPhone(), getWeb());
    }
}
