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
        CacheControllerLogger.warning(getClass(), "warn {}", 2);
        CacheControllerLogger.debug(getClass(), "debug {}", 3);
        CacheControllerLogger.severe(getClass().getName(), "severe {}", 4);
        CacheControllerLogger.errorMessage(getClass(), new RuntimeException("cache"));
        CacheControllerLogger.errorMessage(getClass().getName(), "error {}", 5);
        CacheControllerLogger.errorMessage(this, new RuntimeException("cache-obj"));
        CacheControllerLogger.isDebugEnabled();

        JwtFilterLogger.info(getClass(), "info {}", 1);
        JwtFilterLogger.warning(getClass(), "warn {}", 2);
        JwtFilterLogger.debug(getClass(), "debug {}", 3);
        JwtFilterLogger.severe(getClass().getName(), "severe {}", 4);
        JwtFilterLogger.errorMessage(getClass(), new RuntimeException("jwt"));
        JwtFilterLogger.errorMessage(getClass().getName(), "error {}", 5);
        JwtFilterLogger.errorMessage(getClass(), "error2 {}", 6);
        JwtFilterLogger.errorMessage(this, new RuntimeException("jwt-obj"));
        JwtFilterLogger.isDebugEnabled();

        PdfExporterLog.info(getClass(), "info {}", 1);
        PdfExporterLog.warning(getClass(), "warn {}", 2);
        PdfExporterLog.debug(getClass(), "debug {}", 3);
        PdfExporterLog.severe(getClass().getName(), "severe {}", 4);
        PdfExporterLog.errorMessage(getClass(), new RuntimeException("pdf"));
        PdfExporterLog.errorMessage(getClass().getName(), "error {}", 5);
        PdfExporterLog.errorMessage(this, new RuntimeException("pdf-obj"));
        PdfExporterLog.isDebugEnabled();

        WebsocketsLogger.info(getClass(), "info {}", 1);
        WebsocketsLogger.warning(getClass(), "warn {}", 2);
        WebsocketsLogger.debug(getClass(), "debug {}", 3);
        WebsocketsLogger.severe(getClass().getName(), "severe {}", 4);
        WebsocketsLogger.errorMessage(getClass(), new RuntimeException("ws"));
        WebsocketsLogger.errorMessage(getClass().getName(), "error {}", 5);
        WebsocketsLogger.errorMessage(getClass(), "error2 {}", 6);
        WebsocketsLogger.errorMessage(this, new RuntimeException("ws-obj"));
        WebsocketsLogger.isDebugEnabled();
    }
}

