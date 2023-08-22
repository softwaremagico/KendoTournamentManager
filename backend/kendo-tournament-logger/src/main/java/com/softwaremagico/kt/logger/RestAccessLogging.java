package com.softwaremagico.kt.logger;

/*-
 * #%L
 * Kendo Tournament Manager (Logger)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Logs all file managed by Spring. In this project only are DAOs.
 */
@Aspect
@Component
public class RestAccessLogging extends AbstractLogging {

    /**
     * Following is the definition for a pointcut to select all the methods
     * available. So advice will be called for all the methods.
     */
    @Pointcut("execution(* com.softwaremagico.kt.rest.services..*(..))")
    private void selectAll() {
    }

    @Pointcut("within(org.springframework.web.filter.GenericFilterBean+)")
    public void avoidClasses() {

    }

    /**
     * Using custom annotation.
     *
     * @param auditable if it is auditable
     */
    @Pointcut("@annotation(auditable)")
    public void isAuditable(Auditable auditable) {
    }

    /**
     * This is the method which I would like to execute before a selected method
     * execution.
     *
     * @param joinPoint the joinPoint
     */
    @Before(value = "selectAll() && !avoidClasses()")
    public void beforeAdvice(JoinPoint joinPoint) {

    }

    @Around(value = "selectAll() && !avoidClasses()")
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
     * This is the method which I would like to execute after a selected method
     * execution.
     */
    @After(value = "selectAll() && !avoidClasses()")
    public void afterAdvice() {
    }

    /**
     * This is the method which I would like to execute when any method returns.
     *
     * @param retVal the returning value.
     */
    @AfterReturning(pointcut = "selectAll() && !avoidClasses()", returning = "retVal")
    public void afterReturningAdvice(Object retVal) {
        if (retVal != null) {
            log("Returning: '{}' ", retVal.toString());
        } else {
            log("Returning: 'void'.");
        }
    }

    /**
     * This is the method which I would like to execute if there is an exception
     * raised by any method.
     *
     * @param ex the exception
     */
    @AfterThrowing(pointcut = "selectAll() && !avoidClasses()", throwing = "ex")
    public void afterThrowingAdvice(IllegalArgumentException ex) {
        log("There has been an exception: '{}' ", ex.getMessage());
    }

}
