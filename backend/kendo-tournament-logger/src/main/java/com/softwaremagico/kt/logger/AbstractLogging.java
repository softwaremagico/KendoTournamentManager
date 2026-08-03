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
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Abstract class to provide basic logging capabilities to logging advises.
 */
@Component
@Aspect
public abstract class AbstractLogging {
    // Logger specialized for each subclass.
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Creates a new logging aspect, initializing a logger scoped to the concrete subclass.
     */
    protected AbstractLogging() {
        // Nothing to initialize besides the logger field above.
    }

    protected Logger getLogger() {
        return logger;
    }

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

        logMessage.append(LocalDateTime.now(ZoneId.systemDefault()).format(DATE_TIME_FORMATTER));
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
            logMessage.append(formatParams(joinPoint.getArgs()));

            logMessage.append(") in ");
            logMessage.append(millis);
            logMessage.append(" ms");

            logger.debug(logMessage.toString());
        }
    }

    private String formatParams(Object[] paramValues) {
        if (paramValues == null) {
            return "";
        }
        final StringBuilder params = new StringBuilder();
        for (int i = 0; i < paramValues.length; i++) {
            params.append(formatParamValue(paramValues[i]));
            if (i < paramValues.length - 1) {
                params.append(", ");
            }
        }
        return params.toString();
    }

    private String formatParamValue(Object value) {
        if (value instanceof String) {
            return "'" + value + "'";
        }
        return String.valueOf(value);
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
