package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import com.softwaremagico.kt.core.providers.AuthenticatedUserProvider;
import com.softwaremagico.kt.core.providers.ParticipantProvider;
import com.softwaremagico.kt.persistence.entities.AuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.IAuthenticatedUser;
import com.softwaremagico.kt.persistence.entities.Participant;
import com.softwaremagico.kt.rest.exceptions.UserNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collection;

/**
 * Is automatically injected to the AuthenticationManager in WebSecurityConfig
 */
@Component
public class KendoUserDetailsService implements UserDetailsService {

    private final AuthenticatedUserProvider authenticatedUserProvider;
    private final ParticipantProvider participantProvider;

    public KendoUserDetailsService(AuthenticatedUserProvider authenticatedUserProvider,
                                   ParticipantProvider participantProvider) {
        this.authenticatedUserProvider = authenticatedUserProvider;
        this.participantProvider = participantProvider;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final IAuthenticatedUser user = authenticatedUserProvider.findByUsername(username).orElseThrow(() ->
                new UsernameNotFoundException(String.format("User '%s' not found!", username)));

        if (user instanceof Participant) {
            throw new UserNotFoundException(this.getClass(), "User with username '" + username + "' is not a registered user");
        }

        final AuthenticatedUser authenticatedUser = (AuthenticatedUser) user;

        return new UserDetails() {
            @Override
            public Collection<? extends GrantedAuthority> getAuthorities() {
                return null;
            }

            @Override
            public String getPassword() {
                return authenticatedUser.getPassword();
            }

            @Override
            public String getUsername() {
                return authenticatedUser.getUsername();
            }

            @Override
            public boolean isAccountNonExpired() {
                return authenticatedUser.isAccountNonExpired();
            }

            @Override
            public boolean isAccountNonLocked() {
                return authenticatedUser.isAccountNonLocked();
            }

            @Override
            public boolean isCredentialsNonExpired() {
                return authenticatedUser.isCredentialsNonExpired();
            }

            @Override
            public boolean isEnabled() {
                return authenticatedUser.isEnabled();
            }

            public Integer getId() {
                return authenticatedUser.getId();
            }
        };
    }
}
