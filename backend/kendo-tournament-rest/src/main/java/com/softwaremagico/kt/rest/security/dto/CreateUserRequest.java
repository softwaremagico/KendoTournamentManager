package com.softwaremagico.kt.rest.security.dto;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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

import java.util.Set;

public class CreateUserRequest {
    private String username;
    private String name;
    private String lastname;
    private String password;
    private Set<String> roles;

    public String getUsername() {
        if (username == null) {
            return null;
        }
        return username.trim();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        if (name == null) {
            return null;
        }
        return name.trim();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        if (lastname == null) {
            return null;
        }
        return lastname.trim();
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getPassword() {
        if (password == null) {
            return null;
        }
        return password.trim();
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }
}
