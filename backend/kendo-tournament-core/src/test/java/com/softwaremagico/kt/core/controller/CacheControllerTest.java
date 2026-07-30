package com.softwaremagico.kt.core.controller;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
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

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Test(groups = "cacheController")
public class CacheControllerTest {

    @Mock
    private CacheManager mockCacheManager;

    @Mock
    private Cache mockCache1;

    @Mock
    private Cache mockCache2;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void deleteAllCache_withNullCacheManager_expectNoException() {
        final CacheController controller = new CacheController(null);
        controller.deleteAllCache();
    }

    @Test
    public void deleteAllCache_withCacheManager_expectAllCachesInvalidated() {
        when(mockCacheManager.getCacheNames()).thenReturn(List.of("cache1", "cache2"));
        when(mockCacheManager.getCache("cache1")).thenReturn(mockCache1);
        when(mockCacheManager.getCache("cache2")).thenReturn(mockCache2);

        final CacheController controller = new CacheController(mockCacheManager);
        controller.deleteAllCache();

        verify(mockCache1, times(1)).invalidate();
        verify(mockCache2, times(1)).invalidate();
    }

    @Test
    public void deleteAllCache_withEmptyCacheManager_expectNoInvalidateCalls() {
        when(mockCacheManager.getCacheNames()).thenReturn(List.of());

        final CacheController controller = new CacheController(mockCacheManager);
        controller.deleteAllCache();

        verify(mockCacheManager, never()).getCache(anyString());
    }
}

