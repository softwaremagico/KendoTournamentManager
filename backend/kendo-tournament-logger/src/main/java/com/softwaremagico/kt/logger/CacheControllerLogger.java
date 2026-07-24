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
 * Logger wrapper for cache controller operations.
 */
public final class CacheControllerLogger extends AbstractLoggerWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheControllerLogger.class);

    private CacheControllerLogger() {
    }

    public static void info(Class<?> clazz, String messageTemplate, Object... arguments) {
        delegateInfo(LOGGER, clazz, messageTemplate, arguments);
    }


    public static void debug(Class<?> clazz, String messageTemplate, Object... arguments) {
        delegateDebug(LOGGER, clazz, messageTemplate, arguments);
    }
}
