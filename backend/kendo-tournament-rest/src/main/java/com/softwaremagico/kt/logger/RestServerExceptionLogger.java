package com.softwaremagico.kt.logger;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

/**
 * Logger wrapper for REST server exception handling.
 */
public final class RestServerExceptionLogger extends AbstractLoggerWrapper {

    private static final LoggerFacade DELEGATE = new LoggerFacade(RestServerExceptionLogger.class);

    private RestServerExceptionLogger() {
    }

    public static void info(Class<?> clazz, String messageTemplate, Object... arguments) {
        DELEGATE.info(clazz, messageTemplate, arguments);
    }

    public static void warning(Class<?> clazz, String messageTemplate, Object... arguments) {
        DELEGATE.warning(clazz, messageTemplate, arguments);
    }

    public static void debug(Class<?> clazz, String messageTemplate, Object... arguments) {
        DELEGATE.debug(clazz, messageTemplate, arguments);
    }

    public static void severe(Class<?> clazz, String messageTemplate, Object... arguments) {
        DELEGATE.severe(clazz, messageTemplate, arguments);
    }

    public static void errorMessage(Class<?> clazz, Throwable throwable) {
        DELEGATE.error(clazz, throwable);
    }

    public static void errorMessage(Class<?> clazz, String messageTemplate, Object... arguments) {
        DELEGATE.error(clazz, messageTemplate, arguments);
    }

    public static boolean isDebugEnabled() {
        return DELEGATE.isDebugEnabled();
    }
}
