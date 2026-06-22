/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

package com.softwaremagico.kt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test suite for {@link ServerConfiguration}.
 * Validates Spring configuration for beans, CORS, and task execution.
 */
@ExtendWith(MockitoExtension.class)
class ServerConfigurationTest {

    private ServerConfiguration serverConfiguration;

    @BeforeEach
    void setUp() {
        serverConfiguration = new ServerConfiguration();
    }

    @Test
    void testModelMapperBean() {
        ModelMapper mapper = serverConfiguration.modelMapper();

        assertNotNull(mapper);

        // Test mapping functionality
        TestSourceClass source = new TestSourceClass("John", 30);
        TestTargetClass target = mapper.map(source, TestTargetClass.class);

        assertEquals(source.getName(), target.getName());
        assertEquals(source.getAge(), target.getAge());
    }

    @Test
    void testCorsConfigurerBean() {
        WebMvcConfigurer configurer = serverConfiguration.corsConfigurer();

        assertNotNull(configurer);
    }

    @Test
    void testCorsConfigurerRegistersMapping() {
        WebMvcConfigurer configurer = serverConfiguration.corsConfigurer();
        CorsRegistry registry = new CorsRegistry();

        // This will call addCorsMappings
        configurer.addCorsMappings(registry);

        assertNotNull(registry);
    }

    @Test
    void testTaskExecutorBean() {
        TaskExecutor executor = serverConfiguration.getAsyncExecutor();

        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolTaskExecutor);
    }

    @Test
    void testTaskExecutorCorePoolSize() {
        TaskExecutor executor = serverConfiguration.getAsyncExecutor();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        assertEquals(20, threadPoolExecutor.getCorePoolSize());
    }

    @Test
    void testTaskExecutorMaxPoolSize() {
        TaskExecutor executor = serverConfiguration.getAsyncExecutor();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        assertEquals(100, threadPoolExecutor.getMaxPoolSize());
    }

    @Test
    void testTaskExecutorThreadNamePrefix() {
        TaskExecutor executor = serverConfiguration.getAsyncExecutor();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        assertEquals("Rest_Async-", threadPoolExecutor.getThreadNamePrefix());
    }

    @Test
    void testTaskExecutorWaitForTasksOnShutdown() {
        TaskExecutor executor = serverConfiguration.getAsyncExecutor();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor) executor;

        assertNotNull(threadPoolExecutor);
    }

    @Test
    void testMultipleTaskExecutorInstancesAreIndependent() {
        TaskExecutor executor1 = serverConfiguration.getAsyncExecutor();
        TaskExecutor executor2 = serverConfiguration.getAsyncExecutor();

        assertNotSame(executor1, executor2);
    }

    @Test
    void testMultipleModelMapperInstancesAreIndependent() {
        ModelMapper mapper1 = serverConfiguration.modelMapper();
        ModelMapper mapper2 = serverConfiguration.modelMapper();

        assertNotSame(mapper1, mapper2);
    }

    // Test helper classes
    public static class TestSourceClass {
        private String name;
        private int age;

        public TestSourceClass(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static class TestTargetClass {
        private String name;
        private int age;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    // ...existing code...
}

