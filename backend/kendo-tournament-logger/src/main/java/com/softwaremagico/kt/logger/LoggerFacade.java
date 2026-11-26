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
import org.slf4j.LoggerFactory;

/**
 * Shared implementation behind the static logger facades.
 * <p>
 * Each facade keeps its own public static signatures but delegates all the
 * formatting and severity decisions to this single object.
 * </p>
 */
final class LoggerFacade {

    private final Logger logger;

    LoggerFacade(Class<?> loggerName) {
        this.logger = LoggerFactory.getLogger(loggerName);
    }

    void info(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateInfo(logger, clazz, messageTemplate, arguments);
    }

    void warning(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateWarning(logger, clazz, messageTemplate, arguments);
    }

    void debug(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateDebug(logger, clazz, messageTemplate, arguments);
    }

    void severe(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateSevere(logger, clazz.getName(), messageTemplate, arguments);
    }

    void error(Class<?> clazz, Throwable throwable) {
        AbstractLoggerWrapper.delegateErrorMessage(logger, clazz, throwable);
    }

    void error(Object object, Throwable throwable) {
        AbstractLoggerWrapper.delegateErrorMessage(logger, object, throwable);
    }

    void error(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateErrorMessage(logger, clazz.getName(), messageTemplate, arguments);
    }

    boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }
}
