package com.softwaremagico.kt;

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

import com.softwaremagico.kt.persistence.dialect.KendoH2Dialect;
import com.softwaremagico.kt.persistence.dialect.KendoMySQLDialect;
import com.softwaremagico.kt.persistence.dialect.KendoPostgreSQLDialect;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;

@Configuration
@EnableJpaRepositories(entityManagerFactoryRef = "kendoManagerFactory", transactionManagerRef = "kendoTransactionManager", basePackages = {
        DatabaseConfiguration.PACKAGE})
public class DatabaseConfiguration {
    public static final String PACKAGE = "com.softwaremagico.kt.persistence";

    private final Environment environment;

    public DatabaseConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "spring.kendo.datasource")
    public DataSource kendoDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean kendoManagerFactory(EntityManagerFactoryBuilder builder,
                                                                      @Qualifier("kendoDataSource") DataSource dataSource) {
        final HashMap<String, Object> properties = new HashMap<>();
        properties.put("hibernate.hbm2ddl.auto", environment.getProperty("spring.kendo.datasource.jpa.hibernate.ddl-auto"));
        final String dialect = resolveDialect();
        if (dialect != null) {
            properties.put("hibernate.dialect", dialect);
        }
        return builder.dataSource(dataSource).properties(properties).packages(PACKAGE).build();
    }

    /**
     * Resolves the Hibernate dialect to use.
     * <p>
     * The dialect can be set explicitly with {@code spring.kendo.datasource.hibernate.dialect}.
     * When it is not set, it is derived from {@code spring.kendo.datasource.platform} so that
     * the deployments only have to declare the database they use. When neither of them is
     * available, Hibernate resolves the dialect from the JDBC connection.
     * </p>
     *
     * @return the fully qualified name of the dialect or null when it has to be resolved by
     * Hibernate.
     */
    private String resolveDialect() {
        final String configuredDialect = environment.getProperty("spring.kendo.datasource.hibernate.dialect");
        if (configuredDialect != null && !configuredDialect.isBlank()) {
            return configuredDialect;
        }
        final String platform = environment.getProperty("spring.kendo.datasource.platform");
        if (platform == null) {
            return null;
        }
        return switch (platform.toLowerCase()) {
            case "mysql", "mariadb" -> KendoMySQLDialect.class.getName();
            case "postgresql", "postgres" -> KendoPostgreSQLDialect.class.getName();
            case "h2" -> KendoH2Dialect.class.getName();
            default -> null;
        };
    }

    @Bean
    @Primary
    public PlatformTransactionManager kendoTransactionManager(
            @Qualifier("kendoManagerFactory") EntityManagerFactory kendoEntityManagerFactory) {
        return new JpaTransactionManager(kendoEntityManagerFactory);
    }

}
