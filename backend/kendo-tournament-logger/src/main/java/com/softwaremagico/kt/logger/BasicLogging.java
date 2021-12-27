package com.softwaremagico.kt.logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

/**
 * Logs all file managed by Spring. In this project only are DAOs.
 */
@Aspect
@Component
public class BasicLogging extends AbstractLogging {

    /**
     * Following is the definition for a pointcut to select all the methods
     * available. So advice will be called for all the methods.
     */
    @Pointcut("execution(* com.softwaremagico.kt.rest..*(..)) || execution(* com.softwaremagico.kt.persistence.repositories..*(..))")
    private void selectAll() {
    }

    /**
     * Using an existing annotation.
     */
    @Pointcut("@annotation(org.springframework.transaction.annotation.Transactional)")
    public void isAnnotated() {
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
    @Before(value = "(selectAll() || isAnnotated()) && !avoidClasses()")
    public void beforeAdvice(JoinPoint joinPoint) {

    }

    @Around(value = "(selectAll() || isAnnotated()) && !avoidClasses()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        final StopWatch stopWatch = new StopWatch();
        Object returnValue = null;
        stopWatch.start();
        returnValue = joinPoint.proceed();
        stopWatch.stop();
        log(stopWatch.getTotalTimeMillis(), joinPoint);
        return returnValue;
    }

    /**
     * This is the method which I would like to execute after a selected method
     * execution.
     */
    @After(value = "(selectAll() || isAnnotated()) && !avoidClasses()")
    public void afterAdvice() {
    }

    /**
     * This is the method which I would like to execute when any method returns.
     *
     * @param retVal the returning value.
     */
    @AfterReturning(pointcut = "(selectAll() || isAnnotated()) && !avoidClasses()", returning = "retVal")
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
    @AfterThrowing(pointcut = "(selectAll() || isAnnotated()) && !avoidClasses()", throwing = "ex")
    public void afterThrowingAdvice(IllegalArgumentException ex) {
        log("There has been an exception: '{}' ", ex.getMessage());
    }

}
