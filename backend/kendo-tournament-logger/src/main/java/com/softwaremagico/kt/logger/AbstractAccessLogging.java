package com.softwaremagico.kt.logger;

/*-
 * #%L
 * Kendo Tournament Manager (Logger)
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

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.util.StopWatch;

/**
 * Shared advice implementation for the aspects that log method invocations.
 * <p>
 * Subclasses only declare which join points are selected; the timing, return and
 * exception advice is identical for all of them.
 * </p>
 */
@Aspect
public abstract class AbstractAccessLogging extends AbstractLogging {

    /**
     * Creates a new aspect, keeping the logger scoped to the concrete subclass.
     */
    protected AbstractAccessLogging() {
        super();
    }

    /**
     * Selects the join points to which all the advice in this aspect applies.
     */
    @Pointcut
    protected abstract void selectedMethods();

    @Pointcut("within(org.springframework.web.filter.GenericFilterBean+)")
    public void avoidClasses() {
        // Pointcut declaration.
    }

    /**
     * Using custom annotation.
     *
     * @param auditable if it is auditable
     */
    @Pointcut("@annotation(auditable)")
    public void isAuditable(Auditable auditable) {
        // Pointcut declaration.
    }

    /**
     * Invoked before the selected method execution.
     *
     * @param joinPoint the joinPoint
     */
    @Before("selectedMethods() && !avoidClasses()")
    public void beforeAdvice(JoinPoint joinPoint) {
        // Not needed.
    }

    @Around("selectedMethods() && !avoidClasses()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        if (getLogger().isDebugEnabled()) {
            final StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            final Object returnValue = joinPoint.proceed();
            stopWatch.stop();
            log(stopWatch.getTotalTimeMillis(), joinPoint);
            return returnValue;
        }
        return joinPoint.proceed();
    }

    /**
     * Invoked after the selected method execution.
     */
    @After("selectedMethods() && !avoidClasses()")
    public void afterAdvice() {
        // Not needed.
    }

    /**
     * Invoked when the selected method returns.
     *
     * @param retVal the returning value.
     */
    @AfterReturning(pointcut = "selectedMethods() && !avoidClasses()", returning = "retVal")
    public void afterReturningAdvice(Object retVal) {
        if (retVal != null) {
            log("Returning: '{}' ", retVal.toString());
        } else {
            log("Returning: 'void'.");
        }
    }

    /**
     * Invoked when the selected method raises an exception.
     *
     * @param ex the exception
     */
    @AfterThrowing(pointcut = "selectedMethods() && !avoidClasses()", throwing = "ex")
    public void afterThrowingAdvice(IllegalArgumentException ex) {
        log("There has been an exception: '{}' ", ex.getMessage());
    }
}
