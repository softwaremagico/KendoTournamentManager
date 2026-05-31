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

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines basic log behavior. Uses log4j.properties.
 */
public class CacheEventLogger extends BasicLogger implements CacheEventListener<Object, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheEventLogger.class);

    /**
     * Events that have business meaning (i.e. creating category, deleting form,
     * ...). To follow user actions.
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void info(String className, String messageTemplate, Object... arguments) {
        info(LOGGER, className, messageTemplate, arguments);
    }

    public static void info(Class<?> clazz, String messageTemplate, Object... arguments) {
        info(clazz.getName(), messageTemplate, arguments);
    }

    /**
     * Shows not critical errors. I.e. Email address not found, permissions not
     * allowed for this user, ...
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void warning(String className, String messageTemplate, Object... arguments) {
        warning(LOGGER, className, messageTemplate, arguments);
    }

    public static void warning(Class<?> clazz, String messageTemplate, Object... arguments) {
        warning(clazz.getName(), messageTemplate, arguments);
    }

    /**
     * For following the trace of the execution. I.e. Knowing if the application
     * access to a method, opening database connection, etc.
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void debug(String className, String messageTemplate, Object... arguments) {
        debug(LOGGER, className, messageTemplate, arguments);
    }

    public static void debug(Class<?> clazz, String messageTemplate, Object... arguments) {
        debug(clazz.getName(), messageTemplate, arguments);
    }

    /**
     * To log any not expected error that can cause application malfunction.
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void severe(String className, String messageTemplate, Object... arguments) {
        severe(LOGGER, className, messageTemplate, arguments);
    }

    public static void errorMessage(Class<?> clazz, Throwable throwable) {
        errorMessageNotification(LOGGER, clazz.getName(), throwable);
    }

    /**
     * To log java exceptions and log also the stack trace. If enabled, also can
     * send an email to the administrator to alert of the error.
     *
     * @param className       the name of the class to log.
     * @param messageTemplate string with static text as template.
     * @param arguments       parameters to fill up the template
     */
    public static void errorMessage(String className, String messageTemplate, Object... arguments) {
        errorMessageNotification(LOGGER, className, messageTemplate, arguments);
    }

    public static void errorMessage(Object object, Throwable throwable) {
        errorMessageNotification(LOGGER, object.getClass().getName(), throwable);
    }

    public static boolean isDebugEnabled() {
        return LOGGER.isDebugEnabled();
    }

    @Override
    public void onEvent(CacheEvent<?, ?> cacheEvent) {
        debug(this.getClass(), "Cache updated for '{}' value changed from '{}' to '{}'",
                cacheEvent.getKey(), cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}
