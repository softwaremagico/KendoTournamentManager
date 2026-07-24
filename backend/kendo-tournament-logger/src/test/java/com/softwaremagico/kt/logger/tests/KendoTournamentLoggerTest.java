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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class KendoTournamentLoggerTest {

	private Logger logger;
	private ListAppender<ILoggingEvent> appender;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
        this.logger = (Logger) LoggerFactory.getLogger(KendoTournamentLogger.class);
        this.logger.setLevel(Level.DEBUG);
        this.appender = new ListAppender<>();
        this.appender.start();
        this.logger.addAppender(this.appender);
	}

	@AfterMethod(alwaysRun = true)
	public void tearDown() {
        this.logger.detachAppender(this.appender);
	}

	@DataProvider(name = "logLevels")
	public Object[][] logLevels() {
		return new Object[][]{
				{Level.INFO, "created {}", 7, "created 7"},
				{Level.WARN, "warn {}", "x", "warn x"},
				{Level.DEBUG, "dbg {}", 1, "dbg 1"},
		};
	}

	@Test(groups = "kendoTournamentLoggerTests", dataProvider = "logLevels")
	public void shouldLogAtGivenLevel(Level level, String format, Object argument, String expectedMessage) {
		switch (level.toString()) {
			case "INFO" -> KendoTournamentLogger.info(this.getClass(), format, argument);
			case "WARN" -> KendoTournamentLogger.warning(this.getClass(), format, argument);
			case "DEBUG" -> KendoTournamentLogger.debug(this.getClass(), format, argument);
			default -> throw new IllegalArgumentException("Unsupported level: " + level);
		}

		final ILoggingEvent event = this.appender.list.get(this.appender.list.size() - 1);
		assertEquals(event.getLevel(), level);
		assertTrue(event.getFormattedMessage().contains(this.getClass().getName() + ": " + expectedMessage));
	}

	@Test(groups = "kendoTournamentLoggerTests")
	public void shouldLogErrorMessage() {
		KendoTournamentLogger.errorMessage(this.getClass(), new RuntimeException("error now"));

		final ILoggingEvent event = this.appender.list.get(this.appender.list.size() - 1);
		assertEquals(event.getLevel(), Level.ERROR);
		assertTrue(event.getFormattedMessage().contains(this.getClass().getSimpleName()));
	}
}
