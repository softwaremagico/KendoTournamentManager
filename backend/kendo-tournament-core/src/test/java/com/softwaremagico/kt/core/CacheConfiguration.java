package com.softwaremagico.kt.core;

/*-
 * #%L
 * Kendo Tournament Manager (Core)
 * %%
 * Copyright (C) 2021 - 2023 Softwaremagico
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
public class CacheConfiguration {

//    @Bean
//    public JCacheManagerFactoryBean cacheManagerFactoryBean() throws Exception {
//        JCacheManagerFactoryBean jCacheManagerFactoryBean = new JCacheManagerFactoryBean();
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
}
