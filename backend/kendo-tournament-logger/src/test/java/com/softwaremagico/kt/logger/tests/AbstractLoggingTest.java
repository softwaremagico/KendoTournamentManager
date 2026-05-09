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

import com.softwaremagico.kt.logger.AbstractLogging;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class AbstractLoggingTest {

    private ExposedLogging logging;

    @Mock
    private JoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        logging = new ExposedLogging();
    }

    @Test(groups = "abstractLoggingTests")
    public void shouldBuildLogMessageWithParameters() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("method");
        when(joinPoint.getTarget()).thenReturn(new LocalTarget());

        String message = logging.callLogMessage(joinPoint, "one", 2);

        assertTrue(message.contains("Entering in"));
        assertTrue(message.contains("LocalTarget.method(one, 2)"));
        assertTrue(message.contains(" at "));
        assertFalse(message.contains("["));
    }

    @Test(groups = "abstractLoggingTests")
    public void shouldNormalizeInnerClassName() {
        when(joinPoint.getTarget()).thenReturn(new Outer.Inner());

        String className = logging.callGetTargetClassName(joinPoint);

        assertTrue(className.contains("Outer.Inner"));
        assertFalse(className.contains("$"));
    }

    @Test(groups = "abstractLoggingTests")
    public void shouldAcceptTimedLogWithoutExceptions() {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("method");
        when(joinPoint.getTarget()).thenReturn(new LocalTarget());
        when(joinPoint.getArgs()).thenReturn(new Object[]{"txt", null, 3});

        logging.callTimedLog(25L, joinPoint);
    }

    private static final class ExposedLogging extends AbstractLogging {
        public String callLogMessage(JoinPoint joinPoint, Object... args) {
            return logMessage(joinPoint, args);
        }

        public String callGetTargetClassName(JoinPoint joinPoint) {
            return getTargetClassName(joinPoint);
        }

        public void callTimedLog(long millis, JoinPoint joinPoint, Object... args) {
            log(millis, joinPoint, args);
        }
    }

    private static final class LocalTarget {
    }

    private static final class Outer {
        private static final class Inner {
        }
    }
}




