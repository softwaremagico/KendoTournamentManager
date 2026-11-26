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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.softwaremagico.kt.persistence.encryption.BCryptPasswordConverter;
import com.softwaremagico.kt.persistence.encryption.SHA512HashGenerator;
import com.softwaremagico.kt.persistence.encryption.StringCryptoConverter;
import com.softwaremagico.kt.security.AvailableRole;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.time.LocalDateTime;

@Entity
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "authenticated_users")
@SuppressWarnings("java:S2160")
public class AuthenticatedUser implements UserDetails, IAuthenticatedUser {
    @jakarta.persistence.Id
    @jakarta.persistence.GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Integer id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by")
    private String createdBy;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "password")
    @Convert(converter = BCryptPasswordConverter.class)
    private String password;

    @Column(name = "username")
    @Convert(converter = StringCryptoConverter.class)
    private String username;

    @Column(name = "username_hash", length = SHA512HashGenerator.ALGORITHM_LENGTH)
    @Convert(converter = SHA512HashGenerator.class)
    private String usernameHash;

    @Column(name = "name", nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String name = "";

    @Column(name = "lastname", nullable = false)
    @Convert(converter = StringCryptoConverter.class)
    private String lastname = "";

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "authenticated_user_roles", joinColumns = @JoinColumn(name = "authenticated_user"))
    @Column(name = "roles")
    private Set<String> roles;

    @Column(name = "last_tenant_id")
    private Integer lastTenantId;

    @jakarta.persistence.Transient
    private Integer tenantId;

    public AuthenticatedUser() {
        super();
    }

    public AuthenticatedUser(String username) {
        this();
        setUsername(username);
    }

    /** Creates a transient principal for a known tenant, for token generation and tests. */
    public AuthenticatedUser(String username, Integer tenantId) {
        this(username);
        setTenantId(tenantId);
    }

    @JsonIgnore
    private transient Set<SimpleGrantedAuthority> authorities;

    @JsonIgnore
    @Override
    public String getPassword() {
        return password;
    }

    @JsonIgnore
    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.usernameHash = username;
    }

    public String getUsernameHash() {
        return usernameHash;
    }

    public void setUsernameHash(String usernameHash) {
        this.usernameHash = usernameHash;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getMobilePhone() {
        return null;
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public Integer getLastTenantId() {
        return lastTenantId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setLastTenantId(Integer lastTenantId) {
        this.lastTenantId = lastTenantId;
    }

    public Integer getTenantId() {
        return tenantId;
    }

    public void setTenantId(Integer tenantId) {
        this.tenantId = tenantId;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (authorities == null) {
            authorities = new HashSet<>();
            if (roles != null) {
                roles.forEach(authority -> {
                    final AvailableRole availableRole = AvailableRole.get(authority);
                    if (availableRole != null) {
                        authorities.add(new SimpleGrantedAuthority(availableRole.name()));
                        if (availableRole == AvailableRole.SUPER_ADMIN) {
                            authorities.add(new SimpleGrantedAuthority(AvailableRole.ADMIN.name()));
                        }
                    }
                });
            }
        }
        return authorities;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{"
                + "username='" + username + '\''
                + ", name='" + name + '\''
                + ", lastname='" + lastname + '\''
                + ", roles=" + roles
                + ", authorities=" + authorities
                + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final AuthenticatedUser that = (AuthenticatedUser) o;
        if (id == null || that.id == null) {
            return false;
        }
        return Objects.equals(password, that.password)
                && Objects.equals(username, that.username)
                && Objects.equals(usernameHash, that.usernameHash)
                && Objects.equals(name, that.name)
                && Objects.equals(lastname, that.lastname)
                && Objects.equals(roles, that.roles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, password, username, usernameHash, name, lastname, roles);
    }
}
