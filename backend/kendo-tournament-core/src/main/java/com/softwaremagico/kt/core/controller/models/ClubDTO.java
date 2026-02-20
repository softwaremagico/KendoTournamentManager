package com.softwaremagico.kt.core.controller.models;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.io.Serial;
import java.util.Objects;

public class ClubDTO extends ElementDTO {

    @Serial
    private static final long serialVersionUID = -5342001958437046042L;

    @Size(max = ElementDTO.MAX_NORMAL_FIELD_LENGTH)
    @NotBlank
    private String name = "";

    @Size(max = ElementDTO.MAX_SMALL_FIELD_LENGTH)
    private String country = "";

    @Size(max = ElementDTO.MAX_SMALL_FIELD_LENGTH)
    private String city = "";

    @Size(max = ElementDTO.MAX_BIG_FIELD_LENGTH)
    private String address = "";

    private String representativeId = "";

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}|^$", flags = Pattern.Flag.CASE_INSENSITIVE)
    private String email = "";

    @Size(max = ElementDTO.MAX_SMALL_FIELD_LENGTH)
    private String phone = null;

    @Size(max = ElementDTO.MAX_NORMAL_FIELD_LENGTH)
    private String web = "";

    public ClubDTO() {
        super();
    }

    public ClubDTO(String name, String city) {
        this();
        setName(name);
        setCity(city);
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
        if (!(o instanceof ClubDTO clubDTO)) {
            return false;
        }
        return getName().equals(clubDTO.getName()) && getCountry().equals(clubDTO.getCountry()) && getCity().equals(clubDTO.getCity())
                && Objects.equals(getAddress(), clubDTO.getAddress()) && Objects.equals(getRepresentativeId(), clubDTO.getRepresentativeId())
                && Objects.equals(getEmail(), clubDTO.getEmail()) && Objects.equals(getPhone(), clubDTO.getPhone())
                && Objects.equals(getWeb(), clubDTO.getWeb());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getCountry(), getCity(), getAddress(), getRepresentativeId(), getEmail(), getPhone(), getWeb());
    }
}
