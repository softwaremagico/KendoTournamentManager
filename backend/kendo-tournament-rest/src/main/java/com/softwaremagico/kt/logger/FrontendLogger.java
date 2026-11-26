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

/**
 * Logger wrapper for frontend operations.
 */

public final class FrontendLogger extends AbstractLoggerWrapper {

    private static final LoggerFacade FRONTEND_DELEGATE = new LoggerFacade(FrontendLogger.class);

    public static void info(Class<?> clazz, String messageTemplate, Object... arguments) {
        FRONTEND_DELEGATE.info(clazz, messageTemplate, arguments);
    }
    public static void warning(Class<?> clazz, String messageTemplate, Object... arguments) {
        FRONTEND_DELEGATE.warning(clazz, messageTemplate, arguments);
    }
    public static void debug(Class<?> clazz, String messageTemplate, Object... arguments) {
        FRONTEND_DELEGATE.debug(clazz, messageTemplate, arguments);
    }
    public static void severe(Class<?> clazz, String messageTemplate, Object... arguments) {
        FRONTEND_DELEGATE.severe(clazz, messageTemplate, arguments);
    }
    public static void errorMessage(Class<?> clazz, Throwable throwable) {
        FRONTEND_DELEGATE.error(clazz, throwable);
    }
    public static void errorMessage(Object object, Throwable throwable) {
        FRONTEND_DELEGATE.error(object, throwable);
    }
    public static void errorMessage(Class<?> clazz, String messageTemplate, Object... arguments) {
        FRONTEND_DELEGATE.error(clazz, messageTemplate, arguments);
    }
    public static boolean isDebugEnabled() {
        return FRONTEND_DELEGATE.isDebugEnabled();
    }

    private FrontendLogger() {
    }
}
