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

import org.slf4j.Logger;

/**
 * Abstract base class for logger wrappers that delegate to BasicLogger.
 * Eliminates code duplication across all specific logger implementations.
 */
@SuppressWarnings("squid:S2629")
abstract class AbstractLoggerWrapper {

    protected AbstractLoggerWrapper() {
    }

    protected static void delegateInfo(Logger logger, String className, String messageTemplate, Object... arguments) {
        BasicLogger.info(logger, className, messageTemplate, arguments);
    }

    protected static void delegateInfo(Logger logger, Class<?> clazz, String messageTemplate, Object... arguments) {
        delegateInfo(logger, clazz.getName(), messageTemplate, arguments);
    }

    protected static void delegateWarning(Logger logger, String className, String messageTemplate, Object... arguments) {
        BasicLogger.warning(logger, className, messageTemplate, arguments);
    }

    protected static void delegateWarning(Logger logger, Class<?> clazz, String messageTemplate, Object... arguments) {
        delegateWarning(logger, clazz.getName(), messageTemplate, arguments);
    }

    protected static void delegateDebug(Logger logger, String className, String messageTemplate, Object... arguments) {
        BasicLogger.debug(logger, className, messageTemplate, arguments);
    }

    protected static void delegateDebug(Logger logger, Class<?> clazz, String messageTemplate, Object... arguments) {
        delegateDebug(logger, clazz.getName(), messageTemplate, arguments);
    }

    protected static void delegateSevere(Logger logger, String className, String messageTemplate, Object... arguments) {
        BasicLogger.severe(logger, className, messageTemplate, arguments);
    }

    protected static void delegateErrorMessage(Logger logger, Class<?> clazz, Throwable throwable) {
        BasicLogger.errorMessageNotification(logger, clazz.getName(), throwable);
    }

    protected static void delegateErrorMessage(Logger logger, String className, String messageTemplate, Object... arguments) {
        BasicLogger.errorMessageNotification(logger, className, messageTemplate, arguments);
    }

    protected static void delegateErrorMessage(Logger logger, Object object, Throwable throwable) {
        BasicLogger.errorMessageNotification(logger, object.getClass().getName(), throwable);
    }

    protected static boolean isDebugEnabled(Logger logger) {
        return logger.isDebugEnabled();
    }
}

