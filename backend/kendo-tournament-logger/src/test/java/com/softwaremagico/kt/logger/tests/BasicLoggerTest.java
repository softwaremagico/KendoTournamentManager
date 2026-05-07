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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
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
    public void shouldSanitizeWarningArgsAndTemplate() {
        when(logger.isWarnEnabled()).thenReturn(true);

        BasicLogger.warning(logger, "Clazz", "line\n{}", "a\tb");

        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(logger).warn(eq("Clazz: line_{}"), argsCaptor.capture());
        assertEquals(argsCaptor.getValue()[0], "a_b");
    }

    @Test(groups = "basicLoggerTests")
    public void shouldNotLogWarningWhenDisabled() {
        when(logger.isWarnEnabled()).thenReturn(false);

        BasicLogger.warning(logger, "Clazz", "msg", "x");

        verify(logger, never()).warn(anyString(), any(Object[].class));
    }

    @Test(groups = "basicLoggerTests")
    public void shouldSanitizeInfo() {
        when(logger.isInfoEnabled()).thenReturn(true);

        BasicLogger.info(logger, "info\r{}", "a\nb");

        verify(logger).isInfoEnabled();
    }

    @Test(groups = "basicLoggerTests")
    public void shouldSanitizeDebugWithClassName() {
        when(logger.isDebugEnabled()).thenReturn(true);

        BasicLogger.debug(logger, "Clazz", "d\tebug {}", "v\n1");

        ArgumentCaptor<Object[]> argsCaptor = ArgumentCaptor.forClass(Object[].class);
        verify(logger).debug(eq("Clazz: d_ebug {}"), argsCaptor.capture());
        assertEquals(argsCaptor.getValue()[0], "v_1");
    }

    @Test(groups = "basicLoggerTests")
    public void shouldLogSevereWithClassPrefix() {
        when(logger.isErrorEnabled()).thenReturn(true);

        BasicLogger.severe(logger, "Clazz", "boom");

        verify(logger).error(eq("Clazz: boom"), eq(new Object[]{}));
    }

    @Test(groups = "basicLoggerTests")
    public void shouldLogThrowableNotification() {
        when(logger.isErrorEnabled()).thenReturn(true);
        Throwable throwable = new IllegalArgumentException("bad");

        BasicLogger.errorMessageNotification(logger, "Clazz", throwable);

        verify(logger).error(eq("Exception on class {}:\n"), eq("Clazz"), eq(throwable));
    }

    @Test(groups = "basicLoggerTests")
    public void shouldBuildStackTraceText() {
        RuntimeException exception = new RuntimeException("kaboom");

        String stackTrace = BasicLogger.getStackTrace(exception);

        assertTrue(stackTrace.contains("kaboom"));
        assertTrue(stackTrace.contains("RuntimeException"));
    }
}

