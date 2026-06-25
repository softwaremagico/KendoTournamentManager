package com.softwaremagico.kt.rest.security;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

@Test(groups = "restServicesUnit")
public class WebSecurityConfigTests {

    private WebSecurityConfig webSecurityConfig;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        webSecurityConfig = new WebSecurityConfig(mock(JwtTokenFilter.class));
    }

    @Test
    public void shouldCreatePasswordEncoder() {
        final PasswordEncoder passwordEncoder = webSecurityConfig.passwordEncoder();

        assertNotNull(passwordEncoder);
        final String encoded = passwordEncoder.encode("secret");
        assertTrue(passwordEncoder.matches("secret", encoded));
    }

    @Test
    public void shouldReturnAuthenticationManagerBean() throws Exception {
        final AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
        final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        final AuthenticationManager result = webSecurityConfig.authenticationManager(authenticationConfiguration);

        assertEquals(result, authenticationManager);
    }

    @Test
    public void shouldAllowAllOriginsWhenCorsDomainsNull() {
        ReflectionTestUtils.setField(webSecurityConfig, "serverCorsDomains", null);

        final CorsConfigurationSource source = (CorsConfigurationSource) ReflectionTestUtils
                .invokeMethod(webSecurityConfig, "generateCorsConfigurationSource");
        final CorsConfiguration corsConfiguration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(corsConfiguration);
        assertTrue(corsConfiguration.getAllowedOriginPatterns().contains("*"));
    }

    @Test
    public void shouldUseConfiguredOriginsWhenCorsDomainsProvided() {
        ReflectionTestUtils.setField(webSecurityConfig, "serverCorsDomains", List.of("https://example.com"));

        final CorsConfigurationSource source = (CorsConfigurationSource) ReflectionTestUtils
                .invokeMethod(webSecurityConfig, "generateCorsConfigurationSource");
        final CorsConfiguration corsConfiguration = source.getCorsConfiguration(new MockHttpServletRequest());

        assertNotNull(corsConfiguration);
        assertEquals(corsConfiguration.getAllowedOrigins(), List.of("https://example.com"));
        assertTrue(Boolean.TRUE.equals(corsConfiguration.getAllowCredentials()));
    }
}

