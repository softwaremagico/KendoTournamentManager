package com.softwaremagico.kt.logger;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Abstract class to provide basic logging capabilities to logging advises.
 */
@Component
@Aspect
public abstract class AbstractLogging {
    // Logger specialized for each subclass.
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String logMessage(JoinPoint joinPoint, Object... args) {
        final StringBuilder logMessage = new StringBuilder();
        logMessage.append("Entering in ");
        logMessage.append(getTargetClassName(joinPoint));
        logMessage.append(".");
        logMessage.append(joinPoint.getSignature().getName());
        logMessage.append("(");
        if (args.length > 0) {
            String str = Arrays.toString(args);
            // removing initial and ending chars ([, ])
            str = str.substring(1, str.length() - 1);
            logMessage.append(str);
        }
        logMessage.append(") at ");

        final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        final Calendar cal = Calendar.getInstance();
        logMessage.append(dateFormat.format(cal.getTime()));
        return logMessage.toString();
    }

    /**
     * Method used for logging the name of the target class, parameters and the
     * starting time.
     *
     * @param joinPoint join point containing all target information.
     * @param args      any arguments you wish to log between parentheses.
     */
    protected void log(JoinPoint joinPoint, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(logMessage(joinPoint, args));
        }
    }

    /**
     * Method used for logging the name of the target class, parameters and the
     * execution time.
     *
     * @param millis    execution time.
     * @param joinPoint join point containing all target information.
     * @param args      any arguments you wish to log between parentheses.
     */
    protected void log(long millis, JoinPoint joinPoint, Object... args) {
        if (logger.isDebugEnabled()) {
            final StringBuilder logMessage = new StringBuilder();
            logMessage.append("Executed ");

            // Method name.
            logMessage.append(getTargetClassName(joinPoint));
            logMessage.append(".");
            logMessage.append(joinPoint.getSignature().getName());
            logMessage.append("(");

            // Add params
            final Object[] paramValues = joinPoint.getArgs();
            if (paramValues != null) {
                for (int i = 0; i < paramValues.length; i++) {
                    if (paramValues[i] != null) {
                        if (paramValues[i] instanceof String) {
                            logMessage.append("'").append(paramValues[i].toString()).append("'");
                        } else {
                            logMessage.append(paramValues[i].toString());
                        }
                    } else {
                        logMessage.append(paramValues[i]);
                    }
                    if (i < paramValues.length - 1) {
                        logMessage.append(", ");
                    }
                }
            }

            logMessage.append(") in ");
            logMessage.append(millis);
            logMessage.append(" ms");

            logger.debug(logMessage.toString());
        }
    }

    protected String getTargetClassName(JoinPoint joinPoint) {
        // Get the fully-qualified name of the class
        String clsName = joinPoint.getTarget().getClass().getName();

        // Get the unqualified name of a class
        if (clsName.lastIndexOf('.') > 0) {
            clsName = clsName.substring(clsName.lastIndexOf('.') + 1);
        }

        // The $ can be converted to a .
        clsName = clsName.replace('$', '.');

        return clsName;
    }

    protected void log(String messageTemplate, Object... arguments) {
        logger.debug(messageTemplate, arguments);
    }
}
