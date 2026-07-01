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
public class CacheEventLogger implements CacheEventListener<Object, Object> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CacheEventLogger.class);

    public static void info(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateInfo(LOGGER, clazz, messageTemplate, arguments);
    }

    public static void warning(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateWarning(LOGGER, clazz, messageTemplate, arguments);
    }

    public static void debug(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateDebug(LOGGER, clazz, messageTemplate, arguments);
    }

    public static void severe(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateSevere(LOGGER, clazz.getName(), messageTemplate, arguments);
    }

    public static void errorMessage(Class<?> clazz, Throwable throwable) {
        AbstractLoggerWrapper.delegateErrorMessage(LOGGER, clazz, throwable);
    }

    public static void errorMessage(Class<?> clazz, String messageTemplate, Object... arguments) {
        AbstractLoggerWrapper.delegateErrorMessage(LOGGER, clazz.getName(), messageTemplate, arguments);
    }

    public static void errorMessage(Object object, Throwable throwable) {
        AbstractLoggerWrapper.delegateErrorMessage(LOGGER, object, throwable);
    }

    public static boolean isDebugEnabled() {
        return AbstractLoggerWrapper.isDebugEnabled(LOGGER);
    }

    @Override
    public void onEvent(CacheEvent<?, ?> cacheEvent) {
        debug(this.getClass(), "Cache updated for '{}' value changed from '{}' to '{}'", cacheEvent.getKey(),
                cacheEvent.getOldValue(), cacheEvent.getNewValue());
    }
}
