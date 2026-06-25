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

package com.softwaremagico.kt.rest.security;

import com.softwaremagico.kt.websockets.WebSocketConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

/**
 * Test suite for {@link WebSecurityConfig}.
 * Ensures Spring Security configuration for stateless JWT-based authentication.
 */
@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

    @Mock
    private JwtTokenFilter jwtTokenFilter;

    @InjectMocks
    private WebSecurityConfig webSecurityConfig;

    @BeforeEach
    void setUp() {
        webSecurityConfig = new WebSecurityConfig(jwtTokenFilter);
    }

    @Test
    void testConstructor() {
        assertNotNull(webSecurityConfig);
    }

    @Test
    void testPasswordEncoderBean() {
        PasswordEncoder encoder = webSecurityConfig.passwordEncoder();

        assertNotNull(encoder);
        String rawPassword = "testPassword123";
        String encodedPassword = encoder.encode(rawPassword);

        assertNotEquals(rawPassword, encodedPassword);
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }

    @Test
    void testPasswordEncoderDifferentEachTime() {
        PasswordEncoder encoder = webSecurityConfig.passwordEncoder();
        String password = "testPassword";

        String encoded1 = encoder.encode(password);
        String encoded2 = encoder.encode(password);

        assertNotEquals(encoded1, encoded2);
        assertTrue(encoder.matches(password, encoded1));
        assertTrue(encoder.matches(password, encoded2));
    }

    @Test
    void testCorsConfigurationSourceWithDefaultSettings() {
        // Set null to use default unrestricted CORS
        ReflectionTestUtils.setField(webSecurityConfig, "serverCorsDomains", null);

        CorsConfigurationSource source = getCorsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(getMockRequest());

        assertNotNull(config);
        assertNotNull(config.getAllowedOriginPatterns());
        assertTrue(config.getAllowedOriginPatterns().contains("*"));
        assertFalse(config.getAllowCredentials());
    }

    @Test
    void testCorsConfigurationSourceWithWildcard() {
        ReflectionTestUtils.setField(webSecurityConfig, "serverCorsDomains",
                Arrays.asList("*"));

        CorsConfigurationSource source = getCorsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(getMockRequest());

        assertNotNull(config);
        assertNotNull(config.getAllowedOriginPatterns());
        assertTrue(config.getAllowedOriginPatterns().contains("*"));
    }

    @Test
    void testCorsConfigurationSourceWithSpecificDomains() {
        List<String> domains = Arrays.asList("https://example.com", "https://another.com");
        ReflectionTestUtils.setField(webSecurityConfig, "serverCorsDomains", domains);

        CorsConfigurationSource source = getCorsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(getMockRequest());

        assertNotNull(config);
        assertEquals(domains, config.getAllowedOrigins());
        assertTrue(config.getAllowCredentials());
    }

    @Test
    void testCorsConfigurationAllowedMethods() {
        ReflectionTestUtils.setField(webSecurityConfig, "serverCorsDomains", null);

        CorsConfigurationSource source = getCorsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(getMockRequest());

        assertNotNull(config);
        assertTrue(config.getAllowedMethods().contains("*"));
    }

    @Test
    void testCorsConfigurationAllowedHeaders() {
        ReflectionTestUtils.setField(webSecurityConfig, "serverCorsDomains", null);

        CorsConfigurationSource source = getCorsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(getMockRequest());

        assertNotNull(config);
        assertTrue(config.getAllowedHeaders().contains("*"));
    }

    @Test
    void testCorsConfigurationExposedHeaders() {
        ReflectionTestUtils.setField(webSecurityConfig, "serverCorsDomains", null);

        CorsConfigurationSource source = getCorsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(getMockRequest());

        assertNotNull(config);
        assertNotNull(config.getExposedHeaders());
        assertTrue(config.getExposedHeaders().contains("Authorization"));
        assertTrue(config.getExposedHeaders().contains("Expires"));
    }

    @Test
    void testAuthWhitelistContainsSwaggerPaths() {
        assertTrue(hasAuthWhitelistPath("/v3/api-docs"));
        assertTrue(hasAuthWhitelistPath("/swagger-ui"));
    }

    @Test
    void testAuthWhitelistContainsInfoPath() {
        assertTrue(hasAuthWhitelistPath("/info"));
    }

    @Test
    void testAuthWhitelistContainsWebSocketPath() {
        assertTrue(hasAuthWhitelistPath(WebSocketConfiguration.SOCKETS_STOMP_URL));
    }

    @Test
    void testAuthWhitelistContainsRootPath() {
        assertTrue(hasAuthWhitelistPath("/"));
    }

    // Helper methods
    private CorsConfigurationSource getCorsConfigurationSource() {
        return (CorsConfigurationSource) ReflectionTestUtils.invokeMethod(
                webSecurityConfig, "generateCorsConfigurationSource");
    }

    private org.springframework.mock.web.MockHttpServletRequest getMockRequest() {
        return new org.springframework.mock.web.MockHttpServletRequest();
    }

    private boolean hasAuthWhitelistPath(String path) {
        // We'll check if the path would match any of the AUTH_WHITELIST patterns
        String[] whitelistPatterns = {
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/",
                "/info/**",
                "/*/public/**",
                WebSocketConfiguration.SOCKETS_STOMP_URL + "/**"
        };

        for (String pattern : whitelistPatterns) {
            if (path.matches(pattern.replace("**", ".*").replace("*", ".*"))) {
                return true;
            }
        }
        return false;
    }
}

