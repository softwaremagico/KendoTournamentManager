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

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.softwaremagico.kt.logger.KendoTournamentLogger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class KendoTournamentLoggerTest {

    private Logger logger;
    private ListAppender<ILoggingEvent> appender;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        logger = (Logger) LoggerFactory.getLogger(KendoTournamentLogger.class);
        logger.setLevel(Level.DEBUG);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        logger.detachAppender(appender);
    }

    @Test(groups = "kendoTournamentLoggerTests")
    public void shouldLogInfo() {
        KendoTournamentLogger.info(getClass(), "created {}", 7);

        ILoggingEvent event = appender.list.get(appender.list.size() - 1);
        assertEquals(event.getLevel(), Level.INFO);
        assertTrue(event.getFormattedMessage().contains(getClass().getName() + ": created 7"));
    }

    @Test(groups = "kendoTournamentLoggerTests")
    public void shouldLogWarning() {
        KendoTournamentLogger.warning(getClass(), "warn {}", "x");

        ILoggingEvent event = appender.list.get(appender.list.size() - 1);
        assertEquals(event.getLevel(), Level.WARN);
        assertTrue(event.getFormattedMessage().contains(getClass().getName() + ": warn x"));
    }

    @Test(groups = "kendoTournamentLoggerTests")
    public void shouldLogDebug() {
        KendoTournamentLogger.debug(getClass(), "dbg {}", 1);

        ILoggingEvent event = appender.list.get(appender.list.size() - 1);
        assertEquals(event.getLevel(), Level.DEBUG);
        assertTrue(event.getFormattedMessage().contains(getClass().getName() + ": dbg 1"));
    }

    @Test(groups = "kendoTournamentLoggerTests")
    public void shouldLogErrorMessage() {
        KendoTournamentLogger.errorMessage(getClass(), new RuntimeException("error now"));

        ILoggingEvent event = appender.list.get(appender.list.size() - 1);
        assertEquals(event.getLevel(), Level.ERROR);
        assertTrue(event.getFormattedMessage().contains(getClass().getSimpleName()));
    }
}

