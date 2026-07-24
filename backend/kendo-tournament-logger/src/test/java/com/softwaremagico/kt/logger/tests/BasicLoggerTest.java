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

import com.softwaremagico.kt.logger.BasicLogger;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class BasicLoggerTest {

	@Mock
	private Logger logger;

	@BeforeMethod(alwaysRun = true)
	public void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test(groups = "basicLoggerTests")
	@SuppressWarnings("java:S6068")
	public void shouldSanitizeWarningArgsAndTemplate() {
		when(this.logger.isWarnEnabled()).thenReturn(true);

		BasicLogger.warning(this.logger, "Clazz", "line\n{}", "a\tb");

		final ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
		verify(this.logger).warn(eq("Clazz: line_{}"), argsCaptor.capture());
		assertEquals(argsCaptor.getValue()[0], "a_b");
	}

	@Test(groups = "basicLoggerTests")
	public void shouldNotLogWarningWhenDisabled() {
		when(this.logger.isWarnEnabled()).thenReturn(false);

		BasicLogger.warning(this.logger, "Clazz", "msg", "x");

		verify(this.logger, never()).warn(anyString(), any(Object[].class));
	}

	@Test(groups = "basicLoggerTests")
	public void shouldSanitizeInfo() {
		when(this.logger.isInfoEnabled()).thenReturn(true);

		BasicLogger.info(this.logger, "info\r{}", "a\nb");

		verify(this.logger).isInfoEnabled();
	}

	@Test(groups = "basicLoggerTests")
	@SuppressWarnings("java:S6068")
	public void shouldSanitizeDebugWithClassName() {
		when(this.logger.isDebugEnabled()).thenReturn(true);

		BasicLogger.debug(this.logger, "Clazz", "d\tebug {}", "v\n1");

		final ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
		verify(this.logger).debug(eq("Clazz: d_ebug {}"), argsCaptor.capture());
		assertEquals(argsCaptor.getValue()[0], "v_1");
	}

	@Test(groups = "basicLoggerTests")
	public void shouldLogSevereWithClassPrefix() {
		when(this.logger.isErrorEnabled()).thenReturn(true);

		BasicLogger.severe(this.logger, "Clazz", "boom");

		verify(this.logger).error("Clazz: boom", new Object[0]);
	}

	@Test(groups = "basicLoggerTests")
	public void shouldLogThrowableNotification() {
		when(this.logger.isErrorEnabled()).thenReturn(true);
		final Throwable throwable = new IllegalArgumentException("bad");

		BasicLogger.errorMessageNotification(this.logger, "Clazz", throwable);

		verify(this.logger).error("Exception on class {}:\n", "Clazz", throwable);
	}

	@Test(groups = "basicLoggerTests")
	public void shouldBuildStackTraceText() {
		final RuntimeException exception = new RuntimeException("kaboom");

		final String stackTrace = BasicLogger.getStackTrace(exception);

		assertTrue(stackTrace.contains("kaboom"));
		assertTrue(stackTrace.contains("RuntimeException"));
	}
}
