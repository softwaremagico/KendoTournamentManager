package com.softwaremagico.kt.logger.tests;

/*-
 * #%L
 * Kendo Tournament Manager (Logger)
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

import com.softwaremagico.kt.logger.CacheControllerLogger;
import com.softwaremagico.kt.logger.JwtFilterLogger;
import com.softwaremagico.kt.logger.PdfExporterLog;
import com.softwaremagico.kt.logger.WebsocketsLogger;
import org.testng.annotations.Test;

@Test(groups = "loggerAdaptersTests")
public class LoggerAdaptersCoverageTest {

    @Test
    public void shouldInvokeAllLoggerAdaptersWithoutErrors() {
        CacheControllerLogger.info(getClass(), "info {}", 1);
        CacheControllerLogger.debug(getClass(), "debug {}", 3);

        JwtFilterLogger.info(getClass(), "info {}", 1);
        JwtFilterLogger.warning(getClass(), "warn {}", 2);
        JwtFilterLogger.debug(getClass(), "debug {}", 3);
        JwtFilterLogger.errorMessage(getClass(), new RuntimeException("jwt"));
        JwtFilterLogger.errorMessage(getClass(), "error {}", 5);
        JwtFilterLogger.isDebugEnabled();

        PdfExporterLog.severe(getClass(), "severe {}", 4);
        PdfExporterLog.errorMessage(getClass(), new RuntimeException("pdf"));

        WebsocketsLogger.warning(getClass(), "warn {}", 2);
        WebsocketsLogger.debug(getClass(), "debug {}", 3);
        WebsocketsLogger.errorMessage(getClass(), new RuntimeException("ws"));
    }
}

