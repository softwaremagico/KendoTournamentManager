package com.softwaremagico.kt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.time.Period;
import java.util.Collections;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {
    private static final String SWAGGER_TITLE = "Kendo Tournament Generator";
    private static final String SWAGGER_REST_LOCATION = "com.softwaremagico.kt.rest";
    private static final Class[] IGNORED_CLASSES = {};

    @Bean
    public Docket templateApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                // OpenAPI doc cannot handle period
                .directModelSubstitute(Period.class, String.class).select()
                .apis(RequestHandlerSelectors.basePackage(SWAGGER_REST_LOCATION)).paths(PathSelectors.any()).build()
                .forCodeGeneration(true).apiInfo(getApiInfo()).ignoredParameterTypes(IGNORED_CLASSES);
    }

    private ApiInfo getApiInfo() {
        return new ApiInfo(SWAGGER_TITLE, SWAGGER_TITLE, "1.0", "",
                new Contact("Support", "https://softwaremagico.github.io/KendoTournament/", "softwaremagico@gmail.com"),
                "", "https://softwaremagico.github.io/KendoTournament/", Collections.emptyList());
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("HEAD", "GET", "PUT", "POST", "DELETE", "PATCH");
            }
        };
    }

    @Bean("threadPoolExecutor")
    public TaskExecutor getAsyncExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        executor.setMaxPoolSize(100);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setThreadNamePrefix("Rest_Async-");
        return executor;
    }

}
