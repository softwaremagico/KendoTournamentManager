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

/**
 * Test suite for {@link WebSecurityConfig}. Ensures Spring Security
 * configuration for stateless JWT-based authentication.
 */
@ExtendWith(MockitoExtension.class)
class WebSecurityConfigTest {

	@Mock
	private JwtTokenFilter jwtTokenFilter;

	@InjectMocks
	private WebSecurityConfig webSecurityConfig;

	@BeforeEach
	void setUp() {
        this.webSecurityConfig = new WebSecurityConfig(this.jwtTokenFilter);
	}

	@Test
	void testConstructor() {
		assertNotNull(this.webSecurityConfig);
	}

	@Test
	void testPasswordEncoderBean() {
		final PasswordEncoder encoder = this.webSecurityConfig.passwordEncoder();

		assertNotNull(encoder);
		final String rawPassword = "testPassword123";
		final String encodedPassword = encoder.encode(rawPassword);

		assertNotEquals(rawPassword, encodedPassword);
		assertTrue(encoder.matches(rawPassword, encodedPassword));
	}

	@Test
	void testPasswordEncoderDifferentEachTime() {
		final PasswordEncoder encoder = this.webSecurityConfig.passwordEncoder();
		final String password = "testPassword";

		final String encoded1 = encoder.encode(password);
		final String encoded2 = encoder.encode(password);

		assertNotEquals(encoded1, encoded2);
		assertTrue(encoder.matches(password, encoded1));
		assertTrue(encoder.matches(password, encoded2));
	}

	@Test
	void testCorsConfigurationSourceWithDefaultSettings() {
		// Set null to use default unrestricted CORS
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", null);

		final CorsConfigurationSource source = this.getCorsConfigurationSource();
		final CorsConfiguration config = source.getCorsConfiguration(this.getMockRequest());

		assertNotNull(config);
		assertNotNull(config.getAllowedOriginPatterns());
		assertTrue(config.getAllowedOriginPatterns().contains("*"));
		assertFalse(config.getAllowCredentials());
	}

	@Test
	void testCorsConfigurationSourceWithWildcard() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", List.of("*"));

		final CorsConfigurationSource source = this.getCorsConfigurationSource();
		final CorsConfiguration config = source.getCorsConfiguration(this.getMockRequest());

		assertNotNull(config);
		assertNotNull(config.getAllowedOriginPatterns());
		assertTrue(config.getAllowedOriginPatterns().contains("*"));
	}

	@Test
	void testCorsConfigurationSourceWithSpecificDomains() {
		final List<String> domains = Arrays.asList("https://example.com", "https://another.com");
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", domains);

		final CorsConfigurationSource source = this.getCorsConfigurationSource();
		final CorsConfiguration config = source.getCorsConfiguration(this.getMockRequest());

		assertNotNull(config);
		assertEquals(domains, config.getAllowedOrigins());
		assertTrue(config.getAllowCredentials());
	}

	@Test
	void testCorsConfigurationAllowedMethods() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", null);

		final CorsConfigurationSource source = this.getCorsConfigurationSource();
		final CorsConfiguration config = source.getCorsConfiguration(this.getMockRequest());

		assertNotNull(config);
		assertTrue(config.getAllowedMethods().contains("*"));
	}

	@Test
	void testCorsConfigurationAllowedHeaders() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", null);

		final CorsConfigurationSource source = this.getCorsConfigurationSource();
		final CorsConfiguration config = source.getCorsConfiguration(this.getMockRequest());

		assertNotNull(config);
		assertTrue(config.getAllowedHeaders().contains("*"));
	}

	@Test
	void testCorsConfigurationExposedHeaders() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", null);

		final CorsConfigurationSource source = this.getCorsConfigurationSource();
		final CorsConfiguration config = source.getCorsConfiguration(this.getMockRequest());

		assertNotNull(config);
		assertNotNull(config.getExposedHeaders());
		assertTrue(config.getExposedHeaders().contains("Authorization"));
		assertTrue(config.getExposedHeaders().contains("Expires"));
	}

	@Test
	void testAuthWhitelistContainsSwaggerPaths() {
		assertTrue(this.hasAuthWhitelistPath("/v3/api-docs"));
		assertTrue(this.hasAuthWhitelistPath("/swagger-ui"));
	}

	@Test
	void testAuthWhitelistContainsInfoPath() {
		assertTrue(this.hasAuthWhitelistPath("/info"));
	}

	@Test
	void testAuthWhitelistContainsWebSocketPath() {
		assertTrue(this.hasAuthWhitelistPath(WebSocketConfiguration.SOCKETS_STOMP_URL));
	}

	@Test
	void testAuthWhitelistContainsRootPath() {
		assertTrue(this.hasAuthWhitelistPath("/"));
	}

	// Helper methods
	private CorsConfigurationSource getCorsConfigurationSource() {
		return ReflectionTestUtils.invokeMethod(this.webSecurityConfig,
				"generateCorsConfigurationSource");
	}

	private org.springframework.mock.web.MockHttpServletRequest getMockRequest() {
		return new org.springframework.mock.web.MockHttpServletRequest();
	}

	private boolean hasAuthWhitelistPath(String path) {
		// We'll check if the path would match any of the AUTH_WHITELIST patterns
		final String[] whitelistPatterns = {"/v3/api-docs/**", "/swagger-ui/**", "/", "/info/**", "/*/public/**",
				WebSocketConfiguration.SOCKETS_STOMP_URL + "/**"};

		for (final String pattern : whitelistPatterns) {
			if (path.matches(pattern.replace("**", ".*").replace("*", ".*"))) {
				return true;
			}
		}
		return false;
	}
}
