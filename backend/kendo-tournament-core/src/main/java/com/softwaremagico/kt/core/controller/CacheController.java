package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2025 Softwaremagico
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


import com.softwaremagico.kt.logger.CacheControllerLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;

@Controller
public class CacheController {

    private final CacheManager cacheManager;

    public CacheController(@Autowired(required = false) CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }


    public void deleteAllCache() {
        if (cacheManager != null) {
            CacheControllerLogger.debug(this.getClass(), "Clearing all caches.");
            cacheManager.getCacheNames().parallelStream().forEach(name -> {
                CacheControllerLogger.info(this.getClass(), "Clearing cache '{}'.", name);
                cacheManager.getCache(name).invalidate();
            });
        }
    }
}
