package com.softwaremagico.kt.rest.services;

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

import com.softwaremagico.kt.core.controller.VersionController;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;

@Test(groups = "restServicesUnit")
public class InfoTest {

	@Mock
	private VersionController versionController;

	private Info info;
	private HttpServletRequest httpServletRequest;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
		this.info = new Info(this.versionController);
		this.httpServletRequest = mock(HttpServletRequest.class);
	}

	@Test
	public void shouldReturnLatestVersionAndUseCachedValue() throws Exception {
		when(this.versionController.getLatestVersionFromGithub()).thenReturn("3.3.0");

		final ResponseEntity<String> firstResponse = this.info.getLatestVersion(this.httpServletRequest);
		final ResponseEntity<String> secondResponse = this.info.getLatestVersion(this.httpServletRequest);

		assertEquals(firstResponse.getBody(), "3.3.0");
		assertEquals(secondResponse.getBody(), "3.3.0");
		verify(this.versionController, times(1)).getLatestVersionFromGithub();
	}

	@Test
	public void shouldReturnEmptyOnVersionLookupError() throws Exception {
		when(this.versionController.getLatestVersionFromGithub()).thenThrow(new IllegalStateException("boom"));

		final ResponseEntity<String> response = this.info.getLatestVersion(this.httpServletRequest);

		assertEquals(response.getBody(), "");
	}

	@Test
	public void shouldExecuteHealthCheck() throws Exception {
		this.info.healthCheck(null);
	}
}
