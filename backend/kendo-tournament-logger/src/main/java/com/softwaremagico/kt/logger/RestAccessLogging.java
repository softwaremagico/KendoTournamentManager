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

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

/**
 * Logs all methods managed by Spring. In this project only REST services.
 */
@Aspect
@Component
public class RestAccessLogging extends AbstractAccessLogging {

    /**
     * Creates a new aspect that logs REST service invocations.
     */
    public RestAccessLogging() {
        super();
    }

    @Pointcut("execution(* com.softwaremagico.kt.rest.services..*(..))")
    @Override
    protected void selectedMethods() {
        // Pointcut declaration.
    }
}
