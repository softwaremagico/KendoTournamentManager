package com.softwaremagico.kt;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
 * %%
 * Copyright (C) 2021 - 2026 SoftwareMagico
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

import org.modelmapper.ModelMapper;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNotSame;
import static org.testng.Assert.assertTrue;

@Test(groups = "restServicesUnit")
public class ServerConfigurationTests {

    private ServerConfiguration serverConfiguration;

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        serverConfiguration = new ServerConfiguration();
    }

    @Test
    public void shouldCreateModelMapperBean() {
        final ModelMapper modelMapper = serverConfiguration.modelMapper();

        assertNotNull(modelMapper);
    }

    @Test
    public void shouldCreateIndependentModelMapperInstances() {
        final ModelMapper first = serverConfiguration.modelMapper();
        final ModelMapper second = serverConfiguration.modelMapper();

        assertNotSame(first, second);
    }

    @Test
    public void shouldCreateCorsConfigurerAndAllowMappingRegistration() {
        final WebMvcConfigurer configurer = serverConfiguration.corsConfigurer();

        assertNotNull(configurer);
        configurer.addCorsMappings(new CorsRegistry());
    }

    @Test
    public void shouldCreateConfiguredThreadPoolExecutor() {
        final TaskExecutor taskExecutor = serverConfiguration.getAsyncExecutor();

        assertTrue(taskExecutor instanceof ThreadPoolTaskExecutor);
        final ThreadPoolTaskExecutor threadPoolTaskExecutor = (ThreadPoolTaskExecutor) taskExecutor;
        assertEquals(threadPoolTaskExecutor.getCorePoolSize(), 20);
        assertEquals(threadPoolTaskExecutor.getMaxPoolSize(), 100);
        assertEquals(threadPoolTaskExecutor.getThreadNamePrefix(), "Rest_Async-");
    }
}

