package com.softwaremagico.kt.rest;

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

import com.softwaremagico.kt.DatabaseConfiguration;
import com.softwaremagico.kt.persistence.dialect.KendoH2Dialect;
import com.softwaremagico.kt.persistence.dialect.KendoMySQLDialect;
import com.softwaremagico.kt.persistence.dialect.KendoPostgreSQLDialect;
import org.springframework.mock.env.MockEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

@Test(groups = "databaseConfiguration")
public class DatabaseConfigurationTest {

    @Test
    public void resolveDialectUsesConfiguredDialect() throws Exception {
        final MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.kendo.datasource.hibernate.dialect", "com.example.CustomDialect");
        environment.setProperty("spring.kendo.datasource.platform", "mysql");

        Assert.assertEquals(resolveDialect(environment), "com.example.CustomDialect");
    }

    @Test
    public void resolveDialectUsesPlatform() throws Exception {
        Assert.assertEquals(resolveDialect(environmentWithPlatform("mysql")), KendoMySQLDialect.class.getName());
        Assert.assertEquals(resolveDialect(environmentWithPlatform("mariadb")), KendoMySQLDialect.class.getName());
        Assert.assertEquals(resolveDialect(environmentWithPlatform("postgresql")), KendoPostgreSQLDialect.class.getName());
        Assert.assertEquals(resolveDialect(environmentWithPlatform("postgres")), KendoPostgreSQLDialect.class.getName());
        Assert.assertEquals(resolveDialect(environmentWithPlatform("h2")), KendoH2Dialect.class.getName());
    }

    @Test
    public void resolveDialectReturnsNullWhenNotConfigured() throws Exception {
        Assert.assertNull(resolveDialect(new MockEnvironment()));
        Assert.assertNull(resolveDialect(environmentWithPlatform("oracle")));
    }

    private MockEnvironment environmentWithPlatform(String platform) {
        final MockEnvironment environment = new MockEnvironment();
        environment.setProperty("spring.kendo.datasource.platform", platform);
        return environment;
    }

    private String resolveDialect(MockEnvironment environment) throws Exception {
        final DatabaseConfiguration configuration = new DatabaseConfiguration(environment);
        final Method method = DatabaseConfiguration.class.getDeclaredMethod("resolveDialect");
        method.setAccessible(true);
        return (String) method.invoke(configuration);
    }
}
