package com.softwaremagico.kt;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2024 Softwaremagico
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

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class EhcacheConfig {
    private static final int MB = 10;
    private static final int SECONDS = 10;

//    @Bean
//    public JCacheManagerFactoryBean cacheManagerFactoryBean() throws Exception {
//        final JCacheManagerFactoryBean jCacheManagerFactoryBean = new JCacheManagerFactoryBean();
//        jCacheManagerFactoryBean.setCacheManagerUri(new ClassPathResource("ehcache.xml").getURI());
//        return jCacheManagerFactoryBean;
//    }
//
//    @Bean
//    public CacheManager cacheManager() throws Exception {
//        final JCacheCacheManager jCacheCacheManager = new JCacheCacheManager();
//        jCacheCacheManager.setCacheManager(cacheManagerFactoryBean().getObject());
//        return jCacheCacheManager;
//    }


//    @Bean
//    public CacheManager cacheManager() {
//        return new ConcurrentMapCacheManager("tournamentsById");
//    }

//    @Bean
//    public CacheManager ehcacheManager() {
//
//        final CacheConfiguration<Integer, Tournament> cacheConfig = CacheConfigurationBuilder
//                .newCacheConfigurationBuilder(Integer.class,
//                        Tournament.class,
//                        ResourcePoolsBuilder.newResourcePoolsBuilder()
//                                .offheap(MB, MemoryUnit.MB)
//                                .build())
//                .withExpiry(ExpiryPolicyBuilder.timeToIdleExpiration(Duration.ofSeconds(SECONDS)))
//                .build();
//
//        final CachingProvider cachingProvider = Caching.getCachingProvider();
//        final CacheManager cacheManager = cachingProvider.getCacheManager();
//
//        final javax.cache.configuration.Configuration<Integer, Tournament> configuration = Eh107Configuration.fromEhcacheCacheConfiguration(cacheConfig);
//        cacheManager.createCache("tournamentsById", configuration);
//        return cacheManager;
//    }

//    @Bean
//    public CacheManager ehcacheManager() {
//        final CachingProvider cachingProvider = Caching.getCachingProvider();
//        final CacheManager manager;
//        try {
//            manager = cachingProvider.getCacheManager(
//                    getClass().getResource("/ehcache.xml").toURI(),
//                    getClass().getClassLoader());
//        } catch (URISyntaxException e) {
//            throw new RuntimeException(e);
//        }
//        final Cache<Integer, Tournament> readyCache = manager.getCache("tournamentById", Integer.class, Tournament.class);
//        return manager;
//    }
}
