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
import com.softwaremagico.kt.logger.RestAccessLogging;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class RestAccessLoggingTest {

    private RestAccessLogging restAccessLogging;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Mock
    private Signature signature;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        restAccessLogging = new RestAccessLogging();
        ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(RestAccessLogging.class)).setLevel(Level.DEBUG);
    }

    @Test(groups = "restAccessLoggingTests")
    public void shouldExecuteAroundAndReturnValue() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("method");
        when(joinPoint.getTarget()).thenReturn(this);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = restAccessLogging.logAround(joinPoint);

        assertEquals(result, "ok");
        verify(joinPoint, times(1)).proceed();
    }

    @Test(groups = "restAccessLoggingTests")
    public void shouldHandleNullReturnValue() throws Throwable {
        when(joinPoint.getSignature()).thenReturn(signature);
        when(signature.getName()).thenReturn("method");
        when(joinPoint.getTarget()).thenReturn(this);
        when(joinPoint.getArgs()).thenReturn(new Object[0]);
        when(joinPoint.proceed()).thenReturn(null);

        Object result = restAccessLogging.logAround(joinPoint);

        assertNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test(groups = "restAccessLoggingTests")
    public void shouldAcceptBeforeAndAfterAdviceCalls() {
        final JoinPoint before = joinPoint;
        restAccessLogging.beforeAdvice(before);
        restAccessLogging.afterAdvice();
    }

    @Test(groups = "restAccessLoggingTests")
    public void shouldAcceptAfterReturningAdviceForBothBranches() {
        restAccessLogging.afterReturningAdvice("value");
        restAccessLogging.afterReturningAdvice(null);
    }

    @Test(groups = "restAccessLoggingTests")
    public void shouldAcceptAfterThrowingAdvice() {
        restAccessLogging.afterThrowingAdvice(new IllegalArgumentException("bad input"));
    }
}

