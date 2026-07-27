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

import com.softwaremagico.kt.websockets.WebSocketConfiguration;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

@Test(groups = "restServicesUnit")
public class WebSecurityConfigTests {

	private WebSecurityConfig webSecurityConfig;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
        this.webSecurityConfig = new WebSecurityConfig(mock(JwtTokenFilter.class));
	}

	@Test
	public void shouldCreatePasswordEncoder() {
		final PasswordEncoder passwordEncoder = this.webSecurityConfig.passwordEncoder();

		assertNotNull(passwordEncoder);
		final String encoded = passwordEncoder.encode("secret");
		assertTrue(passwordEncoder.matches("secret", encoded));
	}

	@Test
	public void shouldReturnAuthenticationManagerBean() throws Exception {
		final AuthenticationConfiguration authenticationConfiguration = mock(AuthenticationConfiguration.class);
		final AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
		when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

		final AuthenticationManager result = this.webSecurityConfig.authenticationManager(authenticationConfiguration);

		assertEquals(result, authenticationManager);
	}

	@Test
	public void shouldAllowAllOriginsWhenCorsDomainsNull() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", null);

		final CorsConfigurationSource source = ReflectionTestUtils
				.invokeMethod(this.webSecurityConfig, "generateCorsConfigurationSource");
		final CorsConfiguration corsConfiguration = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfiguration);
		assertTrue(corsConfiguration.getAllowedOriginPatterns().contains("*"));
	}

	@Test
	public void shouldUseConfiguredOriginsWhenCorsDomainsProvided() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", List.of("https://example.com"));

		final CorsConfigurationSource source = ReflectionTestUtils
				.invokeMethod(this.webSecurityConfig, "generateCorsConfigurationSource");
		final CorsConfiguration corsConfiguration = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfiguration);
		assertEquals(corsConfiguration.getAllowedOrigins(), List.of("https://example.com"));
        assertEquals(corsConfiguration.getAllowCredentials(), Boolean.TRUE);
	}

	@Test
	public void shouldEncodeDifferentHashesForSamePassword() {
		final PasswordEncoder encoder = this.webSecurityConfig.passwordEncoder();
		final String password = "testPassword";

		final String encoded1 = encoder.encode(password);
		final String encoded2 = encoder.encode(password);

		assertNotEquals(encoded1, encoded2);
		assertTrue(encoder.matches(password, encoded1));
		assertTrue(encoder.matches(password, encoded2));
	}

	@Test
	public void shouldAllowMultipleConfiguredOrigins() {
		final List<String> domains = Arrays.asList("https://example.com", "https://another.com");
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", domains);

		final CorsConfigurationSource source = ReflectionTestUtils
				.invokeMethod(this.webSecurityConfig, "generateCorsConfigurationSource");
		final CorsConfiguration corsConfiguration = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfiguration);
		assertEquals(corsConfiguration.getAllowedOrigins(), domains);
        assertEquals(corsConfiguration.getAllowCredentials(), Boolean.TRUE);
	}

	@Test
	public void shouldAllowAllMethodsByDefault() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", null);

		final CorsConfigurationSource source = ReflectionTestUtils
				.invokeMethod(this.webSecurityConfig, "generateCorsConfigurationSource");
		final CorsConfiguration corsConfiguration = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfiguration);
		assertTrue(corsConfiguration.getAllowedMethods().contains("*"));
	}

	@Test
	public void shouldAllowAllHeadersByDefault() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", null);

		final CorsConfigurationSource source = ReflectionTestUtils
				.invokeMethod(this.webSecurityConfig, "generateCorsConfigurationSource");
		final CorsConfiguration corsConfiguration = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfiguration);
		assertTrue(corsConfiguration.getAllowedHeaders().contains("*"));
	}

	@Test
	public void shouldExposeAuthorizationAndExpiresHeaders() {
		ReflectionTestUtils.setField(this.webSecurityConfig, "serverCorsDomains", null);

		final CorsConfigurationSource source = ReflectionTestUtils
				.invokeMethod(this.webSecurityConfig, "generateCorsConfigurationSource");
		final CorsConfiguration corsConfiguration = source.getCorsConfiguration(new MockHttpServletRequest());

		assertNotNull(corsConfiguration);
		assertNotNull(corsConfiguration.getExposedHeaders());
		assertTrue(corsConfiguration.getExposedHeaders().contains("Authorization"));
		assertTrue(corsConfiguration.getExposedHeaders().contains("Expires"));
	}

	@Test
	public void shouldWhitelistSwaggerPaths() {
		assertTrue(this.hasAuthWhitelistPath("/v3/api-docs"));
		assertTrue(this.hasAuthWhitelistPath("/swagger-ui"));
	}

	@Test
	public void shouldWhitelistInfoPath() {
		assertTrue(this.hasAuthWhitelistPath("/info"));
	}

	@Test
	public void shouldWhitelistWebSocketPath() {
		assertTrue(this.hasAuthWhitelistPath(WebSocketConfiguration.SOCKETS_STOMP_URL));
	}

	@Test
	public void shouldWhitelistRootPath() {
		assertTrue(this.hasAuthWhitelistPath("/"));
	}

	private boolean hasAuthWhitelistPath(String path) {
		final String[] whitelistPatterns = {"/v3/api-docs/**", "/swagger-ui/**", "/", "/info/**", "/*/public/**",
				WebSocketConfiguration.SOCKETS_STOMP_URL + "/**"};

		for (final String pattern : whitelistPatterns) {
			final String regex = pattern.replace("/**", "(/.*)?").replace("*", "[^/]*");
			if (path.matches(regex)) {
				return true;
			}
		}
		return false;
	}
}
