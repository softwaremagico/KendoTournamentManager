package com.softwaremagico.kt;

/*-
 * #%L
 * Kendo Tournament Manager (Rest)
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

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfiguration {
    private static final String SWAGGER_GROUP = "kendo-tournament-public";
    private static final String SWAGGER_TITLE = "Kendo Tournament Manager";
    private static final String SWAGGER_DESCRIPTION = "Kendo Tournament Manager";
    private static final String SWAGGER_README = SWAGGER_TITLE + " Documentation";
    private static final String SWAGGER_URL = "https://softwaremagico.github.io/KendoTournamentManager/";
    private static final String SWAGGER_DEFAULT_VERSION = "Dev";
    private static final String[] PACKAGES_TO_SCAN = new String[]{"com.softwaremagico.kt"};

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group(SWAGGER_GROUP)
                .packagesToScan(PACKAGES_TO_SCAN)
                .pathsToMatch("/**")
                .build();
    }

    @Bean
    public OpenAPI kendoTournamentOpenAPI() {
        return new OpenAPI()
                .info(new Info().title(SWAGGER_TITLE)
                        .description(SWAGGER_DESCRIPTION)
                        .version(SwaggerConfiguration.class.getPackage().getImplementationVersion() != null
                                ? SwaggerConfiguration.class.getPackage().getImplementationVersion() : SWAGGER_DEFAULT_VERSION)
                        .license(new License().name("GNU General Public License v3").url("https://www.gnu.org/licenses/gpl-3.0.html")))
                .externalDocs(new ExternalDocumentation()
                        .description(SWAGGER_README)
                        .url(SWAGGER_URL));
    }
}
