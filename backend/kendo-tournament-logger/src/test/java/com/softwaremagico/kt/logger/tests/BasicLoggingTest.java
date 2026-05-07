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

import com.softwaremagico.kt.logger.BasicLogging;
import org.aspectj.lang.ProceedingJoinPoint;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

public class BasicLoggingTest {

    private BasicLogging basicLogging;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        basicLogging = new BasicLogging();
    }

    @Test(groups = "basicLoggingTests")
    public void shouldExecuteAroundAndReturnValue() throws Throwable {
        when(joinPoint.proceed()).thenReturn("ok");

        Object result = basicLogging.logAround(joinPoint);

        assertEquals(result, "ok");
        verify(joinPoint, times(1)).proceed();
    }

    @Test(groups = "basicLoggingTests")
    public void shouldHandleNullReturnValue() throws Throwable {
        when(joinPoint.proceed()).thenReturn(null);

        Object result = basicLogging.logAround(joinPoint);

        assertNull(result);
        verify(joinPoint, times(1)).proceed();
    }

    @Test(groups = "basicLoggingTests")
    public void shouldAcceptBeforeAndAfterAdviceCalls() {
        basicLogging.beforeAdvice(joinPoint);
        basicLogging.afterAdvice();
    }

    @Test(groups = "basicLoggingTests")
    public void shouldAcceptAfterReturningAdviceForBothBranches() {
        basicLogging.afterReturningAdvice("value");
        basicLogging.afterReturningAdvice(null);
    }

    @Test(groups = "basicLoggingTests")
    public void shouldAcceptAfterThrowingAdvice() {
        basicLogging.afterThrowingAdvice(new IllegalArgumentException("bad input"));
    }
}







