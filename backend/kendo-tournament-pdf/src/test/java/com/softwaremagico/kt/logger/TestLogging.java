package com.softwaremagico.kt.logger;

/*-
 * #%L
 * Kendo Tournament Manager (Persistence)
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


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines basic log behavior. Uses log4j.properties.
 */
public class TestLogging extends BasicLogger {

    private static final Logger logger = LoggerFactory.getLogger(TestLogging.class);

    /**
     * Events that have business meaning (i.e. creating category, deleting form,
     * ...). To follow user actions.
     *
     * @param className the name of the class to log.
     * @param message   the text
     */
    public static void info(String className, String message) {
        info(logger, className, message);
    }

    public static void info(String message) {
        info(logger, message);
    }


    /**
     * Shows not critical errors. I.e. Email address not found, permissions not
     * allowed for this user, ...
     *
     * @param className the name of the class to log.
     * @param message   the text
     */
    public static void warning(String className, String message) {
        warning(logger, className, message);
    }

    public static void warning(String message) {
        warning(logger, message);
    }

    /**
     * For following the trace of the execution. I.e. Knowing if the application
     * access to a method, opening database connection, etc.
     *
     * @param className the name of the class to log.
     * @param message   the text
     */
    public static void debug(String className, String message) {
        debug(logger, className, message);
    }

    public static void debug(String message) {
        debug(logger, message);
    }

    /**
     * To log any not expected error that can cause application malfunction.
     *
     * @param className the name of the class to log.
     * @param message   the text
     */
    public static void severe(String className, String message) {
        severe(logger, className, message);
    }

    /**
     * To log java exceptions and log also the stack trace. If enabled, also can
     * send an email to the administrator to alert of the error.
     *
     * @param className the name of the class to log.
     * @param throwable the exception
     */
    public static void errorMessage(String className, Throwable throwable) {
        errorMessageNotification(logger, className, getStackTrace(throwable));
    }

    public static void errorMessage(Class<?> clazz, Throwable throwable) {
        errorMessageNotification(logger, clazz.getName(), getStackTrace(throwable));
    }

    /**
     * To log java exceptions and log also the stack trace. If enabled, also can
     * send an email to the administrator to alert of the error.
     *
     * @param className the name of the class to log.
     * @param error     the error message.
     */
    public static void errorMessage(String className, String error) {
        errorMessageNotification(logger, className, error);
    }

    public static void errorMessage(String error) {
        errorMessageNotification(logger, error);
    }

    public static void errorMessage(Object object, Throwable throwable) {
        errorMessageNotification(logger, object.getClass().getName(), getStackTrace(throwable));
    }
}
