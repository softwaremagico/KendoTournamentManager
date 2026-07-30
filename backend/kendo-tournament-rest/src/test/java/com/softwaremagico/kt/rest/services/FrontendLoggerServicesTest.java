package com.softwaremagico.kt.rest.services;

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

import com.softwaremagico.kt.core.controller.models.LogDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

@Test(groups = "frontendLoggerServices")
public class FrontendLoggerServicesTest {

    @Mock
    private HttpServletRequest mockRequest;

    private FrontendLoggerServices services;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        services = new FrontendLoggerServices();
    }

    private LogDTO logWithMessage(String message) {
        final LogDTO log = new LogDTO();
        log.setMessage(message);
        return log;
    }

    @Test
    public void info_withPlainMessage_expectNoException() {
        services.info(logWithMessage("Some info message"), mockRequest);
    }

    @Test
    public void info_withMessageContainingControlCharacters_expectSanitizedNoException() {
        services.info(logWithMessage("Line1\nLine2\r\tTabbed"), mockRequest);
    }

    @Test
    public void warning_withPlainMessage_expectNoException() {
        services.warning(logWithMessage("Some warning message"), mockRequest);
    }

    @Test
    public void warning_withMessageContainingControlCharacters_expectSanitizedNoException() {
        services.warning(logWithMessage("Warn\nwith\rcontrol\tchars"), mockRequest);
    }

    @Test
    public void error_withPlainMessage_expectNoException() {
        services.error(logWithMessage("Some error message"), mockRequest);
    }

    @Test
    public void error_withMessageContainingControlCharacters_expectSanitizedNoException() {
        services.error(logWithMessage("Error\nwith\rcontrol\tchars"), mockRequest);
    }
}

